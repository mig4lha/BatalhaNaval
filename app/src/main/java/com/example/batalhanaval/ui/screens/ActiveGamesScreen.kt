package com.example.batalhanaval.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.batalhanaval.data.firebase.FirebaseService

@Composable
fun ActiveGamesScreen(
    playerName: String,
    onGameSelected: (String, String) -> Unit,
    onBackToMenu: () -> Unit
) {
    val games = remember { mutableStateOf<List<Map<String, Any>>?>(null) }

    LaunchedEffect(playerName) {
        FirebaseService.getActiveGamesForPlayer(playerName) { result ->
            games.value = result
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Jogos Ativos de $playerName", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(16.dp))

        if (games.value == null) {
            CircularProgressIndicator()
        } else if (games.value!!.isEmpty()) {
            Text("Nenhum jogo ativo encontrado.")
        } else {
            games.value!!.forEach { game ->
                val gameId = game["id"] as String
                val status = game["status"] as String
                val opponent = game["opponent"] as String

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .clickable {
                            when (status) {
                                "placing" -> onGameSelected(gameId, "placing")
                                "playing" -> onGameSelected(gameId, "playing")
                                else -> { /* Handle other cases if needed */ }
                            }
                        }
                ) {
                    Text("$opponent - $status")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onBackToMenu) {
            Text("Voltar")
        }
    }
}
