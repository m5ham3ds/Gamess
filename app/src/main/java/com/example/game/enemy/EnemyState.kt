package com.example.game.enemy

import com.example.game.player.Direction

/**
 * AI states for the different enemy behaviors.
 */
enum class EnemyAIState {
    IDLE,
    PATROLLING,
    AGGRO,
    RETURNING,
    CHARGING,
    DEFEATED
}

/**
 * Dynamic properties of each individual enemy instance.
 */
data class Enemy(
    val id: String,
    val x: Float,
    val y: Float,
    val type: EnemyType,
    val hp: Float,
    val maxHp: Float,
    val speed: Float,
    val radius: Float = 22f,
    val isGrounded: Boolean = false,
    val direction: Direction = Direction.LEFT,
    val spawnX: Float = x,
    val spawnY: Float = y,
    val aiState: EnemyAIState = EnemyAIState.IDLE,
    val lastShotTime: Long = 0L,
    
    // Aesthetic variation for animations
    val animationOffset: Float = (Math.random() * 100).toFloat(),
    val isStunned: Boolean = false,
    val stunUntil: Long = 0L
)
