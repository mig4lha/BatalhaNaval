package com.example.batalhanaval.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun LeaderboardScreen(
    viewModel: LeaderboardScreenViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val scores = viewModel.scores.collectAsState().value

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Título
        Text(
            text = "Top Ten Best Players",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF6200EE), // Cor roxa para destaque
            fontSize = 24.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Lista de classificação
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF1F1F1)) // Fundo cinza claro
                .padding(8.dp)
        ) {
            itemsIndexed(scores) { index, item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .background(
                            if (index % 2 == 0) Color(0xFFF8F8FF) else Color.White // Alternar entre duas cores
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Nome do jogador
                    Text(
                        text = "${index + 1}. ${item.name}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF333333)
                    )

                    // Pontuação
                    Text(
                        text = "${item.score}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6200EE) // Mesma cor do título
                    )
                }
            }
        }
    }
}

