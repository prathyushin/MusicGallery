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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Podcasts
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.alpha
import androidx.compose.ui.blur
import androidx.compose.ui.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
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
        if (!hasAudioPermission()) {
            val permission = if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_AUDIO else Manifest.permission.READ_EXTERNAL_STORAGE
            permissionLauncher.launch(permission)
        }
        setContent { MusicGalleryApp(this) }
    }

    private fun hasAudioPermission(): Boolean {
        val permission = if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_AUDIO else Manifest.permission.READ_EXTERNAL_STORAGE
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }
}

private enum class Destination(val label: String) {
    HOME("Home"), LIBRARY("Library"), PODCASTS("Podcasts"), SEARCH("Search")
}

@Composable
fun MusicGalleryApp(activity: ComponentActivity) {
    var destination by remember { mutableStateOf(Destination.HOME) }
    var tracks by remember { mutableStateOf(emptyList<Track>()) }
    var libraryMode by remember { mutableStateOf("Songs") }
    var query by remember { mutableStateOf("") }
    var showPlayer by remember { mutableStateOf(false) }
    var playbackPosition by remember { mutableLongStateOf(0L) }
    var playbackDuration by remember { mutableLongStateOf(0L) }

    val controller = remember { PlaybackController(activity.applicationContext) }
    val playing by controller.isPlaying.collectAsState()
    val currentMediaId by controller.currentMediaId.collectAsState()
    val shuffle by controller.shuffleEnabled.collectAsState()
    val repeat by controller.repeatMode.collectAsState()
    val current = tracks.firstOrNull { it.id.toString() == currentMediaId }

    LaunchedEffect(Unit) {
        tracks = withContext(Dispatchers.IO) { MusicScanner(activity.contentResolver).scan() }
    }

    LaunchedEffect(currentMediaId, playing, tracks) {
        while (current != null) {
            playbackPosition = controller.currentPosition()
            playbackDuration = controller.currentDuration().takeIf { it > 0 } ?: current.durationMs
            delay(250)
        }
    }

    DisposableEffect(Unit) { onDispose { controller.release() } }

    fun playAt(index: Int) {
        if (index in tracks.indices) {
            controller.playQueue(tracks, index)
            showPlayer = true
        }
    }

    MusicGalleryTheme {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            Scaffold(
                containerColor = Color.Transparent,
                contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0),
                bottomBar = {
                    AnimatedVisibility(
                        visible = !showPlayer,
                        enter = fadeIn() + slideInVertically { it / 2 },
                        exit = fadeOut() + slideOutVertically { it / 2 }
                    ) {
                        Column(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 12.dp, vertical = 8.dp)) {
                            AnimatedVisibility(visible = current != null, enter = fadeIn(), exit = fadeOut()) {
                                current?.let { track ->
                                    MiniPlayer(track, playing, playbackPosition, playbackDuration, { showPlayer = true }, { controller.togglePlayPause() }, { controller.next() })
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            NavigationDock(destination) { destination = it }
                        }
                    }
                }
            ) { padding ->
                when (destination) {
                    Destination.HOME -> HomeScreen(tracks, ::playAt, Modifier.padding(padding))
                    Destination.LIBRARY -> LibraryScreen(tracks, libraryMode, { libraryMode = it }, ::playAt, Modifier.padding(padding))
                    Destination.PODCASTS -> PodcastScreen(Modifier.padding(padding))
                    Destination.SEARCH -> SearchScreen(query, { query = it }, tracks, ::playAt, Modifier.padding(padding))
                }
            }

            AnimatedVisibility(
                visible = showPlayer && current != null,
                enter = fadeIn() + slideInVertically { it },
                exit = fadeOut() + slideOutVertically { it }
            ) {
                current?.let { track ->
                    NowPlaying(track, playing, playbackPosition, playbackDuration, shuffle, repeat != androidx.media3.common.Player.REPEAT_MODE_OFF,
                        { showPlayer = false }, { controller.togglePlayPause() }, { controller.previous() }, { controller.next() }, { controller.seekTo(it) }, { controller.toggleShuffle() }, { controller.toggleRepeat() })
                }
            }
        }
    }
}

