package com.example.batalhanaval.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.batalhanaval.data.firebase.FirebaseService
import com.example.batalhanaval.ui.components.CellState
import com.example.batalhanaval.ui.components.Ship

@SuppressLint("MutableCollectionMutableState")
@Composable
fun GameSetupScreen(
    gameId: String,
    opponentName: String,
    onSetupComplete: () -> Unit
) {
    val board = remember { mutableStateOf(createMutableEmptyBoard()) }
    val ships = remember { mutableStateListOf(Ship(1), Ship(1), Ship(2), Ship(3), Ship(4)) }
    var selectedShip by remember { mutableStateOf(ships[0]) }
    var isHorizontal by remember { mutableStateOf(true) }

    fun placeShip(row: Int, col: Int): Boolean {
        if (isHorizontal) {
            if (col + selectedShip.size <= 8 && (0 until selectedShip.size).all { board.value[row][col + it] == CellState.EMPTY }) {
                val newPositions = (0 until selectedShip.size).map { Pair(row, col + it) }
                selectedShip.position = newPositions
                for (i in 0 until selectedShip.size) {
                    board.value[row][col + i] = CellState.SHIP
                }
                return true
            }
        } else {
            if (row + selectedShip.size <= 8 && (0 until selectedShip.size).all { board.value[row + it][col] == CellState.EMPTY }) {
                val newPositions = (0 until selectedShip.size).map { Pair(row + it, col) }
                selectedShip.position = newPositions
                for (i in 0 until selectedShip.size) {
                    board.value[row + i][col] = CellState.SHIP
                }
                return true
            }
        }
        return false
    }

    fun saveBoardToFirebase() {
        FirebaseService.saveBoardToFirebase(gameId, board.value) { success ->
            if (success) {
                onSetupComplete()
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text("Place your ships against $opponentName")

        // Mostrar barcos disponíveis para seleção
        Row(modifier = Modifier.padding(bottom = 16.dp)) {
            ships.forEachIndexed { index, ship ->
                Button(onClick = { selectedShip = ships[index] }) {
                    Text("Barco ${index + 1} (${ship.size} quadrados)")
                }
            }
        }

        // Mostrar o tabuleiro
        board.value.forEachIndexed { rowIndex, row ->
            Row {
                row.forEachIndexed { colIndex, cell ->
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .border(1.dp, Color.Black)
                            .clickable {
                                placeShip(rowIndex, colIndex)
                            }
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

        Button(onClick = { saveBoardToFirebase() }) {
            Text("Finish Setup")
        }
    }
}

fun createMutableEmptyBoard(): MutableList<MutableList<CellState>> {
    return MutableList(8) {
        MutableList(8) { CellState.EMPTY }
    }
}