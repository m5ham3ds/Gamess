package com.example.game

import android.view.MotionEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.unit.dp
import com.example.game.player.*

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun GameScreen(viewModel: GameViewModel) {
    val gameState by viewModel.gameState.collectAsState()
    val player by viewModel.player.collectAsState()
    val platforms by viewModel.platforms.collectAsState()

    if (gameState == GameState.START) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
            Button(onClick = { viewModel.startGame() }) {
                Text("Start Adventure")
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF202025))) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Draw platforms
                platforms.forEach { plat ->
                    drawRect(
                        color = Color(0xFF454550),
                        topLeft = Offset(plat.x, plat.y),
                        size = Size(plat.width, plat.height)
                    )
                }

                // Draw Player Character (Fully delegated to PlayerCharacter.kt)
                drawPlayerCharacter(player)
            }

            // HUD
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "HP: ${player.hp.toInt()}/${player.maxHp.toInt()}", color = Color.Red, style = MaterialTheme.typography.titleMedium)
                Text(text = "EN: ${player.energy.toInt()}/${player.maxEnergy.toInt()}", color = Color.Cyan, style = MaterialTheme.typography.titleMedium)
                Text(text = "WEAPON: ${player.activeWeapon.name}", color = player.activeWeapon.color, style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(4.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.6f)),
                    modifier = Modifier.widthIn(max = 280.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(text = "SKIN: ${player.activeSkin.name}", color = player.activeSkin.accentColor, style = MaterialTheme.typography.bodySmall)
                        Text(text = player.activeSkin.description, color = Color.LightGray, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            // Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(32.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                // D-PAD
                Row {
                    Card(
                        modifier = Modifier
                            .size(64.dp)
                            .pointerInteropFilter {
                                when (it.action) {
                                    MotionEvent.ACTION_DOWN -> { viewModel.setMoveLeft(true); true }
                                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> { viewModel.setMoveLeft(false); true }
                                    else -> false
                                }
                            }
                    ) { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("<", style = MaterialTheme.typography.titleLarge) } }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Card(
                        modifier = Modifier
                            .size(64.dp)
                            .pointerInteropFilter {
                                when (it.action) {
                                    MotionEvent.ACTION_DOWN -> { viewModel.setMoveRight(true); true }
                                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> { viewModel.setMoveRight(false); true }
                                    else -> false
                                }
                            }
                    ) { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(">", style = MaterialTheme.typography.titleLarge) } }
                }

                // Actions
                Column(horizontalAlignment = Alignment.End) {
                    Row {
                        Button(onClick = { viewModel.dash() }) { Text("Dash") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = { viewModel.switchWeapon() }) { Text("Weapon") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = { viewModel.switchSkin() }) { Text("Skin") }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row {
                        Button(onClick = { viewModel.attack() }, modifier = Modifier.size(64.dp)) { Text("ATK") }
                        Spacer(modifier = Modifier.width(16.dp))
                        Button(onClick = { viewModel.jump() }, modifier = Modifier.size(64.dp)) { Text("JMP") }
                    }
                }
            }
        }
    }
}
