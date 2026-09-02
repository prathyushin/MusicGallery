package com.prathyushin.musicgallery.playback

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.prathyushin.musicgallery.model.Track

class PlaybackController(context: Context) {
    private val player = ExoPlayer.Builder(context).build()

    fun play(track: Track, mediaUri: String) {
        val item = MediaItem.Builder()
            .setMediaId(track.id.toString())
            .setUri(mediaUri)
            .setMediaMetadata(
                androidx.media3.common.MediaMetadata.Builder()
                    .setTitle(track.title)
                    .setArtist(track.artist)
                    .setAlbumTitle(track.album)
                    .build()
            )
            .build()
        player.setMediaItem(item)
        player.prepare()
        player.play()
    }

    fun pause() = player.pause()
    fun resume() = player.play()
    fun release() = player.release()
    fun isPlaying(): Boolean = player.isPlaying
}
