package com.prathyushin.musicgallery

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Podcasts
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.Upgrade
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.prathyushin.musicgallery.library.MusicScanner
import com.prathyushin.musicgallery.model.Track
import com.prathyushin.musicgallery.playback.PlaybackController
import com.prathyushin.musicgallery.ui.MusicGalleryTheme

class MainActivity : ComponentActivity() {
    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) recreate()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!hasAudioPermission()) requestAudioPermission()
        setContent { MusicGalleryApp(this) }
    }

    private fun hasAudioPermission(): Boolean {
        val permission = if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_AUDIO else Manifest.permission.READ_EXTERNAL_STORAGE
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestAudioPermission() {
        val permission = if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_AUDIO else Manifest.permission.READ_EXTERNAL_STORAGE
        permissionLauncher.launch(permission)
    }
}

private data class NavItem(val label: String, val icon: @Composable () -> Unit)

@Composable
fun MusicGalleryApp(activity: ComponentActivity) {
    var selected by remember { mutableIntStateOf(0) }
    var query by remember { mutableStateOf("") }
    var tracks by remember { mutableStateOf(emptyList<Track>()) }
    var current by remember { mutableStateOf<Track?>(null) }
    var playing by remember { mutableStateOf(false) }
    val controller = remember { PlaybackController(activity.applicationContext) }

    LaunchedEffect(Unit) {
        tracks = MusicScanner(activity.contentResolver).scan()
    }

    DisposableEffect(Unit) {
        onDispose { controller.release() }
    }

    fun playTrack(track: Track) {
        track.contentUri?.let {
            controller.play(track, it)
            current = track
            playing = true
        }
    }

    val navItems = listOf(
        NavItem("Home") { Icon(Icons.Rounded.Home, null) },
        NavItem("Library") { Icon(Icons.Rounded.LibraryMusic, null) },
        NavItem("Podcasts") { Icon(Icons.Rounded.Podcasts, null) },
        NavItem("Search") { Icon(Icons.Rounded.Search, null) }
    )

    MusicGalleryTheme {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                Column {
                    current?.let { track ->
                        MiniPlayerBar(
                            track = track,
                            playing = playing,
                            onPlayPause = {
                                if (playing) controller.pause() else controller.resume()
                                playing = !playing
                            },
                            onNext = {
                                val index = tracks.indexOfFirst { it.id == track.id }
                                if (index >= 0 && index + 1 < tracks.size) playTrack(tracks[index + 1])
                            },
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                    NavigationBar(modifier = Modifier.navigationBarsPadding()) {
                        navItems.forEachIndexed { index, item ->
                            NavigationBarItem(
                                selected = selected == index,
                                onClick = { selected = index },
                                icon = item.icon,
                                label = { Text(item.label) }
                            )
                        }
                    }
                }
            }
        ) { padding ->
            when (selected) {
                1 -> LibraryHome(tracks, ::playTrack, Modifier.padding(padding))
                2 -> PodcastHome(Modifier.padding(padding))
                3 -> SearchHome(query, { query = it }, tracks, ::playTrack, Modifier.padding(padding))
                else -> HomeScreen(tracks, ::playTrack, Modifier.padding(padding))
            }
        }
    }
}

@Composable
private fun HomeScreen(tracks: List<Track>, onPlay: (Track) -> Unit, modifier: Modifier = Modifier) {
    LazyColumn(modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        item {
            Spacer(Modifier.height(24.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Listen now", style = MaterialTheme.typography.labelLarge)
                    Text("Music Gallery", style = MaterialTheme.typography.headlineLarge)
                }
                Surface(shape = RoundedCornerShape(50), tonalElevation = 3.dp) {
                    Icon(Icons.Rounded.Headphones, "Music", Modifier.padding(12.dp))
                }
            }
            Spacer(Modifier.height(12.dp))
            VersionPill()
            Spacer(Modifier.height(22.dp))
            ReleaseCard()
            Spacer(Modifier.height(26.dp))
            Text(if (tracks.isEmpty()) "Your library" else "Recently added", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))
        }
        if (tracks.isEmpty()) item { EmptyLibraryCard() }
        else items(tracks.take(12), key = { it.id }) { track -> TrackRow(track, onPlay) }
    }
}

@Composable
private fun LibraryHome(tracks: List<Track>, onPlay: (Track) -> Unit, modifier: Modifier = Modifier) {
    LazyColumn(modifier.fillMaxSize().padding(20.dp)) {
        item {
            Spacer(Modifier.height(24.dp))
            Text("Library", style = MaterialTheme.typography.headlineLarge)
            Text("${tracks.size} songs on this device", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(20.dp))
        }
        if (tracks.isEmpty()) item { EmptyLibraryCard() }
        else items(tracks, key = { it.id }) { track -> TrackRow(track, onPlay) }
    }
}

