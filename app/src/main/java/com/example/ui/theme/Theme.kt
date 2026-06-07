package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

private val SilentWandererColorScheme = darkColorScheme(
    primary = RadianceWhite,
    onPrimary = Color.Black,
    secondary = EchoesBlue,
    onSecondary = Color.Black,
    tertiary = BlightGold,
    onTertiary = Color.Black,
    background = VoidPrimary,
    onBackground = OnSurfaceLight,
    surface = SurfaceDark,
    onSurface = OnSurfaceLight,
    error = VitalityRed,
    onError = Color.White
)

private val SharpShapes = Shapes(
    small = RoundedCornerShape(0.dp),
    medium = RoundedCornerShape(0.dp),
    large = RoundedCornerShape(0.dp)
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SilentWandererColorScheme,
        typography = Typography,
        shapes = SharpShapes,
        content = content
    )
}
