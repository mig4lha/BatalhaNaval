package com.example.batalhanaval.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.batalhanaval.ui.components.CellState
import com.example.batalhanaval.ui.viewmodels.GameScreenViewModel

@Composable
fun GameScreen(
    gameId: String,
    currentPlayer: String,
    onGameEnd: () -> Unit,
    navigateToWaitOpponentScreen: () -> Unit,
    viewModel: GameScreenViewModel = viewModel()
)  {
    val playerBoard = viewModel.playerBoard.collectAsState().value
    val trackingBoard = viewModel.trackingBoard.collectAsState().value
    val currentPlayerState = viewModel.currentPlayerState.collectAsState().value
    val opponentName = viewModel.opponentName.collectAsState().value

    LaunchedEffect(Unit) {
        Log.d("GameScreen", "Chamando initialize com gameId: $gameId e currentPlayer: $currentPlayer")
        viewModel.initialize(gameId, currentPlayer)
    }

    val shouldNavigate = viewModel.shouldNavigateToWaitOpponent.collectAsState().value

    LaunchedEffect(shouldNavigate) {
        if (shouldNavigate) {
            navigateToWaitOpponentScreen()
            viewModel.resetNavigationState()
        }
    }

    val shouldRefreshBoards = viewModel.shouldRefreshBoards.collectAsState().value

    LaunchedEffect(shouldRefreshBoards) {
        if (shouldRefreshBoards) {
            viewModel.refreshBoards(gameId, currentPlayer)
            viewModel.resetRefreshState() // Reseta o estado para evitar loops infinitos
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Informações sobre o jogador atual e o adversário
        Text(
            text = "Jogador Atual: $currentPlayer | Adversário: ${opponentName ?: "Carregando..."}",
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Tabuleiro do jogador
        Text("Seu Tabuleiro:")
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                playerBoard.forEach { row ->
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        row.forEach { cell ->
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .border(1.dp, Color.Black)
                                    .background(
                                        when (cell) {
                                            CellState.EMPTY -> Color.LightGray
                                            CellState.SHIP -> Color.DarkGray
                                            CellState.HIT -> Color.Red
                                            CellState.MISS -> Color.Black
                                        }
                                    )
                            )
                        }
                    }
                }
            }
        }

        // Tabuleiro de tracking
        Text("Tabuleiro de Rastreamento:")
        Box(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                trackingBoard.forEachIndexed { rowIndex, row ->
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        row.forEachIndexed { colIndex, cell ->
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .border(1.dp, Color.Black)
                                    .clickable(enabled = currentPlayerState == "playing" && cell == CellState.EMPTY) {
                                        viewModel.registerShot(
                                            gameId = gameId,
                                            currentPlayer = currentPlayer,
                                            row = rowIndex,
                                            col = colIndex,
                                            onShotComplete = { isGameFinished ->
                                                if (isGameFinished) {
                                                    onGameEnd() // Finaliza o jogo
                                                } else {
                                                    viewModel.refreshBoards(gameId, currentPlayer)
                                                }
                                            },
                                            onNavigateToWaitOpponent = navigateToWaitOpponentScreen
                                        )
                                    }
                                    .background(
                                        when (cell) {
                                            CellState.EMPTY -> Color.LightGray
                                            CellState.HIT -> Color.Red
                                            CellState.MISS -> Color.Black
                                            else -> Color.Transparent
                                        }
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}
