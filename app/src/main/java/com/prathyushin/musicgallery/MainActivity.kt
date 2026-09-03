package com.prathyushin.musicgallery

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil3.compose.AsyncImage
import com.prathyushin.musicgallery.library.MusicScanner
import com.prathyushin.musicgallery.model.Track
import com.prathyushin.musicgallery.playback.PlaybackController
import com.prathyushin.musicgallery.ui.MusicGalleryTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
        val p = if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_AUDIO else Manifest.permission.READ_EXTERNAL_STORAGE
        return ContextCompat.checkSelfPermission(this, p) == PackageManager.PERMISSION_GRANTED
    }
    private fun requestAudioPermission() {
        val p = if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_AUDIO else Manifest.permission.READ_EXTERNAL_STORAGE
        permissionLauncher.launch(p)
    }
}

private enum class Destination { HOME, LIBRARY, PODCASTS, SEARCH }

@Composable
fun MusicGalleryApp(activity: ComponentActivity) {
    var destination by remember { mutableStateOf(Destination.HOME) }
    var tracks by remember { mutableStateOf(emptyList<Track>()) }
    var currentIndex by remember { mutableIntStateOf(-1) }
    var playing by remember { mutableStateOf(false) }
    var showPlayer by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var libraryMode by remember { mutableStateOf("Songs") }
    val controller = remember { PlaybackController(activity.applicationContext) }

    LaunchedEffect(Unit) { tracks = withContext(Dispatchers.IO) { MusicScanner(activity.contentResolver).scan() } }
    DisposableEffect(Unit) { onDispose { controller.release() } }
    val current = tracks.getOrNull(currentIndex)

    fun playAt(index: Int) {
        if (index !in tracks.indices) return
        currentIndex = index
        controller.playQueue(tracks, index)
        playing = true
    }

    MusicGalleryTheme {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                bottomBar = {
                    Column(Modifier.navigationBarsPadding()) {
                        current?.let { track ->
                            MiniPlayer(track, playing, onOpen = { showPlayer = true }, onPlayPause = {
                                if (playing) controller.pause() else controller.resume(); playing = !playing
                            }, onNext = { if (currentIndex + 1 < tracks.size) playAt(currentIndex + 1) })
                        }
                        MusicDock(destination) { destination = it }
                    }
                }
            ) { padding ->
                when (destination) {
                    Destination.HOME -> HomeScreen(tracks, ::playAt, Modifier.padding(padding))
                    Destination.LIBRARY -> LibraryScreen(tracks, ::playAt, libraryMode, { libraryMode = it }, Modifier.padding(padding))
                    Destination.PODCASTS -> PodcastScreen(Modifier.padding(padding))
                    Destination.SEARCH -> SearchScreen(query, { query = it }, tracks, ::playAt, Modifier.padding(padding))
                }
            }
            if (showPlayer && current != null) {
                NowPlaying(current, playing, onClose = { showPlayer = false }, onPlayPause = {
                    if (playing) controller.pause() else controller.resume(); playing = !playing
                }, onNext = { if (currentIndex + 1 < tracks.size) playAt(currentIndex + 1) }, onPrevious = {
                    if (currentIndex > 0) playAt(currentIndex - 1)
                }, onShuffle = { controller.toggleShuffle() }, onRepeat = { controller.toggleRepeat() })
            }
        }
    }
}

@Composable private fun HomeScreen(tracks: List<Track>, onPlay: (Int) -> Unit, modifier: Modifier) {
    LazyColumn(modifier.fillMaxSize().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Spacer(Modifier.height(24.dp))
            Text("Listen now", style = MaterialTheme.typography.labelLarge)
            Text("Your music, beautifully local.", style = MaterialTheme.typography.headlineMedium)
            Text("${tracks.size} songs available on this device", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(8.dp))
        }
        if (tracks.isEmpty()) item { EmptyState() }
        else {
            item { SectionTitle("Recently added", "${tracks.size} songs") }
            items(tracks.take(8), key = { it.id }) { TrackRow(it, onClick = { onPlay(tracks.indexOf(it)) }) }
            item { SectionTitle("Albums", "${tracks.map { it.album }.distinct().size} albums") }
            item {
                LazyVerticalGrid(columns = GridCells.Fixed(2), modifier = Modifier.height(230.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp), userScrollEnabled = false) {
                    items(tracks.distinctBy { it.album }.take(4)) { AlbumCard(it) }
                }
            }
        }
        item { Spacer(Modifier.height(18.dp)) }
    }
}

