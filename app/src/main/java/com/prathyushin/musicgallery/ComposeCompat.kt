package com.prathyushin.musicgallery

import androidx.compose.foundation.layout.Spacer as ComposeSpacer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha as composeAlpha
import androidx.compose.ui.draw.blur as composeBlur
import androidx.compose.ui.draw.clip as composeClip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp

// Kept in the app package so the rebuilt screen remains source-compatible with
// the compact modifier calls used throughout MainActivity.
@Composable
fun Spacer(modifier: Modifier = Modifier) = ComposeSpacer(modifier)

fun Modifier.alpha(value: Float): Modifier = composeAlpha(value)
fun Modifier.blur(radius: Dp): Modifier = composeBlur(radius)
fun Modifier.clip(shape: Shape): Modifier = composeClip(shape)