@Composable
private fun HomeScreen(tracks: List<Track>, onPlay: (Int) -> Unit, modifier: Modifier) {
    val albums = remember(tracks) { tracks.map { it.album }.filter { it.isNotBlank() }.distinct() }
    LazyColumn(modifier.fillMaxSize().padding(horizontal = 22.dp), verticalArrangement = Arrangement.spacedBy(20.dp), contentPadding = PaddingValues(top = 26.dp, bottom = 28.dp)) {
        item {
            Text("LISTEN NOW", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
            Spacer(Modifier.height(5.dp))
            Text("Your library, in flow.", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold))
            Spacer(Modifier.height(6.dp))
            Text(if (tracks.isEmpty()) "Music Gallery uses music already stored on this device." else "${tracks.size} songs  ·  ${albums.size} albums", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (tracks.isEmpty()) {
            item { EmptyLibraryCard() }
        } else {
            item { SectionHeader("Quick picks", "From your library") }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp), contentPadding = PaddingValues(end = 12.dp)) {
                    itemsIndexed(tracks.take(8), key = { _, track -> track.id }) { _, track -> QuickPick(track, Modifier.width(178.dp)) { onPlay(tracks.indexOf(track)) } }
                }
            }
            item { SectionHeader("Recently available", "${tracks.size} songs") }
            items(tracks.take(25), key = { it.id }) { track -> TrackRow(track) { onPlay(tracks.indexOf(track)) } }
        }
    }
}

@Composable
private fun LibraryScreen(tracks: List<Track>, mode: String, onMode: (String) -> Unit, onPlay: (Int) -> Unit, modifier: Modifier) {
    val modes = listOf("Songs", "Albums", "Artists")
    val groups = remember(tracks, mode) {
        when (mode) {
            "Albums" -> tracks.groupBy { it.album.ifBlank { "Unknown album" } }.toList()
            "Artists" -> tracks.groupBy { it.artist.ifBlank { "Unknown artist" } }.toList()
            else -> emptyList()
        }
    }
    Column(modifier.fillMaxSize().padding(horizontal = 22.dp)) {
        Spacer(Modifier.height(26.dp))
        Text("Library", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold))
        Spacer(Modifier.height(4.dp))
        Text("Everything Music Gallery can read on this device", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(18.dp))
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            modes.forEachIndexed { index, item -> SegmentedButton(selected = mode == item, onClick = { onMode(item) }, shape = SegmentedButtonDefaults.itemShape(index, modes.size)) { Text(item) } }
        }
        Spacer(Modifier.height(12.dp))
        if (tracks.isEmpty()) {
            EmptyLibraryCard()
        } else if (mode == "Songs") {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp), contentPadding = PaddingValues(bottom = 28.dp)) {
                items(tracks, key = { it.id }) { track -> TrackRow(track) { onPlay(tracks.indexOf(track)) } }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp), contentPadding = PaddingValues(bottom = 28.dp)) {
                items(groups, key = { it.first }) { group -> EntityRow(group.first, group.second.size, group.second.first().artworkUri) { onPlay(tracks.indexOf(group.second.first())) } }
            }
        }
    }
}

@Composable
private fun SearchScreen(query: String, onQuery: (String) -> Unit, tracks: List<Track>, onPlay: (Int) -> Unit, modifier: Modifier) {
    val results = remember(query, tracks) { if (query.isBlank()) emptyList() else tracks.filter { it.title.contains(query, true) || it.artist.contains(query, true) || it.album.contains(query, true) } }
    Column(modifier.fillMaxSize().padding(horizontal = 22.dp)) {
        Spacer(Modifier.height(26.dp))
        Text("Search", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold))
        Spacer(Modifier.height(14.dp))
        OutlinedTextField(value = query, onValueChange = onQuery, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(20.dp), leadingIcon = { Icon(Icons.Rounded.Search, null) }, placeholder = { Text("Songs, artists or albums") })
        Spacer(Modifier.height(14.dp))
        when {
            query.isBlank() -> Text("Search stays local: results come from your device library.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            results.isEmpty() -> EmptySearch()
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp), contentPadding = PaddingValues(bottom = 28.dp)) { items(results, key = { it.id }) { track -> TrackRow(track) { onPlay(tracks.indexOf(track)) } } }
        }
    }
}

