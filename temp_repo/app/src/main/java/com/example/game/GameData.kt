package com.example.game

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

enum class GameState {
    MENU,
    PLAYING,
    PAUSED,
    ORACLE_CONVERSATION,
    GAME_OVER,
    VICTORY,
    CHRONICLES,
    SHOP,
    MEMORY_TREE
}

enum class Direction {
    LEFT, RIGHT, UP, DOWN
}

enum class EnemyType {
    SHADOW_STALKER, // Walks back and forth
    ABYSSAL_ORB,    // Floats and shoots bullets
    VOID_DEVOURER   // High HP elite miniboss
}

enum class MemoryLeafType {
    COMBAT, MOVEMENT, DEFENSE, MYSTERY
}

data class MemoryLeaf(
    val id: String,
    val title: String,
    val description: String,
    val type: MemoryLeafType,
    val mfCost: Int,
    val fmGain: Int,
    val isRecovered: Boolean = false,
    val requiredLeafId: String? = null
)

data class PlayerState(
    val x: Float = 400f,
    val y: Float = 600f,
    val vy: Float = 0f,
    val isGrounded: Boolean = false,
    val radius: Float = 24f,
    val hp: Float = 100f,
    val maxHp: Float = 100f,
    val energy: Float = 100f,
    val maxEnergy: Float = 100f,
    val speed: Float = 750f,
    val jumpPower: Float = -1150f,
    val attackDamage: Float = 20f,
    val currency: Int = 0,
    val direction: Direction = Direction.RIGHT,
    val score: Int = 0,
    val lives: Int = 3,
    val soulShieldActive: Boolean = false,
    val shieldExpiryTime: Long = 0L,
    val dashCooldown: Long = 0L,
    val lastDashTime: Long = 0L,
    val isDashing: Boolean = false,
    val dashEndTime: Long = 0L,
    val level: Int = 1,
    val memoryFragments: Int = 0,
    val forgetfulness: Int = 0,
    val unlockedMemories: Set<String> = emptySet()
)

enum class GameRegion(val title: String, val bgHex: Long) {
    ASHEN_SPRAWL("Ashen Sprawl", 0xFF2A2826),
    VEILED_ARCHIVES("Veiled Archives", 0xFF1C1A2E),
    HOLLOWED_ARCHIPELAGO("Hollowed Archipelago", 0xFF1D2631),
    GLASSFJORD_CLIFFS("Glassfjord Cliffs", 0xFF213645)
}

data class Platform(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float
)

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
    val state: EnemyState = EnemyState.IDLE,
    val lastShotTime: Long = 0L
)

enum class EnemyState {
    IDLE, AGGRO, RETURNING
}

data class Projectile(
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val isPlayerOwned: Boolean,
    val radius: Float = 8f,
    val damage: Float = 15f
)

data class CoreShard(
    val x: Float,
    val y: Float,
    val radius: Float = 12f,
    val active: Boolean = true
)

data class OracleRelic(
    val x: Float,
    val y: Float,
    val radius: Float = 40f,
    val isUsed: Boolean = false
)

data class Particle(
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val color: Color,
    val size: Float,
    val alpha: Float,
    val decay: Float,
    val lifetime: Int
)

data class GameLevel(
    val num: Int,
    val nameStringRes: String,
    val shadowThemeColor: Color,
    val targetShards: Int,
    val enemySpawnCount: Int
)
