package androidx.compose.ui

import androidx.compose.ui.draw.alpha as composeAlpha
import androidx.compose.ui.draw.blur as composeBlur
import androidx.compose.ui.draw.clip as composeClip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp

// Compatibility bridge for the compact modifier imports used by Music Gallery.
fun Modifier.alpha(value: Float): Modifier = composeAlpha(value)
fun Modifier.blur(radius: Dp): Modifier = composeBlur(radius)
fun Modifier.clip(shape: Shape): Modifier = composeClip(shape)
