package com.strathmore.CareConnect.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

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

@Composable
fun CareConnectTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(colorScheme = colors, content = content)
}