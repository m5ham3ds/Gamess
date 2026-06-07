package com.example.game

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.gestures.*
import androidx.compose.ui.input.pointer.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Adjust
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.player.PlayerState
import com.example.game.player.Direction
import com.example.game.enemy.Enemy
import com.example.game.enemy.EnemyType
import com.example.ui.theme.*

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val gameState by viewModel.gameState.collectAsState()
    val currentRegion by viewModel.currentRegion.collectAsState()
    val player by viewModel.player.collectAsState()
    val enemies by viewModel.enemies.collectAsState()
    val projectiles by viewModel.projectiles.collectAsState()
    val shards by viewModel.shards.collectAsState()
    val platforms by viewModel.platforms.collectAsState()
    val relic by viewModel.relic.collectAsState()
    val particles by viewModel.particles.collectAsState()
    val isSlashing by viewModel.isSlashing.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(VoidPrimary)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        when (gameState) {
            GameState.MENU -> {
                GameMenuScreen(
                    onStart = { viewModel.startNewGame() },
                    onChroniclesClick = { viewModel.openChronicles() }
                )
            }
            GameState.PLAYING -> {
                GamePlayScreen(
                    viewModel = viewModel,
                    player = player,
                    region = currentRegion,
                    platforms = platforms,
                    enemies = enemies,
                    projectiles = projectiles,
                    shards = shards,
                    relic = relic,
                    particles = particles,
                    isSlashing = isSlashing
                )
            }
            GameState.SHOP -> {
                ShopScreen(
                    player = player,
                    onUpgradeHp = { viewModel.upgrade("hp") },
                    onUpgradeDamage = { viewModel.upgrade("damage") },
                    onUpgradeEnergy = { viewModel.upgrade("energy") },
                    onNextLevel = { viewModel.proceedToNextLevel() }
                )
            }
            GameState.ORACLE_CONVERSATION -> {
                OracleConversationScreen(
                    viewModel = viewModel,
                    onClose = { viewModel.resumeGame() }
                )
            }
            GameState.GAME_OVER -> {
                GameOverScreen(
                    score = player.score,
                    onRestart = { viewModel.startNewGame() }
                )
            }
            GameState.VICTORY -> {
                VictoryScreen(
                    score = player.score,
                    onRestart = { viewModel.startNewGame() }
                )
            }
            GameState.PAUSED -> {
                GamePauseScreen(
                    onResume = { viewModel.resumeGame() },
                    onMainMenu = { viewModel.backToMainMenu() }
                )
            }
            GameState.CHRONICLES -> {
                ChroniclesScreen(
                    onClose = { viewModel.closeChronicles() }
                )
            }
            GameState.MEMORY_TREE -> {
                MemoryTreeScreen(
                    viewModel = viewModel,
                    player = player,
                    onClose = { viewModel.closeMemoryTree() }
                )
            }
        }
    }
}

