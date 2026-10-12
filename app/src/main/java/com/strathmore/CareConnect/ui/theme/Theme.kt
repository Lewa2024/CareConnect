package com.strathmore.CareConnect.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
    primary = Navy,
    onPrimary = White,
    primaryContainer = Ice,
    onPrimaryContainer = Navy,
    secondary = BluePrimary,
    onSecondary = White,
    secondaryContainer = Ice,
    onSecondaryContainer = Navy,
    tertiary = BluePrimary,
    background = Mist,
    onBackground = Navy,
    surface = White,
    onSurface = Navy,
    surfaceVariant = SoftSurfaceVariant,
    onSurfaceVariant = SlateText,
    error = AlertRed
)

private val DarkColors = darkColorScheme(
    primary = GreenPrimaryLight,
    onPrimary = White,
    secondary = BlueLight,
    onSecondary = White,
    secondaryContainer = BlueContainerDark,
    onSecondaryContainer = BlueLight,
    tertiary = BlueLight,
    background = SurfaceDark,
    surface = SurfaceDark,
    error = AlertRed
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(12.dp),
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun CareConnectTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        shapes = AppShapes,
        content = content
    )
}