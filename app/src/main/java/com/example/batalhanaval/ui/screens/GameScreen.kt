package com.example.batalhanaval.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.batalhanaval.data.firebase.FirebaseService
import com.example.batalhanaval.ui.components.CellState

@Composable
fun GameScreen(
    gameId: String,
    currentPlayerId: String,
    board: List<List<CellState>>,
    onCellClick: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentTurn by remember { mutableStateOf(1) }
    var isPlayerTurn by remember { mutableStateOf(false) }

    // Verificar o turno atual
    LaunchedEffect(gameId) {
        FirebaseService.getTurn(gameId) { turn ->
            currentTurn = turn ?: 1
            isPlayerTurn = (currentTurn % 2 == 1 && currentPlayerId == "player1Id") ||
                    (currentTurn % 2 == 0 && currentPlayerId == "player2Id")
        }
    }

    if (isPlayerTurn) {
        Column(modifier = modifier) {
            board.forEachIndexed { rowIndex, rowCells ->
                Row {
                    rowCells.forEachIndexed { colIndex, cell ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .border(1.dp, Color.Black)
                                .clickable {
                                    onCellClick(rowIndex, colIndex)
                                    // Atualizar o turno no Firebase
                                    FirebaseService.updateTurn(gameId, currentTurn + 1) { success ->
                                        if (success) {
                                            isPlayerTurn = false
                                        }
                                    }
                                }
                                .background(
                                    when (cell) {
                                        CellState.EMPTY -> Color.LightGray
                                        CellState.SHIP -> Color.DarkGray
                                        CellState.HIT -> Color.Red
                                        CellState.MISS -> Color.Blue
                                    }
                                )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { /* Lógica para reiniciar o jogo */ }) {
                Text("Reiniciar Jogo")
            }
        }
    } else {
        Column(
            modifier = modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Aguardando a jogada do adversário...")
            CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
        }
    }
}
