package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val VoidPrimary = Color(0xFF0A0F14)
val SurfaceDark = Color(0xFF141A22)
val OutlineGray = Color(0xFF4A5568)
val RadianceWhite = Color(0xFFFFFFFF)
val BlightGold = Color(0xFFD4AF37)
val EchoesBlue = Color(0xFF00E5FF)
val VitalityRed = Color(0xFFE53E3E)
val SurfaceContainer = Color(0xFF1B2329)
val OnSurfaceLight = Color(0xFFCBD5E1)
val ShadowGradient = Color(0xFF1A1F26)

private val DarkColorScheme = darkColorScheme(
    primary = BlightGold,
    background = VoidPrimary,
    surface = SurfaceDark,
    onPrimary = VoidPrimary,
    onBackground = RadianceWhite,
    onSurface = OnSurfaceLight
)

@Composable
fun RemixGameBuilderTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
