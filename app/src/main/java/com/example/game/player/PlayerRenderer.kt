package com.example.game.player

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke

/**
 * Extension functions for DrawScope to decouple rendering logic from game views.
 * Draws the character's base silhouette, accessories, active skins, and dynamic combat actions.
 */
fun DrawScope.drawPlayerCharacter(state: PlayerState) {
    val centerX = state.x
    val centerY = state.y - state.radius
    val now = System.currentTimeMillis()
    val skin = state.activeSkin
    
    // --- 1. Invincibility Blinking Aura ---
    val isInvincible = now < state.invincibleUntil
    if (isInvincible) {
        // Draw an outer protective halo with neon shimmer
        val pulseRadius = state.radius * 1.4f + (Math.sin(now.toDouble() / 100.0).toFloat() * 4f)
        drawCircle(
            color = skin.accentColor.copy(alpha = 0.5f),
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
        
        // Draw primary ghostly trail matching the active skin's theme
        drawCircle(
            color = skin.accentColor.copy(alpha = 0.45f),
            radius = state.radius * 1.3f,
            center = Offset(centerX - (trailDistance * directionMultiplier), centerY)
        )
        // Draw secondary outer trails
        drawCircle(
            color = skin.primaryColor.copy(alpha = 0.20f),
            radius = state.radius * 1.1f,
            center = Offset(centerX - (trailDistance * 2f * directionMultiplier), centerY)
        )
    }

    // --- 3. Flowing Cape Physics ---
    drawFlowingCape(centerX, centerY, state.radius, skin, state.direction, state.isDashing, state.vx, now)

    // --- 4. Back Gear / Thrusters (Cyber Wings, Jetpack flames, Gravity orbs) ---
    drawBackGear(centerX, centerY, state.radius, skin, state.isDashing, state.vy, now)

    // --- 5. Ground Level Ambient Shadow ---
    drawOval(
        color = skin.shadowColor,
        topLeft = Offset(centerX - state.radius * 0.9f, state.y - 4f),
        size = Size(state.radius * 1.8f, 8f)
    )

    // --- 6. Main Character Base Suit (Secondary color, e.g. dark undersuit) ---
    drawCircle(
        color = skin.secondaryColor,
        radius = state.radius,
        center = Offset(centerX, centerY)
    )
    
    // --- 7. Outer Chest Armor Plates (Primary color) ---
    drawCircle(
        color = skin.primaryColor,
        radius = state.radius * 0.75f,
        center = Offset(centerX, centerY + 2f)
    )

    // Cosmic logo inside armor plates
    drawCircle(
        color = skin.secondaryColor,
        radius = state.radius * 0.35f,
        center = Offset(centerX, centerY + 4f)
    )

    // --- 8. Headlights, Horns, Crown, and Celestial Glows ---
    drawHeadGlow(centerX, centerY, state.radius, skin, now)

    // --- 9. Futuristic Visor / Mask / Eye light ---
    val dirMultiplier = if (state.direction == Direction.RIGHT) 1f else -1f
    val visorOffsetX = (state.radius * 0.35f) * dirMultiplier
    
    when (skin.visorStyle) {
        VisorStyle.CYBER_RECTANGLE -> {
            // High tech rectangle visor
            drawRect(
                color = Color(0xFF263238),
                topLeft = Offset(centerX + visorOffsetX - 8f, centerY - 8f),
                size = Size(16f, 10f)
            )
            drawRect(
                color = skin.accentColor,
                topLeft = Offset(centerX + visorOffsetX - 5f, centerY - 6f),
                size = Size(10f, 6f)
            )
        }
        VisorStyle.CYCLOPS_BEAM -> {
            // Solid continuous laser eye thread
            drawLine(
                color = skin.accentColor,
                start = Offset(centerX + (state.radius * 0.2f * dirMultiplier), centerY - 4f),
                end = Offset(centerX + (state.radius * 0.8f * dirMultiplier), centerY - 4f),
                strokeWidth = 6f
            )
        }
        VisorStyle.NINJA_MASK -> {
            // Stealth fabric wrap with dual threatening diagonal eye points
            val eyeX = centerX + visorOffsetX
            // Left eye point
            drawLine(
                color = skin.accentColor,
                start = Offset(eyeX - 5f, centerY - 6f),
                end = Offset(eyeX, centerY - 4f),
                strokeWidth = 3f
            )
            // Right eye point
            drawLine(
                color = skin.accentColor,
                start = Offset(eyeX, centerY - 4f),
                end = Offset(eyeX + 5f, centerY - 6f),
                strokeWidth = 3f
            )
        }
        VisorStyle.ROYAL_CROWN -> {
            // Coronet of Light
            val crownY = centerY - state.radius * 0.9f
            drawRect(
                color = skin.accentColor,
                topLeft = Offset(centerX - 10f, crownY),
                size = Size(20f, 4f)
            )
            // Draw spikes of gold crown
            drawLine(color = skin.accentColor, start = Offset(centerX - 8f, crownY), end = Offset(centerX - 8f, crownY - 6f), strokeWidth = 3f)
            drawLine(color = skin.accentColor, start = Offset(centerX, crownY), end = Offset(centerX, crownY - 9f), strokeWidth = 3f)
            drawLine(color = skin.accentColor, start = Offset(centerX + 8f, crownY), end = Offset(centerX + 8f, crownY - 6f), strokeWidth = 3f)
        }
    }

    // --- 10. Active Shield Core Ring (Kinetic Shield Skill) ---
    if (state.unlockedSkills.contains(SkillType.KINETIC_SHIELD)) {
        val rotationAngle = (now / 5) % 360f
        val shieldDist = state.radius * 1.8f
        val sRad = Math.toRadians(rotationAngle.toDouble())
        val ox = (Math.cos(sRad) * shieldDist).toFloat()
        val oy = (Math.sin(sRad) * shieldDist).toFloat()
        
        drawCircle(
            color = skin.accentColor, // match skin glow aura color
            radius = 8f,
            center = Offset(centerX + ox, centerY + oy)
        )
        drawCircle(
            color = skin.accentColor.copy(alpha = 0.25f),
            radius = state.radius * 1.9f,
            center = Offset(centerX, centerY),
            style = Stroke(width = 4f)
        )
    }

    // --- 11. Weapon Slash Swipes & Dynamic Strikes ---
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
        
        // Bright White Saber Core center line
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