@Composable
fun GamePlayScreen(
    viewModel: GameViewModel,
    player: PlayerState,
    region: GameRegion,
    platforms: List<Platform>,
    enemies: List<Enemy>,
    projectiles: List<Projectile>,
    shards: List<CoreShard>,
    relic: OracleRelic?,
    particles: List<Particle>,
    isSlashing: Boolean
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    Box(modifier = Modifier.fillMaxSize()) {
        
        // --- RENDERING CANVAS ---
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cw = size.width
            val ch = size.height

            // Full Screen Background to prevent any empty space
            drawRect(Color(region.bgHex), size = Size(cw, ch))

            // Calculate camera to center player
            val virtualScale = ch / 800f // height acts as base scale for 2D platformer
            var cameraX = player.x * virtualScale - cw / 2f
            var cameraY = player.y * virtualScale - ch / 2f

            // Clamp camera
            val maxCamX = (viewModel.worldWidth * virtualScale) - cw
            val maxCamY = (viewModel.worldHeight * virtualScale) - ch
            cameraX = cameraX.coerceIn(0f, maxCamX.coerceAtLeast(0f))
            cameraY = cameraY.coerceIn(0f, maxCamY.coerceAtLeast(0f))

            translate(left = -cameraX, top = -cameraY) {
                val now = System.currentTimeMillis()
                
                // Content Background Depth (just covering the playable bounds)
                drawRect(Color(region.bgHex), size = Size(viewModel.worldWidth * virtualScale, viewModel.worldHeight * virtualScale))
                
                // Draw decorative background pillars and gears using parallax-like spacing
                for (i in 0..15) {
                    val pX = i * 250f * virtualScale
                    
                    // Gear
                    val gearY = (i % 3) * 300f + 200f
                    drawCircle(Color.Gray.copy(alpha=0.15f), 100f * virtualScale, Offset(pX + 30f*virtualScale, gearY * virtualScale), style = Stroke(width = 20f * virtualScale))
                    for (j in 0..7) {
                        val angle = Math.toRadians((j * 45).toDouble()) + (now / 2000.0) * (if(i%2==0) 1 else -1)
                        val gx = (pX + 30f*virtualScale) + Math.cos(angle).toFloat() * 110f * virtualScale
                        val gy = (gearY * virtualScale) + Math.sin(angle).toFloat() * 110f * virtualScale
                        drawCircle(Color.Gray.copy(alpha=0.15f), 15f * virtualScale, Offset(gx, gy))
                    }

                    // Pillar
                    drawRect(
                        color = Color(0xFF1B2329).copy(alpha = 0.8f),
                        topLeft = Offset(pX, 0f),
                        size = Size(60f * virtualScale, viewModel.worldHeight * virtualScale)
                    )
                    // Pillar shadows
                    drawRect(Color.Black.copy(alpha=0.4f), Offset(pX, 0f), Size(10f*virtualScale, viewModel.worldHeight * virtualScale))
                    drawRect(Color.Black.copy(alpha=0.4f), Offset(pX + 50f*virtualScale, 0f), Size(10f*virtualScale, viewModel.worldHeight * virtualScale))
                    
                    // Runic glowing symbols on pillars
                    val runeY = (i % 4) * 200f + 100f
                    val rA = ((Math.sin(now/800.0 + i) + 1)/2).toFloat()
                    drawLine(Color(0xFF00E5FF).copy(alpha=0.4f + 0.4f*rA), Offset(pX+20f*virtualScale, runeY*virtualScale), Offset(pX+40f*virtualScale, runeY*virtualScale+20f*virtualScale), strokeWidth = 3f*virtualScale)
                    drawLine(Color(0xFF00E5FF).copy(alpha=0.4f + 0.4f*rA), Offset(pX+40f*virtualScale, runeY*virtualScale+20f*virtualScale), Offset(pX+20f*virtualScale, runeY*virtualScale+50f*virtualScale), strokeWidth = 3f*virtualScale)
                    drawLine(Color(0xFF00E5FF).copy(alpha=0.4f + 0.4f*rA), Offset(pX+40f*virtualScale, runeY*virtualScale+50f*virtualScale), Offset(pX+20f*virtualScale, runeY*virtualScale+70f*virtualScale), strokeWidth = 3f*virtualScale)
                }

                // Draw CENTRAL PORTAL
                val portalX = viewModel.worldWidth * 0.5f * virtualScale
                val portalY = (viewModel.worldHeight - 50f) * virtualScale // Resting on floor level
                
                // Portal Arch
                val archWidth = 350f * virtualScale
                val archHeight = 450f * virtualScale
                val rect = androidx.compose.ui.geometry.Rect(portalX - archWidth/2f, portalY - archHeight, portalX + archWidth/2f, portalY)
                val path = Path().apply {
                    moveTo(rect.left, rect.bottom)
                    lineTo(rect.left, rect.top + archWidth/2f)
                    arcTo(androidx.compose.ui.geometry.Rect(rect.left, rect.top, rect.right, rect.top + archWidth), 180f, 180f, false)
                    lineTo(rect.right, rect.bottom)
                    close()
                }
                drawPath(path, Color(0xFF283640), style = Stroke(width = 40f*virtualScale))
                drawPath(path, Color(0xFF141F25), style = Stroke(width = 15f*virtualScale))
                // Portal base stand
                drawRect(Color(0xFF141F25), Offset(portalX - archWidth/2f - 30f*virtualScale, portalY - 20f*virtualScale), Size(archWidth + 60f*virtualScale, 20f*virtualScale))
                
                // Portal Swirl
                for (i in 1..5) {
                    val radius = (archWidth / 2f - 20f*virtualScale) * (1f - (i * 0.15f))
                    val alphaMod = (Math.sin(now / 400.0 + i) + 1f) / 2f
                    drawCircle(Color(0xFF00E5FF).copy(alpha = 0.2f + 0.4f*alphaMod.toFloat()), radius, Offset(portalX, portalY - archHeight/2f - 30f*virtualScale))
                }
                drawCircle(Color.White, 30f * virtualScale, Offset(portalX, portalY - archHeight/2f - 30f*virtualScale))

                // Draw Platforms
                platforms.forEach { plt ->
                    // Stone block
                    drawRect(
                        color = Color(0xFF323B44), 
                        topLeft = Offset(plt.x * virtualScale, plt.y * virtualScale),
                        size = Size(plt.width * virtualScale, plt.height * virtualScale)
                    )
                    // Inner shadow for depth
                    drawRect(
                        color = Color(0xFF11171B), 
                        topLeft = Offset(plt.x * virtualScale, plt.y * virtualScale + plt.height/2f*virtualScale),
                        size = Size(plt.width * virtualScale, plt.height/2f * virtualScale)
                    )
                    // Top glowing edge
                    drawRect(
                        color = Color(0xFF678292).copy(alpha = 0.8f),
                        topLeft = Offset(plt.x * virtualScale, plt.y * virtualScale),
                        size = Size(plt.width * virtualScale, 4f * virtualScale)
                    )
                    // Side columns
                    for(px in 10..plt.width.toInt() step 40) {
                        drawRect(Color(0xFF1B2329), Offset((plt.x + px)*virtualScale, plt.y*virtualScale + 4f*virtualScale), Size(8f*virtualScale, plt.height*virtualScale - 4f*virtualScale))
                    }
                }

                // Draw Oracle Relic
                relic?.let { r ->
                    val rx = r.x * virtualScale
                    val ry = r.y * virtualScale
                    val rr = r.radius * virtualScale
                    val color = if (r.isUsed) ShadowGradient else BlightGold
                    drawCircle(color.copy(alpha = 0.3f), rr * 2.2f, Offset(rx, ry))
                    drawCircle(color, rr, Offset(rx, ry), style = Stroke(width = 2.5f))
                    drawCircle(if (r.isUsed) VoidPrimary else RadianceWhite, 12f * virtualScale, Offset(rx, ry))
                }

                // Draw Projectiles
                projectiles.forEach { proj ->
                    val px = proj.x * virtualScale
                    val py = proj.y * virtualScale
                    val color = if (proj.isPlayerOwned) EchoesBlue else VitalityRed
                    drawRect(color, Offset(px - proj.radius, py - proj.radius), Size(proj.radius * 2 * virtualScale, proj.radius * 2 * virtualScale)) // Sharp squares
                }

                // Draw Particles
                particles.forEach { part ->
                    val px = part.x * virtualScale
                    val py = part.y * virtualScale
                    drawRect(part.color.copy(alpha = part.alpha), Offset(px, py), Size(part.size * virtualScale, part.size * virtualScale)) // Ash squares
                }

                // Draw Enemies
                enemies.forEach { enemy ->
                    val ex = enemy.x * virtualScale
                    val ey = enemy.y * virtualScale
                    val er = enemy.radius * virtualScale

                    val edirMult = if(enemy.direction == Direction.LEFT) -1f else 1f

                    when (enemy.type) {
                        EnemyType.SHADOW_STALKER -> {
                            // Body (Carapace)
                            val bodyPath = Path().apply {
                                moveTo(ex - er*1.2f, ey)
                                quadraticBezierTo(ex, ey - er*2f, ex + er*1.2f, ey)
                                quadraticBezierTo(ex, ey + er*0.8f, ex - er*1.2f, ey)
                                close()
                            }
                            drawPath(bodyPath, SurfaceContainer)
                            drawPath(bodyPath, SurfaceDark, style = Stroke(width = 2f*virtualScale))
                            
                            // Head / Mandibles
                            drawOval(VoidPrimary, Offset(ex + edirMult*er*0.8f - er*0.6f, ey - er*0.6f), Size(er*1.2f, er*1.2f))
                            
                            // Glowing eye
                            drawOval(VitalityRed, Offset(ex + edirMult*er*1.2f, ey - er*0.2f), Size(4f * virtualScale, 4f * virtualScale))
                        }
                        EnemyType.ABYSSAL_ORB -> {
                            val tOffset = (now % 1000) / 1000f * Math.PI * 2
                            for(i in 0..3) {
                                val angle = i * Math.PI / 2 + tOffset
                                val tx = ex + Math.cos(angle).toFloat() * er * 1.5f
                                val ty = ey + Math.sin(angle).toFloat() * er * 1.5f
                                drawLine(ShadowGradient, Offset(ex, ey), Offset(tx.toFloat(), ty.toFloat()), strokeWidth = 3f*virtualScale)
                            }
                            // Body core
                            drawOval(VitalityRed.copy(alpha = 0.3f), Offset(ex - er*1.5f, ey - er*1.5f), Size(er * 3f, er * 3f))
                            drawOval(ShadowGradient, Offset(ex - er, ey - er), Size(er * 2, er * 2))
                            drawOval(VitalityRed, Offset(ex - er*0.8f, ey - er*0.8f), Size(er * 1.6f, er * 1.6f), style = Stroke(width = 2f))
                        }
                        EnemyType.VOID_DEVOURER -> {
                            val bodyPath = Path().apply {
                                moveTo(ex - er, ey + er)
                                lineTo(ex - er*1.2f, ey - er*0.5f)
                                lineTo(ex, ey - er*1.5f)
                                lineTo(ex + er*1.2f, ey - er*0.5f)
                                lineTo(ex + er, ey + er)
                                close()
                            }
                            drawPath(bodyPath, VitalityRed.copy(alpha = 0.4f))
                            drawPath(bodyPath, VoidPrimary, style=Stroke(width = 2f))
                        }
                        EnemyType.NEON_SPIDER -> {
                            // Bright yellowish-orange electronic crawlers
                            drawRect(BlightGold, Offset(ex - er, ey - er/2f), Size(er * 2, er))
                            drawRect(EchoesBlue, Offset(ex - 4f * virtualScale, ey - er/2f), Size(8f * virtualScale, 8f * virtualScale))
                        }
                        EnemyType.CHRONO_SENTINEL -> {
                            // Flying chrono sentinels (with clockwork or sonic rings)
                            drawCircle(Color(0xFFE0E0E0), er, Offset(ex, ey), style = Stroke(width = 3f * virtualScale))
                            drawCircle(BlightGold, er * 0.4f, Offset(ex, ey))
                        }
                    }

                    // Enemy mini-HP bar
                    val hpRatio = (enemy.hp / enemy.maxHp).coerceIn(0f, 1f)
                    drawRect(SurfaceDark, Offset(ex - er, ey - er - 14f), Size(er * 2, 4f))
                    drawRect(VitalityRed, Offset(ex - er, ey - er - 14f), Size((er * 2) * hpRatio, 4f))
                }

                // Draw Player (The Silent Wanderer)
                val px = player.x * virtualScale
                val py = player.y * virtualScale
                val pr = player.radius * virtualScale
                val dirMult = if (player.direction == Direction.LEFT) -1f else 1f

                if (player.soulShieldActive) {
                    drawRect(RadianceWhite.copy(alpha = 0.25f), Offset(px - pr * 2.1f, py - pr * 2.1f), Size(pr * 4.2f, pr * 4.2f))
                    drawRect(RadianceWhite, Offset(px - pr * 2.1f, py - pr * 2.1f), Size(pr * 4.2f, pr * 4.2f), style = Stroke(width = 1f))
                }

                // Legs & Feet
                drawOval(Color(0xFF111111), Offset(px - 7f*virtualScale, py + pr * 0.5f), Size(5f*virtualScale, pr * 1.5f))
                drawOval(Color(0xFF111111), Offset(px + 2f*virtualScale, py + pr * 0.5f), Size(5f*virtualScale, pr * 1.5f))
                // Feet
                drawOval(Color(0xFF000000), Offset(px - 9f*virtualScale + (dirMult*2f*virtualScale), py + pr * 1.8f), Size(8f*virtualScale, 4f*virtualScale))
                drawOval(Color(0xFF000000), Offset(px + (dirMult*2f*virtualScale), py + pr * 1.8f), Size(8f*virtualScale, 4f*virtualScale))

                // Back Arm
                drawOval(Color(0xFF0A0A0A), Offset(px - pr * 0.8f, py), Size(6f*virtualScale, pr * 1.1f))
                drawOval(Color(0xFFEEEEEE), Offset(px - pr * 0.8f + (dirMult*1f*virtualScale), py + pr*0.9f), Size(5f*virtualScale, 5f*virtualScale))

                // Coat (Trench coat)
                val coatPath = Path().apply {
                    moveTo(px - pr * 0.8f, py + pr * 1.2f)
                    lineTo(px - pr * 0.5f, py - pr * 0.2f)
                    lineTo(px + pr * 0.5f, py - pr * 0.2f)
                    lineTo(px + pr * 0.8f, py + pr * 1.2f)
                    lineTo(px, py + pr * 0.8f)
                    close()
                }
                drawPath(coatPath, VoidPrimary)
                drawPath(coatPath, SurfaceContainer, style = Stroke(width = 1f))

                // Scarf / Collar
                drawOval(SurfaceDark, Offset(px - pr * 0.5f, py - pr * 0.5f), Size(pr, pr * 0.6f))

                // Mask (White, pointed chin)
                val maskPath = Path().apply {
                    moveTo(px - pr * 0.6f, py - pr * 1.0f)
                    lineTo(px + pr * 0.6f, py - pr * 1.0f)
                    lineTo(px + pr * 0.4f, py - pr * 0.2f)
                    lineTo(px, py + pr * 0.1f)
                    lineTo(px - pr * 0.4f, py - pr * 0.2f)
                    close()
                }
                drawPath(maskPath, RadianceWhite)

                // Glowing Eyes
                val eyeOffset = dirMult * 2f * virtualScale
                drawOval(VoidPrimary, Offset(px - 4f*virtualScale + eyeOffset, py - pr*0.7f), Size(3f*virtualScale, 2f*virtualScale))
                drawOval(VoidPrimary, Offset(px + 1f*virtualScale + eyeOffset, py - pr*0.7f), Size(3f*virtualScale, 2f*virtualScale))
                drawOval(RadianceWhite, Offset(px - 3.5f*virtualScale + eyeOffset, py - pr*0.65f), Size(2f*virtualScale, 1f*virtualScale))
                drawOval(RadianceWhite, Offset(px + 1.5f*virtualScale + eyeOffset, py - pr*0.65f), Size(2f*virtualScale, 1f*virtualScale))

                // Hat
                drawOval(VoidPrimary, Offset(px - pr*1.2f, py - pr*1.2f), Size(pr*2.4f, pr*0.3f))
                drawOval(SurfaceContainer, Offset(px - pr*1.2f, py - pr*1.2f), Size(pr*2.4f, pr*0.3f), style = Stroke(width = 1f))
                val hatTopPath = Path().apply {
                    moveTo(px - pr*0.5f, py - pr*1.1f)
                    lineTo(px - pr*0.4f, py - pr*1.6f)
                    lineTo(px + pr*0.4f, py - pr*1.6f)
                    lineTo(px + pr*0.5f, py - pr*1.1f)
                    close()
                }
                drawPath(hatTopPath, VoidPrimary)
                drawPath(hatTopPath, SurfaceContainer, style = Stroke(width = 1f))
                
                // Front Arm
                drawOval(Color(0xFF222222), Offset(px + pr * 0.2f, py - pr*0.2f), Size(7f*virtualScale, pr * 1.3f))
                drawOval(Color(0xFFEEEEEE), Offset(px + pr * 0.2f + (dirMult*2f*virtualScale), py + pr*0.8f), Size(6f*virtualScale, 6f*virtualScale))

                // Weapon (Sword/Nail)
                val swordHiltX = px + (dirMult * pr * 1.2f)
                val swordHiltY = py + pr * 1.0f

                rotate(if (player.direction == Direction.LEFT) -60f else 60f, Offset(swordHiltX, swordHiltY)) {
                    // Hilt
                    drawRect(Color(0xFF333333), Offset(swordHiltX - 3f*virtualScale, swordHiltY - 8f*virtualScale), Size(6f*virtualScale, 16f*virtualScale))
                    drawRect(Color(0xFF111111), Offset(swordHiltX - 8f*virtualScale, swordHiltY - 8f*virtualScale), Size(16f*virtualScale, 4f*virtualScale))
                    
                    // Blade
                    val bladePath = Path().apply {
                        moveTo(swordHiltX - 4f*virtualScale, swordHiltY - 8f*virtualScale)
                        lineTo(swordHiltX, swordHiltY - 45f*virtualScale)
                        lineTo(swordHiltX + 4f*virtualScale, swordHiltY - 8f*virtualScale)
                        close()
                    }
                    drawPath(bladePath, RadianceWhite)
                    drawPath(bladePath, OutlineGray, style=Stroke(width = 2f))
                    
                    drawLine(OutlineGray, Offset(swordHiltX, swordHiltY - 8f*virtualScale), Offset(swordHiltX, swordHiltY - 40f*virtualScale), strokeWidth = 1f*virtualScale)
                }

                if (isSlashing) {
                    val angleOffset = when (player.direction) {
                        Direction.LEFT -> 180f
                        Direction.RIGHT -> 0f
                    }
                    rotate(degrees = angleOffset, pivot = Offset(px, py)) {
                        drawArc(
                            color = RadianceWhite.copy(alpha = 0.8f),
                            startAngle = -45f, sweepAngle = 90f, useCenter = false,
                            topLeft = Offset(px + pr * 0.3f, py - pr * 2.2f),
                            size = Size(pr * 4f, pr * 4.4f),
                            style = Stroke(width = 6f)
                        )
                    }
                }
            }
        }

        // --- HUD / OVERLAYS ---
        Box(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 16.dp)) {
            
            // Top Center - Pause Button
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                IconButton(onClick = { viewModel.pauseGame() }) {
                    Icon(Icons.Filled.Settings, tint = RadianceWhite, contentDescription = "Pause / Settings")
                }
            }

            // Top Left Info
            Column(modifier = Modifier.align(Alignment.TopStart)) {
                // VITALITY
                Text("VITALITY", fontSize = 10.sp, color = RadianceWhite.copy(alpha=0.7f), letterSpacing = 2.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Box(modifier = Modifier.width(140.dp).height(8.dp).background(Color.Transparent).border(1.dp, VitalityRed.copy(alpha=0.8f), RoundedCornerShape(4.dp)), contentAlignment = Alignment.CenterStart) {
                   Box(modifier = Modifier.fillMaxHeight().fillMaxWidth((player.hp / player.maxHp).coerceIn(0f, 1f)).clip(RoundedCornerShape(4.dp)).background(VitalityRed))
                   Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.SpaceEvenly) {
                       repeat(10) { Box(modifier = Modifier.fillMaxHeight().width(1.dp).background(Color.Black.copy(alpha=0.4f))) }
                   }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // SOUL
                Text("SOUL", fontSize = 10.sp, color = RadianceWhite.copy(alpha=0.7f), letterSpacing = 2.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Box(modifier = Modifier.width(140.dp).height(8.dp).background(Color.Transparent).border(1.dp, EchoesBlue.copy(alpha=0.8f), RoundedCornerShape(4.dp)), contentAlignment = Alignment.CenterStart) {
                   Box(modifier = Modifier.fillMaxHeight().fillMaxWidth((player.energy / player.maxEnergy).coerceIn(0f, 1f)).clip(RoundedCornerShape(4.dp)).background(EchoesBlue))
                   Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.SpaceEvenly) {
                       repeat(10) { Box(modifier = Modifier.fillMaxHeight().width(1.dp).background(Color.Black.copy(alpha=0.4f))) }
                   }
                }

                Spacer(modifier = Modifier.height(8.dp))
                
                // FORGETFULNESS METER (FM)
                Text("FORGETFULNESS", fontSize = 10.sp, color = RadianceWhite.copy(alpha=0.7f), letterSpacing = 2.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Box(modifier = Modifier.width(140.dp).height(6.dp).background(Color.Transparent).border(1.dp, Color(0xFF9050C0).copy(alpha=0.8f), RoundedCornerShape(3.dp)), contentAlignment = Alignment.CenterStart) {
                   Box(modifier = Modifier.fillMaxHeight().fillMaxWidth((player.forgetfulness / 100f).coerceIn(0f, 1f)).clip(RoundedCornerShape(3.dp)).background(Color(0xFFB470E0)))
                }

                Spacer(modifier = Modifier.height(12.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("LEVEL ${player.level}", fontSize = 12.sp, fontWeight = FontWeight.Normal, color = RadianceWhite, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(modifier = Modifier.size(16.dp).background(BlightGold, CircleShape).border(1.dp, VoidPrimary, CircleShape), contentAlignment = Alignment.Center) {
                        Text("🪙", fontSize = 8.sp)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("${player.currency}", fontSize = 14.sp, color = BlightGold)
                }
            }

            // Top Right Info
            Column(modifier = Modifier.align(Alignment.TopEnd), horizontalAlignment = Alignment.End) {
                Text("ECHOES (SCORE)", fontSize = 10.sp, color = RadianceWhite.copy(alpha=0.8f), letterSpacing = 2.sp)
                Text("${player.score}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = RadianceWhite)
                if (player.soulShieldActive) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("🛡️ SHIELDED", fontSize = 10.sp, color = EchoesBlue, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("MEMORY FRAGMENTS", fontSize = 10.sp, color = Color(0xFFB470E0).copy(alpha=0.8f), letterSpacing = 2.sp)
                Text("${player.memoryFragments}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB470E0))
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { viewModel.openMemoryTree() },
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark),
                    shape = RoundedCornerShape(0.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    modifier = Modifier.border(1.dp, Color(0xFFB470E0)).height(28.dp)
                ) {
                    Text("RECALL MEMORIES", color = Color(0xFFB470E0), fontSize = 10.sp)
                }
            }

            // Bottom Left Controls
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF5A524A))
                    .border(3.dp, Color(0xFF38302A), RoundedCornerShape(12.dp))
                    .padding(8.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    HoldButton(
                        icon = { Icon(Icons.Filled.Visibility, contentDescription = "Interact", modifier = Modifier.size(24.dp), tint = BlightGold) },
                        onTick = { if (it) viewModel.onOracleInteract() },
                        size = 80.dp, height = 36.dp,
                        isStoned = true
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        HoldButton(
                            icon = { Icon(Icons.Filled.ArrowBack, contentDescription = "Left", modifier = Modifier.size(24.dp), tint = BlightGold) },
                            onTick = { viewModel.movingLeft = it },
                            size = 36.dp, height = 36.dp, isStoned = true
                        )
                        HoldButton(
                            icon = { Icon(Icons.Filled.ArrowForward, contentDescription = "Right", modifier = Modifier.size(24.dp), tint = BlightGold) },
                            onTick = { viewModel.movingRight = it },
                            size = 36.dp, height = 36.dp, isStoned = true
                        )
                    }
                }
            }

            // Bottom Right Controls
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF5A524A))
                    .border(3.dp, Color(0xFF38302A), RoundedCornerShape(12.dp))
                    .padding(8.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    ActionButton(
                        icon = { Icon(Icons.Filled.Star, contentDescription = "Memory", modifier = Modifier.size(20.dp), tint = Color(0xFFB470E0)) },
                        activeColor = Color(0xFFB470E0),
                        onClick = { viewModel.useMemoryPower() },
                        isStoned = true,
                        size = 40.dp
                    )
                    ActionButton(
                        icon = { Box(modifier = Modifier.size(20.dp), contentAlignment = Alignment.Center) { 
                            Text("🗡️", fontSize = 16.sp) 
                        }},
                        activeColor = VitalityRed,
                        onClick = { viewModel.onSlashAttack() },
                        isStoned = true,
                        size = 40.dp
                    )
                    ActionButton(
                        icon = { Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Jump", modifier = Modifier.size(24.dp), tint = RadianceWhite) },
                        activeColor = OutlineGray,
                        onClick = { viewModel.handleJump() },
                        isStoned = true,
                        size = 40.dp
                    )
                    ActionButton(
                        icon = { Icon(Icons.Filled.FlashOn, contentDescription = "Dash", modifier = Modifier.size(20.dp), tint = EchoesBlue) },
                        activeColor = EchoesBlue,
                        onClick = { viewModel.onDashAction() },
                        isStoned = true,
                        size = 40.dp
                    )
                }
            }
        }
    }
}

