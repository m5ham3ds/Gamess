package com.example.game.player

/**
 * Easily configure the main character's core specs and behavior constants here.
 * Tweaking these values will instantly re-balance the entire gameplay experience.
 */
object PlayerConfig {
    // --- Vitals & Attributes ---
    const val STARTING_HP = 100f
    const val STARTING_MAX_HP = 100f
    const val STARTING_ENERGY = 100f
    const val STARTING_MAX_ENERGY = 100f
    const val STARTING_LIVES = 3

    // --- Physics & Mechanics ---
    const val DEFAULT_RADIUS = 28f
    const val MOVEMENT_SPEED = 700f
    const val JUMP_POWER = -1100f
    const val GRAVITY = 2500f // Controls falling speed velocity

    // --- Special Skills Cooldowns & Speeds ---
    const val DASH_SPEED_MULTIPLIER = 2.5f
    const val DASH_DURATION_MS = 200L
    const val DASH_COOLDOWN_MS = 1000L
    
    // --- Attack & Damage Configs ---
    const val ATTACK_DURATION_MS = 300L
    const val INVINCIBILITY_MS = 1500L
    
    // --- Energy costs ---
    const val SOUL_PULSE_COST = 40f
    const val DEFLECTOR_ENERGY_DRAIN = 15f // Drain rate per second of passive activation
    
    // --- Level Up thresholds ---
    /**
     * Determines how much experience is needed to reach the next level.
     */
    fun xpNeededForLevel(currentLevel: Int): Int {
        return currentLevel * 150 + 100
    }
}
