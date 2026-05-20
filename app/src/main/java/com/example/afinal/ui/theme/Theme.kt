package com.example.afinal.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val PlantColorScheme = lightColorScheme(
    primary = PlantGreenPrimary,
    onPrimary = PlantSurface,
    secondary = PlantGreenSecondary,
    background = PlantBackground,
    surface = PlantSurface,
    onSurface = PlantGreenDark
)

@Composable
fun PlantAssistantTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = PlantColorScheme,
        content = content
    )
}
