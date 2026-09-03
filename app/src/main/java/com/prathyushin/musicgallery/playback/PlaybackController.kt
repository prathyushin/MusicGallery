package com.prathyushin.musicgallery.playback

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.prathyushin.musicgallery.model.Track
import java.util.concurrent.Executors

class PlaybackController(context: Context) {
    private val executor = Executors.newSingleThreadExecutor()
    private val controllerFuture: ListenableFuture<MediaController> = MediaController.Builder(
        context,
        SessionToken(context, ComponentName(context, MusicPlaybackService::class.java))
    ).buildAsync()
    @Volatile private var controller: MediaController? = null

    init {
        controllerFuture.addListener({
            try { controller = controllerFuture.get() } catch (_: Exception) { controller = null }
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

    fun play(track: Track, mediaUri: String = track.contentUri.orEmpty()) {
        if (mediaUri.isBlank()) return
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
        controller?.repeatMode = if (controller?.repeatMode == androidx.media3.common.Player.REPEAT_MODE_ALL)
            androidx.media3.common.Player.REPEAT_MODE_OFF else androidx.media3.common.Player.REPEAT_MODE_ALL
    }
    fun isPlaying(): Boolean = controller?.isPlaying == true
    fun release() { controller?.release(); controller = null; executor.shutdownNow() }
}
