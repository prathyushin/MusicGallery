package com.prathyushin.musicgallery

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.prathyushin.musicgallery.library.MusicScanner
import com.prathyushin.musicgallery.model.Track
import com.prathyushin.musicgallery.playback.PlaybackController
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

private val Ink = Color(0xFFF7F7F8)
private val Muted = Color(0xFFA7A7AE)
private val Background = Color(0xFF09090B)
private val SurfaceDark = Color(0xFF151518)
private val SurfaceLight = Color(0xFF202024)
private val Accent = Color(0xFFD4FF26)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MusicGalleryApp() }
    }
}

@Composable
private fun MusicGalleryApp() {
    val navController = rememberNavController()
    val context = androidx.compose.ui.platform.LocalContext.current
    val playback = remember { PlaybackController(context.applicationContext) }
    val playerState by playback.state.collectAsState()
    var tracks by remember { mutableStateOf<List<Track>>(emptyList()) }
    var permissionGranted by remember {
        mutableStateOf(hasAudioPermission(context))
    }
    var showPlayer by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        permissionGranted = granted
        if (granted) tracks = MusicScanner(context.contentResolver).scan()
    }

    LaunchedEffect(permissionGranted) {
        if (!permissionGranted) {
            permissionLauncher.launch(requiredAudioPermission())
        } else {
            tracks = MusicScanner(context.contentResolver).scan()
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            playback.updatePosition()
            delay(500)
        }
    }

    DisposableEffect(Unit) { onDispose { playback.release() } }

    MaterialTheme(colorScheme = androidx.compose.material3.darkColorScheme(background = Background)) {
        Box(Modifier.fillMaxSize().background(Background)) {
            Scaffold(
                containerColor = Color.Transparent,
                bottomBar = {
                    Column(Modifier.navigationBarsPadding()) {
                        AnimatedVisibility(playerState.track != null && !showPlayer, enter = fadeIn(), exit = fadeOut()) {
                            MiniPlayer(
                                state = playerState,
                                onOpen = { showPlayer = true },
                                onPlayPause = playback::toggle,
                                onNext = playback::next
                            )
                        }
                        BottomNavigation(navController)
                    }
                }
            ) { padding ->
                NavHost(
                    navController = navController,
                    startDestination = "home",
                    modifier = Modifier.padding(padding)
                ) {
                    composable("home") {
                        HomeScreen(
                            tracks = tracks,
                            permissionGranted = permissionGranted,
                            onPlay = { track -> playback.play(track, tracks); showPlayer = true },
                            onOpenLibrary = { navController.navigate("library") }
                        )
                    }
                    composable("library") {
                        LibraryScreen(
                            tracks = tracks,
                            onPlay = { track -> playback.play(track, tracks) },
                            onRefresh = { tracks = MusicScanner(context.contentResolver).scan() }
                        )
                    }
                    composable("podcasts") { PodcastsScreen() }
                    composable("search") {
                        SearchScreen(tracks = tracks, onPlay = { track -> playback.play(track, tracks) })
                    }
                }
            }

            if (showPlayer && playerState.track != null) {
                NowPlayingScreen(
                    state = playerState,
                    onClose = { showPlayer = false },
                    onPlayPause = playback::toggle,
                    onPrevious = playback::previous,
                    onNext = playback::next,
                    onSeek = playback::seekTo
                )
            }
        }
    }
}

private fun hasAudioPermission(context: android.content.Context): Boolean =
    ContextCompat.checkSelfPermission(context, requiredAudioPermission()) == PackageManager.PERMISSION_GRANTED

private fun requiredAudioPermission(): String =
    if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_AUDIO
    else Manifest.permission.READ_EXTERNAL_STORAGE

@Composable
private fun HomeScreen(
    tracks: List<Track>,
    permissionGranted: Boolean,
    onPlay: (Track) -> Unit,
    onOpenLibrary: () -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(top = 52.dp, bottom = 24.dp, start = 20.dp, end = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Music Gallery", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = Ink)
            Text("Your music. Your library. Nothing in the way.", color = Muted, modifier = Modifier.padding(top = 6.dp))
            Spacer(Modifier.height(24.dp))
        }
        if (!permissionGranted) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = SurfaceDark)) {
                    Column(Modifier.padding(20.dp)) {
                        Text("Music access is required", color = Ink, fontWeight = FontWeight.Bold)
                        Text("Allow audio access to build your local library.", color = Muted, modifier = Modifier.padding(top = 6.dp))
                    }
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Recently added", style = MaterialTheme.typography.titleLarge, color = Ink, fontWeight = FontWeight.Bold)
                OutlinedButton(onClick = onOpenLibrary) { Text("See all") }
            }
        }
        if (tracks.isEmpty()) {
            item { EmptyState("No local music found yet.") }
        } else {
            items(tracks.take(8), key = { it.id }) { track -> TrackRow(track, onPlay) }
        }
    }
}

