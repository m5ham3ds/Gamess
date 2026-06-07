package com.example.game.hud

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.GameViewModel
import com.example.game.player.PlayerState
import com.example.ui.theme.*

@Composable
fun GameHudOverlay(
    viewModel: GameViewModel,
    player: PlayerState,
    onPauseClick: () -> Unit,
    onRecallMemoriesClick: () -> Unit
) {
    val hudScale by viewModel.hudScale.collectAsState()
    val controlScale by viewModel.controlButtonScale.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // --- 1. TOP CENTER PAUSE BUTTON ---
        Box(
            modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter),
            contentAlignment = Alignment.TopCenter
        ) {
            IconButton(
                onClick = onPauseClick,
                modifier = Modifier
                    .size(44.dp)
                    .background(SurfaceDark.copy(alpha = 0.6f), CircleShape)
                    .border(1.dp, OutlineGray.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    Icons.Filled.Settings,
                    tint = RadianceWhite,
                    contentDescription = "Pause Game",
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // --- 2. TOP LEFT STATS BAR (VITALITY & SOUL & COINS) ---
        // Dynamically scaled by hudScale!
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .width(200.dp * hudScale)
                .background(SurfaceDark.copy(alpha = 0.85f), RoundedCornerShape(8.dp))
                .border(1.dp, OutlineGray.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                .padding(8.dp * hudScale)
        ) {
            // VITALITY (HP Bar)
            Text(
                text = "جَوْهَرُ الحَيَاةِ • VITALITY",
                fontSize = (8.sp.value * hudScale).sp,
                color = VitalityRed,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(2.dp * hudScale))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp * hudScale)
                    .background(Color.Black.copy(alpha = 0.4f))
                    .border(1.dp, VitalityRed.copy(alpha = 0.7f), RoundedCornerShape(2.dp)),
                contentAlignment = Alignment.CenterStart
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth((player.hp / player.maxHp).coerceIn(0f, 1f))
                        .background(VitalityRed)
                )
            }

            Spacer(modifier = Modifier.height(6.dp * hudScale))

            // SOUL / MANA BAR
            Text(
                text = "طَاقَةُ الصَّدَى • SOUL",
                fontSize = (8.sp.value * hudScale).sp,
                color = EchoesBlue,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(2.dp * hudScale))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp * hudScale)
                    .background(Color.Black.copy(alpha = 0.4f))
                    .border(1.dp, EchoesBlue.copy(alpha = 0.7f), RoundedCornerShape(2.dp)),
                contentAlignment = Alignment.CenterStart
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth((player.energy / player.maxEnergy).coerceIn(0f, 1f))
                        .background(EchoesBlue)
                )
            }

            Spacer(modifier = Modifier.height(6.dp * hudScale))

            // FORGETFULNESS METER (FM)
            Text(
                text = "عَمَقُ النِّسْيَانِ • FORGETFULNESS",
                fontSize = (7.5.sp.value * hudScale).sp,
                color = Color(0xFFB470E0),
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp * hudScale))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp * hudScale)
                    .background(Color.Black.copy(alpha = 0.4f))
                    .border(1.dp, Color(0xFFB470E0).copy(alpha = 0.6f), RoundedCornerShape(1.dp)),
                contentAlignment = Alignment.CenterStart
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth((player.forgetfulness / 100f).coerceIn(0f, 1f))
                        .background(Color(0xFFB470E0))
                )
            }

            Spacer(modifier = Modifier.height(8.dp * hudScale))

            // COINS / WALLET
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp * hudScale)
            ) {
                Box(
                    modifier = Modifier
                        .size(14.dp * hudScale)
                        .background(BlightGold, CircleShape)
                        .border(1.dp, VoidPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🪙", fontSize = (7.sp.value * hudScale).sp)
                }
                Text(
                    text = "${player.currency}",
                    fontSize = (12.sp.value * hudScale).sp,
                    color = BlightGold,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp * hudScale))
                Text(
                    text = "Lvl ${player.level}",
                    fontSize = (10.sp.value * hudScale).sp,
                    color = RadianceWhite
                )
            }
        }

        // --- 3. TOP RIGHT METRICS (ECHO SCORE & MEMORY FRAGMENTS) ---
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .background(SurfaceDark.copy(alpha = 0.85f), RoundedCornerShape(8.dp))
                .border(1.dp, Color(0xFFB470E0).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .padding(8.dp * hudScale),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                "الأصْدَاء • SCORE",
                fontSize = (8.sp.value * hudScale).sp,
                color = RadianceWhite,
                fontWeight = FontWeight.Bold
            )
            Text(
                "${player.score}",
                fontSize = (16.sp.value * hudScale).sp,
                fontWeight = FontWeight.Bold,
                color = RadianceWhite
            )

            Spacer(modifier = Modifier.height(4.dp * hudScale))

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (player.soulShieldActive) {
                    Text(
                        "🛡️ دِرْعُ الروح ",
                        fontSize = (8.sp.value * hudScale).sp,
                        color = EchoesBlue,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                    "شَظَايَا • FRAGS: ",
                    fontSize = (8.sp.value * hudScale).sp,
                    color = Color(0xFFB470E0)
                )
                Text(
                    "${player.memoryFragments}",
                    fontSize = (12.sp.value * hudScale).sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFB470E0)
                )
            }

            Spacer(modifier = Modifier.height(4.dp * hudScale))

            // Recall Memories button
            Button(
                onClick = onRecallMemoriesClick,
                colors = ButtonDefaults.buttonColors(containerColor = VoidPrimary),
                shape = RoundedCornerShape(4.dp),
                contentPadding = PaddingValues(horizontal = 6.dp * hudScale, vertical = 2.dp),
                modifier = Modifier
                    .border(1.dp, Color(0xFFB470E0), RoundedCornerShape(4.dp))
                    .height(24.dp * hudScale)
            ) {
                Text(
                    "شَجَرَة الحِجْنَة • RECALL",
                    color = Color(0xFFB470E0),
                    fontSize = (8.sp.value * hudScale).sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // --- 4. BOTTOM LEFT DPAD (MOVE CONTROLS) ---
        // Scaled by controlScale dynamically!
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF5A524A).copy(alpha = 0.9f))
                .border(3.dp, Color(0xFF38302A), RoundedCornerShape(12.dp))
                .padding(6.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp * controlScale),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Interact Button
                HoldButtonWidget(
                    icon = {
                        Icon(
                            Icons.Filled.Star,
                            contentDescription = "Interact",
                            modifier = Modifier.size(20.dp * controlScale),
                            tint = BlightGold
                        )
                    },
                    onTick = { active ->
                        viewModel.movingInteract = active
                        if (active) {
                            viewModel.onOracleInteract()
                        }
                    },
                    size = 72.dp * controlScale,
                    height = 32.dp * controlScale
                )

                // Left/Right Row
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp * controlScale)) {
                    HoldButtonWidget(
                        icon = {
                            Icon(
                                Icons.Filled.ArrowBack,
                                contentDescription = "Move Left",
                                modifier = Modifier.size(20.dp * controlScale),
                                tint = BlightGold
                            )
                        },
                        onTick = { active ->
                            viewModel.movingLeft = active
                        },
                        size = 34.dp * controlScale,
                        height = 34.dp * controlScale
                    )
                    HoldButtonWidget(
                        icon = {
                            Icon(
                                Icons.Filled.ArrowForward,
                                contentDescription = "Move Right",
                                modifier = Modifier.size(20.dp * controlScale),
                                tint = BlightGold
                            )
                        },
                        onTick = { active ->
                            viewModel.movingRight = active
                        },
                        size = 34.dp * controlScale,
                        height = 34.dp * controlScale
                    )
                }
            }
        }

        // --- 5. BOTTOM RIGHT ACTIONS PAD ---
        // Jump, Slash, Dash, Memory Power
        // Scaled by controlScale dynamically!
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF5A524A).copy(alpha = 0.9f))
                .border(3.dp, Color(0xFF38302A), RoundedCornerShape(12.dp))
                .padding(6.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp * controlScale),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Weapon Memory Power Button (Star)
                ActionButtonWidget(
                    icon = {
                        Icon(
                            Icons.Filled.Star,
                            contentDescription = "Memory Power",
                            modifier = Modifier.size(18.dp * controlScale),
                            tint = Color(0xFFB470E0)
                        )
                    },
                    activeColor = Color(0xFFB470E0),
                    onClick = {
                        viewModel.useMemoryPower()
                    },
                    size = 36.dp * controlScale
                )

                // Slash Attack Button (Sword Symbol)
                ActionButtonWidget(
                    icon = {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                "🗡️",
                                fontSize = (14.sp.value * controlScale).sp
                            )
                        }
                    },
                    activeColor = VitalityRed,
                    onClick = {
                        viewModel.onSlashAttack()
                    },
                    size = 36.dp * controlScale
                )

                // Jump Button (Up Arrow)
                ActionButtonWidget(
                    icon = {
                        Icon(
                            Icons.Filled.KeyboardArrowUp,
                            contentDescription = "Jump",
                            modifier = Modifier.size(20.dp * controlScale),
                            tint = RadianceWhite
                        )
                    },
                    activeColor = OutlineGray,
                    onClick = {
                        viewModel.handleJump()
                    },
                    size = 36.dp * controlScale
                )

                // Dash Action Button (Lightning Bolt)
                ActionButtonWidget(
                    icon = {
                        Icon(
                            Icons.Filled.Refresh,
                            contentDescription = "Dash",
                            modifier = Modifier.size(18.dp * controlScale),
                            tint = EchoesBlue
                        )
                    },
                    activeColor = EchoesBlue,
                    onClick = {
                        viewModel.onDashAction()
                    },
                    size = 36.dp * controlScale
                )
            }
        }
    }
}

