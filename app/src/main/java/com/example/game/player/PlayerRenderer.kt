package com.example.game.player

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke

/**
 * Extension functions for DrawScope to decouple rendering logic from game views.
 * You can effortlessly change the character's body shapes, visual effects (particles, slashes, glows, shields),
 * and dynamic animations directly in this file.
 */
fun DrawScope.drawPlayerCharacter(state: PlayerState) {
    val centerX = state.x
    val centerY = state.y - state.radius
    val now = System.currentTimeMillis()
    
    // --- 1. Invincibility Blinking Aura ---
    val isInvincible = now < state.invincibleUntil
    if (isInvincible) {
        // Draw an outer protective halo with neon shimmer
        val pulseRadius = state.radius * 1.4f + (Math.sin(now.toDouble() / 100.0).toFloat() * 4f)
        drawCircle(
            color = Color(0x77FFFFFF),
            radius = pulseRadius,
            center = Offset(centerX, centerY),
            style = Stroke(width = 3f)
        )
        // Blink body frame to signal temporary hurt invulnerability
        if ((now / 80) % 2 == 0L) {
            return 
        }
    }
    
    // --- 2. Temporal Dash Afterimages ---
    if (state.isDashing) {
        val trailDistance = 35f
        val directionMultiplier = if (state.direction == Direction.RIGHT) 1f else -1f
        
        // Draw primary ghostly trail
        drawCircle(
            color = state.activeWeapon.color.copy(alpha = 0.45f),
            radius = state.radius * 1.3f,
            center = Offset(centerX - (trailDistance * directionMultiplier), centerY)
        )
        // Draw secondary outer trails
        drawCircle(
            color = state.activeWeapon.color.copy(alpha = 0.20f),
            radius = state.radius * 1.1f,
            center = Offset(centerX - (trailDistance * 2f * directionMultiplier), centerY)
        )
    }

    // --- 3. Ground Level Ambient Shadow ---
    drawOval(
        color = Color.Black.copy(alpha = 0.45f),
        topLeft = Offset(centerX - state.radius * 0.9f, state.y - 4f),
        size = Size(state.radius * 1.8f, 8f)
    )

    // --- 4. Main Character Body & Outer Wear ---
    val bodyColor = if (state.isDashing) state.activeWeapon.color else Color(0xFFECEFF1)
    
    // Draw Suit Base
    drawCircle(
        color = bodyColor,
        radius = state.radius,
        center = Offset(centerX, centerY)
    )
    
    // Draw Dark Cybernetic Armor Plate
    drawCircle(
        color = Color(0xFF37474F),  // Slate Gray Core
        radius = state.radius * 0.7f,
        center = Offset(centerX, centerY + 3f)
    )

    // --- 5. Futuristic Visor / Electronic Eye (Indicates Direction) ---
    val visorOffsetX = if (state.direction == Direction.RIGHT) (state.radius * 0.4f) else -(state.radius * 0.4f)
    // Draw outer visor frame
    drawRect(
        color = Color(0xFF263238),
        topLeft = Offset(centerX + visorOffsetX - 6f, centerY - 10f),
        size = Size(12f, 12f)
    )
    // Draw inner neon state light matching the active weapon color!
    drawRect(
        color = state.activeWeapon.color,
        topLeft = Offset(centerX + visorOffsetX - 3f, centerY - 8f),
        size = Size(6f, 6f)
    )

    // --- 6. Active Shield Ring (If passive deflector/shield is active) ---
    if (state.unlockedSkills.contains(SkillType.KINETIC_SHIELD)) {
        val rotationAngle = (now / 5) % 360f
        val shieldDist = state.radius * 1.8f
        val sRad = Math.toRadians(rotationAngle.toDouble())
        val ox = (Math.cos(sRad) * shieldDist).toFloat()
        val oy = (Math.sin(sRad) * shieldDist).toFloat()
        
        drawCircle(
            color = Color(0xFF2979FF), // Bright Blue
            radius = 8f,
            center = Offset(centerX + ox, centerY + oy)
        )
        drawCircle(
            color = Color(0x332979FF),
            radius = state.radius * 1.9f,
            center = Offset(centerX, centerY),
            style = Stroke(width = 4f)
        )
    }

    // --- 7. Weapon Dynamics & Attack Slashing Swipes ---
    if (state.isAttacking) {
        val dirSign = if (state.direction == Direction.RIGHT) 1f else -1f
        val weaponStartX = centerX + (state.radius * dirSign)
        
        // Attack reaches peak at 0.5 prog, then retreats/fades
        val stretch = if (state.attackProgress < 0.5f) {
            state.attackProgress * 2f
        } else {
            (1f - state.attackProgress) * 2f
        }
        
        val attackReach = state.activeWeapon.range * stretch
        val weaponEndX = weaponStartX + (attackReach * dirSign)
        
        // Weapon Strike Line (Glow Laser/Saber style)
        drawLine(
            color = state.activeWeapon.color,
            start = Offset(weaponStartX, centerY + 2f),
            end = Offset(weaponEndX, centerY - 2f),
            strokeWidth = 12f,
            cap = Stroke.DefaultCap
        )
        
        // Bright White Saber Core Core line
        drawLine(
            color = Color.White,
            start = Offset(weaponStartX, centerY + 2f),
            end = Offset(weaponEndX, centerY - 2f),
            strokeWidth = 4f,
            cap = Stroke.DefaultCap
        )
        
        // Swipe Impact Spark / Trail Ring
        val trailRadius = state.activeWeapon.impactSize * stretch
        drawCircle(
            color = state.activeWeapon.trailColor,
            radius = trailRadius,
            center = Offset(weaponEndX, centerY),
            style = Stroke(width = 8f)
        )
        
        // Dynamic arc slash depending on attack type classification
        if (state.attackType == AttackType.HEAVY) {
            // Draw a wider destructive arc sweep for high heavy axes, hammers or giant swords
            drawArc(
                color = state.activeWeapon.trailColor,
                startAngle = if (state.direction == Direction.RIGHT) -60f else 120f,
                sweepAngle = 120f,
                useCenter = false,
                topLeft = Offset(weaponEndX - trailRadius * 1.2f, centerY - trailRadius * 1.2f),
                size = Size(trailRadius * 2.4f, trailRadius * 2.4f),
                style = Stroke(width = 10f)
            )
        } else {
            // Light blade swoosh arc
            drawArc(
                color = state.activeWeapon.trailColor,
                startAngle = if (state.direction == Direction.RIGHT) -30f else 150f,
                sweepAngle = 60f,
                useCenter = false,
                topLeft = Offset(weaponEndX - trailRadius, centerY - trailRadius),
                size = Size(trailRadius * 2f, trailRadius * 2f),
                style = Stroke(width = 5f)
            )
        }
    }
}
