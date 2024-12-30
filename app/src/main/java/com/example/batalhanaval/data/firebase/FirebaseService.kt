package com.example.batalhanaval.data.firebase

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.example.batalhanaval.data.model.ScoreItem
import com.example.batalhanaval.ui.components.CellState
import com.google.firebase.firestore.SetOptions

object FirebaseService {

    // Instância do FirebaseAuth
    private val auth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    // Instância do Firestore
    private val db: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
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

    fun getPlayers(gameId: String, onComplete: (String?, String?) -> Unit) {
        db.collection("games").document(gameId)
            .get()
            .addOnSuccessListener { document ->
                val player1 = document.getString("player1")
                val player2 = document.getString("player2")

                if (player1 != null && player2 != null) {
                    onComplete(player1, player2)
                } else {
                    Log.e("FirebaseService", "Os campos player1 ou player2 estão nulos no jogo $gameId")
                    onComplete(null, null)
                }
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseService", "Erro ao obter players no jogo $gameId", e)
                onComplete(null, null)
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


    //Salva o estado do tabuleiro no Firestore
    fun saveBoardToFirebase(
        gameId: String,
        currentPlayer: String,
        board: List<List<CellState>>,
        onComplete: (Boolean) -> Unit
    ) {
        // Obtém os jogadores antes de salvar o tabuleiro
        getPlayers(gameId) { player1, player2 ->
            if (player1 == null || player2 == null) {
                Log.e("FirebaseService", "Não foi possível obter os jogadores para o jogo $gameId")
                onComplete(false)
                return@getPlayers
            }

            // Determina a chave do tabuleiro com base no jogador atual
            val boardKey = if (currentPlayer == player1) "boardPlayer1" else "boardPlayer2"

            // Formata o tabuleiro para salvar no Firestore
            val formattedBoard = board.flatMapIndexed { row, cols ->
                cols.mapIndexed { col, cell ->
                    mapOf(
                        "row" to row,
                        "col" to col,
                        "state" to cell.name
                    )
                }
            }

            // Atualiza o Firestore com o tabuleiro correto
            db.collection("games").document(gameId)
                .update(boardKey, formattedBoard)
                .addOnSuccessListener {
                    Log.d("FirebaseService", "Tabuleiro salvo com sucesso no $boardKey")
                    onComplete(true)
                }
                .addOnFailureListener { e ->
                    Log.e("FirebaseService", "Erro ao salvar tabuleiro no $boardKey", e)
                    onComplete(false)
                }
        }
    }

    // Recupera os tabuleiros de tracking para o jogador atual e o adversário
    fun getGameBoards(
        gameId: String,
        currentPlayer: String,
        onComplete: (Map<String, List<List<CellState>>>) -> Unit
    ) {
        db.collection("games").document(gameId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val player1 = documentSnapshot.getString("player1") ?: ""
                val player2 = documentSnapshot.getString("player2") ?: ""

                if (player1.isEmpty() || player2.isEmpty()) {
                    Log.e("FirebaseService", "Os jogadores não estão definidos no documento: player1=$player1, player2=$player2")
                    onComplete(emptyMap())
                    return@addOnSuccessListener
                }

                Log.d("FirebaseService", "Jogadores encontrados: player1=$player1, player2=$player2")
                Log.d("FirebaseService", "currentPlayer: $currentPlayer")

                val boardPlayer1 = parseBoard(documentSnapshot.get("boardPlayer1") as? List<Map<String, Any>>)
                val boardPlayer2 = parseBoard(documentSnapshot.get("boardPlayer2") as? List<Map<String, Any>>)
                val boardTrackingPlayer1 = parseBoard(documentSnapshot.get("boardPlayer1Tracking") as? List<Map<String, Any>>)
                val boardTrackingPlayer2 = parseBoard(documentSnapshot.get("boardPlayer2Tracking") as? List<Map<String, Any>>)

                val isPlayer1 = currentPlayer == player1

                val currentPlayerBoard = if (isPlayer1) boardPlayer1 else boardPlayer2
                val trackingBoard = if (isPlayer1) boardTrackingPlayer1 else boardTrackingPlayer2
                val opponentBoard = if (isPlayer1) boardPlayer2 else boardPlayer1

                Log.d("FirebaseService", "Tabuleiro do jogador atual: $currentPlayerBoard")
                Log.d("FirebaseService", "Tabuleiro de rastreamento: $trackingBoard")
                Log.d("FirebaseService", "Tabuleiro do oponente: $opponentBoard")

                onComplete(
                    mapOf(
                        "currentPlayerBoard" to currentPlayerBoard,
                        "trackingBoard" to trackingBoard,
                        "opponentBoard" to opponentBoard
                    )
                )
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseService", "Erro ao carregar os tabuleiros", e)
                onComplete(emptyMap())
            }
    }

    fun parseBoard(boardData: List<Map<String, Any>>?): List<List<CellState>> {
        // Inicializar o tabuleiro vazio
        val emptyBoard = List(8) { List(8) { CellState.EMPTY } }

        if (boardData == null) {
            Log.e("FirebaseService", "Os dados do tabuleiro são nulos.")
            return emptyBoard
        }

        // Criar uma matriz mutável para atualizar os estados
        val board = emptyBoard.map { it.toMutableList() }

        boardData.forEach { cell ->
            try {
                val row = (cell["row"] as? Long)?.toInt() ?: -1
                val col = (cell["col"] as? Long)?.toInt() ?: -1
                val state = cell["state"] as? String ?: CellState.EMPTY.name

                if (row in 0..7 && col in 0..7) {
                    board[row][col] = CellState.valueOf(state)
                } else {
                    Log.e("FirebaseService", "Coordenadas inválidas: row=$row, col=$col")
                }
            } catch (e: Exception) {
                Log.e("FirebaseService", "Erro ao processar célula: $cell", e)
            }
        }

        // Log do tabuleiro atualizado
        board.forEachIndexed { rowIndex, row ->
            row.forEachIndexed { colIndex, cell ->
                Log.d("FirebaseService", "Célula [$rowIndex][$colIndex]: $cell")
            }
        }

        return board
    }

    fun updateCellState(
        boardArray: MutableList<MutableMap<String, Any>>,
        changes: Map<String, String>
    ): MutableList<MutableMap<String, Any>> {
        changes.forEach { (cell, state) ->
            val (row, col) = cell.split(".").map { it.toInt() }

            // Logs adicionais para verificar os valores
            Log.d("updateCellState", "Procurando célula: row=$row, col=$col")

            val existingCell = boardArray.find {
                (it["row"] as? Int ?: (it["row"] as? Long)?.toInt()) == row &&
                        (it["col"] as? Int ?: (it["col"] as? Long)?.toInt()) == col
            }

            Log.d("updateCellState", "Encontrada a célula: row=$row, col=$col")

            if (existingCell != null) {
                // Atualiza o estado da célula existente
                Log.d("updateCellState", "Atualizando célula existente: row=$row, col=$col, oldState=${existingCell["state"]}, newState=$state")
                existingCell["state"] = state
            } else {
                // Adiciona a célula caso ela não exista
                Log.d("updateCellState", "Adicionando nova célula: row=$row, col=$col, state=$state")
                boardArray.add(mutableMapOf("row" to row, "col" to col, "state" to state))
            }
        }
        Log.d("updateCellState", "Tamanho do boardArray após atualizações: ${boardArray.size}")
        return boardArray
    }

    fun updateBoards(
        gameId: String,
        currentPlayerName: String,
        trackingBoardChanges: Map<String, String>,
        opponentBoardChanges: Map<String, String>,
        onComplete: (Boolean) -> Unit
    ) {
        db.collection("games").document(gameId).get()
            .addOnSuccessListener { gameDocument ->
                val player1Name = gameDocument.getString("player1") ?: ""
                val player2Name = gameDocument.getString("player2") ?: ""

                val updates = mutableMapOf<String, Any>()

                // Obtém os arrays existentes do Firestore
                val boardPlayer1Tracking = (gameDocument["boardPlayer1Tracking"] as? List<Map<String, Any>>)
                    ?.map { it.toMutableMap() }?.toMutableList() ?: mutableListOf()
                val boardPlayer2Tracking = (gameDocument["boardPlayer2Tracking"] as? List<Map<String, Any>>)
                    ?.map { it.toMutableMap() }?.toMutableList() ?: mutableListOf()
                val boardPlayer1 = (gameDocument["boardPlayer1"] as? List<Map<String, Any>>)
                    ?.map { it.toMutableMap() }?.toMutableList() ?: mutableListOf()
                val boardPlayer2 = (gameDocument["boardPlayer2"] as? List<Map<String, Any>>)
                    ?.map { it.toMutableMap() }?.toMutableList() ?: mutableListOf()

                Log.d("updateBoards", "Tamanho inicial de boardPlayer1Tracking: ${boardPlayer1Tracking.size}")
                Log.d("updateBoards", "Tamanho inicial de boardPlayer2Tracking: ${boardPlayer2Tracking.size}")
                Log.d("updateBoards", "Tamanho inicial de boardPlayer1: ${boardPlayer1.size}")
                Log.d("updateBoards", "Tamanho inicial de boardPlayer2: ${boardPlayer2.size}")

                if (currentPlayerName == player1Name) {
                    if (trackingBoardChanges.isNotEmpty()) {
                        val updatedBoard = updateCellState(boardPlayer1Tracking, trackingBoardChanges)
                        updates["boardPlayer1Tracking"] = updatedBoard
                    }
                    if (opponentBoardChanges.isNotEmpty()) {
                        val updatedBoard = updateCellState(boardPlayer2, opponentBoardChanges)
                        updates["boardPlayer2"] = updatedBoard
                    }
                } else if (currentPlayerName == player2Name) {
                    if (trackingBoardChanges.isNotEmpty()) {
                        val updatedBoard = updateCellState(boardPlayer2Tracking, trackingBoardChanges)
                        updates["boardPlayer2Tracking"] = updatedBoard
                    }
                    if (opponentBoardChanges.isNotEmpty()) {
                        val updatedBoard = updateCellState(boardPlayer1, opponentBoardChanges)
                        updates["boardPlayer1"] = updatedBoard
                    }
                }

                Log.d("updateBoards", "Tamanho final de boardPlayer1Tracking: ${boardPlayer1Tracking.size}")
                Log.d("updateBoards", "Tamanho final de boardPlayer2Tracking: ${boardPlayer2Tracking.size}")
                Log.d("updateBoards", "Tamanho final de boardPlayer1: ${boardPlayer1.size}")
                Log.d("updateBoards", "Tamanho final de boardPlayer2: ${boardPlayer2.size}")

                if (updates.isNotEmpty()) {
                    db.collection("games").document(gameId)
                        .set(updates, SetOptions.merge()) // Usa merge para substituir arrays corretamente
                        .addOnSuccessListener {
                            Log.d("FirebaseService", "Tabuleiros atualizados com sucesso no Firestore.")
                            onComplete(true)
                        }
                        .addOnFailureListener { e ->
                            Log.e("FirebaseService", "Erro ao atualizar tabuleiros no Firestore: ", e)
                            onComplete(false)
                        }
                } else {
                    Log.w("FirebaseService", "Nenhuma mudança detectada nos tabuleiros.")
                    onComplete(false)
                }
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseService", "Erro ao buscar documento do jogo: ", e)
                onComplete(false)
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


    fun updatePlayerStates(
        gameId: String,
        currentPlayerName: String,
        onComplete: (Boolean) -> Unit
    ) {
        db.collection("games").document(gameId).get()
            .addOnSuccessListener { gameDocument ->
                val player1Name = gameDocument.getString("player1") ?: ""
                val player2Name = gameDocument.getString("player2") ?: ""
                val currentTurn = gameDocument.getLong("turn") ?: 0

                val updates = mutableMapOf<String, Any>()

                if (currentPlayerName == player1Name) {
                    updates["player1Status"] = "wait"
                    updates["player2Status"] = "playing"
                } else if (currentPlayerName == player2Name) {
                    updates["player1Status"] = "playing"
                    updates["player2Status"] = "wait"
                }

                // Incrementa o turno
                updates["turn"] = currentTurn + 1

                db.collection("games").document(gameId)
                    .update(updates)
                    .addOnSuccessListener {
                        Log.d("FirebaseService", "Estados dos jogadores e turno atualizados com sucesso.")
                        onComplete(true)
                    }
                    .addOnFailureListener { e ->
                        Log.e("FirebaseService", "Erro ao atualizar estados dos jogadores e turno: ", e)
                        onComplete(false)
                    }
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseService", "Erro ao buscar documento do jogo: ", e)
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

    fun updateGameStatusToFinished(
        gameId: String,
        winner: String,
        onComplete: (Boolean) -> Unit
    ) {
        val gameRef = db.collection("games").document(gameId)

        gameRef.get().addOnSuccessListener { document ->
            val player1 = document.getString("player1") ?: return@addOnSuccessListener
            val player2 = document.getString("player2") ?: return@addOnSuccessListener
            val turnCount = document.getLong("turn")?.toInt() ?: 0

            // Obtém o timestamp como número
            val timestamp = document.getLong("timestamp") ?: run {
                Log.e("FirebaseService", "Campo 'timestamp' ausente ou inválido.")
                onComplete(false)
                return@addOnSuccessListener
            }

            val updates = mutableMapOf<String, Any>(
                "status" to "finished",
                "winner" to winner
            )

            if (winner == player1) {
                updates["player1Status"] = "winner"
                updates["player2Status"] = "looser"
            } else {
                updates["player1Status"] = "looser"
                updates["player2Status"] = "winner"
            }

            // Atualiza o status do jogo no Firestore
            gameRef.update(updates).addOnSuccessListener {
                Log.d("FirebaseService", "Jogo finalizado com sucesso.")

                // Verifica se já existe uma entrada no leaderboard com o mesmo vencedor e timestamp
                val leaderboardRef = db.collection("leaderboard")
                leaderboardRef
                    .whereEqualTo("name", winner)
                    .whereEqualTo("timestamp", timestamp)
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        if (querySnapshot.isEmpty) {
                            // Adiciona a entrada ao leaderboard se não existir duplicado
                            val leaderboardEntry = mapOf(
                                "name" to winner,
                                "score" to turnCount,
                                "timestamp" to timestamp // Adiciona o timestamp como número
                            )
                            leaderboardRef.add(leaderboardEntry).addOnSuccessListener {
                                Log.d("FirebaseService", "Leaderboard atualizado com sucesso.")
                                onComplete(true)
                            }.addOnFailureListener { e ->
                                Log.e("FirebaseService", "Erro ao atualizar leaderboard.", e)
                                onComplete(false)
                            }
                        } else {
                            Log.d("FirebaseService", "Entrada duplicada detectada no leaderboard. Nenhuma ação necessária.")
                            onComplete(true)
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("FirebaseService", "Erro ao verificar duplicados no leaderboard.", e)
                        onComplete(false)
                    }
            }.addOnFailureListener { e ->
                Log.e("FirebaseService", "Erro ao finalizar jogo.", e)
                onComplete(false)
            }
        }.addOnFailureListener { e ->
            Log.e("FirebaseService", "Erro ao buscar jogo.", e)
            onComplete(false)
        }
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
            .orderBy("score", Query.Direction.ASCENDING)
            .limit(10)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val list = querySnapshot.documents.mapNotNull { doc ->
                    val score = doc.getLong("score")?.toInt()
                    val playerName = doc.getString("name") // Corrigido para o campo correto
                    // Só adiciona na lista se os campos estiverem corretos
                    if (score != null && playerName != null) {
                        ScoreItem(playerName, score) // Use o nome do jogador em vez de playerId
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