@Composable
fun HoldButtonWidget(
    icon: @Composable () -> Unit,
    onTick: (Boolean) -> Unit,
    size: Dp,
    height: Dp
) {
    var isPressed by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .size(width = size, height = height)
            .clip(RoundedCornerShape(6.dp))
            .background(if (isPressed) Color(0xFF5B4A3B) else Color(0xFF4A3A2C))
            .border(2.dp, Color(0xFF201A13), RoundedCornerShape(6.dp))
            .pointerInput(onTick) {
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
        // Inner stone button details to give high quality feeling
        Box(modifier = Modifier.fillMaxSize().padding(2.dp).border(1.dp, Color(0xFF30251A), RoundedCornerShape(4.dp)))
        icon()
    }
}

@Composable
fun ActionButtonWidget(
    icon: @Composable () -> Unit,
    activeColor: Color,
    onClick: () -> Unit,
    size: Dp
) {
    var isPressed by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isPressed) Color(0xFF3F362F) else Color(0xFF2A231C))
            .border(2.dp, if (isPressed) activeColor else activeColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            .pointerInput(onClick) {
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
        Box(modifier = Modifier.fillMaxSize().padding(3.dp).border(1.5.dp, Color(0xFF1B1611), RoundedCornerShape(5.dp)))
        CompositionLocalProvider(LocalContentColor provides if (isPressed) VoidPrimary else activeColor) {
            icon()
        }
    }
}