@Composable
private fun PodcastScreen(modifier: Modifier) {
    LazyColumn(modifier.fillMaxSize().padding(horizontal = 22.dp), verticalArrangement = Arrangement.spacedBy(14.dp), contentPadding = PaddingValues(top = 26.dp, bottom = 28.dp)) {
        item {
            Text("Podcasts", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold))
            Spacer(Modifier.height(5.dp))
            Text("A dedicated space for RSS shows, subscriptions and offline listening.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item { FeatureCard(Icons.Rounded.Podcasts, "Discover", "Connect a real RSS feed when podcast discovery is enabled.") }
        item { FeatureCard(Icons.Rounded.Person, "Your shows", "Subscriptions and episode progress will live here without sample content.") }
        item { FeatureCard(Icons.Rounded.Download, "Offline", "Downloaded episodes will appear here when download support is enabled.") }
    }
}

@Composable
private fun MiniPlayer(track: Track, playing: Boolean, position: Long, duration: Long, onOpen: () -> Unit, onPlayPause: () -> Unit, onNext: () -> Unit) {
    val progress = if (duration > 0) (position.toFloat() / duration).coerceIn(0f, 1f) else 0f
    Surface(modifier = Modifier.fillMaxWidth().height(68.dp).clickable(onClick = onOpen), shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh, tonalElevation = 4.dp) {
        Box {
            Row(Modifier.fillMaxSize().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Artwork(track.artworkUri, 52.dp)
                Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                    Text(track.title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold)
                    Text(track.artist, maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                }
                IconButton(onClick = onPlayPause) { Icon(if (playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, "Play or pause") }
                IconButton(onClick = onNext) { Icon(Icons.Rounded.SkipNext, "Next") }
            }
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(2.dp), color = MaterialTheme.colorScheme.primary, trackColor = Color.Transparent)
        }
    }
}

@Composable
private fun NowPlaying(track: Track, playing: Boolean, position: Long, duration: Long, shuffle: Boolean, repeat: Boolean, onClose: () -> Unit, onPlayPause: () -> Unit, onPrevious: () -> Unit, onNext: () -> Unit, onSeek: (Long) -> Unit, onShuffle: () -> Unit, onRepeat: () -> Unit) {
    var slider by remember(track.id, position) { mutableFloatStateOf(if (duration > 0) (position.toFloat() / duration).coerceIn(0f, 1f) else 0f) }
    val shownPosition = (slider * duration).toLong()
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        if (!track.artworkUri.isNullOrBlank()) {
            AsyncImage(model = track.artworkUri, contentDescription = null, modifier = Modifier.fillMaxSize().blur(55.dp).alpha(0.13f), contentScale = ContentScale.Crop)
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, MaterialTheme.colorScheme.background))))
        }
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onClose) { Icon(Icons.Rounded.KeyboardArrowDown, "Close player") }
                Text("NOW PLAYING", fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.6.sp)
                IconButton(onClick = {}) { Icon(Icons.Rounded.MoreHoriz, "More options") }
            }
            Spacer(Modifier.height(18.dp))
            Artwork(track.artworkUri, 318.dp, Modifier.clip(RoundedCornerShape(28.dp)))
            Spacer(Modifier.height(24.dp))
            Column(Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(track.title, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Spacer(Modifier.height(3.dp))
                        Text(track.artist, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    IconButton(onClick = {}) { Icon(Icons.Rounded.FavoriteBorder, "Favorite") }
                }
                Spacer(Modifier.height(14.dp))
                Slider(value = slider, onValueChange = { slider = it }, onValueChangeFinished = { onSeek((slider * duration).toLong()) }, enabled = duration > 0, modifier = Modifier.fillMaxWidth())
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(formatTime(shownPosition), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(formatTime(duration), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onShuffle) { Icon(Icons.Rounded.Shuffle, "Shuffle", tint = if (shuffle) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant) }
                IconButton(onClick = onPrevious, modifier = Modifier.size(58.dp)) { Icon(Icons.Rounded.SkipPrevious, "Previous", modifier = Modifier.size(34.dp)) }
                Surface(modifier = Modifier.size(76.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primary) {
                    IconButton(onClick = onPlayPause) { Icon(if (playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, "Play or pause", tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(36.dp)) }
                }
                IconButton(onClick = onNext, modifier = Modifier.size(58.dp)) { Icon(Icons.Rounded.SkipNext, "Next", modifier = Modifier.size(34.dp)) }
                IconButton(onClick = onRepeat) { Icon(Icons.Rounded.Repeat, "Repeat", tint = if (repeat) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        }
    }
}

@Composable
private fun NavigationDock(selected: Destination, onSelect: (Destination) -> Unit) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainer, tonalElevation = 0.dp, modifier = Modifier.clip(RoundedCornerShape(30.dp))) {
        Destination.values().forEach { destination ->
            NavigationBarItem(
                selected = selected == destination,
                onClick = { onSelect(destination) },
                icon = { Icon(when (destination) { Destination.HOME -> Icons.Rounded.Home; Destination.LIBRARY -> Icons.Rounded.LibraryMusic; Destination.PODCASTS -> Icons.Rounded.Podcasts; Destination.SEARCH -> Icons.Rounded.Search }, destination.label) },
                label = { Text(destination.label) },
                colors = NavigationBarItemDefaults.colors(indicatorColor = MaterialTheme.colorScheme.primaryContainer, selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer, selectedTextColor = MaterialTheme.colorScheme.onSurface, unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant, unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant)
            )
        }
    }
}

@Composable
private fun QuickPick(track: Track, modifier: Modifier, onClick: () -> Unit) {
    Surface(modifier = modifier.height(232.dp).clickable(onClick = onClick), shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh) {
        Column {
            Artwork(track.artworkUri, 178.dp, Modifier.clip(RoundedCornerShape(24.dp)))
            Column(Modifier.padding(horizontal = 12.dp, vertical = 9.dp)) {
                Text(track.title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold)
                Text(track.artist, maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun TrackRow(track: Track, onPlay: () -> Unit) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable(onClick = onPlay).padding(vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
        Artwork(track.artworkUri, 58.dp)
        Column(Modifier.weight(1f).padding(horizontal = 13.dp)) {
            Text(track.title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Medium)
            Text("${track.artist}  ·  ${formatTime(track.durationMs)}", maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
        }
        IconButton(onClick = onPlay) { Icon(Icons.Rounded.PlayArrow, "Play") }
    }
}

@Composable
private fun EntityRow(title: String, count: Int, artworkUri: String?, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick).padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Artwork(artworkUri, 58.dp)
        Column(Modifier.weight(1f).padding(horizontal = 13.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("$count songs", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
        }
        Icon(Icons.Rounded.PlayArrow, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(end = 10.dp))
    }
}

@Composable
private fun FeatureCard(icon: ImageVector, title: String, body: String) {
    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(26.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh) {
        Row(Modifier.padding(22.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(Modifier.size(58.dp), shape = CircleShape, color = MaterialTheme.colorScheme.surfaceContainer) { Icon(icon, null, modifier = Modifier.padding(16.dp), tint = MaterialTheme.colorScheme.primary) }
            Spacer(Modifier.width(18.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                Spacer(Modifier.height(4.dp))
                Text(body, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 21.sp)
            }
        }
    }
}

@Composable
private fun EmptyLibraryCard() {
    Surface(Modifier.fillMaxWidth().padding(top = 12.dp), shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh) {
        Column(Modifier.padding(24.dp)) {
            Icon(Icons.Rounded.LibraryMusic, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(34.dp))
            Spacer(Modifier.height(14.dp))
            Text("Your library is empty", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
            Spacer(Modifier.height(6.dp))
            Text("Add music to the device and reopen Music Gallery. No sample songs are inserted.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun EmptySearch() {
    Column(Modifier.fillMaxWidth().padding(top = 36.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Rounded.Search, null, modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(10.dp))
        Text("No matches", fontWeight = FontWeight.SemiBold)
        Text("Try another title, artist or album.", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SectionHeader(title: String, trailing: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
        Text(title, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
        Text(trailing, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
    }
}

@Composable
private fun Artwork(uri: String?, size: Dp, modifier: Modifier = Modifier) {
    Box(modifier.size(size).clip(RoundedCornerShape(18.dp)).background(MaterialTheme.colorScheme.surfaceContainer), contentAlignment = Alignment.Center) {
        if (uri.isNullOrBlank()) {
            Icon(Icons.Rounded.Album, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(size * 0.36f))
        } else {
            AsyncImage(model = uri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        }
    }
}

private fun formatTime(ms: Long): String {
    if (ms <= 0L) return "0:00"
    val totalSeconds = ms / 1000L
    return "${totalSeconds / 60}:${(totalSeconds % 60).toString().padStart(2, '0')}"
}
