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
    playerName: String, // Nome (nick) do jogador
    onStartLocalGame: () -> Unit,
    onStartOnlineGame: () -> Unit,
    onShowLeaderboard: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        // TopAppBar com o nome do jogador
        TopAppBar(
            title = { Text("Bem-vindo, $playerName!") },
            modifier = Modifier.padding(bottom = 16.dp) // Espaçamento entre a barra e o conteúdo
        )

        // Botões do menu principal
        Button(onClick = { onStartLocalGame() }) {
            Text("Start Local Game")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { onStartOnlineGame() }) {
            Text("Start Online Game")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { onShowLeaderboard() }) {
            Text("Leaderboard")
        }
    }
}
