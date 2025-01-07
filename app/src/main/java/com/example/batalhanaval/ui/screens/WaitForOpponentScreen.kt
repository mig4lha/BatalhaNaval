package com.example.batalhanaval.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun WaitForOpponentScreen(
    gameId: String,
    currentPlayer: String,
    onBackToMenu: (String) -> Unit
) {
    // Captura o evento de voltar e impede que o jogador volte para a tela anterior
    BackHandler {
        // Não faz nada ao pressionar o botão de voltar
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Waiting for the opponent...")

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { onBackToMenu(currentPlayer) }, modifier = Modifier.padding(top = 16.dp)) {
            Text("Return to Menu")
        }
    }
}


