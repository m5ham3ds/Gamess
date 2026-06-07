package com.example.game.player

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke

/**
 * Available skin profiles for the main character.
 */
enum class SkinId {
    NEON_CENTURION,
    CYBER_NINJA,
    GOLDEN_OVERLORD,
    COSMIC_PHANTOM
}

/**
 * Complete aesthetic blueprint of the main character.
 * You can easily modify, add or edit parameters here to completely change the character's design,
 * including armor colors, visual themes, hair/horn colors, visor shapes, and custom dynamic wing/thruster physics.
 */
data class CharacterSkin(
    val id: SkinId,
    val name: String,
    val description: String,
    
    // --- Body & Armor Color Scheme ---
    val primaryColor: Color,     // Outer costume/armor plate color
    val secondaryColor: Color,   // Inner fiber/cybernetic suit color
    val accentColor: Color,      // Light indicators, runes, or visor glow
    val shadowColor: Color = Color.Black.copy(alpha = 0.5f),

    // --- Silhouette Features ---
    val visorStyle: VisorStyle = VisorStyle.CYBER_RECTANGLE,
    val headGlowType: HeadGlowType = HeadGlowType.NONE,
    val backGearStyle: BackGearStyle = BackGearStyle.NONE,
    val hasFlowingCape: Boolean = false,
    val capeColor: Color = Color.Transparent,
    
    // --- Customizable Particle Trails ---
    val movementTrailAlpha: Float = 0.3f,
    val particleColor: Color = Color.White
)

enum class VisorStyle {
    CYBER_RECTANGLE,
    CYCLOPS_BEAM,
    NINJA_MASK,
    ROYAL_CROWN
}

enum class HeadGlowType {
    NONE,
    NEON_Horns,
    HALO_LIGHT,
    COSMIC_FLAME
}

enum class BackGearStyle {
    NONE,
    CYBER_WINGS,    // Neon blades/wings that flap
    JETPACK_THRUSTER, // Glowing electric thruster sparks
    GRAVITY_ORBS    // Spheres orbiting the chest/back
}

/**
 * Easily modify, add or edit skins in this central registry to change the character's in-game appearance.
 */
object PlayerAppearance {
    
    val NEON_CENTURION = CharacterSkin(
        id = SkinId.NEON_CENTURION,
        name = "Neon Centurion",
        description = "Standard tactical cyber-armor equipped with high-impact photon emitters.",
        primaryColor = Color(0xFFECEFF1),      // Pearl White Armor
        secondaryColor = Color(0xFF37474F),    // Slate Core
        accentColor = Color(0xFF00E5FF),       // Cyan visor/runes
        visorStyle = VisorStyle.CYBER_RECTANGLE,
        headGlowType = HeadGlowType.HALO_LIGHT,
        backGearStyle = BackGearStyle.JETPACK_THRUSTER,
        hasFlowingCape = false,
        particleColor = Color(0x8800E5FF)
    )

    val CYBER_NINJA = CharacterSkin(
        id = SkinId.CYBER_NINJA,
        name = "Cyber Shadow Ninja",
        description = "A stealth-oriented outfit designed with advanced dark-matter absorption.",
        primaryColor = Color(0xFF212121),      // Pitch Black Armor
        secondaryColor = Color(0xFF4A148C),    // Deep Purple Secondary
        accentColor = Color(0xFFD500F9),       // Neon Magenta Visor
        visorStyle = VisorStyle.NINJA_MASK,
        headGlowType = HeadGlowType.NEON_Horns,
        backGearStyle = BackGearStyle.NONE,
        hasFlowingCape = true,
        capeColor = Color(0xCC7B1FA2),          // Deep Translucent Amethyst Cape
        particleColor = Color(0x66D500F9)
    )

