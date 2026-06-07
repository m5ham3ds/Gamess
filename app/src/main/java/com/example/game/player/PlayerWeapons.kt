package com.example.game.player

import androidx.compose.ui.graphics.Color

/**
 * Represents a weapon equipped by the main character.
 */
data class Weapon(
    val name: String,
    val damage: Float,
    val range: Float,
    val color: Color,
    val trailColor: Color,
    val impactSize: Float,
    val attackSpeedFactor: Float = 1.0f // Multiplies default attack animations
)

/**
 * Easily define, configure, and expand all weapons the main character can use.
 */
object PlayerWeapons {
    val PHOTON_SABER = Weapon(
        name = "Photon Blade",
        damage = 25f,
        range = 120f,
        color = Color(0xFF00E5FF), // Cyan glow
        trailColor = Color(0x6600E5FF),
        impactSize = 40f,
        attackSpeedFactor = 1.0f
    )

    val TITAN_HAMMER = Weapon(
        name = "Titan Hammer",
        damage = 60f,
        range = 90f,
        color = Color(0xFFFF5252), // Bright Crimson heavy impact
        trailColor = Color(0x66FF5252),
        impactSize = 80f,
        attackSpeedFactor = 0.65f // Slower swing speed
    )

    val VOID_DAGGER = Weapon(
        name = "Void Dagger",
        damage = 15f,
        range = 95f,
        color = Color(0xFFD500F9), // Purple neon stealth slash
        trailColor = Color(0x55D500F9),
        impactSize = 30f,
        attackSpeedFactor = 1.6f // Extremely fast attack speed
    )

    val PLASMA_GLAIVE = Weapon(
        name = "Plasma Glaive",
        damage = 35f,
        range = 160f, // Longer range
        color = Color(0xFFFFEA00), // Electric Yellow
        trailColor = Color(0x66FFEA00),
        impactSize = 50f,
        attackSpeedFactor = 0.9f
    )

    /**
     * Helper list of all available weapons for quick cycle / upgrade selectors.
     */
    val ALL_WEAPONS = listOf(
        PHOTON_SABER,
        TITAN_HAMMER,
        VOID_DAGGER,
        PLASMA_GLAIVE
    )
}
