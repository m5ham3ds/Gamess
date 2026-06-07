package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.game.GameScreen
import com.example.game.GameViewModel
import com.example.ui.theme.RemixGameBuilderTheme

class MainActivity : ComponentActivity() {
    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            RemixGameBuilderTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    GameScreen(
                        viewModel = viewModel,
                        onOpenSettings = {
                            // Handled internally now
                        }
                    )
                }
            }
        }
    }
}
