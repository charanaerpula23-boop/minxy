package com.charanhyper.tech.minxy.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val MinxyColorScheme = darkColorScheme(
    primary = White,
    onPrimary = Black,
    secondary = SoftWhite,
    onSecondary = Black,
    background = Black,
    surface = DarkSurface,
    onBackground = White,
    onSurface = SoftWhite
)

@Composable
fun MinxyTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = MinxyColorScheme,
        typography = Typography,
        content = content
    )
}