@Composable
fun HoldButton(
    icon: @Composable () -> Unit, 
    onTick: (Boolean) -> Unit, 
    size: androidx.compose.ui.unit.Dp = 64.dp,
    height: androidx.compose.ui.unit.Dp? = null,
    isStoned: Boolean = false
) {
    var isPressed by remember { mutableStateOf(false) }

    val finalHeight = height ?: size
    val bgColor = if (isStoned) (if (isPressed) Color(0xFF5B4A3B) else Color(0xFF4A3A2C)) else (if (isPressed) RadianceWhite else SurfaceDark.copy(alpha=0.6f))
    val brdColor = if (isStoned) Color(0xFF201A13) else (if(isPressed) RadianceWhite else OutlineGray.copy(alpha=0.5f))
    val radius = if (isStoned) 8.dp else 0.dp

    Box(
        modifier = Modifier
            .size(width = size, height = finalHeight)
            .clip(RoundedCornerShape(radius))
            .background(bgColor)
            .border(3.dp, brdColor, RoundedCornerShape(radius))
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        val pressed = event.changes.any { it.pressed }
                        if (pressed != isPressed) {
                            isPressed = pressed
                            onTick(pressed)
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        if (isStoned) {
            Box(modifier = Modifier.fillMaxSize().padding(4.dp).border(1.dp, Color(0xFF30251A), RoundedCornerShape(radius - 4.dp)))
            Box(modifier = Modifier.align(Alignment.TopStart).padding(6.dp).size(4.dp).background(Color(0xFF2C2218), CircleShape))
            Box(modifier = Modifier.align(Alignment.TopEnd).padding(6.dp).size(4.dp).background(Color(0xFF2C2218), CircleShape))
            Box(modifier = Modifier.align(Alignment.BottomStart).padding(6.dp).size(4.dp).background(Color(0xFF2C2218), CircleShape))
            Box(modifier = Modifier.align(Alignment.BottomEnd).padding(6.dp).size(4.dp).background(Color(0xFF2C2218), CircleShape))
        }

        icon()
    }
}

@Composable
fun ActionButton(
    icon: @Composable () -> Unit, 
    activeColor: Color, 
    size: androidx.compose.ui.unit.Dp = 84.dp, 
    onClick: () -> Unit,
    isStoned: Boolean = false
) {
    var isPressed by remember { mutableStateOf(false) }
    
    val bgColor = if (isStoned) (if (isPressed) Color(0xFF3F362F) else Color(0xFF2A231C)) else (if (isPressed) activeColor else SurfaceDark.copy(alpha=0.6f))
    val radius = if (isStoned) 16.dp else 0.dp

    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(radius))
            .background(bgColor)
            .border(3.dp, if(isPressed) activeColor else activeColor.copy(alpha=0.4f), RoundedCornerShape(radius))
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        val pressed = event.changes.any { it.pressed }
                        if (pressed && !isPressed) {
                            isPressed = true
                            onClick()
                        } else if (!pressed && isPressed) {
                            isPressed = false
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        if (isStoned) {
            Box(modifier = Modifier.fillMaxSize().padding(6.dp).border(2.dp, Color(0xFF1B1611), RoundedCornerShape(radius - 6.dp)))
        }

        CompositionLocalProvider(androidx.compose.material3.LocalContentColor provides if(isPressed) VoidPrimary else activeColor) {
             icon()
        }
    }
}

