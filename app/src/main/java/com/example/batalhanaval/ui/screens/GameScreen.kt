package com.example.batalhanaval.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.batalhanaval.ui.components.CellState

@Composable
fun GameScreen(
    board: List<List<CellState>>,
    onCellClick: (row: Int, col: Int) -> Unit,
    modifier: Modifier = Modifier // Adicionando o parâmetro modifier aqui
) {
    Column(modifier = modifier) { // Usando o modifier aqui
        board.forEachIndexed { rowIndex, rowCells ->
            Row {
                rowCells.forEachIndexed { colIndex, cell ->
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .border(1.dp, Color.Black)
                            .clickable { onCellClick(rowIndex, colIndex) }
                            .background(
                                when (cell) {
                                    CellState.EMPTY -> Color.LightGray
                                    CellState.SHIP -> Color.DarkGray
                                    CellState.HIT -> Color.Red
                                    CellState.MISS -> Color.Blue
                                }
                            )
                    ) {}
                }
            }
        }
    }
}
