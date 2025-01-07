// Ajuste no fluxo para criação de jogo ao escolher o oponente

package com.example.batalhanaval.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.batalhanaval.data.firebase.FirebaseService
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChooseOpponentScreen(
    playerName: String, // Nome do jogador logado
    onOpponentSelected: (String, String) -> Unit, // Envia o oponente e o ID do jogo
    onBackToMenu: () -> Unit
) {
    val playersList = remember { mutableStateListOf<String>() } // Lista de jogadores disponíveis
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: "" // ID do jogador logado

    // Buscar a lista de jogadores do Firebase, excluindo o jogador logado
    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotEmpty()) {
            FirebaseService.getAllPlayersExceptCurrentUser(currentUserId) { players ->
                // Filtragem para garantir que o jogador logado não apareça na lista
                playersList.clear()
                playersList.addAll(players.filter { it != playerName }) // Exclui o jogador logado da lista
            }
        }
    }

    // Aguardando a atualização da lista de jogadores
    if (playersList.isEmpty()) {
        Text(text = "Carregando jogadores...")
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Text(text = "Choose Your Opponent")

            // Exibe a lista de jogadores disponíveis para o desafio
            playersList.forEach { opponent ->
                Button(onClick = {
                    FirebaseService.createNewGame(playerName, opponent) { gameId ->
                        if (gameId != null) {
                            onOpponentSelected(opponent, gameId) // Envia o oponente e o ID do jogo
                        }
                    }
                }) {
                    Text("Challenge $opponent")
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = { onBackToMenu() }) {
                Text("Back to Main Menu")
            }
        }
    }
}
