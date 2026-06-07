package com.example.game

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
