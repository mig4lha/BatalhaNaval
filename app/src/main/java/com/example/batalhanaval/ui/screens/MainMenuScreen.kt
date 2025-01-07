package com.example.batalhanaval.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MainMenuScreen(
    playerName: String,
    onStartOnlineGame: () -> Unit,
    onShowLeaderboard: () -> Unit,
    onShowActiveGames: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Título centralizado
        Text("Main Menu", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Welcome, $playerName!")

        Spacer(modifier = Modifier.height(24.dp))

        // Botões centralizados
        Button(onClick = { onStartOnlineGame() }, modifier = Modifier.fillMaxWidth(0.8f)) {
            Text("Start Online Game")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { onShowLeaderboard() }, modifier = Modifier.fillMaxWidth(0.8f)) {
            Text("Leaderboard")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { onShowActiveGames() }, modifier = Modifier.fillMaxWidth(0.8f)) {
            Text("Active Games")
        }
    }
}
