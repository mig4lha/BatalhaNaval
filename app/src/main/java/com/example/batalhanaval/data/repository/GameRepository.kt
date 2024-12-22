package com.example.batalhanaval.data.repository

import com.example.batalhanaval.data.model.ScoreItem
import com.example.batalhanaval.data.firebase.FirebaseService

/**
 * Repositório responsável por gerenciar a lógica de dados do jogo,
 * incluindo autenticação e manipulação de pontuações no Firebase.
 */
class GameRepository {

    /**
     * Registra um novo jogador no Firebase com o nick e a senha fornecidos, delegando para o FirebaseService.
     * Retorna o playerId ao callback caso o registro seja bem-sucedido; caso contrário, null.
     */
    fun registerPlayer(nick: String, password: String, onComplete: (String?) -> Unit) {
        FirebaseService.registerPlayer(nick, password, onComplete)
    }

    /**
     * Salva uma pontuação no Firestore (leaderboard).
     * @param playerId identificador do jogador (pode ser o uid retornado pela Auth).
     * @param score pontuação a ser salva.
     */
    fun saveScore(playerId: String, score: Int) {
        FirebaseService.saveScore(playerId, score)
    }

    /**
     * Busca os 10 melhores scores (ranking) e retorna via callback [onResult].
     * @param onResult callback que recebe uma lista de [ScoreItem].
     */
    fun getTopScores(onResult: (List<ScoreItem>) -> Unit) {
        FirebaseService.getTopScores(onResult)
    }
}
