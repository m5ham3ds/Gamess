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
                Text(text = "HP: ${player.hp.toInt()}/${player.maxHp.toInt()}", color = Color.Red)
                Text(text = "EN: ${player.energy.toInt()}/${player.maxEnergy.toInt()}", color = Color.Cyan)
                Text(text = "WEP: ${player.activeWeapon.name}", color = player.activeWeapon.color)
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
                    ) { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("<") } }
                    
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
                    ) { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(">") } }
                }

                // Actions
                Column(horizontalAlignment = Alignment.End) {
                    Row {
                        Button(onClick = { viewModel.dash() }) { Text("Dash") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = { viewModel.switchWeapon() }) { Text("Swp Wep") }
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