    val GOLDEN_OVERLORD = CharacterSkin(
        id = SkinId.GOLDEN_OVERLORD,
        name = "Golden Dawn Overlord",
        description = "Royal experimental armor powered directly by solar-flare generators.",
        primaryColor = Color(0xFFFFD700),      // Pure Gold
        secondaryColor = Color(0xFF4E342E),    // Dark Mahogany Leather Undercoat
        accentColor = Color(0xFFFF4500),       // Fiery Solar Orange Light
        visorStyle = VisorStyle.ROYAL_CROWN,
        headGlowType = HeadGlowType.HALO_LIGHT,
        backGearStyle = BackGearStyle.CYBER_WINGS,
        hasFlowingCape = true,
        capeColor = Color(0xCCE53935),          // Imperial Red Cape
        particleColor = Color(0xBBFFD700)
    )

    val COSMIC_PHANTOM = CharacterSkin(
        id = SkinId.COSMIC_PHANTOM,
        name = "Cosmic Phantom",
        description = "An astral shell woven from decaying neutron stars and zero-point energy fields.",
        primaryColor = Color(0xFF1A237E),      // Cosmic Dark Blue
        secondaryColor = Color(0xFF0D47A1),    // Deep Nebula Blue
        accentColor = Color(0xFF76FF03),       // Radiant Cosmic Lime
        visorStyle = VisorStyle.CYCLOPS_BEAM,
        headGlowType = HeadGlowType.COSMIC_FLAME,
        backGearStyle = BackGearStyle.GRAVITY_ORBS,
        hasFlowingCape = false,
        particleColor = Color(0xEE76FF03)
    )

    val ALL_SKINS = listOf(
        NEON_CENTURION,
        CYBER_NINJA,
        GOLDEN_OVERLORD,
        COSMIC_PHANTOM
    )
}

/**
 * Dynamic design utility to render character clothes, accessories, and wings cleanly in modular layers.
 */
fun DrawScope.drawBackGear(
    centerX: Float,
    centerY: Float,
    radius: Float,
    skin: CharacterSkin,
    isDashing: Boolean,
    vy: Float,
    timeMs: Long
) {
    val wingsAngle = (Math.sin(timeMs.toDouble() / 150.0) * 18.0).toFloat()
    
    when (skin.backGearStyle) {
        BackGearStyle.CYBER_WINGS -> {
            // Left Wing
            drawArc(
                color = skin.accentColor.copy(alpha = 0.8f),
                startAngle = 180f - wingsAngle,
                sweepAngle = 45f,
                useCenter = true,
                topLeft = Offset(centerX - radius * 2.8f, centerY - radius * 1.5f),
                size = Size(radius * 2.2f, radius * 1.5f)
            )
            // Right Wing
            drawArc(
                color = skin.accentColor.copy(alpha = 0.8f),
                startAngle = -45f + wingsAngle,
                sweepAngle = 45f,
                useCenter = true,
                topLeft = Offset(centerX + radius * 0.6f, centerY - radius * 1.5f),
                size = Size(radius * 2.2f, radius * 1.5f)
            )
        }
        BackGearStyle.JETPACK_THRUSTER -> {
            // Draw dual thrusters nozzles on the back
            val nozzleOffset = 18f
            // Glow flame thrusting downwards
            val thrustFactor = if (vy < -50) 1.8f else if (isDashing) 2.2f else 1.0f
            val flameHeight = 15f * thrustFactor + (Math.sin(timeMs.toDouble() / 50.0).toFloat() * 3f)
            
            // Left flame
            drawOval(
                color = skin.accentColor,
                topLeft = Offset(centerX - nozzleOffset - 4f, centerY + radius * 0.5f),
                size = Size(8f, flameHeight)
            )
            // Right flame
            drawOval(
                color = skin.accentColor,
                topLeft = Offset(centerX + nozzleOffset - 4f, centerY + radius * 0.5f),
                size = Size(8f, flameHeight)
            )
        }
        BackGearStyle.GRAVITY_ORBS -> {
            // Draw three cute futuristic spheres orbiting the host
            val angle = (timeMs / 4) % 360f
            for (i in 0 until 3) {
                val offsetAngle = angle + (i * 120f)
                val rad = Math.toRadians(offsetAngle.toDouble())
                val ox = (Math.cos(rad) * radius * 1.8f).toFloat()
                val oy = (Math.sin(rad) * radius * 0.8f).toFloat()
                
                drawCircle(
                    color = skin.accentColor,
                    radius = 5f,
                    center = Offset(centerX + ox, centerY + oy)
                )
            }
        }
        BackGearStyle.NONE -> {}
    }
}

