package com.prathyushin.musicgallery.playback

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.prathyushin.musicgallery.model.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.Executors

class PlaybackController(context: Context) {
    private val executor = Executors.newSingleThreadExecutor()
    private val controllerFuture = MediaController.Builder(
        context,
        SessionToken(context, ComponentName(context, MusicPlaybackService::class.java))
    ).buildAsync()
    @Volatile private var controller: MediaController? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying
    private val _currentMediaId = MutableStateFlow<String?>(null)
    val currentMediaId: StateFlow<String?> = _currentMediaId

    private val listener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) { _isPlaying.value = isPlaying }
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            _currentMediaId.value = mediaItem?.mediaId
        }
    }

    init {
        controllerFuture.addListener({
            try {
                controller = controllerFuture.get().also {
                    it.addListener(listener)
                    _isPlaying.value = it.isPlaying
                    _currentMediaId.value = it.currentMediaItem?.mediaId
                }
            } catch (_: Exception) {
                controller = null
            }
        }, executor)
    }

    private fun item(track: Track) = MediaItem.Builder()
        .setMediaId(track.id.toString())
        .setUri(track.contentUri)
        .setMediaMetadata(MediaMetadata.Builder()
            .setTitle(track.title)
            .setArtist(track.artist)
            .setAlbumTitle(track.album)
            .setArtworkUri(track.artworkUri?.let(android.net.Uri::parse))
            .build())
        .build()

    fun play(track: Track) {
        if (track.contentUri.isNullOrBlank()) return
        controller?.apply {
            setMediaItem(item(track))
            prepare()
            play()
        }
    }

    fun playQueue(tracks: List<Track>, index: Int) {
        if (tracks.isEmpty() || index !in tracks.indices) return
        controller?.apply {
            setMediaItems(tracks.map(::item), index, 0L)
            prepare()
            play()
        }
    }

    fun pause() { controller?.pause() }
    fun resume() { controller?.play() }
    fun next() { controller?.seekToNextMediaItem() }
    fun previous() { controller?.seekToPreviousMediaItem() }
    fun seekTo(positionMs: Long) { controller?.seekTo(positionMs) }
    fun toggleShuffle() { controller?.shuffleModeEnabled = !(controller?.shuffleModeEnabled ?: false) }
    fun toggleRepeat() {
        controller?.repeatMode = if (controller?.repeatMode == Player.REPEAT_MODE_ALL) Player.REPEAT_MODE_OFF else Player.REPEAT_MODE_ALL
    }
    fun currentPosition(): Long = controller?.currentPosition ?: 0L
    fun duration(): Long = controller?.duration ?: 0L
    fun release() {
        controller?.removeListener(listener)
        controller?.release()
        controller = null
        executor.shutdownNow()
    }
}
