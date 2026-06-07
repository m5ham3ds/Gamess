package com.example.game

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.player.PlayerState
import com.example.ui.theme.*

@Composable
fun MemoryTreeScreen(
    viewModel: GameViewModel,
    player: PlayerState,
    onClose: () -> Unit
) {
    val memoryTree = viewModel.memoryTree

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidPrimary)
            .padding(16.dp)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(
                onClick = onClose,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = RadianceWhite),
                border = BorderStroke(1.dp, RadianceWhite.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(0.dp)
            ) {
                Text("RETURN")
            }
            Text(
                text = "MEMORY RECALL",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFFB470E0)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .height(2.dp)
                .background(Color(0xFFB470E0).copy(alpha = 0.3f))
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Text("MF (Fragments): ${player.memoryFragments}", color = RadianceWhite, fontSize = 16.sp)
            Text("FM (Forgetfulness): ${player.forgetfulness}%", color = Color(0xFFB470E0), fontSize = 16.sp)
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            items(memoryTree) { leaf ->
                val isUnlocked = player.unlockedMemories.contains(leaf.id)
                val canAfford = player.memoryFragments >= leaf.mfCost
                val requirementMet = leaf.requiredLeafId == null || player.unlockedMemories.contains(leaf.requiredLeafId)
                val isAffordButLocked = canAfford && requirementMet && !isUnlocked

                val cardColor = if (isUnlocked) SurfaceDark else SurfaceContainer
                val borderColor = if (isUnlocked) BlightGold else if (isAffordButLocked) EchoesBlue else OutlineGray.copy(alpha=0.3f)

                Card(
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    shape = RoundedCornerShape(0.dp),
                    border = BorderStroke(1.dp, borderColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = leaf.title,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isUnlocked) BlightGold else RadianceWhite
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = leaf.description,
                                fontSize = 14.sp,
                                color = RadianceWhite.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Cost: ${leaf.mfCost} MF", color = EchoesBlue, fontSize = 12.sp)
                                Text("Cost: ${leaf.fmGain} FM", color = Color(0xFFB470E0), fontSize = 12.sp)
                            }
                            if (leaf.requiredLeafId != null && !requirementMet) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Requires prior memory", color = VitalityRed, fontSize = 12.sp)
                            }
                        }

                        if (isUnlocked) {
                            Text("RECOVERED", color = BlightGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        } else {
                            Button(
                                onClick = { viewModel.recoverMemory(leaf.id) },
                                enabled = isAffordButLocked,
                                shape = RoundedCornerShape(0.dp),
                                modifier = Modifier.border(1.dp, if (isAffordButLocked) EchoesBlue else OutlineGray.copy(alpha=0.3f)),
                                colors = ButtonDefaults.buttonColors(containerColor = VoidPrimary, disabledContainerColor = VoidPrimary)
                            ) {
                                Text("RECALL", color = if (isAffordButLocked) EchoesBlue else OutlineGray.copy(alpha=0.5f), fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