@Composable private fun LibraryScreen(tracks: List<Track>, onPlay: (Int) -> Unit, mode: String, onMode: (String) -> Unit, modifier: Modifier) {
    val modes = listOf("Songs", "Albums", "Artists")
    val displayed = when (mode) {
        "Albums" -> tracks.distinctBy { it.album }
        "Artists" -> tracks.distinctBy { it.artist }
        else -> tracks
    }
    Column(modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(24.dp)); Text("Library", style = MaterialTheme.typography.headlineLarge)
        Text("Everything stored on this device", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(16.dp))
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) { modes.forEach { item -> SegmentedButton(selected = mode == item, onClick = { onMode(item) }, shape = SegmentedButtonDefaults.itemShape(modes.indexOf(item), modes.size)) { Text(item) } } }
        Spacer(Modifier.height(14.dp))
        if (displayed.isEmpty()) EmptyState() else LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(displayed, key = { "${mode}-${it.id}" }) { track ->
                if (mode == "Songs") TrackRow(track, onClick = { onPlay(tracks.indexOf(track)) }) else LibraryEntityRow(track, mode)
            }
        }
    }
}

@Composable private fun SearchScreen(query: String, onQuery: (String) -> Unit, tracks: List<Track>, onPlay: (Int) -> Unit, modifier: Modifier) {
    val results = tracks.filter { q -> query.isNotBlank() && listOf(q.title, q.artist, q.album).any { it.contains(query, true) } }
    Column(modifier.fillMaxSize().padding(20.dp)) {
        Spacer(Modifier.height(24.dp)); Text("Search", style = MaterialTheme.typography.headlineLarge); Spacer(Modifier.height(14.dp))
        OutlinedTextField(query, onQuery, Modifier.fillMaxWidth(), singleLine = true, leadingIcon = { Icon(Icons.Rounded.Search, null) }, placeholder = { Text("Songs, artists or albums") })
        Spacer(Modifier.height(18.dp))
        when {
            query.isBlank() -> Text("Search your local library", style = MaterialTheme.typography.titleMedium)
            results.isEmpty() -> Text("No matches", style = MaterialTheme.typography.titleMedium)
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) { items(results, key = { it.id }) { t -> TrackRow(t, onClick = { onPlay(tracks.indexOf(t)) }) } }
        }
    }
}

@Composable private fun PodcastScreen(modifier: Modifier) {
    LazyColumn(modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { Spacer(Modifier.height(24.dp)); Text("Podcasts", style = MaterialTheme.typography.headlineLarge); Text("A real podcast workspace is next in the 4.2 line.", style = MaterialTheme.typography.bodyLarge); Spacer(Modifier.height(8.dp)) }
        item { FeatureCard(Icons.Rounded.Podcasts, "Discover", "Search and add RSS shows without an account.") }
        item { FeatureCard(Icons.Rounded.Subscriptions, "Your shows", "Subscriptions, episode progress and show artwork.") }
        item { FeatureCard(Icons.Rounded.Download, "Offline", "Resumable episode downloads and storage controls.") }
    }
}

@Composable private fun TrackRow(track: Track, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).clickable(onClick = onClick).padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Artwork(track.artworkUri, 56.dp); Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
            Text(track.title, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.titleMedium)
            Text("${track.artist} · ${track.album}", maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall)
        }
        IconButton(onClick = onClick) { Icon(Icons.Rounded.PlayArrow, "Play") }
    }
}

@Composable private fun LibraryEntityRow(track: Track, mode: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Artwork(track.artworkUri, 58.dp); Column(Modifier.padding(horizontal = 12.dp)) { Text(if (mode == "Albums") track.album else track.artist, style = MaterialTheme.typography.titleMedium); Text(if (mode == "Albums") track.artist else track.album, style = MaterialTheme.typography.bodySmall) }
    }
}

@Composable private fun AlbumCard(track: Track) {
    Column(Modifier.fillMaxWidth()) { Artwork(track.artworkUri, 105.dp); Text(track.album, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelLarge); Text(track.artist, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelSmall) }
}

@Composable private fun Artwork(uri: String?, size: androidx.compose.ui.unit.Dp, modifier: Modifier = Modifier) {
    if (uri.isNullOrBlank()) Surface(Modifier.size(size), RoundedCornerShape(14.dp), tonalElevation = 2.dp) { Icon(Icons.Rounded.MusicNote, null, Modifier.padding(size / 3)) }
    else AsyncImage(model = uri, contentDescription = "Album artwork", modifier = modifier.size(size).clip(RoundedCornerShape(14.dp)), contentScale = ContentScale.Crop)
}

