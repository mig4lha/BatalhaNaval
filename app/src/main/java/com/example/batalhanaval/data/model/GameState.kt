package com.example.batalhanaval.data.model

/**
 * Enum que descreve o estado de uma célula do tabuleiro.
 */
enum class CellState {
    EMPTY,    // Sem navio
    SHIP,     // Contém navio
    HIT,      // Navio atingido
    MISS      // Tiro na água
}

/**
 * Representa o estado de uma partida de Batalha Naval.
 *
 * @param boardPlayer1 Tabuleiro do jogador 1.
 * @param boardPlayer2 Tabuleiro do jogador 2.
 * @param currentPlayer Indica qual jogador está realizando a jogada (1 ou 2).
 * @param gameOver Indica se a partida já terminou.
 */
data class GameState(
    val boardPlayer1: List<List<CellState>> = emptyBoard(),
    val boardPlayer2: List<List<CellState>> = emptyBoard(),
    val currentPlayer: Int = 1,
    val gameOver: Boolean = false
) {
    companion object {
        /**
         * Cria um tabuleiro 10x10 vazio (por exemplo).
         * Ajuste para o tamanho desejado.
         */
        fun emptyBoard(): List<List<CellState>> {
            return List(10) {
                List(10) { CellState.EMPTY }
            }
        }
    }

    // A função 'attack' agora faz parte da classe GameState
    fun attack(row: Int, col: Int): GameState {
        val newBoard = if (currentPlayer == 1) boardPlayer2 else boardPlayer1
        val newCellState = if (newBoard[row][col] == CellState.SHIP) CellState.HIT else CellState.MISS

        // Atualizar o estado da célula do tabuleiro
        val updatedBoard = newBoard.mapIndexed { rIndex, rowCells ->
            rowCells.mapIndexed { cIndex, cell ->
                if (rIndex == row && cIndex == col) newCellState else cell
            }
        }

        // Alterar o turno
        val newCurrentPlayer = if (currentPlayer == 1) 2 else 1

        // Atualizar o estado do jogo e retornar uma nova instância de GameState
        return this.copy(
            boardPlayer1 = if (currentPlayer == 1) updatedBoard else boardPlayer1,
            boardPlayer2 = if (currentPlayer == 2) updatedBoard else boardPlayer2,
            currentPlayer = newCurrentPlayer
        )
    }
}
