package com.prathyushin.musicgallery.playback

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.exoplayer.ExoPlayer
import com.prathyushin.musicgallery.model.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class PlayerUiState(
    val track: Track? = null,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val index: Int = -1,
    val queueSize: Int = 0
)

class PlaybackController(context: Context) {
    private val player = ExoPlayer.Builder(context.applicationContext).build().apply {
        setHandleAudioBecomingNoisy(true)
    }
    private val _state = MutableStateFlow(PlayerUiState())
    val state: StateFlow<PlayerUiState> = _state.asStateFlow()
    private var queue: List<Track> = emptyList()

    private val listener = object : androidx.media3.common.Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) = publish()
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) = publish()
        override fun onPlaybackStateChanged(playbackState: Int) = publish()
    }

    init { player.addListener(listener) }

    fun play(track: Track, tracks: List<Track> = listOf(track)) {
        queue = tracks.filter { !it.contentUri.isNullOrBlank() }.ifEmpty { listOf(track) }
        val startIndex = queue.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
        val items = queue.map { item ->
            MediaItem.Builder()
                .setMediaId(item.id.toString())
                .setUri(item.contentUri)
                .setMediaMetadata(MediaMetadata.Builder()
                    .setTitle(item.title)
                    .setArtist(item.artist)
                    .setAlbumTitle(item.album)
                    .build())
                .build()
        }
        player.setMediaItems(items, startIndex, 0L)
        player.prepare()
        player.play()
        publish()
    }

    fun toggle() { if (player.isPlaying) player.pause() else player.play(); publish() }
    fun pause() { player.pause(); publish() }
    fun resume() { player.play(); publish() }
    fun next() { if (player.hasNextMediaItem) player.seekToNextMediaItem(); player.play(); publish() }
    fun previous() {
        if (player.currentPosition > 3_000L) player.seekTo(0L)
        else if (player.hasPreviousMediaItem) player.seekToPreviousMediaItem()
        player.play()
        publish()
    }
    fun seekTo(positionMs: Long) { player.seekTo(positionMs); publish() }
    fun updatePosition() { if (player.mediaItemCount > 0) publish() }

    private fun publish() {
        val index = player.currentMediaItemIndex
        val track = queue.getOrNull(index)
        _state.value = PlayerUiState(
            track = track,
            isPlaying = player.isPlaying,
            positionMs = player.currentPosition.coerceAtLeast(0L),
            durationMs = player.duration.takeIf { it > 0 } ?: track?.durationMs ?: 0L,
            index = index,
            queueSize = player.mediaItemCount
        )
    }

    fun release() { player.removeListener(listener); player.release() }
}