@Composable
private fun LibraryScreen(tracks: List<Track>, onPlay: (Track) -> Unit, onRefresh: () -> Unit) {
    LazyColumn(
        contentPadding = PaddingValues(top = 52.dp, bottom = 24.dp, start = 20.dp, end = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Library", style = MaterialTheme.typography.headlineLarge, color = Ink, fontWeight = FontWeight.Bold)
                    Text("${tracks.size} songs", color = Muted, modifier = Modifier.padding(top = 4.dp))
                }
                OutlinedButton(onClick = onRefresh) { Text("Refresh") }
            }
            Spacer(Modifier.height(18.dp))
        }
        if (tracks.isEmpty()) item { EmptyState("Your device has no music that Music Gallery can read.") }
        else items(tracks, key = { it.id }) { TrackRow(it, onPlay) }
    }
}

@Composable
private fun SearchScreen(tracks: List<Track>, onPlay: (Track) -> Unit) {
    var query by remember { mutableStateOf("") }
    val results = remember(query, tracks) {
        if (query.isBlank()) tracks else tracks.filter {
            "${it.title} ${it.artist} ${it.album}".contains(query, ignoreCase = true)
        }
    }
    LazyColumn(contentPadding = PaddingValues(top = 48.dp, bottom = 24.dp, start = 20.dp, end = 20.dp)) {
        item {
            Text("Search", style = MaterialTheme.typography.headlineLarge, color = Ink, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            TextField(
                value = query,
                onValueChange = { query = it },
                singleLine = true,
                placeholder = { Text("Songs, artists, albums") },
                leadingIcon = { Icon(Icons.Rounded.Search, null) },
                colors = TextFieldDefaults.colors(unfocusedContainerColor = SurfaceDark, focusedContainerColor = SurfaceLight)
            )
            Spacer(Modifier.height(18.dp))
        }
        items(results, key = { it.id }) { TrackRow(it, onPlay) }
    }
}

@Composable
private fun TrackRow(track: Track, onPlay: (Track) -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable { onPlay(track) }.padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AlbumArt(track.artworkUri, Modifier.size(62.dp))
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(track.title, color = Ink, fontWeight = FontWeight.SemiBold, maxLines = 1)
            Text("${track.artist} • ${track.album}", color = Muted, maxLines = 1, style = MaterialTheme.typography.bodySmall)
        }
        Text(formatDuration(track.durationMs), color = Muted, style = MaterialTheme.typography.labelSmall)
        Icon(Icons.Rounded.MoreVert, null, tint = Muted, modifier = Modifier.padding(start = 8.dp))
    }
}

