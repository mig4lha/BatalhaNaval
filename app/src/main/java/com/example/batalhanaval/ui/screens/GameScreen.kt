package com.example.batalhanaval.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
    viewModel: GameScreenViewModel = viewModel()
) {
    // Obter estados do ViewModel
    val playerBoard = viewModel.currentPlayerBoard.collectAsState().value
    val trackingBoard = viewModel.opponentBoard.collectAsState().value
    val currentPlayerState = viewModel.currentPlayerState.collectAsState().value
    val opponentName = viewModel.opponentName.collectAsState().value ?: "Adversário"

    // Layout da tela
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Jogador Atual: $currentPlayer | Adversário: $opponentName",
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Mostrar o tabuleiro do jogador
        Text("Seu Tabuleiro:")
        playerBoard.forEachIndexed { rowIndex, row ->
            Row {
                row.forEachIndexed { colIndex, cell ->
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
                    ) {}
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Mostrar o tabuleiro de rastreamento
        Text("Tabuleiro de Rastreamento:")
        trackingBoard.forEachIndexed { rowIndex, row ->
            Row {
                row.forEachIndexed { colIndex, cell ->
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .border(1.dp, Color.Black)
                            .clickable(enabled = currentPlayerState == "playing" && cell == CellState.EMPTY) {
                                viewModel.registerShot(gameId, currentPlayer, rowIndex, colIndex) { isGameFinished ->
                                    if (isGameFinished) {
                                        onGameEnd()
                                    }
                                }
                            }
                            .background(
                                when (cell) {
                                    CellState.EMPTY -> Color.LightGray
                                    CellState.HIT -> Color.Red
                                    CellState.MISS -> Color.Black
                                    else -> Color.Transparent
                                }
                            )
                    ) {}
                }
            }
        }

        // Mensagem para o estado de espera
        if (currentPlayerState == "waiting") {
            Text(
                text = "Aguardando a jogada do adversário...",
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 16.dp)
            )
        }
    }
}