@Composable private fun MiniPlayer(track: Track, playing: Boolean, onOpen: () -> Unit, onPlayPause: () -> Unit, onNext: () -> Unit) {
    Surface(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 6.dp).clickable(onClick = onOpen), shape = RoundedCornerShape(20.dp), tonalElevation = 5.dp) {
        Row(Modifier.padding(7.dp), verticalAlignment = Alignment.CenterVertically) { Artwork(track.artworkUri, 46.dp); Column(Modifier.weight(1f).padding(horizontal = 10.dp)) { Text(track.title, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.titleSmall); Text(track.artist, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall) }; IconButton(onClick = onPlayPause) { Icon(if (playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, "Play or pause") }; IconButton(onClick = onNext) { Icon(Icons.Rounded.SkipNext, "Next") } }
    }
}

@Composable private fun NowPlaying(track: Track, playing: Boolean, onClose: () -> Unit, onPlayPause: () -> Unit, onNext: () -> Unit, onPrevious: () -> Unit, onShuffle: () -> Unit, onRepeat: () -> Unit) {
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
        Column(Modifier.fillMaxSize().padding(horizontal = 22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(Modifier.fillMaxWidth().padding(top = 18.dp), horizontalArrangement = Arrangement.SpaceBetween) { IconButton(onClick = onClose) { Icon(Icons.Rounded.KeyboardArrowDown, "Close player") }; Text("NOW PLAYING", Modifier.align(Alignment.CenterVertically), style = MaterialTheme.typography.labelMedium); Spacer(Modifier.size(48.dp)) }
            Spacer(Modifier.weight(.12f)); Artwork(track.artworkUri, 310.dp); Spacer(Modifier.height(26.dp))
            Column(Modifier.fillMaxWidth()) { Text(track.title, style = MaterialTheme.typography.headlineSmall, maxLines = 1, overflow = TextOverflow.Ellipsis); Text(track.artist, style = MaterialTheme.typography.titleMedium); Text(track.album, style = MaterialTheme.typography.bodyMedium) }
            Spacer(Modifier.height(22.dp)); Slider(value = 0f, onValueChange = {}, modifier = Modifier.fillMaxWidth()); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("0:00", style = MaterialTheme.typography.labelSmall); Text(formatDuration(track.durationMs), style = MaterialTheme.typography.labelSmall) }
            Spacer(Modifier.height(8.dp)); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) { IconButton(onClick = onShuffle) { Icon(Icons.Rounded.Shuffle, "Shuffle") }; IconButton(onClick = onPrevious) { Icon(Icons.Rounded.SkipPrevious, "Previous") }; FilledIconButton(onClick = onPlayPause, modifier = Modifier.size(64.dp)) { Icon(if (playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, "Play or pause") }; IconButton(onClick = onNext) { Icon(Icons.Rounded.SkipNext, "Next") }; IconButton(onClick = onRepeat) { Icon(Icons.Rounded.Repeat, "Repeat") } }
            Spacer(Modifier.weight(.12f))
        }
    }
}

@Composable private fun MusicDock(selected: Destination, onSelect: (Destination) -> Unit) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainer) {
        listOf(Destination.HOME to Icons.Rounded.Home, Destination.LIBRARY to Icons.Rounded.LibraryMusic, Destination.PODCASTS to Icons.Rounded.Podcasts, Destination.SEARCH to Icons.Rounded.Search).forEach { (d, icon) -> NavigationBarItem(selected = selected == d, onClick = { onSelect(d) }, icon = { Icon(icon, null) }, label = { Text(d.name.lowercase().replaceFirstChar { it.uppercase() }) }) }
    }
}

@Composable private fun SectionTitle(title: String, meta: String) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text(title, style = MaterialTheme.typography.titleLarge); Text(meta, style = MaterialTheme.typography.labelMedium) } }
@Composable private fun EmptyState() { Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) { Column(Modifier.padding(22.dp)) { Icon(Icons.Rounded.LibraryMusic, null); Spacer(Modifier.height(10.dp)); Text("No music found", style = MaterialTheme.typography.titleLarge); Text("Grant audio access and Music Gallery will scan the device library.", style = MaterialTheme.typography.bodyMedium) } } }
@Composable private fun FeatureCard(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, body: String) { Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) { Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, Modifier.size(28.dp)); Column(Modifier.padding(start = 16.dp)) { Text(title, style = MaterialTheme.typography.titleMedium); Text(body, style = MaterialTheme.typography.bodyMedium) } } } }
private fun formatDuration(ms: Long): String { val total = ms / 1000; return "%d:%02d".format(total / 60, total % 60) }