@Composable
private fun MiniPlayer(
    state: com.prathyushin.musicgallery.playback.PlayerUiState,
    onOpen: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit
) {
    val track = state.track ?: return
    Surface(color = SurfaceDark, modifier = Modifier.fillMaxWidth().clickable { onOpen() }) {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            AlbumArt(track.artworkUri, Modifier.size(48.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(track.title, color = Ink, fontWeight = FontWeight.SemiBold, maxLines = 1)
                Text(track.artist, color = Muted, style = MaterialTheme.typography.bodySmall, maxLines = 1)
            }
            IconButton(onClick = onPlayPause) { Icon(if (state.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, null, tint = Ink) }
            IconButton(onClick = onNext) { Icon(Icons.Rounded.SkipNext, null, tint = Ink) }
        }
    }
}

@Composable
private fun BottomNavigation(navController: NavHostController) {
    val current = navController.currentBackStackEntryAsState().value?.destination?.route
    val items = listOf("home" to Icons.Rounded.Home, "library" to Icons.Rounded.LibraryMusic, "podcasts" to Icons.Rounded.Mic, "search" to Icons.Rounded.Search)
    Surface(color = Background) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp), horizontalArrangement = Arrangement.SpaceAround) {
            items.forEach { (route, icon) ->
                val selected = current == route
                Column(Modifier.clip(RoundedCornerShape(18.dp)).clickable {
                    navController.navigate(route) { popUpTo("home") { saveState = true }; launchSingleTop = true; restoreState = true }
                }.padding(horizontal = 18.dp, vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(icon, null, tint = if (selected) Accent else Muted)
                    Text(route.replaceFirstChar { it.uppercase() }, color = if (selected) Ink else Muted, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
private fun NowPlayingScreen(
    state: com.prathyushin.musicgallery.playback.PlayerUiState,
    onClose: () -> Unit,
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSeek: (Long) -> Unit
) {
    val track = state.track ?: return
    val fraction = if (state.durationMs > 0) (state.positionMs.toFloat() / state.durationMs).coerceIn(0f, 1f) else 0f
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF24242A), Background)))) {
        Column(Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 30.dp)) {
            IconButton(onClick = onClose) { Icon(Icons.Rounded.ArrowBack, null, tint = Ink) }
            Spacer(Modifier.height(22.dp))
            AlbumArt(track.artworkUri, Modifier.fillMaxWidth().height(330.dp))
            Spacer(Modifier.height(28.dp))
            Text(track.title, style = MaterialTheme.typography.headlineSmall, color = Ink, fontWeight = FontWeight.Bold, maxLines = 2)
            Text(track.artist, style = MaterialTheme.typography.titleMedium, color = Muted, modifier = Modifier.padding(top = 5.dp))
            Spacer(Modifier.height(18.dp))
            Slider(value = fraction, onValueChange = { onSeek((it * state.durationMs).roundToInt().toLong()) })
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(formatDuration(state.positionMs), color = Muted, style = MaterialTheme.typography.labelSmall)
                Text(formatDuration(state.durationMs), color = Muted, style = MaterialTheme.typography.labelSmall)
            }
            Spacer(Modifier.height(18.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onPrevious) { Icon(Icons.Rounded.SkipPrevious, null, tint = Ink, modifier = Modifier.size(42.dp)) }
                Box(Modifier.size(76.dp).clip(CircleShape).background(Accent).clickable { onPlayPause() }, contentAlignment = Alignment.Center) {
                    Icon(if (state.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, null, tint = Background, modifier = Modifier.size(38.dp))
                }
                IconButton(onClick = onNext) { Icon(Icons.Rounded.SkipNext, null, tint = Ink, modifier = Modifier.size(42.dp)) }
            }
            Spacer(Modifier.weight(1f))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Icon(Icons.Rounded.QueueMusic, null, tint = Muted)
                Spacer(Modifier.width(8.dp))
                Text(if (state.queueSize > 1) "${state.queueSize} songs in queue" else "Single track", color = Muted)
            }
        }
    }
}

@Composable
private fun PodcastsScreen() {
    LazyColumn(contentPadding = PaddingValues(top = 52.dp, bottom = 24.dp, start = 20.dp, end = 20.dp)) {
        item {
            Text("Podcasts", style = MaterialTheme.typography.headlineLarge, color = Ink, fontWeight = FontWeight.Bold)
            Text("RSS-ready podcast space", color = Muted, modifier = Modifier.padding(top = 6.dp))
            Spacer(Modifier.height(24.dp))
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = SurfaceDark)) {
                Column(Modifier.padding(20.dp)) {
                    Text("Your podcast shelf", color = Ink, fontWeight = FontWeight.Bold)
                    Text("Add a podcast RSS feed to bring episodes into Music Gallery. Audio playback uses the same player architecture.", color = Muted, modifier = Modifier.padding(top = 8.dp))
                    Spacer(Modifier.height(14.dp))
                    Button(onClick = { }) { Text("Add feed") }
                }
            }
        }
    }
}

@Composable
private fun AlbumArt(uri: String?, modifier: Modifier) {
    Box(modifier.clip(RoundedCornerShape(16.dp)).background(SurfaceLight), contentAlignment = Alignment.Center) {
        if (uri.isNullOrBlank()) {
            Text("♪", color = Muted, style = MaterialTheme.typography.headlineMedium)
        } else {
            AsyncImage(model = uri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        }
    }
}

@Composable
private fun EmptyState(message: String) {
    Card(colors = CardDefaults.cardColors(containerColor = SurfaceDark), modifier = Modifier.fillMaxWidth()) {
        Text(message, color = Muted, modifier = Modifier.padding(20.dp))
    }
}

private fun formatDuration(ms: Long): String {
    val totalSeconds = (ms.coerceAtLeast(0L) / 1000L)
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return "%d:%02d".format(minutes, seconds)
}
