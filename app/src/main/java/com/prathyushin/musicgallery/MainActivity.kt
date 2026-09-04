package com.prathyushin.musicgallery

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil3.compose.AsyncImage
import com.prathyushin.musicgallery.library.MusicScanner
import com.prathyushin.musicgallery.model.Track
import com.prathyushin.musicgallery.playback.PlaybackController
import com.prathyushin.musicgallery.ui.MusicGalleryTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) recreate()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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

private enum class Destination { HOME, LIBRARY, PODCASTS, SEARCH }

@Composable
fun MusicGalleryApp(activity: ComponentActivity) {
    var destination by remember { mutableStateOf(Destination.HOME) }
    var tracks by remember { mutableStateOf(emptyList<Track>()) }
    var currentIndex by remember { mutableIntStateOf(-1) }
    var showPlayer by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var libraryMode by remember { mutableStateOf("Songs") }
    val controller = remember { PlaybackController(activity.applicationContext) }
    val playing by controller.isPlaying.collectAsState()
    val currentMediaId by controller.currentMediaId.collectAsState()
    val current = tracks.firstOrNull { it.id.toString() == currentMediaId } ?: tracks.getOrNull(currentIndex)

    LaunchedEffect(Unit) {
        tracks = withContext(Dispatchers.IO) { MusicScanner(activity.contentResolver).scan() }
    }

    LaunchedEffect(currentMediaId, tracks) {
        currentIndex = tracks.indexOfFirst { it.id.toString() == currentMediaId }.takeIf { it >= 0 } ?: currentIndex
    }

    DisposableEffect(Unit) { onDispose { controller.release() } }

    fun playAt(index: Int) {
        if (index !in tracks.indices) return
        currentIndex = index
        controller.playQueue(tracks, index)
    }

    MusicGalleryTheme {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            Scaffold(
                containerColor = Color.Transparent,
                contentWindowInsets = WindowInsets(0),
                bottomBar = {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 10.dp)
                    ) {
                        AnimatedVisibility(
                            visible = current != null && !showPlayer,
                            enter = fadeIn() + slideInVertically { it / 2 },
                            exit = fadeOut() + slideOutVertically { it / 2 }
                        ) {
                            current?.let { track ->
                                FlowMiniPlayer(
                                    track = track,
                                    playing = playing,
                                    progress = if (track.durationMs > 0) controller.currentPosition().toFloat() / track.durationMs else 0f,
                                    onOpen = { showPlayer = true },
                                    onPlayPause = { if (playing) controller.pause() else controller.resume() },
                                    onNext = { controller.next() }
                                )
                            }
                        }
                        AnimatedVisibility(visible = !showPlayer) {
                            FloatingDock(destination) { destination = it }
                        }
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

            AnimatedVisibility(
                visible = showPlayer && current != null,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
            ) {
                current?.let { track ->
                    FlowNowPlaying(
                        track = track,
                        playing = playing,
                        controller = controller,
                        onClose = { showPlayer = false },
                        onNext = { controller.next() },
                        onPrevious = { controller.previous() },
                        onShuffle = { controller.toggleShuffle() },
                        onRepeat = { controller.toggleRepeat() }
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeScreen(tracks: List<Track>, onPlay: (Int) -> Unit, modifier: Modifier) {
    val albums = remember(tracks) { tracks.filter { it.album.isNotBlank() }.distinctBy { it.album } }
    LazyColumn(
        modifier.fillMaxSize().padding(horizontal = 22.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        contentPadding = PaddingValues(top = 28.dp, bottom = 150.dp)
    ) {
        item {
            Text("Listen now", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(4.dp))
            Text("Your library, in flow.", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold))
            Spacer(Modifier.height(6.dp))
            Text(
                if (tracks.isEmpty()) "Music Gallery will use the music already stored on this device."
                else "${tracks.size} songs · ${albums.size} albums",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (tracks.isEmpty()) {
            item { EmptyState() }
        } else {
            item { SectionHeader("Quick picks", "From your library") }
            item {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.height(240.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    userScrollEnabled = false
                ) {
                    items(tracks.take(4), key = { it.id }) { track ->
                        QuickPickCard(track) { onPlay(tracks.indexOf(track)) }
                    }
                }
            }
            item { SectionHeader("Recently available", "${tracks.size} songs") }
            items(tracks.take(10), key = { it.id }) { track ->
                TrackRow(track) { onPlay(tracks.indexOf(track)) }
            }
        }
    }
}

@Composable
private fun LibraryScreen(
    tracks: List<Track>,
    onPlay: (Int) -> Unit,
    mode: String,
    onMode: (String) -> Unit,
    modifier: Modifier
) {
    val modes = listOf("Songs", "Albums", "Artists")
    val displayed = when (mode) {
        "Albums" -> tracks.filter { it.album.isNotBlank() }.distinctBy { it.album }
        "Artists" -> tracks.filter { it.artist.isNotBlank() }.distinctBy { it.artist }
        else -> tracks
    }
    Column(modifier.fillMaxSize().padding(horizontal = 22.dp)) {
        Spacer(Modifier.height(28.dp))
        Text("Library", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold))
        Text("Everything Music Gallery can read on this device", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(18.dp))
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            modes.forEachIndexed { index, item ->
                SegmentedButton(
                    selected = mode == item,
                    onClick = { onMode(item) },
                    shape = SegmentedButtonDefaults.itemShape(index, modes.size)
                ) { Text(item) }
            }
        }
        Spacer(Modifier.height(14.dp))
        if (displayed.isEmpty()) {
            EmptyState()
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(bottom = 150.dp)
            ) {
                items(displayed, key = { "${mode}-${it.id}" }) { track ->
                    if (mode == "Songs") TrackRow(track) { onPlay(tracks.indexOf(track)) }
                    else LibraryEntityRow(track, mode)
                }
            }
        }
    }
}

@Composable
private fun SearchScreen(
    query: String,
    onQuery: (String) -> Unit,
    tracks: List<Track>,
    onPlay: (Int) -> Unit,
    modifier: Modifier
) {
    val results = remember(query, tracks) {
        if (query.isBlank()) emptyList() else tracks.filter {
            it.title.contains(query, true) || it.artist.contains(query, true) || it.album.contains(query, true)
        }
    }
    Column(modifier.fillMaxSize().padding(horizontal = 22.dp)) {
        Spacer(Modifier.height(28.dp))
        Text("Search", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold))
        Spacer(Modifier.height(14.dp))
        OutlinedTextField(
            value = query,
            onValueChange = onQuery,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(20.dp),
            leadingIcon = { Icon(Icons.Rounded.Search, null) },
            placeholder = { Text("Songs, artists or albums") }
        )
        Spacer(Modifier.height(18.dp))
        when {
            query.isBlank() -> Text("Search stays local: results come from your device library.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            results.isEmpty() -> EmptySearchState()
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp), contentPadding = PaddingValues(bottom = 150.dp)) {
                items(results, key = { it.id }) { track -> TrackRow(track) { onPlay(tracks.indexOf(track)) } }
            }
        }
    }
}

@Composable
private fun PodcastScreen(modifier: Modifier) {
    LazyColumn(
        modifier.fillMaxSize().padding(horizontal = 22.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 28.dp, bottom = 150.dp)
    ) {
        item {
            Text("Podcasts", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold))
            Spacer(Modifier.height(6.dp))
            Text("A dedicated space for RSS shows, subscriptions and offline listening.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item { FlowFeatureCard(Icons.Rounded.Podcasts, "Discover", "Connect a real RSS feed when podcast discovery is enabled.") }
        item { FlowFeatureCard(Icons.Rounded.Subscriptions, "Your shows", "Subscriptions and episode progress belong here without sample content.") }
        item { FlowFeatureCard(Icons.Rounded.Download, "Offline", "Downloaded episodes will appear here when download support is enabled.") }
    }
}

@Composable
private fun FlowMiniPlayer(
    track: Track,
    playing: Boolean,
    progress: Float,
    onOpen: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit
) {
    val height by animateDpAsState(if (playing) 62.dp else 58.dp, label = "miniHeight")
    Surface(
        modifier = Modifier.fillMaxWidth().height(height).clickable(onClick = onOpen),
        shape = RoundedCornerShape(22.dp),
        tonalElevation = 8.dp,
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Box {
            Row(Modifier.fillMaxSize().padding(7.dp), verticalAlignment = Alignment.CenterVertically) {
                Artwork(track.artworkUri, 48.dp)
                Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                    Text(track.title, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.titleSmall)
                    Text(track.artist, maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                }
                IconButton(onClick = onPlayPause) { Icon(if (playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, "Play or pause") }
                IconButton(onClick = onNext) { Icon(Icons.Rounded.SkipNext, "Next") }
            }
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(2.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = Color.Transparent
            )
        }
    }
}

@Composable
private fun FlowNowPlaying(
    track: Track,
    playing: Boolean,
    controller: PlaybackController,
    onClose: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onShuffle: () -> Unit,
    onRepeat: () -> Unit
) {
    var position by remember(track.id) { mutableLongStateOf(0L) }
    var duration by remember(track.id) { mutableLongStateOf(track.durationMs) }
    var dragging by remember { mutableStateOf(false) }

    LaunchedEffect(track.id, playing) {
        while (true) {
            if (!dragging) {
                position = controller.currentPosition().coerceAtLeast(0L)
                duration = controller.duration().takeIf { it > 0 } ?: track.durationMs
            }
            delay(400)
        }
    }

    val progress = if (duration > 0) (position.toFloat() / duration).coerceIn(0f, 1f) else 0f

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        if (!track.artworkUri.isNullOrBlank()) {
            AsyncImage(
                model = track.artworkUri,
                contentDescription = null,
                modifier = Modifier.fillMaxSize().blur(48.dp),
                contentScale = ContentScale.Crop,
                alpha = 0.16f
            )
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, MaterialTheme.colorScheme.background))))
        }
        Column(
            Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onClose) { Icon(Icons.Rounded.KeyboardArrowDown, "Close player") }
                Text("NOW PLAYING", style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.6.sp))
                IconButton(onClick = {}) { Icon(Icons.Rounded.MoreHoriz, "More options") }
            }
            Spacer(Modifier.weight(0.08f))
            Artwork(track.artworkUri, 320.dp, Modifier.clip(RoundedCornerShape(28.dp)))
            Spacer(Modifier.height(28.dp))
            Column(Modifier.fillMaxWidth()) {
                Text(track.title, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(4.dp))
                Text(track.artist, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(track.album, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Spacer(Modifier.height(22.dp))
            Slider(
                value = progress,
                onValueChange = {
                    dragging = true
                    position = (it * duration).toLong()
                },
                onValueChangeFinished = {
                    controller.seekTo(position)
                    dragging = false
                },
                modifier = Modifier.fillMaxWidth()
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(formatDuration(position), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(formatDuration(duration), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onShuffle) { Icon(Icons.Rounded.Shuffle, "Shuffle") }
                IconButton(onClick = onPrevious, modifier = Modifier.size(56.dp)) { Icon(Icons.Rounded.SkipPrevious, "Previous", Modifier.size(34.dp)) }
                FilledIconButton(onClick = { if (playing) controller.pause() else controller.resume() }, modifier = Modifier.size(70.dp), shape = CircleShape) {
                    Icon(if (playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, "Play or pause", Modifier.size(34.dp))
                }
                IconButton(onClick = onNext, modifier = Modifier.size(56.dp)) { Icon(Icons.Rounded.SkipNext, "Next", Modifier.size(34.dp)) }
                IconButton(onClick = onRepeat) { Icon(Icons.Rounded.Repeat, "Repeat") }
            }
            Spacer(Modifier.weight(0.1f))
        }
    }
}

@Composable
private fun FloatingDock(selected: Destination, onSelect: (Destination) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
        shape = RoundedCornerShape(26.dp),
        tonalElevation = 10.dp,
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 5.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            DockItem(Destination.HOME, Icons.Rounded.Home, "Home", selected, onSelect)
            DockItem(Destination.LIBRARY, Icons.Rounded.LibraryMusic, "Library", selected, onSelect)
            DockItem(Destination.PODCASTS, Icons.Rounded.Podcasts, "Podcasts", selected, onSelect)
            DockItem(Destination.SEARCH, Icons.Rounded.Search, "Search", selected, onSelect)
        }
    }
}

