package com.example.game.enemy

import com.example.game.player.Direction

enum class EnemyType {
    SHADOW_STALKER,
    ABYSSAL_ORB,
    VOID_DEVOURER,
    NEON_SPIDER,
    CHRONO_SENTINEL
}

data class Enemy(
    val id: String,
    val x: Float,
    val y: Float,
    val vx: Float = 0f,
    val vy: Float = 0f,
    val hp: Float,
    val maxHp: Float,
    val radius: Float = 18f,
    val type: EnemyType,
    val direction: Direction = Direction.LEFT,
    val lastShotTime: Long = 0L,
    val startX: Float, // Original position to patrol relative to
    val startY: Float, // Original position's vertical height
    val patrolDistance: Float = 120f
)
