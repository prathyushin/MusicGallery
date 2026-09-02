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

/**
 * V4 playback client. The actual Player lives in MusicPlaybackService so
 * playback can continue when the Activity is stopped or the screen is locked.
 */
class PlaybackController(context: Context) {
    private val executor = Executors.newSingleThreadExecutor()
    private val controllerFuture: ListenableFuture<MediaController> =
        MediaController.Builder(
            context,
            SessionToken(context, ComponentName(context, MusicPlaybackService::class.java))
        ).buildAsync()

    @Volatile private var controller: MediaController? = null

    init {
        controllerFuture.addListener({
            try {
                controller = controllerFuture.get()
            } catch (_: Exception) {
                controller = null
            }
        }, executor)
    }

    fun play(track: Track, mediaUri: String) {
        val item = MediaItem.Builder()
            .setMediaId(track.id.toString())
            .setUri(mediaUri)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(track.title)
                    .setArtist(track.artist)
                    .setAlbumTitle(track.album)
                    .build()
            )
            .build()
        controller?.apply {
            setMediaItem(item)
            prepare()
            play()
        }
    }

    fun pause() { controller?.pause() }
    fun resume() { controller?.play() }
    fun release() {
        controller?.release()
        controller = null
        executor.shutdownNow()
    }
    fun isPlaying(): Boolean = controller?.isPlaying == true
}
