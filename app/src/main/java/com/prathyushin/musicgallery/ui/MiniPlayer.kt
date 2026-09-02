package com.prathyushin.musicgallery.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        tonalElevation = 4.dp,
        shadowElevation = 3.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 14.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                modifier = Modifier.size(42.dp),
                shape = MaterialTheme.shapes.medium,
                tonalElevation = 2.dp
            ) {}
            androidx.compose.foundation.layout.Column(Modifier.weight(1f)) {
                Text(state.title, maxLines = 1, style = MaterialTheme.typography.titleSmall)
                Text(state.subtitle, maxLines = 1, style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onPlayPause) {
                Icon(if (state.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, "Play or pause")
            }
            IconButton(onClick = onNext) {
                Icon(Icons.Rounded.SkipNext, "Next")
            }
        }
    }
}
