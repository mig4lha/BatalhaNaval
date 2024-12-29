package com.example.batalhanaval.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun WaitForOpponentScreen(
    gameId: String,
    currentPlayer: String,
    onBackToMenu: (String) -> Unit
){
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Aguardando pelo adversário...")

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { onBackToMenu(currentPlayer) }, modifier = Modifier.padding(top = 16.dp)) {
            Text("Voltar ao Menu Principal")
        }
    }
}

