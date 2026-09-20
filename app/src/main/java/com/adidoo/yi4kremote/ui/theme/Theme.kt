package com.adidoo.yi4kremote.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AccentGreen = Color(0xFF00E676)
private val NearBlack = Color(0xFF101014)

private val DarkColors = darkColorScheme(
    primary = AccentGreen,
    background = NearBlack,
    surface = NearBlack,
)

private val LightColors = lightColorScheme(
    primary = AccentGreen,
)

@Composable
fun Yi4kRemoteTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
