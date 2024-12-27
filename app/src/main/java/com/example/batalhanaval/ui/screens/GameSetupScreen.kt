package com.example.batalhanaval.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.batalhanaval.ui.components.CellState
import com.example.batalhanaval.ui.viewmodels.GameSetupScreenViewModel

@Composable
fun GameSetupScreen(
    gameId: String,
    currentPlayer: String,
    onWaitForOpponent: () -> Unit,
    onBackToMenu: () -> Unit,
    viewModel: GameSetupScreenViewModel = viewModel()
) {
    val board = viewModel.board.collectAsState().value
    val ships = viewModel.ships.collectAsState().value
    val selectedShip = viewModel.selectedShip.collectAsState().value
    val isHorizontal = viewModel.isHorizontal.collectAsState().value

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Coloque seus barcos")

        // Mostrar os barcos disponíveis
        Row(modifier = Modifier.padding(bottom = 16.dp)) {
            ships.forEach { ship ->
                Button(onClick = { viewModel.selectShip(ship) }) {
                    Text("Barco (${ship.size} células)")
                }
            }
        }

        // Mostrar tabuleiro
        board.forEachIndexed { rowIndex, row ->
            Row {
                row.forEachIndexed { colIndex, cell ->
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .border(1.dp, Color.Black)
                            .clickable { viewModel.placeShip(rowIndex, colIndex) }
                            .background(
                                when (cell) {
                                    CellState.EMPTY -> Color.LightGray
                                    CellState.SHIP -> Color.DarkGray
                                    else -> Color.Transparent
                                }
                            )
                    ) {}
                }
            }
        }

        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Button(onClick = { viewModel.toggleOrientation() }) {
                Text(if (isHorizontal) "Horizontal" else "Vertical")
            }
            Button(onClick = {
                viewModel.saveBoardAndSwitchPlayer(gameId, currentPlayer) {
                    onWaitForOpponent()
                }
            }) {
                Text("Finalizar Configuração")
            }
        }

        Button(onClick = onBackToMenu, modifier = Modifier.padding(top = 16.dp)) {
            Text("Voltar ao Menu Principal")
        }
    }
}
