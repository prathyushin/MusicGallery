package com.prathyushin.musicgallery.playback

import android.content.ComponentName
import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.prathyushin.musicgallery.model.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.Executors

/**
 * V5.2 connection-safe playback bridge.
 *
 * UI commands are queued until MediaController is connected, so an immediate
 * tap on a song is never silently discarded during service startup.
 */
class PlaybackController(context: Context) {
    private val executor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())
    private val appContext = context.applicationContext

    private val controllerFuture = MediaController.Builder(
        appContext,
        SessionToken(appContext, ComponentName(appContext, MusicPlaybackService::class.java))
    ).buildAsync()

    @Volatile private var controller: MediaController? = null
    @Volatile private var released = false
    private val pendingCommands = ArrayDeque<() -> Unit>()

    private val _connected = MutableStateFlow(false)
    val connected: StateFlow<Boolean> = _connected

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _currentMediaId = MutableStateFlow<String?>(null)
    val currentMediaId: StateFlow<String?> = _currentMediaId

    private val _buffering = MutableStateFlow(false)
    val buffering: StateFlow<Boolean> = _buffering

    private val listener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.value = isPlaying
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            _buffering.value = playbackState == Player.STATE_BUFFERING
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            _currentMediaId.value = mediaItem?.mediaId
        }
    }

    init {
        controllerFuture.addListener({
            if (released) return@addListener
            try {
                val connectedController = controllerFuture.get()
                controller = connectedController
                connectedController.addListener(listener)
                _connected.value = true
                _isPlaying.value = connectedController.isPlaying
                _currentMediaId.value = connectedController.currentMediaItem?.mediaId
                flushPending()
            } catch (_: Exception) {
                _connected.value = false
            }
        }, executor)
    }

    private fun item(track: Track) = MediaItem.Builder()
        .setMediaId(track.id.toString())
        .setUri(track.contentUri)
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(track.title)
                .setArtist(track.artist)
                .setAlbumTitle(track.album)
                .setArtworkUri(track.artworkUri?.let(android.net.Uri::parse))
                .build()
        )
        .build()

    private fun enqueueOrRun(command: () -> Unit) {
        if (released) return
        val active = controller
        if (active != null && _connected.value) {
            mainHandler.post {
                if (!released) command()
            }
        } else {
            synchronized(pendingCommands) {
                pendingCommands.addLast(command)
            }
        }
    }

    private fun flushPending() {
        val commands = synchronized(pendingCommands) {
            val copy = pendingCommands.toList()
            pendingCommands.clear()
            copy
        }
        commands.forEach { command ->
            mainHandler.post {
                if (!released) command()
            }
        }
    }

    fun play(track: Track) {
        if (track.contentUri.isNullOrBlank()) return
        enqueueOrRun {
            controller?.apply {
                setMediaItem(item(track))
                prepare()
                play()
            }
        }
    }

    fun playQueue(tracks: List<Track>, index: Int) {
        if (tracks.isEmpty() || index !in tracks.indices) return
        enqueueOrRun {
            controller?.apply {
                setMediaItems(tracks.map(::item), index, 0L)
                prepare()
                play()
            }
        }
    }

    fun pause() = enqueueOrRun { controller?.pause() }
    fun resume() = enqueueOrRun { controller?.play() }
    fun next() = enqueueOrRun { controller?.seekToNextMediaItem() }
    fun previous() = enqueueOrRun { controller?.seekToPreviousMediaItem() }
    fun seekTo(positionMs: Long) = enqueueOrRun { controller?.seekTo(positionMs.coerceAtLeast(0L)) }

    fun toggleShuffle() = enqueueOrRun {
        controller?.shuffleModeEnabled = !(controller?.shuffleModeEnabled ?: false)
    }

    fun toggleRepeat() = enqueueOrRun {
        controller?.repeatMode = when (controller?.repeatMode) {
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_OFF
            else -> Player.REPEAT_MODE_ALL
        }
    }

    fun currentPosition(): Long = controller?.currentPosition ?: 0L
    fun duration(): Long = controller?.duration ?: 0L

    fun release() {
        if (released) return
        released = true
        synchronized(pendingCommands) { pendingCommands.clear() }
        controller?.removeListener(listener)
        controller?.release()
        controller = null
        _connected.value = false
        executor.shutdownNow()
    }
}
