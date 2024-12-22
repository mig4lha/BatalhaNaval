package com.example.batalhanaval.data.firebase

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.example.batalhanaval.data.model.ScoreItem

object FirebaseService {

    // Instância do FirebaseAuth
    private val auth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    // Instância do Firestore
    private val db: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    /**
     * Faz login anônimo no Firebase, delegando para o FirebaseService.
     * Retorna o userId ao callback caso seja bem sucedido; caso contrário, null.
     */
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

    /**
     * Verifica se o nick já existe na coleção players.
     * @param nick Nome do jogador.
     * @param onResult Callback que retorna true se o nick estiver disponível, e false caso contrário.
     */
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

    /**
     * Registra um novo jogador com o nome e a senha fornecidos.
     * @param nick Nome do jogador.
     * @param password Senha do jogador.
     * @param onComplete Callback que retorna o playerId caso o registro seja bem-sucedido ou null caso contrário.
     */
    fun registerPlayer(nick: String, password: String, onComplete: (String?) -> Unit) {
        // Verifica se o jogador com esse nick já existe
        checkIfNickExists(nick) { isAvailable ->
            if (isAvailable) {
                // Se o nick for único, registra o jogador com o nick como ID
                val playerData = hashMapOf(
                    "name" to nick,
                    "password" to password,  // Salvando a senha no Firestore
                    "score" to 0  // Inicializando a pontuação do jogador
                )

                // Salvando o jogador na coleção "players" com o nick como ID
                db.collection("players")
                    .document(nick) // Usando o nick como ID do documento
                    .set(playerData)
                    .addOnSuccessListener {
                        onComplete(nick)  // Registro bem-sucedido
                    }
                    .addOnFailureListener { e ->
                        Log.e("FirebaseService", "Erro ao registrar jogador", e)
                        onComplete(null)  // Erro no registro
                    }
            } else {
                // Nick já existe, não pode registrar
                onComplete(null)  // Indica falha no registro devido a nick duplicado
            }
        }
    }

    /**
     * Realiza o login verificando o nick e a senha.
     * @param nick Nome do jogador.
     * @param password Senha do jogador.
     * @param onLoginComplete Callback que retorna o playerId se o login for bem-sucedido ou null caso contrário.
     */
    fun loginPlayer(nick: String, password: String, onLoginComplete: (String?) -> Unit) {
        db.collection("players")
            .whereEqualTo("name", nick)
            .whereEqualTo("password", password) // Verifica se a senha também corresponde
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    // Se não encontrar o jogador ou senha errada
                    onLoginComplete(null)
                } else {
                    val playerId = querySnapshot.documents[0].id
                    onLoginComplete(playerId)  // Login bem-sucedido
                }
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseService", "Erro ao fazer login", e)
                onLoginComplete(null)  // Erro ao verificar
            }
    }

    /**
     * Salva uma pontuação no Firestore, dentro da coleção "leaderboard".
     * @param playerId identificador do jogador (por exemplo, UID do Firebase Auth).
     * @param score pontuação que será salva.
     */
    fun saveScore(playerId: String, score: Int) {
        val data = mapOf(
            "playerId" to playerId,
            "score" to score,
            "timestamp" to System.currentTimeMillis()
        )
        db.collection("leaderboard")
            .add(data)
            .addOnSuccessListener {
                // Sucesso!
            }
            .addOnFailureListener { e ->
                // Tratar erro
                Log.e("FirebaseService", "Erro ao salvar pontuação", e)
            }
    }

    /**
     * Busca as 10 maiores pontuações na coleção "leaderboard" e retorna via callback [onResult].
     * @param onResult callback que recebe uma lista de [ScoreItem].
     */
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