fun DrawScope.drawHeadGlow(
    centerX: Float,
    centerY: Float,
    radius: Float,
    skin: CharacterSkin,
    timeMs: Long
) {
    when (skin.headGlowType) {
        HeadGlowType.HALO_LIGHT -> {
            // Floating halo above the head
            val bounceY = (Math.sin(timeMs.toDouble() / 200.0) * 3f).toFloat()
            drawOval(
                color = skin.accentColor,
                topLeft = Offset(centerX - radius * 0.7f, centerY - radius * 1.4f + bounceY),
                size = Size(radius * 1.4f, 6f),
                style = Stroke(width = 3f)
            )
        }
        HeadGlowType.NEON_Horns -> {
            // Glowing warrior horns
            // Left horn
            drawArc(
                color = skin.accentColor,
                startAngle = 210f,
                sweepAngle = 40f,
                useCenter = false,
                topLeft = Offset(centerX - radius * 0.8f, centerY - radius * 1.3f),
                size = Size(radius * 0.6f, radius * 0.6f),
                style = Stroke(width = 4f)
            )
            // Right horn
            drawArc(
                color = skin.accentColor,
                startAngle = 290f,
                sweepAngle = 40f,
                useCenter = false,
                topLeft = Offset(centerX + radius * 0.2f, centerY - radius * 1.3f),
                size = Size(radius * 0.6f, radius * 0.6f),
                style = Stroke(width = 4f)
            )
        }
        HeadGlowType.COSMIC_FLAME -> {
            // Dynamic flaming hair effect
            val floatHeight = radius * 1.3f
            for (i in 0 until 4) {
                val px = centerX - radius * 0.6f + (i * radius * 0.4f)
                val py = centerY - radius * 0.9f
                val lengthFluc = 12f + (Math.sin((timeMs + i * 50).toDouble() / 100.0) * 6f).toFloat()
                
                drawLine(
                    color = skin.accentColor.copy(alpha = 0.7f),
                    start = Offset(px, py),
                    end = Offset(px + (Math.sin(timeMs.toDouble() / 80.0) * 4f).toFloat(), py - lengthFluc),
                    strokeWidth = 6f
                )
            }
        }
        HeadGlowType.NONE -> {}
    }
}

fun DrawScope.drawFlowingCape(
    centerX: Float,
    centerY: Float,
    radius: Float,
    skin: CharacterSkin,
    direction: Direction,
    isDashing: Boolean,
    vx: Float,
    timeMs: Long
) {
    if (!skin.hasFlowingCape) return
    
    // Wave animation factor dependent on speed and time
    val speedMod = if (isDashing) 40f else if (Math.abs(vx) > 10) 25f else 10f
    val wave = (Math.sin(timeMs.toDouble() / 120.0) * speedMod).toFloat()
    
    val sign = if (direction == Direction.RIGHT) -1f else 1f
    
    // Cape starting points anchor from shoulders
    val shoulderX = centerX + (radius * 0.3f * -sign)
    val shoulderY = centerY + (radius * 0.1f)
    
    // Cape trail stretches backwards
    val capeEndX = shoulderX + (radius * 2.2f * sign) + (wave * sign * 0.3f)
    val capeEndY = shoulderY + (radius * 1.6f) + wave
    
    // Draw flowing banner mantle
    drawArc(
        color = skin.capeColor,
        startAngle = if (direction == Direction.RIGHT) 90f else 0f,
        sweepAngle = 90f,
        useCenter = true,
        topLeft = Offset(
            if (direction == Direction.RIGHT) centerX - radius * 2.0f else centerX, 
            centerY - radius * 0.2f
        ),
        size = Size(radius * 2f, radius * 1.8f)
    )
}
