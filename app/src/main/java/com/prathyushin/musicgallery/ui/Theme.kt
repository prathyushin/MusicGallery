package com.prathyushin.musicgallery.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

private val GalleryLightColors = lightColorScheme(
    primary = Color(0xFF713548),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF3DCE3),
    onPrimaryContainer = Color(0xFF35101E),
    secondary = Color(0xFF376C69),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD1E8E5),
    onSecondaryContainer = Color(0xFF0C3533),
    tertiary = Color(0xFF946D25),
    onTertiary = Color.White,
    background = Color(0xFFFAF8F5),
    onBackground = Color(0xFF201D1E),
    surface = Color(0xFFFAF8F5),
    onSurface = Color(0xFF201D1E),
    surfaceVariant = Color(0xFFE9E2E1),
    onSurfaceVariant = Color(0xFF655D5F),
    outline = Color(0xFF81787A)
)

private val GalleryDarkColors = darkColorScheme(
    primary = Color(0xFFFFB0C7),
    onPrimary = Color(0xFF4D172C),
    primaryContainer = Color(0xFF63283D),
    onPrimaryContainer = Color(0xFFFFD9E3),
    secondary = Color(0xFFA8D0CC),
    onSecondary = Color(0xFF123A38),
    secondaryContainer = Color(0xFF28514E),
    onSecondaryContainer = Color(0xFFC4ECE8),
    tertiary = Color(0xFFE5BF77),
    onTertiary = Color(0xFF3D2B08),
    background = Color(0xFF141213),
    onBackground = Color(0xFFEAE3E4),
    surface = Color(0xFF141213),
    onSurface = Color(0xFFEAE3E4),
    surfaceVariant = Color(0xFF4B4446),
    onSurfaceVariant = Color(0xFFCFC3C5),
    outline = Color(0xFF988D8F)
)

private val GalleryTypography = Typography().run {
    copy(
        displaySmall = displaySmall.copy(fontWeight = FontWeight.Bold),
        headlineLarge = headlineLarge.copy(fontWeight = FontWeight.Bold),
        headlineSmall = headlineSmall.copy(fontWeight = FontWeight.Bold),
        titleLarge = titleLarge.copy(fontWeight = FontWeight.SemiBold),
        titleMedium = titleMedium.copy(fontWeight = FontWeight.SemiBold)
    )
}

@Composable
fun MusicGalleryTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) GalleryDarkColors else GalleryLightColors,
        typography = GalleryTypography,
        content = content
    )
}
