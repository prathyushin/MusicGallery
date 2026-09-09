package com.prathyushin.musicgallery

import android.Manifest
import android.content.Context
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
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Home
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
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
import androidx.compose.ui.platform.LocalContext
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
    val context = LocalContext.current
    var destination by remember { mutableStateOf(Destination.HOME) }
    var tracks by remember { mutableStateOf(emptyList<Track>()) }
    var libraryMode by remember { mutableStateOf("Songs") }
    var homeFilter by remember { mutableStateOf("All") }
    var query by remember { mutableStateOf("") }
    var showPlayer by remember { mutableStateOf(false) }
    var position by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }
    var favorites by remember { mutableStateOf(loadFavorites(context)) }

    val controller = remember { PlaybackController(context.applicationContext) }
    val playing by controller.isPlaying.collectAsState()
    val currentMediaId by controller.currentMediaId.collectAsState()
    val shuffle by controller.shuffleEnabled.collectAsState()
    val repeatMode by controller.repeatMode.collectAsState()
    val current = tracks.firstOrNull { it.id.toString() == currentMediaId }

    LaunchedEffect(Unit) {
        tracks = withContext(Dispatchers.IO) { MusicScanner(activity.contentResolver).scan() }
    }

    LaunchedEffect(currentMediaId, playing, tracks) {
        while (currentMediaId != null) {
            position = controller.currentPosition()
            duration = controller.currentDuration().takeIf { it > 0L } ?: current?.durationMs ?: 0L
            delay(if (playing) 250L else 500L)
        }
        position = 0L
        duration = 0L
    }

    DisposableEffect(Unit) { onDispose { controller.release() } }

    fun playAt(index: Int, source: List<Track> = tracks) {
        if (index in source.indices) {
            controller.playQueue(source, index)
            showPlayer = true
        }
    }

    fun toggleFavorite(track: Track) {
        favorites = favorites.toMutableSet().apply {
            if (!add(track.id)) remove(track.id)
        }.toSet()
        saveFavorites(context, favorites)
    }

    MusicGalleryTheme {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            Scaffold(
                containerColor = Color.Transparent,
                contentWindowInsets = WindowInsets(0),
                bottomBar = {
                    AnimatedVisibility(
                        visible = !showPlayer,
                        enter = fadeIn() + slideInVertically { it / 2 },
                        exit = fadeOut() + slideOutVertically { it / 2 }
                    ) {
                        Column(
                            Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AnimatedVisibility(visible = current != null, enter = fadeIn(), exit = fadeOut()) {
                                current?.let { track ->
                                    MiniPlayer(track, playing, position, duration, { showPlayer = true }, controller::togglePlayPause, controller::next)
                                }
                            }
                            NavigationDock(destination) { destination = it }
                        }
                    }
                }
            ) { padding ->
                when (destination) {
                    Destination.HOME -> HomeScreen(tracks, homeFilter, { homeFilter = it }, ::playAt, { destination = Destination.LIBRARY }, Modifier.padding(padding))
                    Destination.LIBRARY -> LibraryScreen(tracks, libraryMode, { libraryMode = it }, ::playAt, favorites, ::toggleFavorite, Modifier.padding(padding))
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
                    NowPlaying(
                        track = track,
                        playing = playing,
                        position = position,
                        duration = duration,
                        shuffle = shuffle,
                        repeat = repeatMode != androidx.media3.common.Player.REPEAT_MODE_OFF,
                        favorite = favorites.contains(track.id),
                        onClose = { showPlayer = false },
                        onPlayPause = controller::togglePlayPause,
                        onPrevious = controller::previous,
                        onNext = controller::next,
                        onSeek = controller::seekTo,
                        onShuffle = controller::toggleShuffle,
                        onRepeat = controller::toggleRepeat,
                        onFavorite = { toggleFavorite(track) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeScreen(
    tracks: List<Track>,
    filter: String,
    onFilter: (String) -> Unit,
    onPlay: (Int, List<Track>) -> Unit,
    onOpenLibrary: () -> Unit,
    modifier: Modifier
) {
    val albums = remember(tracks) { tracks.groupBy { it.album.ifBlank { "Unknown album" } }.toList() }
    val artists = remember(tracks) { tracks.groupBy { it.artist.ifBlank { "Unknown artist" } }.toList() }
    val filtered = when (filter) {
        "Albums" -> albums.flatMap { it.second }.distinctBy { it.id }
        "Artists" -> artists.flatMap { it.second }.distinctBy { it.id }
        else -> tracks
    }
    val hero = tracks.firstOrNull()

    LazyColumn(
        modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        contentPadding = PaddingValues(start = 22.dp, end = 22.dp, top = 22.dp, bottom = 28.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text("MUSIC GALLERY", fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.8.sp, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(6.dp))
                    Text("Good music,\nno clutter.", style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold, lineHeight = 39.sp))
                    Spacer(Modifier.height(8.dp))
                    Text(if (tracks.isEmpty()) "A local-first player for the music already on your device." else "${tracks.size} songs  ·  ${albums.size} albums  ·  ${artists.size} artists", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Surface(Modifier.size(46.dp), shape = CircleShape, color = MaterialTheme.colorScheme.surfaceContainerHigh) {
                    IconButton(onClick = onOpenLibrary) { Icon(Icons.Rounded.LibraryMusic, "Open library") }
                }
            }
        }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(listOf("All", "Albums", "Artists")) { chip ->
                    FilterChip(selected = filter == chip, onClick = { onFilter(chip) }, label = { Text(chip) })
                }
            }
        }
        if (hero == null) {
            item { EmptyLibraryCard() }
        } else {
            item { SectionHeader("Featured from your library", "Local") }
            item { HeroTrackCard(hero) { onPlay(tracks.indexOf(hero), tracks) } }
            item { SectionHeader("Quick picks", "Your library") }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp), contentPadding = PaddingValues(end = 12.dp)) {
                    itemsIndexed(filtered.take(10), key = { _, track -> track.id }) { _, track ->
                        QuickPick(track, Modifier.width(168.dp)) { onPlay(tracks.indexOf(track), tracks) }
                    }
                }
            }
            if (albums.isNotEmpty()) {
                item { SectionHeader("Albums", "${albums.size}") }
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp), contentPadding = PaddingValues(end = 12.dp)) {
                        items(albums.take(8), key = { it.first }) { album ->
                            AlbumCard(album.first, album.second.size, album.second.first().artworkUri) { onPlay(tracks.indexOf(album.second.first()), tracks) }
                        }
                    }
                }
            }
            item { SectionHeader("Library picks", "Play anytime") }
            items(filtered.take(20), key = { it.id }) { track ->
                TrackRow(track) { onPlay(tracks.indexOf(track), tracks) }
            }
        }
    }
}