@Composable
private fun TrackRow(track: Track, onPlay: (Track) -> Unit) {
    Surface(shape = RoundedCornerShape(18.dp), tonalElevation = 1.dp, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(Modifier.size(52.dp), shape = RoundedCornerShape(14.dp), tonalElevation = 3.dp) {
                Icon(Icons.Rounded.MusicNote, "Artwork", Modifier.padding(15.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(track.title, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.titleMedium)
                Text("${track.artist} · ${track.album}", maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = { onPlay(track) }) { Icon(Icons.Rounded.PlayArrow, "Play") }
        }
    }
}

@Composable
private fun EmptyLibraryCard() {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(26.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest)) {
        Column(Modifier.padding(22.dp)) {
            Text("No music found yet", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(6.dp))
            Text("Allow Music Gallery to access audio on your device, then reopen the Library.", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun VersionPill() {
    Surface(shape = RoundedCornerShape(50.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            Icon(Icons.Rounded.Upgrade, null, Modifier.size(16.dp))
            Text("Music Gallery ${AppVersion.NAME}", style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun ReleaseCard() {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(26.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest)) {
        Column(Modifier.padding(20.dp)) {
            Text("V3.1 · What's new", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(4.dp))
            Text("A real starting point", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            Text("Local music scanning, real audio playback, persistent navigation and a dedicated podcast foundation. Future releases will build on this version.", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun MiniPlayerBar(track: Track, playing: Boolean, onPlayPause: () -> Unit, onNext: () -> Unit, modifier: Modifier = Modifier) {
    Surface(modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), tonalElevation = 5.dp, shadowElevation = 3.dp) {
        Row(Modifier.padding(start = 12.dp, end = 4.dp, top = 6.dp, bottom = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(Modifier.size(42.dp), shape = RoundedCornerShape(12.dp), tonalElevation = 3.dp) { Icon(Icons.Rounded.Album, "Album", Modifier.padding(11.dp)) }
            Column(Modifier.weight(1f).padding(horizontal = 10.dp)) {
                Text(track.title, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.titleSmall)
                Text(track.artist, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onPlayPause) { Icon(if (playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, "Play or pause") }
            IconButton(onClick = onNext) { Icon(Icons.Rounded.SkipNext, "Next") }
        }
    }
}

@Composable
private fun PodcastHome(modifier: Modifier = Modifier) {
    LazyColumn(modifier.fillMaxSize().padding(20.dp)) {
        item {
            Spacer(Modifier.height(24.dp))
            Text("Podcasts", style = MaterialTheme.typography.headlineLarge)
            Text("A dedicated home for shows and episodes", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(22.dp))
            FeatureCard("Continue listening", "Resume your latest episode")
            Spacer(Modifier.height(14.dp))
            FeatureCard("Your shows", "Subscriptions will live here")
            Spacer(Modifier.height(14.dp))
            FeatureCard("Downloads", "Offline episodes and automatic downloads")
            Spacer(Modifier.height(18.dp))
            Text("Podcast engine is the next V3.1 milestone.", style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun SearchHome(query: String, onQueryChange: (String) -> Unit, tracks: List<Track>, onPlay: (Track) -> Unit, modifier: Modifier = Modifier) {
    val results = tracks.filter { track ->
        query.isNotBlank() && listOf(track.title, track.artist, track.album).any { it.contains(query, ignoreCase = true) }
    }
    Column(modifier.fillMaxSize().padding(20.dp)) {
        Spacer(Modifier.height(24.dp))
        Text("Search", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(16.dp))
        TextField(query, onQueryChange, Modifier.fillMaxWidth(), placeholder = { Text("Songs, artists or albums") }, singleLine = true)
        Spacer(Modifier.height(22.dp))
        if (query.isBlank()) {
            Text("Search your device library", style = MaterialTheme.typography.titleLarge)
            Text("Fast local search across your music collection.", style = MaterialTheme.typography.bodyLarge)
        } else if (results.isEmpty()) {
            Text("No results", style = MaterialTheme.typography.titleLarge)
        } else {
            Text("${results.size} results", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            LazyColumn { items(results, key = { it.id }) { TrackRow(it, onPlay) } }
        }
    }
}

@Composable
private fun FeatureCard(title: String, subtitle: String) {
    Card(Modifier.fillMaxWidth().height(96.dp), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.Center) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
