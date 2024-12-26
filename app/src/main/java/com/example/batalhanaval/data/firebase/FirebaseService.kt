package com.example.batalhanaval.data.firebase

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.example.batalhanaval.data.model.ScoreItem
import com.example.batalhanaval.ui.components.CellState

object FirebaseService {

    // Instância do FirebaseAuth
    private val auth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    // Instância do Firestore
    private val db: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

     //Cria um tabuleiro vazio
    fun createEmptyBoard(): List<List<CellState>> {
        return List(8) {
            List(8) { CellState.EMPTY }
        }
    }

     //Faz login anônimo no Firebase
    fun signInAnonymously(onSignInComplete: (String?) -> Unit) {
        auth.signInAnonymously()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    onSignInComplete(userId)
                } else {
                    Log.e("FirebaseService", "Erro ao autenticar anonimamente", task.exception)
                    onSignInComplete(null)
                }
            }
    }

    // Função para verificar se o nick já existe na coleção players
    fun checkIfNickExists(nick: String, onResult: (Boolean) -> Unit) {
        db.collection("players")
            .whereEqualTo("name", nick)
            .get()
            .addOnSuccessListener { querySnapshot ->
                onResult(querySnapshot.isEmpty) // Se a coleção estiver vazia, o nick está disponível
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseService", "Erro ao verificar nick", e)
                onResult(false)
            }
    }

    // Registra um novo jogador
    fun registerPlayer(nick: String, password: String, onComplete: (String?) -> Unit) {
        checkIfNickExists(nick) { isAvailable ->
            if (isAvailable) {
                val playerData = hashMapOf(
                    "name" to nick,
                    "password" to password,
                    "score" to 0
                )

                db.collection("players")
                    .document(nick) // Usando o nick como ID
                    .set(playerData)
                    .addOnSuccessListener {
                        onComplete(nick)  // Registro bem-sucedido
                    }
                    .addOnFailureListener { e ->
                        Log.e("FirebaseService", "Erro ao registrar jogador", e)
                        onComplete(null)
                    }
            } else {
                onComplete(null)  // Nick já existe
            }
        }
    }

    // Realiza login verificando o nick e a senha
    fun loginPlayer(nick: String, password: String, onLoginComplete: (String?) -> Unit) {
        db.collection("players")
            .whereEqualTo("name", nick)
            .whereEqualTo("password", password)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    onLoginComplete(null)
                } else {
                    val playerId = querySnapshot.documents[0].id
                    onLoginComplete(playerId)
                }
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseService", "Erro ao fazer login", e)
                onLoginComplete(null)
            }
    }

    // Vai buscar o nome do jogador
    fun getPlayerName(userId: String, onResult: (String?) -> Unit) {
        db.collection("players").document(userId).get()
            .addOnSuccessListener { documentSnapshot ->
                val name = documentSnapshot.getString("name")
                onResult(name)
            }
            .addOnFailureListener { exception ->
                Log.e("FirebaseService", "Erro ao obter o nome do jogador", exception)
                onResult(null)
            }
    }

    // Vai buscar o nome de todos os jogadores menos o jogador
    fun getAllPlayersExceptCurrentUser(currentUserId: String, onResult: (List<String>) -> Unit) {
        db.collection("players")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val players = querySnapshot.documents
                    .filter { it.id != currentUserId } // Exclui o jogador logado
                    .map { it.getString("name") ?: "" } // Adiciona apenas os nomes dos jogadores
                onResult(players)
            }
            .addOnFailureListener { exception ->
                Log.e("FirebaseService", "Erro ao obter lista de jogadores", exception)
                onResult(emptyList()) // Retorna lista vazia em caso de falha
            }
    }

    //Vai buscar o ID do jogo
    fun getGameId(onResult: (String?) -> Unit) {
        // Recuperar o ID do jogo da coleção "games"
        db.collection("games")
            .whereEqualTo("status", "waiting")  // Verifique jogos que ainda estão aguardando para começar
            .limit(1)  // Apenas pegar o primeiro jogo (geralmente você vai querer pegar o jogo mais recente)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    onResult(null)  // Nenhum jogo encontrado
                } else {
                    val gameId = querySnapshot.documents[0].id  // Pegue o ID do primeiro jogo
                    onResult(gameId)  // Retorne o ID do jogo
                }
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseService", "Erro ao recuperar o ID do jogo", e)
                onResult(null)  // Retorne null se houver falha
            }
    }

    //Cria um novo jogo no Firestore
    fun createNewGame(player1: String, player2: String, onComplete: (String?) -> Unit) {
        val boardData = List(8) { row ->
            List(8) { col ->
                mapOf(
                    "row" to row,
                    "col" to col,
                    "state" to CellState.EMPTY.name
                )
            }
        }.flatten() // Converte a lista de listas para uma lista linear

        val gameData = hashMapOf(
            "player1" to player1,
            "player2" to player2,
            "status" to "waiting",
            "timestamp" to System.currentTimeMillis(), // Adicionado timestamp para controle temporal
            "boardPlayer1" to boardData,
            "boardPlayer2" to boardData,
            "turn" to 1
        )

        db.collection("games")
            .add(gameData)
            .addOnSuccessListener { documentReference ->
                Log.d("FirebaseService", "Novo jogo criado com ID: ${documentReference.id}")
                onComplete(documentReference.id)
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseService", "Erro ao criar jogo", e)
                onComplete(null)
            }
    }

    // Cria uma nova partida
    fun createGame(player1Id: String, onGameCreated: (String?) -> Unit) {
        val gameData = hashMapOf(
            "player1Id" to player1Id,
            "boardPlayer1" to createEmptyBoard(),
            "boardPlayer2" to createEmptyBoard(),
            "turn" to 1,
            "status" to "waiting"
        )

        db.collection("games")  // Aqui, a coleção "games" será criada se não existir
            .add(gameData)
            .addOnSuccessListener { documentReference ->
                onGameCreated(documentReference.id)  // Retorna o ID do jogo
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseService", "Erro ao criar partida", e)
                onGameCreated(null)  // Caso não consiga criar o jogo
            }
    }

    // Aceita um desafio e inicia a partida
    fun acceptGame(gameId: String, player2Id: String, onGameStarted: (Boolean) -> Unit) {
        val gameRef = db.collection("games").document(gameId)

        gameRef.update("player2Id", player2Id, "status", "in_progress")
            .addOnSuccessListener {
                onGameStarted(true)
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseService", "Erro ao aceitar desafio", e)
                onGameStarted(false)
            }
    }

    //Salva o estado do tabuleiro no Firestore
    fun saveBoardToFirebase(gameId: String, board: List<List<CellState>>, onComplete: (Boolean) -> Unit) {
        val formattedBoard = board.flatMapIndexed { row, cols ->
            cols.mapIndexed { col, cell ->
                mapOf(
                    "row" to row,
                    "col" to col,
                    "state" to cell.name
                )
            }
        }

        db.collection("games").document(gameId)
            .update("boardPlayer1", formattedBoard)
            .addOnSuccessListener {
                Log.d("FirebaseService", "Tabuleiro salvo com sucesso")
                onComplete(true)
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseService", "Erro ao salvar tabuleiro", e)
                onComplete(false)
            }
    }

    //Recupera o estado do tabuleiro do Firestore.
    fun getBoardFromFirebase(gameId: String, onComplete: (List<List<CellState>>?) -> Unit) {
        db.collection("games").document(gameId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val boardData = documentSnapshot.get("boardPlayer1") as? List<Map<String, Any>>
                val board = List(8) { row ->
                    List(8) { col ->
                        val cell = boardData?.find { it["row"] == row && it["col"] == col }
                        CellState.valueOf(cell?.get("state") as? String ?: CellState.EMPTY.name)
                    }
                }
                onComplete(board)
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseService", "Erro ao recuperar tabuleiro", e)
                onComplete(null)
            }
    }

    fun saveFormattedBoards(
        gameId: String,
        boardPlayer1: List<Map<String, Any>>,
        boardPlayer2: List<Map<String, Any>>,
        onComplete: () -> Unit
    ) {
        val gameRef = db.collection("games").document(gameId)

        val boardData = mapOf(
            "boardPlayer1" to boardPlayer1,
            "boardPlayer2" to boardPlayer2
        )

        gameRef.update(boardData)
            .addOnSuccessListener {
                Log.d("FirebaseService", "Tabuleiros salvos com sucesso.")
                onComplete()
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseService", "Erro ao salvar tabuleiros", e)
            }
    }

    //Estado do Jogo no Firebase
    fun updateTurn(gameId: String, nextTurn: Int, onComplete: (Boolean) -> Unit) {
        db.collection("games").document(gameId)
            .update("turn", nextTurn)
            .addOnSuccessListener {
                Log.d("FirebaseService", "Turno atualizado com sucesso")
                onComplete(true)
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseService", "Erro ao atualizar o turno", e)
                onComplete(false)
            }
    }

    //Verificar o Turno Atual
    fun getTurn(gameId: String, onComplete: (Int?) -> Unit) {
        db.collection("games").document(gameId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val turn = documentSnapshot.getLong("turn")?.toInt()
                onComplete(turn)
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseService", "Erro ao obter o turno atual", e)
                onComplete(null)
            }
    }

    //verificar e atualizar o estado do jogo
    fun makeMove(gameId: String, playerId: String, row: Int, col: Int, onComplete: (Boolean) -> Unit) {
        getTurn(gameId) { turn ->
            if (turn != null) {
                if ((turn % 2 == 1 && playerId == "player1Id") || (turn % 2 == 0 && playerId == "player2Id")) {
                    // Lógica para atualizar o tabuleiro com a jogada do jogador
                    val boardField = if (playerId == "player1Id") "boardPlayer1" else "boardPlayer2"
                    val gameRef = db.collection("games").document(gameId)

                    gameRef.update(boardField, listOf(mapOf("row" to row, "col" to col, "state" to CellState.HIT.name))) // Simples exemplo

                    // Atualizar o turno
                    updateTurn(gameId, turn + 1) { success ->
                        onComplete(success)
                    }
                } else {
                    onComplete(false) // Não é o turno do jogador
                }
            }
        }
    }

    //Salva o tabuleiro
    fun saveBoard(gameId: String, boardPlayer1: List<List<CellState>>, boardPlayer2: List<List<CellState>>, onComplete: () -> Unit) {
        val gameRef = db.collection("games").document(gameId)

        val boardData: Map<String, Any> = hashMapOf(
            "boardPlayer1" to boardPlayer1,
            "boardPlayer2" to boardPlayer2
        )

        gameRef.update(boardData)
            .addOnSuccessListener {
                onComplete()  // Chama o callback após a atualização bem-sucedida
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseService", "Erro ao salvar tabuleiro", e)
            }
    }

    // Atualiza o tabuleiro do jogo
    fun saveBoardState(boardPlayer1: List<List<CellState>>, boardPlayer2: List<List<CellState>>) {
        FirebaseService.getGameId { gameId ->
            if (gameId != null) {
                val gameRef = db.collection("games").document(gameId)
                // Salvar os tabuleiros dos jogadores no Firebase
                gameRef.update(
                    "boardPlayer1", boardPlayer1,
                    "boardPlayer2", boardPlayer2
                ).addOnSuccessListener {
                    Log.d("FirebaseService", "Tabuleiro atualizado com sucesso.")
                }.addOnFailureListener { e ->
                    Log.e("FirebaseService", "Erro ao atualizar o tabuleiro", e)
                }
            } else {
                Log.e("FirebaseService", "Erro: Nenhum jogo encontrado.")
            }
        }
    }

    //Salva o score
    fun saveScore(playerId: String, score: Int) {
        val data = mapOf(
            "playerId" to playerId,
            "score" to score,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("leaderboard")
            .add(data)
            .addOnSuccessListener {
                // Sucesso ao salvar a pontuação
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseService", "Erro ao salvar pontuação", e)
            }
    }

    fun getTopScores(onResult: (List<ScoreItem>) -> Unit) {
        db.collection("leaderboard")
            .orderBy("score", Query.Direction.DESCENDING)
            .limit(10)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val list = querySnapshot.documents.mapNotNull { doc ->
                    val score = doc.getLong("score")?.toInt()
                    val playerId = doc.getString("playerId")
                    // Só adiciona na lista se os campos estiverem corretos
                    if (score != null && playerId != null) {
                        ScoreItem(playerId, score)
                    } else {
                        null
                    }
                }
                onResult(list)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }
}
