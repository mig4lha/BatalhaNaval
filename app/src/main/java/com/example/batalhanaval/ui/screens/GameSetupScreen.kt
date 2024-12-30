package com.example.batalhanaval.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
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
    val isHorizontal = viewModel.isHorizontal.collectAsState().value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Título
        Text(
            text = "Place Your Ships",
            style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 16.dp)
        )

        // Tabuleiro em primeiro
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f) // Garante que o tabuleiro tenha prioridade de espaço
                .padding(bottom = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                board.forEachIndexed { rowIndex, row ->
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
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
            }
        }

        // Barcos disponíveis
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            // Dividir barcos em duas linhas
            val firstRowShips = ships.take(ships.size / 2)
            val secondRowShips = ships.drop(ships.size / 2)

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            ) {
                firstRowShips.forEach { ship ->
                    Button(
                        onClick = { viewModel.selectShip(ship) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Ship (${ship.size} cells)")
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                secondRowShips.forEach { ship ->
                    Button(
                        onClick = { viewModel.selectShip(ship) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Ship (${ship.size} cells)")
                    }
                }
            }
        }

        // Botões de controle
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                Button(onClick = { viewModel.toggleOrientation() }) {
                    Text(if (isHorizontal) "Horizontal" else "Vertical")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(onClick = {
                    viewModel.saveBoardAndSwitchPlayer(gameId, currentPlayer) {
                        onWaitForOpponent()
                    }
                }) {
                    Text("Finish Setup")
                }
            }

            Button(
                onClick = onBackToMenu,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Return to Menu")
            }
        }
    }
}
