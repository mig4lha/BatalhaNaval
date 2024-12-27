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
        }.flatten()

        val gameData = hashMapOf(
            "player1" to player1,
            "player2" to player2,
            "status" to "active",
            "player1Status" to "placing",
            "player2Status" to "waiting",
            "turn" to 0,
            "timestamp" to System.currentTimeMillis(),
            "boardPlayer1" to boardData,
            "boardPlayer2" to boardData,
            "boardPlayer1Tracking" to boardData,
            "boardPlayer2Tracking" to boardData
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

    // Recupera os tabuleiros de tracking para o jogador atual e o adversário
    fun getGameBoards(gameId: String, currentPlayer: String, onComplete: (Map<String, List<List<CellState>>>?) -> Unit) {
        db.collection("games").document(gameId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                // Identificar se o jogador é player1 ou player2
                val player1 = documentSnapshot.getString("player1")
                val player2 = documentSnapshot.getString("player2")

                val isPlayer1 = currentPlayer == player1

                // Selecionar os tabuleiros corretos
                val currentPlayerBoardKey = if (isPlayer1) "boardPlayer1Tracking" else "boardPlayer2Tracking"
                val opponentBoardKey = if (isPlayer1) "boardPlayer2" else "boardPlayer1"

                val currentPlayerBoardData = documentSnapshot.get(currentPlayerBoardKey) as? List<Map<String, Any>>
                val opponentBoardData = documentSnapshot.get(opponentBoardKey) as? List<Map<String, Any>>

                val currentPlayerBoard = List(8) { row ->
                    List(8) { col ->
                        val cell = currentPlayerBoardData?.find { it["row"] == row && it["col"] == col }
                        CellState.valueOf(cell?.get("state") as? String ?: CellState.EMPTY.name)
                    }
                }

                val opponentBoard = List(8) { row ->
                    List(8) { col ->
                        val cell = opponentBoardData?.find { it["row"] == row && it["col"] == col }
                        CellState.valueOf(cell?.get("state") as? String ?: CellState.EMPTY.name)
                    }
                }

                // Retornar os dois tabuleiros
                onComplete(
                    mapOf(
                        "currentPlayerBoard" to currentPlayerBoard,
                        "opponentBoard" to opponentBoard
                    )
                )
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseService", "Erro ao recuperar tabuleiros", e)
                onComplete(null)
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

    fun getActiveGamesForPlayer(playerName: String, onComplete: (List<Map<String, Any>>) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val gamesCollection = db.collection("games")

        gamesCollection.whereEqualTo("status", "active")
            .get()
            .addOnSuccessListener { snapshot ->
                val games = snapshot.documents.filter { document ->
                    val isPlayer1 = document.getString("player1") == playerName
                    val isPlayer2 = document.getString("player2") == playerName
                    isPlayer1 || isPlayer2
                }.map { document ->
                    val isPlayer1 = document.getString("player1") == playerName
                    val opponentKey = if (isPlayer1) "player2" else "player1"
                    val statusKey = if (isPlayer1) "player1Status" else "player2Status"

                    mapOf(
                        "id" to (document.id as Any), // Garante compatibilidade de tipos
                        "opponent" to (document.getString(opponentKey) ?: "Desconhecido"),
                        "status" to (document.getString(statusKey) ?: "waiting"),
                        "turn" to (document.getLong("turn")?.toInt() ?: 0) // Convertendo para Int explicitamente
                    )
                }
                onComplete(games)
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseService", "Erro ao buscar jogos", e)
                onComplete(emptyList())
            }
    }

    fun updatePlayerStatusAfterPlacement(gameId: String, currentPlayer: String, onComplete: () -> Unit) {
        val gameRef = FirebaseFirestore.getInstance().collection("games").document(gameId)

        gameRef.get()
            .addOnSuccessListener { document ->
                val player1 = document.getString("player1") ?: ""
                val player2 = document.getString("player2") ?: ""

                val updates = if (currentPlayer == player1) {
                    mapOf(
                        "player1Status" to "wait",
                        "player2Status" to "placing"
                    )
                } else {
                    mapOf(
                        "player2Status" to "wait",
                        "player1Status" to "playing"
                    )
                }

                gameRef.update(updates)
                    .addOnSuccessListener {
                        onComplete()
                    }
                    .addOnFailureListener { e ->
                        Log.e("FirebaseService", "Erro ao atualizar status dos jogadores", e)
                    }
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseService", "Erro ao obter dados do jogo", e)
            }
    }

    fun saveFormattedBoards(
        gameId: String,
        boardPlayer1: List<Map<String, Any>>,
        boardPlayer2: List<Map<String, Any>>,
        onComplete: () -> Unit
    ) {
        val gameRef = db.collection("games").document(gameId)

        gameRef.update(
            mapOf(
                "boardPlayer1" to boardPlayer1,
                "boardPlayer2" to boardPlayer2
            )
        )
            .addOnSuccessListener {
                Log.d("FirebaseService", "Tabuleiros salvos com sucesso.")
                onComplete()
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseService", "Erro ao salvar tabuleiros", e)
            }
    }

    fun updatePlayerStatus(gameId: String, playerStatus: Map<String, String>, onComplete: (Boolean) -> Unit) {
        db.collection("games").document(gameId)
            .update(playerStatus)
            .addOnSuccessListener {
                Log.d("FirebaseService", "Status atualizado com sucesso.")
                onComplete(true)
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseService", "Erro ao atualizar status", e)
                onComplete(false)
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
    fun saveBoard(
        gameId: String,
        boardPlayer1: List<List<CellState>>,
        boardPlayer2: List<List<CellState>>,
        onComplete: () -> Unit
    ) {
        val gameRef = db.collection("games").document(gameId)

        gameRef.update(
            mapOf(
                "boardPlayer1" to boardPlayer1.map { row -> row.map { it.name } },
                "boardPlayer2" to boardPlayer2.map { row -> row.map { it.name } }
            )
        )
            .addOnSuccessListener {
                Log.d("FirebaseService", "Tabuleiros salvos com sucesso.")
                onComplete()
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseService", "Erro ao salvar tabuleiros", e)
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

    /**
     * Registra um tiro no tabuleiro e atualiza o estado do jogo.
     */
    fun registerShot(
        gameId: String,
        playerName: String,
        row: Int,
        col: Int,
        onComplete: (Boolean) -> Unit
    ) {
        db.collection("games").document(gameId).get()
            .addOnSuccessListener { document ->
                val isPlayer1 = document.getString("player1") == playerName
                val opponentBoardField = if (isPlayer1) "boardPlayer2" else "boardPlayer1"

                val opponentBoard = document.get(opponentBoardField) as? List<Map<String, Any>> ?: emptyList()

                // Localizar a célula atingida
                val updatedBoard = opponentBoard.map { cell ->
                    if (cell["row"] == row && cell["col"] == col) {
                        val cellState = cell["state"] as? String ?: CellState.EMPTY.name
                        if (cellState == CellState.SHIP.name) {
                            cell + ("state" to CellState.HIT.name) // Marca como HIT
                        } else {
                            cell + ("state" to CellState.MISS.name) // Marca como MISS
                        }
                    } else {
                        cell
                    }
                }

                // Verificar se o jogo deve terminar
                val allShipsHit = updatedBoard.none { it["state"] == CellState.SHIP.name }
                val updates = hashMapOf<String, Any>(
                    opponentBoardField to updatedBoard
                )

                if (allShipsHit) {
                    updates["status"] = "finished"
                    updates["winner"] = playerName
                }

                // Atualizar o Firestore
                db.collection("games").document(gameId)
                    .update(updates)
                    .addOnSuccessListener {
                        onComplete(true)
                    }
                    .addOnFailureListener {
                        onComplete(false)
                    }
            }
            .addOnFailureListener {
                onComplete(false)
            }
    }

    /**
     * Verifica se o jogo terminou.
     */
    private fun checkGameOver(gameId: String, onComplete: (Boolean) -> Unit) {
        db.collection("games").document(gameId).get()
            .addOnSuccessListener { document ->
                val boardPlayer1 = document.get("boardPlayer1") as? List<Map<String, Any>> ?: emptyList()
                val boardPlayer2 = document.get("boardPlayer2") as? List<Map<String, Any>> ?: emptyList()

                val player1ShipsRemaining = boardPlayer1.count { it["state"] == CellState.SHIP.name }
                val player2ShipsRemaining = boardPlayer2.count { it["state"] == CellState.SHIP.name }

                if (player1ShipsRemaining == 0 || player2ShipsRemaining == 0) {
                    val winner = if (player1ShipsRemaining > 0) document.getString("player1") else document.getString("player2")
                    val loser = if (player1ShipsRemaining > 0) document.getString("player2") else document.getString("player1")
                    val turns = document.getLong("turn")?.toInt() ?: 0

                    db.collection("games").document(gameId).update("status", "finished")
                    db.collection("games").document(gameId).update("winner", winner, "loser", loser)
                    saveScore(winner ?: "", turns)
                    onComplete(true)
                } else {
                    onComplete(false)
                }
            }
            .addOnFailureListener {
                onComplete(false)
            }
    }

    /**
     * Alterna o turno entre os jogadores.
     */
    private fun switchTurn(gameId: String, isPlayer1: Boolean, onComplete: (Boolean) -> Unit) {
        val currentPlayerField = if (isPlayer1) "player1Status" else "player2Status"
        val opponentPlayerField = if (isPlayer1) "player2Status" else "player1Status"

        db.collection("games").document(gameId).update(
            mapOf(
                currentPlayerField to "wait",
                opponentPlayerField to "playing",
                "turn" to FirebaseFirestore.getInstance().collection("games").document(gameId).get().result?.getLong("turn")?.plus(1)
            )
        ).addOnSuccessListener {
            onComplete(true)
        }.addOnFailureListener {
            onComplete(false)
        }
    }

    fun getPlayerState(gameId: String, currentPlayer: String, onComplete: (String?) -> Unit) {
        db.collection("games").document(gameId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val state = if (documentSnapshot.getString("player1") == currentPlayer) {
                    documentSnapshot.getString("player1Status")
                } else if (documentSnapshot.getString("player2") == currentPlayer) {
                    documentSnapshot.getString("player2Status")
                } else {
                    null
                }
                onComplete(state)
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseService", "Erro ao obter estado do jogador", e)
                onComplete(null)
            }
    }

    fun getOpponentName(gameId: String, currentPlayer: String, onComplete: (String?) -> Unit) {
        db.collection("games").document(gameId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val player1 = documentSnapshot.getString("player1")
                val player2 = documentSnapshot.getString("player2")

                val opponentName = if (currentPlayer == player1) {
                    player2
                } else if (currentPlayer == player2) {
                    player1
                } else {
                    null
                }
                onComplete(opponentName)
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseService", "Erro ao buscar nome do oponente", e)
                onComplete(null)
            }
    }

    /**
     * Salva o score no leaderboard.
     */
    private fun saveScore(playerName: String, score: Int) {
        val leaderboardEntry = mapOf(
            "playerName" to playerName,
            "score" to score
        )
        db.collection("leaderboard").add(leaderboardEntry)
    }

    //Salva o score
    fun saveScorePlayer(playerId: String, score: Int) {
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
