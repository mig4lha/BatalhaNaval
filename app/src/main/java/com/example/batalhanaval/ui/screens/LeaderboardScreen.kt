package com.example.batalhanaval.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun LeaderboardScreen(
    viewModel: LeaderboardScreenViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val scores = viewModel.scores.collectAsState().value

    LazyColumn(modifier = modifier) {
        itemsIndexed(scores) { index, item ->
            Text(
                text = "${index + 1}. ${item.name} - ${item.score}",
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

