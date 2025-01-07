package com.example.batalhanaval.ui.components

enum class CellState {
    EMPTY,
    SHIP,
    HIT,
    MISS
}

data class BoardCell(
    val row: Int,
    val col: Int,
    val state: CellState = CellState.EMPTY
)
