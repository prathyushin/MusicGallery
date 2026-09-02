package com.prathyushin.musicgallery.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.prathyushin.musicgallery.model.PlaybackState

@Composable
fun MiniPlayer(
    state: PlaybackState,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(state.title.isNotBlank()) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            tonalElevation = 5.dp,
            shape = MaterialTheme.shapes.large
        ) {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(state.title, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                        Text(state.subtitle, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                    }
                    FilledIconButton(onClick = onPrevious, modifier = Modifier.size(42.dp)) {
                        Icon(Icons.Rounded.SkipPrevious, "Previous")
                    }
                    FilledIconButton(onClick = onPlayPause, modifier = Modifier.size(46.dp)) {
                        Icon(if (state.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, "Play or pause")
                    }
                    FilledIconButton(onClick = onNext, modifier = Modifier.size(42.dp)) {
                        Icon(Icons.Rounded.SkipNext, "Next")
                    }
                }
                Spacer(Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { state.progress.coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