@Composable
private fun RowScope.DockItem(destination: Destination, icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, selected: Destination, onSelect: (Destination) -> Unit) {
    NavigationBarItem(
        selected = selected == destination,
        onClick = { onSelect(destination) },
        icon = { Icon(icon, null) },
        label = { Text(label) }
    )
}

@Composable
private fun TrackRow(track: Track, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).clickable(onClick = onClick).padding(7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Artwork(track.artworkUri, 56.dp)
        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
            Text(track.title.ifBlank { "Untitled track" }, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.titleMedium)
            Text(
                listOf(track.artist, track.album).filter { it.isNotBlank() }.joinToString(" · ").ifBlank { "Local audio" },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = onClick) { Icon(Icons.Rounded.PlayArrow, "Play") }
    }
}

@Composable
private fun QuickPickCard(track: Track, onClick: () -> Unit) {
    Surface(
        Modifier.fillMaxSize().clip(RoundedCornerShape(22.dp)).clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        tonalElevation = 4.dp
    ) {
        Box {
            Artwork(track.artworkUri, 170.dp, Modifier.fillMaxSize())
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.78f)))))
            Column(Modifier.align(Alignment.BottomStart).padding(13.dp)) {
                Text(track.title, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.titleSmall)
                Text(track.artist, color = Color.White.copy(alpha = .72f), maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun LibraryEntityRow(track: Track, mode: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
        Artwork(track.artworkUri, 58.dp)
        Column(Modifier.padding(horizontal = 12.dp)) {
            Text(if (mode == "Albums") track.album.ifBlank { "Unknown album" } else track.artist.ifBlank { "Unknown artist" }, style = MaterialTheme.typography.titleMedium)
            Text(if (mode == "Albums") track.artist.ifBlank { "Unknown artist" } else track.album.ifBlank { "Unknown album" }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun Artwork(uri: String?, size: androidx.compose.ui.unit.Dp, modifier: Modifier = Modifier) {
    if (uri.isNullOrBlank()) {
        Surface(modifier.size(size), RoundedCornerShape(18.dp), tonalElevation = 2.dp) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Icon(Icons.Rounded.MusicNote, "No artwork", Modifier.size(size / 3)) }
        }
    } else {
        AsyncImage(
            model = uri,
            contentDescription = "Album artwork",
            modifier = modifier.size(size).clip(RoundedCornerShape(18.dp)),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
private fun SectionHeader(title: String, meta: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold))
        Text(meta, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun EmptyState() {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(26.dp)) {
        Column(Modifier.padding(24.dp)) {
            Icon(Icons.Rounded.LibraryMusic, null, Modifier.size(30.dp))
            Spacer(Modifier.height(12.dp))
            Text("No music found", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold))
            Spacer(Modifier.height(5.dp))
            Text("Grant audio access and add supported audio files to the device. Music Gallery does not insert sample tracks.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun EmptySearchState() {
    Column(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Rounded.SearchOff, null, Modifier.size(34.dp))
        Spacer(Modifier.height(10.dp))
        Text("No matches", style = MaterialTheme.typography.titleMedium)
        Text("Try a different title, artist or album.", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun FlowFeatureCard(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, body: String) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(Modifier.size(48.dp), CircleShape, tonalElevation = 2.dp) {
                Box(contentAlignment = Alignment.Center) { Icon(icon, null) }
            }
            Column(Modifier.padding(start = 16.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
                Spacer(Modifier.height(3.dp))
                Text(body, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

private fun formatDuration(ms: Long): String {
    val totalSeconds = (ms.coerceAtLeast(0L) / 1000L).toInt()
    return "%d:%02d".format(totalSeconds / 60, totalSeconds % 60)
}
