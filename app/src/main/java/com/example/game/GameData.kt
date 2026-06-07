package com.example.game

enum class GameState { START, PLAYING, GAME_OVER, VICTORY }

data class Platform(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float
)
