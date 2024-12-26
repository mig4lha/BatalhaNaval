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
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainMenuScreen(
    playerName: String,
    onStartOnlineGame: () -> Unit, // Apenas jogo online
    onShowLeaderboard: () -> Unit,
    onShowChooseOpponent: () -> Unit // Nova função para abrir a tela de escolha do adversário
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        TopAppBar(
            title = { Text("Helcome, $playerName!") },
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Button(onClick = { onStartOnlineGame() }) {
            Text("Start Online Game")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { onShowLeaderboard() }) {
            Text("Leaderboard")
        }

        // Botão para navegar para a tela de escolha do adversário
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { onShowChooseOpponent() }) {
            Text("Choose Opponent")
        }
    }
}
