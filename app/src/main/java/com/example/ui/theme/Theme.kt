package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val ZenithColorScheme = darkColorScheme(
    primary = ZenithCyanPrimary,
    onPrimary = ZenithBackgroundDark,
    primaryContainer = ZenithSurfaceVariant,
    onPrimaryContainer = ZenithCyanPrimary,
    secondary = ZenithVioletSecondary,
    onSecondary = ZenithTextPrimary,
    background = ZenithBackgroundDark,
    onBackground = ZenithTextPrimary,
    surface = ZenithSurfaceDark,
    onSurface = ZenithTextPrimary,
    surfaceVariant = ZenithSurfaceVariant,
    onSurfaceVariant = ZenithTextSecondary,
    error = ZenithRedAlert,
    onError = ZenithTextPrimary
)

@Composable
fun ZenithTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ZenithColorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    ZenithTheme(content = content)
}