@Composable
private fun LibraryScreen(
    tracks: List<Track>,
    mode: String,
    onMode: (String) -> Unit,
    onPlay: (Int, List<Track>) -> Unit,
    favorites: Set<Long>,
    onFavorite: (Track) -> Unit,
    modifier: Modifier
) {
    val modes = listOf("Songs", "Albums", "Artists")
    val groups = remember(tracks, mode) {
        when (mode) {
            "Albums" -> tracks.groupBy { it.album.ifBlank { "Unknown album" } }.toList()
            "Artists" -> tracks.groupBy { it.artist.ifBlank { "Unknown artist" } }.toList()
            else -> emptyList()
        }
    }
    Column(modifier.fillMaxSize().padding(horizontal = 22.dp)) {
        Spacer(Modifier.height(22.dp))
        Text("Library", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold))
        Spacer(Modifier.height(4.dp))
        Text("Real media from this device", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(16.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(modes) { item -> FilterChip(selected = mode == item, onClick = { onMode(item) }, label = { Text(item) }) }
        }
        Spacer(Modifier.height(12.dp))
        if (tracks.isEmpty()) {
            EmptyLibraryCard()
        } else if (mode == "Songs") {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(2.dp), contentPadding = PaddingValues(bottom = 28.dp)) {
                items(tracks, key = { it.id }) { track ->
                    TrackRow(track, favorites.contains(track.id), onFavorite = { onFavorite(track) }) { onPlay(tracks.indexOf(track), tracks) }
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp), contentPadding = PaddingValues(bottom = 28.dp)) {
                items(groups, key = { it.first }) { group ->
                    EntityRow(group.first, group.second.size, group.second.first().artworkUri) { onPlay(tracks.indexOf(group.second.first()), tracks) }
                }
            }
        }
    }
}

