package com.example.batalhanaval.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.batalhanaval.data.model.ScoreItem

@Composable
fun LeaderboardScreen(
    scores: List<ScoreItem>,
    modifier: Modifier = Modifier // Adicionando o parâmetro modifier aqui
) {
    LazyColumn(modifier = modifier) { // Usando o modifier aqui
        items(scores.size) { index ->
            val item = scores[index]
            Text(
                text = "${index + 1}. ${item.playerId} - ${item.score}",
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}
