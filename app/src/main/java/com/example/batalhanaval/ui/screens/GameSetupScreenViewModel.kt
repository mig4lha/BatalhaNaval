package com.example.batalhanaval.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.batalhanaval.data.firebase.FirebaseService
import com.example.batalhanaval.ui.components.CellState
import com.example.batalhanaval.ui.components.Ship
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GameSetupScreenViewModel : ViewModel() {

    private val _board = MutableStateFlow(List(8) { List(8) { CellState.EMPTY } })
    val board: StateFlow<List<List<CellState>>> = _board

    private val _ships = MutableStateFlow(
        listOf(
            Ship(1), Ship(1), Ship(2), Ship(2), Ship(3), Ship(4)
        )
    )
    val ships: StateFlow<List<Ship>> = _ships

    private val _selectedShip = MutableStateFlow<Ship?>(null)
    val selectedShip: StateFlow<Ship?> = _selectedShip

    private val _isHorizontal = MutableStateFlow(true)
    val isHorizontal: StateFlow<Boolean> = _isHorizontal

    fun selectShip(ship: Ship) {
        _selectedShip.value = ship
    }

    fun toggleOrientation() {
        _isHorizontal.value = !_isHorizontal.value
    }

    fun placeShip(row: Int, col: Int) {
        val selectedShip = _selectedShip.value ?: return
        val newBoard = _board.value.toMutableList()

        for (i in 0 until selectedShip.size) {
            val r = if (_isHorizontal.value) row else row + i
            val c = if (_isHorizontal.value) col + i else col

            if (r !in 0..7 || c !in 0..7 || newBoard[r][c] != CellState.EMPTY) return

            newBoard[r] = newBoard[r].toMutableList().apply { this[c] = CellState.SHIP }
        }

        _board.value = newBoard
        _ships.value = _ships.value.toMutableList().apply { remove(selectedShip) }
        _selectedShip.value = null
    }

    fun saveBoardAndSwitchPlayer(
        gameId: String,
        currentPlayer: String,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            // Salvar o tabuleiro do jogador atual no Firebase
            FirebaseService.saveBoardToFirebase(gameId, currentPlayer, _board.value) { success ->
                if (success) {
                    // Atualizar o estado dos jogadores após salvar o tabuleiro
                    FirebaseService.updatePlayerStatusAfterPlacement(
                        gameId = gameId,
                        currentPlayer = currentPlayer
                    ) {
                        onComplete()
                    }
                } else {
                    Log.e("GameSetupScreenViewModel", "Erro ao salvar o tabuleiro do jogador $currentPlayer.")
                }
            }
        }
    }
}
