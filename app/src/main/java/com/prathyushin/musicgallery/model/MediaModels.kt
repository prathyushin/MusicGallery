package com.prathyushin.musicgallery.model

data class Track(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val artworkUri: String? = null,
    val contentUri: String? = null
)

data class PodcastShow(
    val id: String,
    val title: String,
    val author: String,
    val artworkUrl: String? = null,
    val feedUrl: String? = null,
    val description: String = ""
)

data class PodcastEpisode(
    val id: String,
    val showId: String,
    val title: String,
    val description: String = "",
    val audioUrl: String? = null,
    val publishedAt: Long = 0L,
    val durationMs: Long = 0L,
    val progressMs: Long = 0L,
    val artworkUrl: String? = null
)

data class PlaybackState(
    val title: String = "Nothing playing",
    val subtitle: String = "Choose something to listen to",
    val artworkUri: String? = null,
    val isPlaying: Boolean = false,
    val progress: Float = 0f
)
