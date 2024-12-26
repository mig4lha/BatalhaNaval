package com.example.batalhanaval.ui.screens

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.batalhanaval.ui.components.CellState
import com.example.batalhanaval.data.firebase.FirebaseService

class GameScreenViewModel : ViewModel() {

    private val _boardPlayer1 = mutableStateOf(createEmptyBoard())
    val boardPlayer1: State<List<List<CellState>>> = _boardPlayer1

    private val _boardPlayer2 = mutableStateOf(createEmptyBoard())
    val boardPlayer2: State<List<List<CellState>>> = _boardPlayer2

    private val _selectedShipSize = mutableStateOf(1)
    val selectedShipSize: State<Int> = _selectedShipSize

    private val _currentPlayer = mutableStateOf(1)
    val currentPlayer: State<Int> = _currentPlayer

    private val _gameOver = mutableStateOf(false)
    val gameOver: State<Boolean> = _gameOver

    fun selectShip(size: Int) {
        _selectedShipSize.value = size
    }

    fun placeShip(row: Int, col: Int, isHorizontal: Boolean): Boolean {
        val currentBoard = if (_currentPlayer.value == 1) _boardPlayer1.value else _boardPlayer2.value
        val mutableBoard = currentBoard.map { it.toMutableList() }.toMutableList()

        if (isHorizontal) {
            if (col + _selectedShipSize.value > 8 || (0 until _selectedShipSize.value).any { mutableBoard[row][col + it] != CellState.EMPTY }) {
                return false
            }
            for (i in 0 until _selectedShipSize.value) {
                mutableBoard[row][col + i] = CellState.SHIP
            }
        } else {
            if (row + _selectedShipSize.value > 8 || (0 until _selectedShipSize.value).any { mutableBoard[row + it][col] != CellState.EMPTY }) {
                return false
            }
            for (i in 0 until _selectedShipSize.value) {
                mutableBoard[row + i][col] = CellState.SHIP
            }
        }

        if (_currentPlayer.value == 1) {
            _boardPlayer1.value = mutableBoard
        } else {
            _boardPlayer2.value = mutableBoard
        }
        return true
    }

    fun attack(row: Int, col: Int) {
        if (_gameOver.value) return

        val updatedBoard = if (_currentPlayer.value == 1) {
            _boardPlayer2.value.map { it.toMutableList() }.mapIndexed { r, rowCells ->
                rowCells.mapIndexed { c, cell ->
                    if (r == row && c == col) if (cell == CellState.SHIP) CellState.HIT else CellState.MISS else cell
                }
            }
        } else {
            _boardPlayer1.value.map { it.toMutableList() }.mapIndexed { r, rowCells ->
                rowCells.mapIndexed { c, cell ->
                    if (r == row && c == col) if (cell == CellState.SHIP) CellState.HIT else CellState.MISS else cell
                }
            }
        }

        if (_currentPlayer.value == 1) _boardPlayer2.value = updatedBoard else _boardPlayer1.value =
            updatedBoard

        // Enviar o ataque para o Firebase
        saveBoardStateToFirebase()

        checkForWinner()

        if (!_gameOver.value) switchPlayer()
    }

    private fun switchPlayer() {
        _currentPlayer.value = if (_currentPlayer.value == 1) 2 else 1
    }

    private fun checkForWinner() {
        if (_boardPlayer1.value.flatten().none { it == CellState.SHIP }) {
            _gameOver.value = true
        } else if (_boardPlayer2.value.flatten().none { it == CellState.SHIP }) {
            _gameOver.value = true
        }
    }

    private fun createEmptyBoard(): List<List<CellState>> {
        return List(8) {
            List(8) { CellState.EMPTY }
        }
    }

    private fun boardToFirebaseFormat(board: List<List<CellState>>): List<Map<String, Any>> {
        return board.flatMapIndexed { rowIndex, row ->
            row.mapIndexed { colIndex, cell ->
                mapOf(
                    "row" to rowIndex,
                    "col" to colIndex,
                    "state" to cell.name
                )
            }
        }
    }

    private fun saveBoardStateToFirebase() {
        FirebaseService.getGameId { gameId ->
            if (gameId != null) {
                val boardPlayer1Formatted = boardToFirebaseFormat(_boardPlayer1.value)
                val boardPlayer2Formatted = boardToFirebaseFormat(_boardPlayer2.value)

                FirebaseService.saveFormattedBoards(gameId, boardPlayer1Formatted, boardPlayer2Formatted) {
                    Log.d("Game", "Tabuleiros salvos com sucesso")
                }
            } else {
                Log.e("Game", "Erro ao recuperar o ID do jogo")
            }
        }
    }
}
