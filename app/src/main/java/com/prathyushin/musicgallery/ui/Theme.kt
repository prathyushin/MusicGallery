package com.prathyushin.musicgallery.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val MusicGalleryDarkColors = darkColorScheme(
    primary = Color(0xFFD7FF35),
    onPrimary = Color(0xFF09090B),
    secondary = Color(0xFFB44CFF),
    onSecondary = Color(0xFFFFFFFF),
    background = Color(0xFF09090B),
    onBackground = Color(0xFFF7F7F7),
    surface = Color(0xFF09090B),
    onSurface = Color(0xFFF7F7F7),
    surfaceVariant = Color(0xFF141418),
    onSurfaceVariant = Color(0xFF9B9BA3),
    outline = Color(0x30FFFFFF)
)

@Composable
fun MusicGalleryTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MusicGalleryDarkColors,
        content = content
    )
}
