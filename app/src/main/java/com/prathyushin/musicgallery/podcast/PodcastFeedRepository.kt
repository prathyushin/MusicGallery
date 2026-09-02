package com.prathyushin.musicgallery.podcast

import android.util.Xml
import com.prathyushin.musicgallery.model.PodcastEpisode
import com.prathyushin.musicgallery.model.PodcastShow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import java.net.HttpURLConnection
import java.net.URL

class PodcastFeedRepository {
    suspend fun load(feedUrl: String): Result<PodcastFeed> = withContext(Dispatchers.IO) {
        runCatching {
            val connection = (URL(feedUrl).openConnection() as HttpURLConnection).apply {
                connectTimeout = 10_000
                readTimeout = 15_000
                requestMethod = "GET"
                setRequestProperty("User-Agent", "MusicGallery/3.1")
            }
            connection.inputStream.use { input -> parse(input, feedUrl) }.also { connection.disconnect() }
        }
    }

    private fun parse(input: java.io.InputStream, feedUrl: String): PodcastFeed {
        val parser = Xml.newPullParser().apply { setInput(input, "UTF-8") }
        var event = parser.eventType
        var inChannel = false
        var inItem = false
        var channelTitle = "Podcast"
        var channelAuthor = ""
        var channelDescription = ""
        var channelImage: String? = null
        var currentTitle = ""
        var currentDescription = ""
        var currentAudio: String? = null
        var currentPublished = 0L
        var currentDuration = 0L
        val episodes = mutableListOf<PodcastEpisode>()

        while (event != XmlPullParser.END_DOCUMENT) {
            when (event) {
                XmlPullParser.START_TAG -> when (parser.name.lowercase()) {
                    "channel" -> inChannel = true
                    "item", "entry" -> {
                        inItem = true
                        currentTitle = ""
                        currentDescription = ""
                        currentAudio = null
                        currentPublished = 0L
                        currentDuration = 0L
                    }
                    "title" -> {
                        val value = parser.nextText().trim()
                        if (inItem) currentTitle = value else if (inChannel) channelTitle = value
                    }
                    "description", "summary", "content:encoded" -> {
                        val value = parser.nextText().trim()
                        if (inItem) currentDescription = value else if (inChannel) channelDescription = value
                    }
                    "author", "itunes:author", "dc:creator" -> {
                        val value = parser.nextText().trim()
                        if (!inItem && inChannel) channelAuthor = value
                    }
                    "image" -> if (!inItem) {
                        channelImage = parser.getAttributeValue(null, "href")
                    }
                    "enclosure" -> if (inItem) {
                        currentAudio = parser.getAttributeValue(null, "url")
                    }
                    "link" -> if (inItem && currentAudio == null) {
                        val rel = parser.getAttributeValue(null, "rel")
                        val type = parser.getAttributeValue(null, "type")
                        if (rel == "enclosure" || type?.startsWith("audio/") == true) currentAudio = parser.getAttributeValue(null, "href") ?: parser.getAttributeValue(null, "url")
                    }
                    "pubdate", "published", "updated" -> if (inItem) {
                        currentPublished = parseDate(parser.nextText())
                    }
                    "itunes:duration" -> if (inItem) currentDuration = parseDuration(parser.nextText())
                }
                XmlPullParser.END_TAG -> when (parser.name.lowercase()) {
                    "item", "entry" -> {
                        if (currentAudio != null && currentTitle.isNotBlank()) {
                            episodes += PodcastEpisode(
                                id = "$feedUrl#$currentTitle".hashCode().toString(),
                                showId = feedUrl,
                                title = currentTitle,
                                description = currentDescription,
                                audioUrl = currentAudio,
                                publishedAt = currentPublished,
                                durationMs = currentDuration,
                                artworkUrl = channelImage
                            )
                        }
                        inItem = false
                    }
                    "channel" -> inChannel = false
                }
            }
            event = parser.next()
        }

        val show = PodcastShow(
            id = feedUrl,
            title = channelTitle,
            author = channelAuthor,
            artworkUrl = channelImage,
            feedUrl = feedUrl,
            description = channelDescription
        )
        return PodcastFeed(show, episodes)
    }

    private fun parseDuration(value: String): Long {
        val parts = value.trim().split(":")
        return when (parts.size) {
            3 -> (parts[0].toLongOrNull() ?: 0) * 3_600_000 + (parts[1].toLongOrNull() ?: 0) * 60_000 + (parts[2].toLongOrNull() ?: 0) * 1_000
            2 -> (parts[0].toLongOrNull() ?: 0) * 60_000 + (parts[1].toLongOrNull() ?: 0) * 1_000
            else -> (value.toLongOrNull() ?: 0) * 1_000
        }
    }

    private fun parseDate(value: String): Long = 0L
}

data class PodcastFeed(val show: PodcastShow, val episodes: List<PodcastEpisode>)
