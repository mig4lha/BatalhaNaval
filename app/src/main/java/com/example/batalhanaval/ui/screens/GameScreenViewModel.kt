package com.example.batalhanaval.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.batalhanaval.data.firebase.FirebaseService
import com.example.batalhanaval.ui.components.CellState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GameScreenViewModel : ViewModel() {

    private val _currentPlayerBoard = MutableStateFlow(List(8) { List(8) { CellState.EMPTY } })
    val currentPlayerBoard: StateFlow<List<List<CellState>>> = _currentPlayerBoard

    private val _opponentBoard = MutableStateFlow(List(8) { List(8) { CellState.EMPTY } })
    val opponentBoard: StateFlow<List<List<CellState>>> = _opponentBoard

    private val _currentPlayerState = MutableStateFlow("waiting")
    val currentPlayerState: StateFlow<String> = _currentPlayerState

    private val _opponentName = MutableStateFlow<String?>(null)
    val opponentName: StateFlow<String?> = _opponentName

    fun loadGameBoards(gameId: String, currentPlayer: String) {
        viewModelScope.launch {
            FirebaseService.getGameBoards(gameId, currentPlayer) { boards ->
                if (boards != null) {
                    _currentPlayerBoard.value = boards["currentPlayerBoard"] ?: _currentPlayerBoard.value
                    _opponentBoard.value = boards["opponentBoard"] ?: _opponentBoard.value
                }
            }

            // Obter o nome do oponente
            FirebaseService.getOpponentName(gameId, currentPlayer) { opponent ->
                _opponentName.value = opponent
            }
        }
    }

    fun registerShot(gameId: String, currentPlayer: String, row: Int, col: Int, onShotComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            FirebaseService.registerShot(gameId, currentPlayer, row, col) { hit ->
                if (hit != null) {
                    // Atualizar os tabuleiros após o tiro
                    loadGameBoards(gameId, currentPlayer)
                    onShotComplete(hit)
                }
            }
        }
    }

    fun updatePlayerState(gameId: String, currentPlayer: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            FirebaseService.getPlayerState(gameId, currentPlayer) { state ->
                if (state != null) {
                    _currentPlayerState.value = state
                    onComplete()
                }
            }
        }
    }
}
