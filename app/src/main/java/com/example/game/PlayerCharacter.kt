package com.example.game

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke

enum class Direction { LEFT, RIGHT }
enum class AttackType { LIGHT, HEAVY, DASH_ATTACK, AERIAL }
enum class SkillType { DASH, DOUBLE_JUMP, WALL_CLIMB, SHIELD }

data class Weapon(
    val name: String,
    val damage: Float,
    val range: Float,
    val color: Color,
    val trailColor: Color,
    val impactSize: Float
)

data class PlayerState(
    val x: Float = 400f,
    val y: Float = 600f,
    val vx: Float = 0f,
    val vy: Float = 0f,
    val isGrounded: Boolean = false,
    val radius: Float = PlayerConfig.DEFAULT_RADIUS,
    val hp: Float = PlayerConfig.STARTING_HP,
    val maxHp: Float = PlayerConfig.STARTING_MAX_HP,
    val energy: Float = PlayerConfig.STARTING_ENERGY,
    val maxEnergy: Float = PlayerConfig.STARTING_MAX_ENERGY,
    val speed: Float = PlayerConfig.MOVEMENT_SPEED,
    val jumpPower: Float = PlayerConfig.JUMP_POWER,
    val direction: Direction = Direction.RIGHT,
    val lives: Int = PlayerConfig.STARTING_LIVES,
    val isDashing: Boolean = false,
    val dashEndTime: Long = 0L,
    val dashCooldown: Long = 0L,
    val lastDashTime: Long = 0L,
    val level: Int = 1,
    val experience: Int = 0,
    val skillPoints: Int = 0,
    val activeWeapon: Weapon = PlayerConfig.DEFAULT_WEAPON,
    val unlockedSkills: Set<SkillType> = setOf(SkillType.DASH),
    val isAttacking: Boolean = false,
    val attackProgress: Float = 0f,
    val attackType: AttackType = AttackType.LIGHT,
    val invincibleUntil: Long = 0L
)

object PlayerConfig {
    const val STARTING_HP = 100f
    const val STARTING_MAX_HP = 100f
    const val STARTING_ENERGY = 100f
    const val STARTING_MAX_ENERGY = 100f
    
    const val DEFAULT_RADIUS = 28f
    const val MOVEMENT_SPEED = 700f
    const val JUMP_POWER = -1100f
    const val DASH_SPEED_MULTIPLIER = 2.5f
    const val DASH_DURATION_MS = 200L
    const val DASH_COOLDOWN_MS = 1000L
    const val STARTING_LIVES = 3
    const val GRAVITY = 2500f
    const val ATTACK_DURATION_MS = 300L
    const val INVINCIBILITY_MS = 1500L
    
    val DEFAULT_WEAPON = Weapon("Photon Blade", 25f, 120f, Color(0xFF00E5FF), Color(0x6600E5FF), 40f)
    val HEAVY_WEAPON = Weapon("Titan Hammer", 50f, 90f, Color(0xFFFF5252), Color(0x66FF5252), 80f)
}

// Renders the player, weapons, and attack effects perfectly encapsulated here
fun DrawScope.drawPlayerCharacter(state: PlayerState) {
    val centerX = state.x
    val centerY = state.y - state.radius
    val now = System.currentTimeMillis()
    
    // Blink effect if invincible
    val isInvincible = now < state.invincibleUntil
    if (isInvincible && (now / 100) % 2 == 0L) {
        return // Skip drawing this frame to create blink
    }
    
    // Dash Trail Effect
    if (state.isDashing) {
        drawCircle(
            color = state.activeWeapon.color.copy(alpha = 0.4f),
            radius = state.radius * 1.5f,
            center = Offset(centerX - (if (state.direction == Direction.RIGHT) 30f else -30f), centerY)
        )
    }

    // Shadow
    drawOval(
        color = Color.Black.copy(alpha = 0.4f),
        topLeft = Offset(centerX - state.radius, state.y - 6f),
        size = Size(state.radius * 2, 12f)
    )

    // Player Body
    val bodyColor = if (state.isDashing) state.activeWeapon.color else Color.White
    drawCircle(
        color = bodyColor,
        radius = state.radius,
        center = Offset(centerX, centerY)
    )
    
    // Visor/Eye (Direction Indicator)
    val visorOffsetX = if (state.direction == Direction.RIGHT) 10f else -10f
    drawRect(
        color = state.activeWeapon.color,
        topLeft = Offset(centerX + visorOffsetX - 4f, centerY - 8f),
        size = Size(8f, 12f)
    )

    // Weapon & Attack Dynamics
    if (state.isAttacking) {
        val dirSign = if (state.direction == Direction.RIGHT) 1f else -1f
        val weaponStartX = centerX + (state.radius * dirSign)
        
        // Attack reaches its peak at progress 0.5
        val ext = if (state.attackProgress < 0.5f) {
            state.attackProgress * 2f
        } else {
            (1f - state.attackProgress) * 2f
        }
        
        val attackReach = state.activeWeapon.range * ext
        val weaponEndX = weaponStartX + (attackReach * dirSign)
        
        // Draw the weapon strike
        drawLine(
            color = state.activeWeapon.color,
            start = Offset(weaponStartX, centerY),
            end = Offset(weaponEndX, centerY),
            strokeWidth = 10f,
            cap = Stroke.DefaultCap
        )
        
        // Draw strike impact trail
        val trailRadius = state.activeWeapon.impactSize * ext
        drawCircle(
            color = state.activeWeapon.trailColor,
            radius = trailRadius,
            center = Offset(weaponEndX, centerY),
            style = Stroke(width = 6f)
        )
        
        // Secondary arc for heavy attacks
        if (state.attackType == AttackType.HEAVY) {
            drawArc(
                color = state.activeWeapon.trailColor,
                startAngle = if (state.direction == Direction.RIGHT) -45f else 135f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(weaponEndX - trailRadius, centerY - trailRadius),
                size = Size(trailRadius * 2, trailRadius * 2),
                style = Stroke(width = 8f)
            )
        }
    }
}
