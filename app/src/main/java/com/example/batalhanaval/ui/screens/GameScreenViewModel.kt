package com.example.batalhanaval.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.batalhanaval.data.firebase.FirebaseService
import com.example.batalhanaval.ui.components.CellState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GameScreenViewModel : ViewModel() {

    private val _playerBoard = MutableStateFlow(List(8) { List(8) { CellState.EMPTY } })
    val playerBoard: StateFlow<List<List<CellState>>> = _playerBoard

    private val _trackingBoard = MutableStateFlow(List(8) { List(8) { CellState.EMPTY } })
    val trackingBoard: StateFlow<List<List<CellState>>> = _trackingBoard

    private val _currentPlayerState = MutableStateFlow("waiting")
    val currentPlayerState: StateFlow<String> = _currentPlayerState

    private val _opponentName = MutableStateFlow("")
    val opponentName: StateFlow<String> = _opponentName

    private var opponentBoard: List<List<CellState>> = emptyList()


    fun loadGameBoards(gameId: String, currentPlayer: String) {
        viewModelScope.launch {
            FirebaseService.getGameBoards(gameId, currentPlayer) { boards ->
                if (boards.isNotEmpty()) {
                    _playerBoard.value = boards["currentPlayerBoard"] ?: _playerBoard.value
                    _trackingBoard.value = boards["trackingBoard"] ?: _trackingBoard.value
                    opponentBoard = boards["opponentBoard"] ?: emptyList()

                    Log.d("GameScreenViewModel", "Tabuleiros carregados com sucesso.")
                } else {
                    Log.e("GameScreenViewModel", "Erro ao carregar tabuleiros.")
                }
            }
        }
    }

    fun registerShot(
        gameId: String,
        currentPlayer: String,
        row: Int,
        col: Int,
        onShotComplete: (Boolean) -> Unit
    ) {
        FirebaseService.getGameBoards(gameId, currentPlayer) { boards ->
            if (boards.isEmpty()) {
                Log.e("RegisterShot", "Erro: Boards não carregados ou nulos.")
                onShotComplete(false)
                return@getGameBoards
            }

            val trackingBoard = boards["trackingBoard"]?.map { it.toMutableList() }?.toMutableList() ?: run {
                Log.e("RegisterShot", "Erro: TrackingBoard não encontrado.")
                onShotComplete(false)
                return@getGameBoards
            }

            val opponentBoard = boards["opponentBoard"]?.map { it.toMutableList() }?.toMutableList() ?: run {
                Log.e("RegisterShot", "Erro: OpponentBoard não encontrado.")
                onShotComplete(false)
                return@getGameBoards
            }

            try {
                val cellState = opponentBoard[row][col] // Obtém o estado diretamente

                // Verifica se a célula é válida
                when (cellState) {
                    CellState.SHIP -> {
                        Log.d("RegisterShot", "HIT na célula [$row][$col].")
                        trackingBoard[row][col] = CellState.HIT
                        opponentBoard[row][col] = CellState.HIT

                        // Atualiza os campos modificados
                        val trackingBoardChanges = mapOf("$row.$col" to CellState.HIT.name)
                        val opponentBoardChanges = mapOf("$row.$col" to CellState.HIT.name)

                        FirebaseService.updateBoards(
                            gameId,
                            currentPlayer,
                            trackingBoardChanges,
                            opponentBoardChanges
                        ) { success ->
                            if (success) {
                                Log.d("RegisterShot", "HIT atualizado com sucesso.")
                                onShotComplete(true) // Jogador continua
                            } else {
                                Log.e("RegisterShot", "Erro ao atualizar boards após HIT.")
                                onShotComplete(false)
                            }
                        }
                    }
                    CellState.EMPTY -> {
                        Log.d("RegisterShot", "MISS na célula [$row][$col].")
                        trackingBoard[row][col] = CellState.MISS
                        opponentBoard[row][col] = CellState.MISS

                        // Atualiza os campos modificados
                        val trackingBoardChanges = mapOf("$row.$col" to CellState.MISS.name)
                        val opponentBoardChanges = mapOf("$row.$col" to CellState.MISS.name)

                        FirebaseService.updateBoards(
                            gameId,
                            currentPlayer,
                            trackingBoardChanges,
                            opponentBoardChanges
                        ) { success ->
                            if (success) {
                                Log.d("RegisterShot", "MISS atualizado com sucesso.")
                                FirebaseService.updateTurn(
                                    gameId,
                                    getNextTurn(currentPlayer, boards["player1"] as String, boards["player2"] as String)
                                ) { turnUpdated ->
                                    if (turnUpdated) {
                                        Log.d("RegisterShot", "Turno atualizado após MISS.")
                                        onShotComplete(false)
                                    } else {
                                        Log.e("RegisterShot", "Erro ao atualizar turno.")
                                        onShotComplete(false)
                                    }
                                }
                            } else {
                                Log.e("RegisterShot", "Erro ao atualizar boards após MISS.")
                                onShotComplete(false)
                            }
                        }
                    }
                    else -> {
                        Log.e("RegisterShot", "Estado inválido na célula [$row][$col]: $cellState.")
                        onShotComplete(false)
                    }
                }
            } catch (e: IndexOutOfBoundsException) {
                Log.e("RegisterShot", "Erro: Índice fora dos limites para row=$row, col=$col.", e)
                onShotComplete(false)
            }
        }
    }

    private fun getNextTurn(currentPlayer: String, player1Id: String, player2Id: String): Int {
        return if (currentPlayer == player1Id) 2 else 1
    }

    private fun convertToMutableCellStateBoard(data: Any?): MutableList<MutableList<CellState>>? {
        return (data as? List<*>)?.mapNotNull { row ->
            (row as? List<*>)?.mapNotNull { cell ->
                try {
                    CellState.valueOf(cell as String)
                } catch (e: Exception) {
                    Log.e("ConvertToMutableCellStateBoard", "Erro ao converter célula: $cell")
                    null
                }
            }?.toMutableList()
        }?.toMutableList()
    }

    /*private fun updateBoardsAndContinue(
        gameId: String,
        currentPlayer: String,
        trackingBoard: List<List<CellState>>,
        opponentBoard: List<List<CellState>>,
        onShotComplete: (Boolean) -> Unit
    ) {
        FirebaseService.updateBoards(gameId, currentPlayer, trackingBoard, opponentBoard) { success ->
            if (success) {
                Log.d("UpdateBoards", "Boards atualizados com sucesso.")
                onShotComplete(true)
            } else {
                Log.e("UpdateBoards", "Erro ao atualizar boards.")
                onShotComplete(false)
            }
        }
    }*/

    fun initialize(gameId: String, currentPlayer: String) {
        Log.d("GameScreenViewModel", "Inicializando com gameId: $gameId e currentPlayer: $currentPlayer")
        loadGameBoards(gameId, currentPlayer)
        loadOpponentName(gameId, currentPlayer)
        updatePlayerState(gameId, currentPlayer) {
            Log.d("GameScreenViewModel", "updatePlayerState concluído")
        }
    }

    fun loadOpponentName(gameId: String, currentPlayer: String) {
        viewModelScope.launch {
            FirebaseService.getOpponentName(gameId, currentPlayer) { opponent ->
                if (opponent != null) {
                    _opponentName.value = opponent
                    Log.d("GameScreenViewModel", "Nome do oponente carregado: $opponent")
                } else {
                    Log.e("GameScreenViewModel", "Erro ao carregar o nome do oponente.")
                }
            }
        }
    }

    fun updatePlayerState(gameId: String, playerId: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            FirebaseService.getPlayerState(gameId, playerId) { state ->
                if (state != null) {
                    _currentPlayerState.value = state
                    Log.d("GameScreenViewModel", "Estado do jogador atualizado: $state")
                    onComplete()
                } else {
                    Log.e("GameScreenViewModel", "Erro ao atualizar estado do jogador.")
                }
            }
        }
    }
}
