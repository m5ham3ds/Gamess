package com.example.game.player

/**
 * Enumeration of available abilities and passive boosts for the main character.
 */
enum class SkillType {
    DASH,
    DOUBLE_JUMP,
    KINETIC_SHIELD,
    SOUL_PULSE,
    REGENERATE_PASSIVE,
    CRITICAL_MIND
}

/**
 * Holds skill descriptions, levels, energy cost, and leveling-up prerequisites.
 */
data class PlayerSkill(
    val type: SkillType,
    val name: String,
    val description: String,
    val energyCost: Float,
    val skillPointPrerequisite: Int = 1,
    val isPassive: Boolean = false
)

/**
 * Easy configuration matrix for the player's skills, abilities, development paths, and milestones.
 */
object PlayerSkills {
    val SKILLS_CATALOG = mapOf(
        SkillType.DASH to PlayerSkill(
            type = SkillType.DASH,
            name = "Temporal Dash",
            description = "Fling forward through physical collisions, leaving an afterimage trail.",
            energyCost = 0f,
            skillPointPrerequisite = 0
        ),
        SkillType.DOUBLE_JUMP to PlayerSkill(
            type = SkillType.DOUBLE_JUMP,
            name = "Gravity Leap",
            description = "Compress kinetic energy in Mid-air to activate a second jump.",
            energyCost = 0f,
            skillPointPrerequisite = 1
        ),
        SkillType.KINETIC_SHIELD to PlayerSkill(
            type = SkillType.KINETIC_SHIELD,
            name = "Core Barrier",
            description = "Consume energy to establish a shielding orbit absorbing all projectile strikes.",
            energyCost = 30f,
            skillPointPrerequisite = 2
        ),
        SkillType.SOUL_PULSE to PlayerSkill(
            type = SkillType.SOUL_PULSE,
            name = "Aura Shockwave",
            description = "Discharge mental shards in a 360-degree radial blast, pushing away threats.",
            energyCost = PlayerConfig.SOUL_PULSE_COST,
            skillPointPrerequisite = 2
        ),
        SkillType.REGENERATE_PASSIVE to PlayerSkill(
            type = SkillType.REGENERATE_PASSIVE,
            name = "Focus Reconstitution",
            description = "Gradually recover lost HP over time whenever energy is at max level.",
            energyCost = 0f,
            skillPointPrerequisite = 3,
            isPassive = true
        ),
        SkillType.CRITICAL_MIND to PlayerSkill(
            type = SkillType.CRITICAL_MIND,
            name = "Cognitive Strike",
            description = "Your attacks have a 25% chance to deal double damage to enemies.",
            energyCost = 0f,
            skillPointPrerequisite = 3,
            isPassive = true
        )
    )

    /**
     * Determines whether a skill can be unlocked based on player's level and available skill points.
     */
    fun canUnlock(state: PlayerState, skillType: SkillType): Boolean {
        val skill = SKILLS_CATALOG[skillType] ?: return false
        return state.skillPoints >= skill.skillPointPrerequisite && !state.unlockedSkills.contains(skillType)
    }
}
