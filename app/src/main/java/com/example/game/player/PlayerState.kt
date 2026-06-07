package com.example.game.player

enum class Direction {
    LEFT, RIGHT
}

data class PlayerState(
    val x: Float = 150f,
    val y: Float = 500f,
    val vx: Float = 0f,
    val vy: Float = 0f,
    val hp: Float = 100f,
    val maxHp: Float = 100f,
    val energy: Float = 50f,
    val maxEnergy: Float = 100f,
    val forgetfulness: Float = 0f,
    val level: Int = 1,
    val currency: Int = 0,
    val score: Int = 0,
    val memoryFragments: Int = 0,
    val radius: Float = 20f,
    val direction: Direction = Direction.RIGHT,
    val soulShieldActive: Boolean = false,
    val isGrounded: Boolean = false
)
