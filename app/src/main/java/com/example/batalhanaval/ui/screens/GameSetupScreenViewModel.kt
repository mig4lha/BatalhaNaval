package com.example.batalhanaval.ui.screens

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import com.example.batalhanaval.ui.components.CellState
import com.example.batalhanaval.ui.components.Ship
import com.example.batalhanaval.ui.components.ShipOrientation

class GameSetupScreenViewModel : ViewModel() {

    private val _board = mutableStateOf(createEmptyBoard())
    val board: State<List<List<CellState>>> = _board

    private val _ships = mutableStateOf(mutableListOf<Ship>())
    val ships: State<MutableList<Ship>> = _ships

    private val _selectedShip = mutableStateOf(_ships.value[0])
    val selectedShip: State<Ship> = _selectedShip

    private val _isHorizontal = mutableStateOf(true) // Para alternar entre horizontal e vertical
    val isHorizontal: State<Boolean> = _isHorizontal

    // Função para alternar a orientação do barco (horizontal ou vertical)
    fun toggleOrientation() {
        _isHorizontal.value = !_isHorizontal.value
    }

    // Função para tentar colocar o barco no tabuleiro
    fun placeShip(row: Int, col: Int): Boolean {
        val ship = _selectedShip.value
        if (_isHorizontal.value) {
            // Verifica se o barco cabe horizontalmente
            if (col + ship.size <= 10 && ship.position.all { _board.value[row][it.second] == CellState.EMPTY }) {
                // Atualiza a posição do barco no board
                val updatedPosition = List(ship.size) { Pair(row, col + it) }
                ship.position = updatedPosition // Atualiza a posição do barco
                val updatedBoard = _board.value.mapIndexed { rIndex, rowCells ->
                    rowCells.mapIndexed { cIndex, cell ->
                        if (updatedPosition.any { it.first == rIndex && it.second == cIndex }) {
                            CellState.SHIP
                        } else {
                            cell
                        }
                    }
                }
                _board.value = updatedBoard // Atualiza o board com o novo estado
                return true
            }
        } else {
            // Verifica se o barco cabe verticalmente
            if (row + ship.size <= 10 && ship.position.all { _board.value[it.first][col] == CellState.EMPTY }) {
                // Atualiza a posição do barco no board
                val updatedPosition = List(ship.size) { Pair(row + it, col) }
                ship.position = updatedPosition // Atualiza a posição do barco
                val updatedBoard = _board.value.mapIndexed { rIndex, rowCells ->
                    rowCells.mapIndexed { cIndex, cell ->
                        if (updatedPosition.any { it.first == rIndex && it.second == cIndex }) {
                            CellState.SHIP
                        } else {
                            cell
                        }
                    }
                }
                _board.value = updatedBoard // Atualiza o board com o novo estado
                return true
            }
        }
        return false
    }

    // Função para finalizar a configuração e começar o jogo
    fun finishSetup() {
        // Finalizar configuração, pode realizar navegação ou outros processos
    }

    // Função para criar o tabuleiro vazio
    private fun createEmptyBoard(): List<List<CellState>> {
        return List(8) {
            List(8) { CellState.EMPTY }
        }
    }

    // Função para criar os barcos iniciais
    private fun createDefaultShips(): MutableList<Ship> {
        return mutableListOf(
            Ship(size = 1),
            Ship(size = 1),
            Ship(size = 1),
            Ship(size = 2),
            Ship(size = 2),
            Ship(size = 3),
            Ship(size = 4)
        )
    }
}
