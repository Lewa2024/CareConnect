package com.strathmore.CareConnect.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = GreenPrimary,
    onPrimary = White,
    secondary = BluePrimary,
    onSecondary = White,
    secondaryContainer = BlueContainer,
    onSecondaryContainer = BluePrimary,
    tertiary = BluePrimary,
    background = OffWhite,
    surface = White,
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