package com.example.game.enemy

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.game.player.Direction

/**
 * Extension functions for DrawScope to render various custom, highly distinct enemy types
 * with glowing outlines, shields, kinetic details, and custom aesthetics.
 */
fun DrawScope.drawAdversary(enemy: Enemy) {
    val cx = enemy.x
    val cy = enemy.y
    val blueprint = EnemyConfig.getBlueprint(enemy.type)
    val time = System.currentTimeMillis() + (enemy.animationOffset * 100).toLong()
    val r = enemy.radius

    // --- 1. Stunned Indicator Arc ---
    if (enemy.isStunned) {
        val bounceA = (time / 3) % 360f
        drawArc(
            color = Color.Yellow,
            startAngle = bounceA,
            sweepAngle = 120f,
            useCenter = false,
            topLeft = Offset(cx - r * 1.3f, cy - r * 1.3f),
            size = Size(r * 2.6f, r * 2.6f),
            style = Stroke(width = 4f)
        )
    }

    // --- 2. Floating level dynamic health plates (rendered only when damaged) ---
    val hpRatio = enemy.hp / enemy.maxHp
    if (hpRatio < 1f) {
        val barWidth = r * 1.8f
        val barHeight = 6f
        val barX = cx - (barWidth / 2f)
        val barY = cy - r * 1.3f
        
        // Background track (Dark Charcoal)
        drawRect(
            color = Color(0x992B2B2B),
            topLeft = Offset(barX, barY),
            size = Size(barWidth, barHeight)
        )
        // Red Health Gauge segment fill
        drawRect(
            color = if (hpRatio > 0.4f) Color(0xFF00E676) else Color(0xFFFF1744), // Green if healthy, red if critical
            topLeft = Offset(barX, barY),
            size = Size(barWidth * hpRatio, barHeight)
        )
    }

    // --- 3. Draw Unique Base silhouette depending on the enemy type ---
    when (enemy.type) {
        EnemyType.SHADOW_STALKER -> {
            // Dark ninja stalker shape
            // Shadow trail
            drawCircle(
                color = blueprint.primaryColor,
                radius = r,
                center = Offset(cx, cy)
            )
            // Glowing core eye
            val lookDir = if (enemy.direction == Direction.RIGHT) 1f else -1f
            drawCircle(
                color = blueprint.glowColor,
                radius = r * 0.35f,
                center = Offset(cx + (r * 0.4f * lookDir), cy - r * 0.1f)
            )
            // Cyber ears / Spiked armor plates
            drawLine(
                color = blueprint.primaryColor,
                start = Offset(cx - r * 0.5f, cy - r * 0.8f),
                end = Offset(cx - r * 0.8f, cy - r * 1.3f),
                strokeWidth = 5f
            )
            drawLine(
                color = blueprint.primaryColor,
                start = Offset(cx + r * 0.5f, cy - r * 0.8f),
                end = Offset(cx + r * 0.8f, cy - r * 1.3f),
                strokeWidth = 5f
            )
        }
        
        EnemyType.ABYSSAL_ORB -> {
            // Floating plasma sphere
            val pulse = (Math.sin(time.toDouble() / 150.0) * (r * 0.12f)).toFloat()
            val activeRadius = r + pulse

            // Outer rings
            val spinAngle = (time / 4) % 360f
            drawArc(
                color = blueprint.glowColor.copy(alpha = 0.6f),
                startAngle = spinAngle,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(cx - activeRadius * 1.3f, cy - activeRadius * 1.3f),
                size = Size(activeRadius * 2.6f, activeRadius * 2.6f),
                style = Stroke(width = 3f)
            )

            // Inner solid sphere core
            drawCircle(
                color = blueprint.primaryColor,
                radius = activeRadius * 0.8f,
                center = Offset(cx, cy)
            )
            drawCircle(
                color = blueprint.glowColor,
                radius = activeRadius * 0.4f,
                center = Offset(cx, cy)
            )
            // Tiny central white spark
            drawCircle(
                color = Color.White,
                radius = activeRadius * 0.15f,
                center = Offset(cx, cy)
            )
        }

        EnemyType.VOID_DEVOURER -> {
            // Mega armored miniboss
            val shakeX = if (enemy.aiState == EnemyAIState.AGGRO) (Math.sin(time.toDouble() / 30.0) * 2f).toFloat() else 0f
            val dcX = cx + shakeX
            
            // Draw armored heavy hexagon body
            drawCircle(
                color = blueprint.primaryColor,
                radius = r,
                center = Offset(dcX, cy)
            )
            
            // Plate overlapping lines
            drawCircle(
                color = blueprint.glowColor.copy(alpha = 0.4f),
                radius = r * 0.85f,
                center = Offset(dcX, cy),
                style = Stroke(width = 3f)
            )
            
            // Multiple threatening red/pink glowing vents
            val lookDir = if (enemy.direction == Direction.RIGHT) 1f else -1f
            drawRect(
                color = blueprint.glowColor,
                topLeft = Offset(dcX + (r * 0.3f * lookDir) - 4f, cy - 8f),
                size = Size(8f, 16f)
            )
            // Spikes on the spine back
            val sXSign = if (enemy.direction == Direction.RIGHT) -1f else 1f
            for (i in 0 until 3) {
                val spikeY = cy - r * 0.6f + (i * r * 0.5f)
                val spikeX = dcX + (r * sXSign)
                drawLine(
                    color = blueprint.primaryColor,
                    start = Offset(spikeX, spikeY),
                    end = Offset(spikeX + (r * 0.3f * sXSign), spikeY - 6f),
                    strokeWidth = 6f
                )
            }
        }

        EnemyType.NEON_SPIDER -> {
            // Insectoid mechanical land rover
            // Draw main body pod
            drawRect(
                color = blueprint.primaryColor,
                topLeft = Offset(cx - r, cy - r * 0.6f),
                size = Size(r * 2f, r * 1.2f)
            )
            
            // Legs structure
            val walkingLeap = (Math.sin(time.toDouble() / 80.0) * (r * 0.3f)).toFloat()
            
            // Left articulated leg
            drawLine(
                color = blueprint.primaryColor,
                start = Offset(cx - r * 0.6f, cy),
                end = Offset(cx - r * 1.3f, cy + r * 0.8f + walkingLeap),
                strokeWidth = 4f
            )
            // Right articulated leg
            drawLine(
                color = blueprint.primaryColor,
                start = Offset(cx + r * 0.6f, cy),
                end = Offset(cx + r * 1.3f, cy + r * 0.8f - walkingLeap),
                strokeWidth = 4f
            )
            
            // Yellow visual radar lens emitter
            val lookDir = if (enemy.direction == Direction.RIGHT) 1f else -1f
            drawCircle(
                color = blueprint.glowColor,
                radius = 5f,
                center = Offset(cx + (r * lookDir), cy - r * 0.1f)
            )
        }

        EnemyType.CHRONO_SENTINEL -> {
            // Flying hover glider drone
            val hoverY = (Math.sin(time.toDouble() / 150.0) * 4f).toFloat()
            val dCy = cy + hoverY
            
            // Wings/propellers
            val wingSpan = (Math.cos(time.toDouble() / 60.0) * (r * 0.8f)).toFloat()
            
            // Lateral engine pods
            drawCircle(
                color = blueprint.primaryColor,
                radius = r * 0.6f,
                center = Offset(cx, dCy)
            )
            
            // Left motor bar
            drawLine(
                color = blueprint.primaryColor,
                start = Offset(cx, dCy),
                end = Offset(cx - r * 1.4f, dCy),
                strokeWidth = 4f
            )
            // Left wing rotors
            drawLine(
                color = blueprint.glowColor,
                start = Offset(cx - r * 1.4f, dCy - wingSpan),
                end = Offset(cx - r * 1.4f, dCy + wingSpan),
                strokeWidth = 3f
            )
            
            // Right motor bar
            drawLine(
                color = blueprint.primaryColor,
                start = Offset(cx, dCy),
                end = Offset(cx + r * 1.4f, dCy),
                strokeWidth = 4f
            )
            // Right wing rotors
            drawLine(
                color = blueprint.glowColor,
                start = Offset(cx + r * 1.4f, dCy - wingSpan),
                end = Offset(cx + r * 1.4f, dCy + wingSpan),
                strokeWidth = 3f
            )
            
            // Emitter dome
            drawCircle(
                color = blueprint.glowColor,
                radius = r * 0.25f,
                center = Offset(cx, dCy + r * 0.1f)
            )
        }
    }
}
