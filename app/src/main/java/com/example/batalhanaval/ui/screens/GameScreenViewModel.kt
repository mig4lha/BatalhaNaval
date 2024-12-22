package com.example.batalhanaval.ui.screens

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.batalhanaval.ui.components.CellState

class GameScreenViewModel : ViewModel() {

    private val _boardPlayer1 = mutableStateOf(createEmptyBoard())
    val boardPlayer1: State<List<List<CellState>>> = _boardPlayer1

    private val _boardPlayer2 = mutableStateOf(createEmptyBoard())
    val boardPlayer2: State<List<List<CellState>>> = _boardPlayer2

    private val _currentPlayer = mutableStateOf(1)
    val currentPlayer: State<Int> = _currentPlayer

    fun attack(row: Int, col: Int) {
        if (_currentPlayer.value == 1) {
            val updatedBoard = _boardPlayer2.value.mapIndexed { r, rowCells ->
                rowCells.mapIndexed { c, cell ->
                    if (r == row && c == col) {
                        if (cell == CellState.SHIP) CellState.HIT else if (cell == CellState.EMPTY) CellState.MISS else cell
                    } else cell
                }
            }
            _boardPlayer2.value = updatedBoard
        } else {
            // Player 2 attacking Player 1
            val updatedBoard = _boardPlayer1.value.mapIndexed { r, rowCells ->
                rowCells.mapIndexed { c, cell ->
                    if (r == row && c == col) {
                        if (cell == CellState.SHIP) CellState.HIT else if (cell == CellState.EMPTY) CellState.MISS else cell
                    } else cell
                }
            }
            _boardPlayer1.value = updatedBoard
        }
        _currentPlayer.value = if (_currentPlayer.value == 1) 2 else 1
    }

    private fun createEmptyBoard(): List<List<CellState>> {
        return List(10) {
            List(10) { CellState.EMPTY }
        }
    }
}
