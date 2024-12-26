package com.example.batalhanaval.ui.components

// Enum que define a direção do barco (horizontal ou vertical)
enum class ShipOrientation {
    HORIZONTAL,
    VERTICAL
}

// Classe para representar um barco
data class Ship(
    val size: Int,  // Tamanho do barco (número de células que ele ocupa)
    var position: List<Pair<Int, Int>> = listOf(),  // Lista de coordenadas das células ocupadas pelo barco
    var orientation: ShipOrientation = ShipOrientation.HORIZONTAL
)
