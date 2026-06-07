package com.example.game.enemy

import androidx.compose.ui.graphics.Color

/**
 * Aesthetic parameters and physics thresholds for each type of enemy.
 */
data class EnemyBlueprint(
    val type: EnemyType,
    val defaultMaxHp: Float,
    val defaultSpeed: Float,
    val radius: Float,
    val primaryColor: Color,
    val glowColor: Color,
    val aggroRange: Float = 450f,
    val maxTetherRange: Float = 650f,
    val shootCooldownMs: Long = 2000L,
    val projectileDamage: Float = 15f
)

object EnemyConfig {
    
    val BLUEPRINTS = mapOf(
        EnemyType.SHADOW_STALKER to EnemyBlueprint(
            type = EnemyType.SHADOW_STALKER,
            defaultMaxHp = 30f,
            defaultSpeed = 120f,
            radius = 22f,
            primaryColor = Color(0xFF2C2C2C), // Dark basalt surface
            glowColor = Color(0xFFFF1744)       // Red laser eye
        ),
        EnemyType.ABYSSAL_ORB to EnemyBlueprint(
            type = EnemyType.ABYSSAL_ORB,
            defaultMaxHp = 25f,
            defaultSpeed = 60f,
            radius = 20f,
            primaryColor = Color(0xFF1E293B), // Space indigo
            glowColor = Color(0xFF7C4DFF)       // Violet plasma core
        ),
        EnemyType.VOID_DEVOURER to EnemyBlueprint(
            type = EnemyType.VOID_DEVOURER,
            defaultMaxHp = 80f,
            defaultSpeed = 80f,
            radius = 32f,
            primaryColor = Color(0xFF0F172A), // Void metal
            glowColor = Color(0xFFD500F9)       // Purple nebula shell
        ),
        EnemyType.NEON_SPIDER to EnemyBlueprint(
            type = EnemyType.NEON_SPIDER,
            defaultMaxHp = 40f,
            defaultSpeed = 150f,
            radius = 18f,
            primaryColor = Color(0xFF3F4E4F), // Cyber bronze
            glowColor = Color(0xFFFFEA00)       // Neon yellow warning spark
        ),
        EnemyType.CHRONO_SENTINEL to EnemyBlueprint(
            type = EnemyType.CHRONO_SENTINEL,
            defaultMaxHp = 35f,
            defaultSpeed = 90f,
            radius = 21f,
            primaryColor = Color(0xFF1A1A24), // Chromium grey
            glowColor = Color(0xFF00E5FF)       // Emerald cyan wave
        )
    )

    /**
     * Retrieve specialized custom constants for a specific category of enemy.
     */
    fun getBlueprint(type: EnemyType): EnemyBlueprint {
        return BLUEPRINTS[type] ?: BLUEPRINTS[EnemyType.SHADOW_STALKER]!!
    }
}