@Composable
private fun SearchScreen(query: String, onQuery: (String) -> Unit, tracks: List<Track>, onPlay: (Int, List<Track>) -> Unit, modifier: Modifier) {
    val results = remember(query, tracks) { if (query.isBlank()) emptyList() else tracks.filter { it.title.contains(query, true) || it.artist.contains(query, true) || it.album.contains(query, true) } }
    Column(modifier.fillMaxSize().padding(horizontal = 22.dp)) {
        Spacer(Modifier.height(22.dp))
        Text("Search", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold))
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(value = query, onValueChange = onQuery, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(18.dp), leadingIcon = { Icon(Icons.Rounded.Search, null) }, placeholder = { Text("Songs, artists or albums") })
        Spacer(Modifier.height(14.dp))
        when {
            query.isBlank() -> SearchHint()
            results.isEmpty() -> EmptySearch()
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(2.dp), contentPadding = PaddingValues(bottom = 28.dp)) { items(results, key = { it.id }) { track -> TrackRow(track) { onPlay(tracks.indexOf(track), tracks) } } }
        }
    }
}

@Composable
private fun PodcastScreen(modifier: Modifier) {
    LazyColumn(modifier.fillMaxSize().padding(horizontal = 22.dp), verticalArrangement = Arrangement.spacedBy(14.dp), contentPadding = PaddingValues(top = 22.dp, bottom = 28.dp)) {
        item {
            Text("Podcasts", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold))
            Spacer(Modifier.height(5.dp))
            Text("A quiet space for podcast features as they become fully supported.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item { FeatureCard(Icons.Rounded.Podcasts, "RSS-ready", "The UI is reserved for real feeds, shows and episodes—not sample content.") }
        item { FeatureCard(Icons.Rounded.Person, "Your shows", "Subscriptions and episode progress will appear here when persistence is enabled.") }
        item { FeatureCard(Icons.Rounded.Download, "Offline listening", "Downloads will be surfaced here when podcast download support is ready.") }
    }
}

@Composable
private fun HeroTrackCard(track: Track, onClick: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth().height(286.dp).clickable(onClick = onClick), shape = RoundedCornerShape(30.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh) {
        Box {
            Artwork(track.artworkUri, 286.dp, Modifier.fillMaxSize(), track.title)
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.78f)))))
            Column(Modifier.align(Alignment.BottomStart).padding(22.dp)) {
                Text("PLAY FROM YOUR LIBRARY", color = Color.White.copy(alpha = 0.82f), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp)
                Spacer(Modifier.height(5.dp))
                Text(track.title, color = Color.White, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(3.dp))
                Text(track.artist, color = Color.White.copy(alpha = 0.8f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Surface(Modifier.align(Alignment.BottomEnd).padding(18.dp).size(54.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primary) {
                IconButton(onClick = onClick) { Icon(Icons.Rounded.PlayArrow, "Play", tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(28.dp)) }
            }
        }
    }
}

@Composable
private fun MiniPlayer(track: Track, playing: Boolean, position: Long, duration: Long, onOpen: () -> Unit, onPlayPause: () -> Unit, onNext: () -> Unit) {
    val progress = if (duration > 0) (position.toFloat() / duration).coerceIn(0f, 1f) else 0f
    Surface(modifier = Modifier.fillMaxWidth().height(70.dp).clickable(onClick = onOpen), shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh, tonalElevation = 3.dp) {
        Box {
            Row(Modifier.fillMaxSize().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Artwork(track.artworkUri, 54.dp, contentDescription = track.title)
                Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                    Text(track.title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold)
                    Text(track.artist, maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                }
                IconButton(onClick = onPlayPause) { Icon(if (playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, "Play or pause") }
                IconButton(onClick = onNext) { Icon(Icons.Rounded.SkipNext, "Next") }
            }
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(2.dp), color = MaterialTheme.colorScheme.primary, trackColor = Color.Transparent)
        }
    }
}

@Composable
private fun NowPlaying(
    track: Track,
    playing: Boolean,
    position: Long,
    duration: Long,
    shuffle: Boolean,
    repeat: Boolean,
    favorite: Boolean,
    onClose: () -> Unit,
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSeek: (Long) -> Unit,
    onShuffle: () -> Unit,
    onRepeat: () -> Unit,
    onFavorite: () -> Unit
) {
    var slider by remember(track.id, position) { mutableFloatStateOf(if (duration > 0) (position.toFloat() / duration).coerceIn(0f, 1f) else 0f) }
    val shownPosition = (slider * duration).toLong()
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        if (!track.artworkUri.isNullOrBlank()) {
            AsyncImage(model = track.artworkUri, contentDescription = null, modifier = Modifier.fillMaxSize().blur(60.dp).alpha(0.16f), contentScale = ContentScale.Crop)
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, MaterialTheme.colorScheme.background.copy(alpha = 0.96f)))))
        }
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onClose) { Icon(Icons.Rounded.ArrowBack, "Close player") }
                Text("NOW PLAYING", fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.7.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                IconButton(onClick = {}) { Icon(Icons.Rounded.MoreHoriz, "More options") }
            }
            Spacer(Modifier.height(16.dp))
            Artwork(track.artworkUri, 316.dp, Modifier.clip(RoundedCornerShape(30.dp)), track.title)
            Spacer(Modifier.height(22.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(track.title, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(3.dp))
                    Text(track.artist, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                IconButton(onClick = onFavorite) { Icon(if (favorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder, "Favorite", tint = if (favorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant) }
            }
            Spacer(Modifier.height(12.dp))
            Slider(value = slider, onValueChange = { slider = it }, onValueChangeFinished = { onSeek((slider * duration).toLong()) }, enabled = duration > 0, modifier = Modifier.fillMaxWidth())
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(formatTime(shownPosition), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(formatTime(duration), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onShuffle) { Icon(Icons.Rounded.Shuffle, "Shuffle", tint = if (shuffle) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant) }
                IconButton(onClick = onPrevious, modifier = Modifier.size(58.dp)) { Icon(Icons.Rounded.SkipPrevious, "Previous", modifier = Modifier.size(34.dp)) }
                Surface(Modifier.size(76.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primary) {
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
    NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainer, tonalElevation = 0.dp, modifier = Modifier.clip(RoundedCornerShape(28.dp))) {
        Destination.values().forEach { destination ->
            NavigationBarItem(
                selected = selected == destination,
                onClick = { onSelect(destination) },
                icon = { Icon(destination.icon(), destination.label) },
                label = { Text(destination.label, fontSize = 11.sp) },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

private fun Destination.icon(): ImageVector = when (this) {
    Destination.HOME -> Icons.Rounded.Home
    Destination.LIBRARY -> Icons.Rounded.LibraryMusic
    Destination.PODCASTS -> Icons.Rounded.Podcasts
    Destination.SEARCH -> Icons.Rounded.Search
}

@Composable
private fun QuickPick(track: Track, modifier: Modifier, onClick: () -> Unit) {
    Column(modifier.clickable(onClick = onClick)) {
        Artwork(track.artworkUri, 168.dp, contentDescription = track.title)
        Spacer(Modifier.height(9.dp))
        Text(track.title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(2.dp))
        Text(track.artist, maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
    }
}

@Composable
private fun AlbumCard(title: String, count: Int, artworkUri: String?, onClick: () -> Unit) {
    Column(Modifier.width(168.dp).clickable(onClick = onClick)) {
        Artwork(artworkUri, 168.dp, contentDescription = title)
        Spacer(Modifier.height(9.dp))
        Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold)
        Text("$count songs", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
    }
}

@Composable
private fun TrackRow(track: Track, favorite: Boolean = false, isPlaying: Boolean = false, onFavorite: (() -> Unit)? = null, onPlay: () -> Unit) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).clickable(onClick = onPlay).padding(vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
        Artwork(track.artworkUri, 58.dp, contentDescription = track.title)
        Column(Modifier.weight(1f).padding(horizontal = 13.dp)) {
            Text(track.title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = if (isPlaying) FontWeight.Bold else FontWeight.Medium, color = if (isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
            Text("${track.artist}  ·  ${formatTime(track.durationMs)}", maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        }
        if (onFavorite != null) {
            IconButton(onClick = onFavorite) { Icon(if (favorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder, "Favorite", tint = if (favorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant) }
        }
        IconButton(onClick = onPlay) { Icon(if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, if (isPlaying) "Pause" else "Play") }
    }
}

@Composable
private fun EntityRow(title: String, count: Int, artworkUri: String?, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).clickable(onClick = onClick).padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Artwork(artworkUri, 58.dp, contentDescription = title)
        Column(Modifier.weight(1f).padding(horizontal = 13.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("$count songs", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        }
        IconButton(onClick = onClick) { Icon(Icons.Rounded.PlayArrow, "Play") }
    }
}

@Composable
private fun FeatureCard(icon: ImageVector, title: String, body: String) {
    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(26.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh) {
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(Modifier.size(56.dp), shape = CircleShape, color = MaterialTheme.colorScheme.surfaceContainer) { Icon(icon, null, modifier = Modifier.padding(15.dp), tint = MaterialTheme.colorScheme.primary) }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                Spacer(Modifier.height(4.dp))
                Text(body, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 20.sp)
            }
        }
    }
}

@Composable
private fun EmptyLibraryCard() {
    Surface(Modifier.fillMaxWidth().padding(top = 6.dp), shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh) {
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
private fun SearchHint() {
    Column(Modifier.fillMaxWidth().padding(top = 30.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Rounded.Search, null, modifier = Modifier.size(38.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(10.dp))
        Text("Search your music", fontWeight = FontWeight.SemiBold)
        Text("Results stay on this device.", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun EmptySearch() {
    Column(Modifier.fillMaxWidth().padding(top = 30.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Rounded.Search, null, modifier = Modifier.size(38.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(10.dp))
        Text("No matches", fontWeight = FontWeight.SemiBold)
        Text("Try another title, artist or album.", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SectionHeader(title: String, trailing: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
        Text(title, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
        Text(trailing, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
    }
}

@Composable
private fun Artwork(uri: String?, size: Dp, modifier: Modifier = Modifier, contentDescription: String? = null) {
    Box(modifier.size(size).clip(RoundedCornerShape(18.dp)).background(MaterialTheme.colorScheme.surfaceContainer), contentAlignment = Alignment.Center) {
        if (uri.isNullOrBlank()) {
            Icon(Icons.Rounded.Album, contentDescription, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(size * 0.36f))
        } else {
            AsyncImage(model = uri, contentDescription = contentDescription, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        }
    }
}

private fun formatTime(ms: Long): String {
    if (ms <= 0L) return "0:00"
    val totalSeconds = ms / 1000L
    return "${totalSeconds / 60}:${(totalSeconds % 60).toString().padStart(2, '0')}"
}

private const val FAVORITES_PREFS = "music_gallery_preferences"
private const val FAVORITES_KEY = "favorite_track_ids"

private fun loadFavorites(context: Context): Set<Long> = context.getSharedPreferences(FAVORITES_PREFS, Context.MODE_PRIVATE)
    .getStringSet(FAVORITES_KEY, emptySet())
    ?.mapNotNull { it.toLongOrNull() }
    ?.toSet()
    ?: emptySet()

private fun saveFavorites(context: Context, ids: Set<Long>) {
    context.getSharedPreferences(FAVORITES_PREFS, Context.MODE_PRIVATE).edit()
        .putStringSet(FAVORITES_KEY, ids.map(Long::toString).toSet())
        .apply()
}
