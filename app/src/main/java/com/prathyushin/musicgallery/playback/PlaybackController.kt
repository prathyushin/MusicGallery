package com.prathyushin.musicgallery.playback

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.prathyushin.musicgallery.model.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors

/**
 * UI-facing Media3 controller.
 *
 * UI actions are never dropped while the asynchronous MediaController
 * connection is being established.
 */
class PlaybackController(context: Context) {
    private val executor = Executors.newSingleThreadExecutor()
    private val controllerFuture = MediaController.Builder(
        context,
        SessionToken(context, ComponentName(context, MusicPlaybackService::class.java))
    ).buildAsync()

    @Volatile private var controller: MediaController? = null
    private val pendingCommands = CopyOnWriteArrayList<(MediaController) -> Unit>()

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying
    private val _currentMediaId = MutableStateFlow<String?>(null)
    val currentMediaId: StateFlow<String?> = _currentMediaId
    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration
    private val _position = MutableStateFlow(0L)
    val position: StateFlow<Long> = _position
    private val _shuffleEnabled = MutableStateFlow(false)
    val shuffleEnabled: StateFlow<Boolean> = _shuffleEnabled
    private val _repeatMode = MutableStateFlow(Player.REPEAT_MODE_OFF)
    val repeatMode: StateFlow<Int> = _repeatMode

    private val listener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) = syncState()
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) = syncState()
        override fun onPlaybackStateChanged(playbackState: Int) = syncState()
        override fun onShuffleModeEnabledChanged(enabled: Boolean) = syncState()
        override fun onRepeatModeChanged(mode: Int) = syncState()
        override fun onTimelineChanged(timeline: androidx.media3.common.Timeline, reason: Int) = syncState()
    }

    init {
        controllerFuture.addListener({
            try {
                val connected = controllerFuture.get()
                controller = connected
                connected.addListener(listener)
                _isConnected.value = true
                syncState(connected)

                val commands = pendingCommands.toList()
                pendingCommands.clear()
                commands.forEach { command -> runCatching { command(connected) } }
                syncState(connected)
            } catch (_: Exception) {
                controller = null
                _isConnected.value = false
            }
        }, executor)
    }

    private fun mediaItem(track: Track): MediaItem? {
        val uri = track.contentUri?.takeIf(String::isNotBlank)?.let(Uri::parse) ?: return null
        return MediaItem.Builder()
            .setMediaId(track.id.toString())
            .setUri(uri)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(track.title)
                    .setArtist(track.artist)
                    .setAlbumTitle(track.album)
                    .setArtworkUri(track.artworkUri?.takeIf(String::isNotBlank)?.let(Uri::parse))
                    .build()
            )
            .build()
    }

    private fun execute(command: (MediaController) -> Unit) {
        val active = controller
        if (active != null) {
            runCatching { command(active) }.onFailure { _isConnected.value = false }
        } else {
            pendingCommands += command
        }
    }

    fun play(track: Track) {
        val item = mediaItem(track) ?: return
        execute {
            it.setMediaItem(item)
            it.prepare()
            it.play()
        }
    }

    fun playQueue(tracks: List<Track>, index: Int) {
        if (tracks.isEmpty() || index !in tracks.indices) return
        val items = tracks.mapNotNull(::mediaItem)
        if (items.isEmpty()) return

        val requestedId = tracks[index].id.toString()
        val safeIndex = items.indexOfFirst { it.mediaId == requestedId }.takeIf { it >= 0 } ?: 0

        execute {
            it.setMediaItems(items, safeIndex, 0L)
            it.prepare()
            it.play()
        }
    }

    fun togglePlayPause() = execute {
        if (it.isPlaying) it.pause() else it.play()
    }

    fun pause() = execute { it.pause() }
    fun resume() = execute { it.play() }
    fun next() = execute { it.seekToNextMediaItem() }

    fun previous() = execute {
        if (it.currentPosition > 4_000L) it.seekTo(0L) else it.seekToPreviousMediaItem()
    }

    fun seekTo(positionMs: Long) = execute {
        it.seekTo(positionMs.coerceAtLeast(0L))
    }

    fun toggleShuffle() = execute {
        it.shuffleModeEnabled = !it.shuffleModeEnabled
    }

    fun toggleRepeat() = execute {
        it.repeatMode = when (it.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
    }

    fun currentPosition(): Long = controller?.currentPosition?.coerceAtLeast(0L) ?: position.value

    fun currentDuration(): Long = controller?.duration?.takeIf { it > 0L } ?: duration.value

    private fun syncState(source: Player? = controller) {
        source ?: return
        _isPlaying.value = source.isPlaying
        _currentMediaId.value = source.currentMediaItem?.mediaId
        _duration.value = source.duration.takeIf { it > 0L } ?: 0L
        _position.value = source.currentPosition.coerceAtLeast(0L)
        _shuffleEnabled.value = source.shuffleModeEnabled
        _repeatMode.value = source.repeatMode
    }

    fun release() {
        pendingCommands.clear()
        controller?.removeListener(listener)
        controller?.release()
        controller = null
        _isConnected.value = false
        executor.shutdownNow()
    }
}
