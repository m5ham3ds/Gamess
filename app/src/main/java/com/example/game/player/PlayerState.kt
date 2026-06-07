package com.example.game.player

/**
 * Represents the 2D movement or face direction of the main character.
 */
enum class Direction {
    LEFT,
    RIGHT
}

/**
 * Defines various attack types available to the main character.
 */
enum class AttackType {
    LIGHT,
    HEAVY,
    DASH_ATTACK,
    AERIAL
}

/**
 * Primary state model for the main character.
 * Holds all dynamic properties including positioning, vital stats, levels, and inventory/equipment.
 */
data class PlayerState(
    // 2D Physics and Coordinates
    val x: Float = 400f,
    val y: Float = 600f,
    val vx: Float = 0f,
    val vy: Float = 0f,
    val isGrounded: Boolean = false,
    val radius: Float = PlayerConfig.DEFAULT_RADIUS,

    // Health points (HP) & Vitality
    val hp: Float = PlayerConfig.STARTING_HP,
    val maxHp: Float = PlayerConfig.STARTING_MAX_HP,
    val lives: Int = PlayerConfig.STARTING_LIVES,

    // Energy points (EN) for special moves and abilities
    val energy: Float = PlayerConfig.STARTING_ENERGY,
    val maxEnergy: Float = PlayerConfig.STARTING_MAX_ENERGY,

    // Movement and combat statistics
    val speed: Float = PlayerConfig.MOVEMENT_SPEED,
    val jumpPower: Float = PlayerConfig.JUMP_POWER,
    val direction: Direction = Direction.RIGHT,

    // Dash status indicators
    val isDashing: Boolean = false,
    val dashEndTime: Long = 0L,
    val dashCooldown: Long = 0L,
    val lastDashTime: Long = 0L,

    // Combat states
    val isAttacking: Boolean = false,
    val attackProgress: Float = 0f, // From 0.0f to 1.0f during swipe animations
    val attackType: AttackType = AttackType.LIGHT,
    val invincibleUntil: Long = 0L, // Timestamp up to which player is immune to damage

    // Leveling, Progression and Skill points
    val level: Int = 1,
    val experience: Int = 0,
    val skillPoints: Int = 0,
    val memoryFragmentsCount: Int = 0,

    // Active Weaponry and Skills (Imported from other modular files in the package)
    val activeWeapon: Weapon = PlayerWeapons.PHOTON_SABER,
    val unlockedSkills: Set<SkillType> = setOf(SkillType.DASH)
)