@Composable
fun ShopScreen(
    player: PlayerState,
    onUpgradeHp: () -> Unit,
    onUpgradeDamage: () -> Unit,
    onUpgradeEnergy: () -> Unit,
    onNextLevel: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        AshParticles()
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("ملاذ الروح المتجول", fontSize = 32.sp, color = BlightGold, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Text("🪙 ECHOES: ${player.currency}", fontSize = 16.sp, color = RadianceWhite)
            Spacer(modifier = Modifier.height(32.dp))

            Button(onClick = onUpgradeHp, modifier = Modifier.fillMaxWidth(0.8f).height(60.dp).border(1.dp, OutlineGray), shape = RectangleShape, colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark)) {
                Text("VITALITY UPGRADE - COST: 30", color = VitalityRed, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onUpgradeDamage, modifier = Modifier.fillMaxWidth(0.8f).height(60.dp).border(1.dp, OutlineGray), shape = RectangleShape, colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark)) {
                Text("NAIL SHARPENING - COST: 40", color = RadianceWhite, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onUpgradeEnergy, modifier = Modifier.fillMaxWidth(0.8f).height(60.dp).border(1.dp, OutlineGray), shape = RectangleShape, colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark)) {
                Text("SOUL EXPANSION - COST: 20", color = EchoesBlue, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(64.dp))
            Button(
                onClick = onNextLevel,
                modifier = Modifier.size(240.dp, 60.dp).border(1.dp, ShadowGradient),
                shape = RectangleShape,
                colors = ButtonDefaults.buttonColors(containerColor = BlightGold, contentColor = VoidPrimary)
            ) {
                Text("DESCEND DEEPER ▶", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun GamePauseScreen(onResume: () -> Unit, onMainMenu: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(Color(0xD00A0F14))) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("PAUSED", fontSize = 48.sp, color = RadianceWhite, letterSpacing = 4.sp)
            Spacer(modifier = Modifier.height(48.dp))
            Button(onClick = onResume, shape = RectangleShape, modifier = Modifier.border(1.dp, BlightGold).width(200.dp), colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark, contentColor = BlightGold)) {
                Text("RESUME", fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onMainMenu, shape = RectangleShape, modifier = Modifier.border(1.dp, OutlineGray).width(200.dp), colors = ButtonDefaults.buttonColors(containerColor = VoidPrimary, contentColor = RadianceWhite)) {
                Text("MAIN MENU", fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun GameOverScreen(score: Int, onRestart: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        AshParticles()
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("THE VESSEL HAS SHATTERED...", fontSize = 32.sp, color = VitalityRed, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(16.dp))
            Text("ECHOES: $score", fontSize = 16.sp, color = RadianceWhite)
            Spacer(modifier = Modifier.height(48.dp))
            Button(onClick = onRestart, shape = RectangleShape, modifier = Modifier.border(1.dp, OutlineGray), colors = ButtonDefaults.buttonColors(containerColor = VoidPrimary, contentColor = RadianceWhite)) {
                Text("AWAKEN ANEW", fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun VictoryScreen(score: Int, onRestart: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        AshParticles()
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("THE CORE IS CLEANSED", fontSize = 38.sp, color = BlightGold, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(16.dp))
            Text("You have traversed the deep oblivion.", fontSize = 16.sp, color = OnSurfaceLight, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(8.dp))
            Text("FINAL ECHOES: $score", fontSize = 18.sp, color = RadianceWhite)
            Spacer(modifier = Modifier.height(48.dp))
            Button(onClick = onRestart, shape = RectangleShape, modifier = Modifier.border(1.dp, BlightGold), colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark, contentColor = BlightGold)) {
                Text("BEGIN NEW JOURNEY", fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun OracleConversationScreen(viewModel: GameViewModel, onClose: () -> Unit) {
    val riddle by viewModel.oracleRiddle.collectAsState()
    val feedback by viewModel.oracleFeedback.collectAsState()
    val isLoading by viewModel.isOracleLoading.collectAsState()
    var answerInput by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        AshParticles()
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("🔮", fontSize = 54.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text("THE ELDRITCH ORACLE", fontSize = 24.sp, color = EchoesBlue)
            Spacer(modifier = Modifier.height(24.dp))
            
            Box(modifier = Modifier.fillMaxWidth().padding(16.dp).background(SurfaceDark).border(1.dp, OutlineGray)) {
                if (isLoading) {
                    CircularProgressIndicator(color = BlightGold, modifier = Modifier.align(Alignment.Center).padding(24.dp))
                } else {
                    Text(riddle, color = RadianceWhite, fontSize = 16.sp, modifier = Modifier.padding(16.dp), textAlign = TextAlign.Center)
                }
            }

            feedback?.let { fb ->
                Text(fb, color = if (fb.contains("مبارك") || fb.contains("أحسنت")) Color.Green else VitalityRed, fontSize = 14.sp, modifier = Modifier.padding(16.dp), textAlign = TextAlign.Center)
            }

            OutlinedTextField(
                value = answerInput,
                onValueChange = { answerInput = it },
                label = { Text("Speak thy truth...") },
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = RadianceWhite, unfocusedTextColor = OnSurfaceLight, focusedBorderColor = OutlineGray, unfocusedBorderColor = SurfaceContainer),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RectangleShape
            )

            Spacer(modifier = Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(onClick = onClose, shape = RectangleShape, modifier = Modifier.border(1.dp, SurfaceContainer), colors = ButtonDefaults.buttonColors(containerColor = VoidPrimary, contentColor = OutlineGray)) {
                    Text("DEPART")
                }
                Button(onClick = { viewModel.submitOracleAnswer(answerInput) }, shape = RectangleShape, border = BorderStroke(1.dp, OutlineGray), colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark, contentColor = RadianceWhite)) {
                    Text("OFFER ANSWER")
                }
            }
        }
    }
}

@Composable
fun GameMenuScreen(onStart: () -> Unit, onChroniclesClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        AshParticles()
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("SANCTUM OF SHADOW", fontSize = 42.sp, color = RadianceWhite, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(64.dp))
            Button(
                onClick = onStart,
                modifier = Modifier.size(240.dp, 56.dp).border(1.dp, RadianceWhite),
                shape = RectangleShape,
                colors = ButtonDefaults.buttonColors(containerColor = VoidPrimary, contentColor = RadianceWhite)
            ) {
                Text("ENTER THE VOID", fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedButton(
                onClick = onChroniclesClick,
                modifier = Modifier.size(240.dp, 50.dp),
                border = BorderStroke(1.dp, OutlineGray),
                shape = RectangleShape
            ) {
                Text("CHRONICLES", color = OutlineGray, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun AshParticles() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val time = System.currentTimeMillis()
        for (i in 0..50) {
            val x = ((Math.sin(time / 1000.0 + i) * 1000) % w).toFloat()
            val y = (h - ((time / 20.0 + i * 40) % h)).toFloat()
            val sx = if (x < 0) x + w else x
            val sy = if (y < 0) y + h else y
            drawRect(OutlineGray.copy(alpha = 0.5f), Offset(sx, sy), Size(3f, 3f))
        }
    }
}
