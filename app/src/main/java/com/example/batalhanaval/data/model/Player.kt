package com.example.batalhanaval.data.model

/**
 * Representa um jogador no jogo de Batalha Naval.
 *
 * @param userId Identificador (pode ser o UID do Firebase ou algo gerado no app).
 * @param name Nome do jogador (opcional).
 * @param score Pontuação do jogador (caso queira armazenar localmente).
 */
data class Player(
    val userId: String,
    val name: String? = null,
    val score: Int = 0
)
