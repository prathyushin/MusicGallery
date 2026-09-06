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
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PlayCircleOutline
import androidx.compose.material.icons.rounded.Podcasts
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.Subscriptions
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.prathyushin.musicgallery.library.MusicScanner
import com.prathyushin.musicgallery.model.Track
import com.prathyushin.musicgallery.playback.PlaybackController
import com.prathyushin.musicgallery.ui.MusicGalleryTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.math.absoluteValue

private val MgBackground = Color(0xFF070709)
private val MgGlass = Color(0x1AFFFFFF)
private val MgBorder = Color(0x30FFFFFF)
private val MgText = Color(0xFFF7F7F7)
private val MgMuted = Color(0xFF9B9BA3)
private val MgAura = Color(0xFFB44CFF)
private val MgAccent = Color(0xFFD7FF35)

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
        val index = tracks.indexOfFirst { it.id.toString() == currentMediaId }
        if (index >= 0) currentIndex = index
    }

    DisposableEffect(Unit) { onDispose { controller.release() } }

    fun playAt(index: Int) {
        if (index !in tracks.indices) return
        currentIndex = index
        controller.playQueue(tracks, index)
    }

    MusicGalleryTheme {
        Box(Modifier.fillMaxSize().background(MgBackground)) {
            AuraBackground()
            Scaffold(
                containerColor = Color.Transparent,
                contentWindowInsets = WindowInsets(0),
                bottomBar = {
                    if (!showPlayer) {
                        Column(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 10.dp, vertical = 8.dp)) {
                            AnimatedVisibility(
                                visible = current != null,
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
                                        onNext = controller::next
                                    )
                                }
                            }
                            Spacer(Modifier.height(8.dp))
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
                enter = fadeIn() + slideInVertically(initialOffsetY = { it }, animationSpec = spring(stiffness = Spring.StiffnessLow)),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it }, animationSpec = spring(stiffness = Spring.StiffnessLow))
            ) {
                current?.let { track ->
                    FlowNowPlaying(
                        tracks = tracks,
                        currentIndex = currentIndex,
                        track = track,
                        playing = playing,
                        controller = controller,
                        onClose = { showPlayer = false },
                        onPageSelected = ::playAt
                    )
                }
            }
        }
    }
}

@Composable
private fun AuraBackground() {
    val transition = rememberInfiniteTransition(label = "aura")
    val scale by transition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(tween(6500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "auraScale"
    )
    val drift by transition.animateFloat(
        initialValue = -35f,
        targetValue = 45f,
        animationSpec = infiniteRepeatable(tween(8000, easing = LinearEasing), RepeatMode.Reverse),
        label = "auraDrift"
    )
    Box(Modifier.fillMaxSize().graphicsLayer { translationX = drift }) {
        Box(
            Modifier.offset((-90).dp, (-70).dp).size(390.dp)
                .graphicsLayer { scaleX = scale; scaleY = scale }
                .blur(110.dp)
                .background(Brush.radialGradient(listOf(MgAura.copy(alpha = 0.42f), Color.Transparent)), CircleShape)
        )
        Box(
            Modifier.align(Alignment.BottomEnd).offset(100.dp, 80.dp).size(330.dp)
                .blur(110.dp)
                .background(Brush.radialGradient(listOf(MgAccent.copy(alpha = 0.13f), Color.Transparent)), CircleShape)
        )
    }
}

@Composable
private fun FloatingDock(destination: Destination, onDestination: (Destination) -> Unit) {
    val items = listOf(
        Destination.HOME to Icons.Rounded.Home,
        Destination.LIBRARY to Icons.Rounded.LibraryMusic,
        Destination.PODCASTS to Icons.Rounded.Podcasts,
        Destination.SEARCH to Icons.Rounded.Search
    )
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(34.dp)).background(MgGlass)
            .border(1.dp, MgBorder, RoundedCornerShape(34.dp)).padding(7.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { (item, icon) ->
            val selected = destination == item
            Box(
                Modifier.weight(1f).height(48.dp).clip(CircleShape)
                    .background(if (selected) MgAccent else Color.Transparent)
                    .clickable { onDestination(item) },
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, item.name, tint = if (selected) MgBackground else MgText, modifier = Modifier.size(23.dp))
            }
        }
    }
}

@Composable
private fun HomeScreen(tracks: List<Track>, onPlay: (Int) -> Unit, modifier: Modifier) {
    val albums = remember(tracks) { tracks.mapNotNull { it.album.takeIf(String::isNotBlank) }.distinct() }
    LazyColumn(
        modifier.fillMaxSize().padding(horizontal = 22.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        contentPadding = PaddingValues(top = 30.dp, bottom = 155.dp)
    ) {
        item {
            Text("MUSIC GALLERY", style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 2.sp), color = MgAccent)
            Spacer(Modifier.height(5.dp))
            Text("Your library,\nin flow.", style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold), color = MgText)
            Spacer(Modifier.height(8.dp))
            Text(
                if (tracks.isEmpty()) "Music Gallery reads music already stored on this device."
                else "${tracks.size} songs · ${albums.size} albums",
                style = MaterialTheme.typography.bodyLarge,
                color = MgMuted
            )
        }
        if (tracks.isEmpty()) {
            item { EmptyState() }
        } else {
            item { SectionHeader("Quick picks", "From your local library") }
            item {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.height(246.dp),
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
private fun LibraryScreen(tracks: List<Track>, onPlay: (Int) -> Unit, mode: String, onMode: (String) -> Unit, modifier: Modifier) {
    val modes = listOf("Songs", "Albums", "Artists")
    val displayed = when (mode) {
        "Albums" -> tracks.filter { it.album.isNotBlank() }.distinctBy { it.album }
        "Artists" -> tracks.filter { it.artist.isNotBlank() }.distinctBy { it.artist }
        else -> tracks
    }
    Column(modifier.fillMaxSize().padding(horizontal = 22.dp)) {
        Spacer(Modifier.height(28.dp))
        Text("Library", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold), color = MgText)
        Text("Everything Music Gallery can read on this device", color = MgMuted)
        Spacer(Modifier.height(18.dp))
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            modes.forEachIndexed { index, item ->
                SegmentedButton(selected = mode == item, onClick = { onMode(item) }, shape = SegmentedButtonDefaults.itemShape(index, modes.size)) { Text(item) }
            }
        }
        Spacer(Modifier.height(14.dp))
        if (displayed.isEmpty()) EmptyState()
        else LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp), contentPadding = PaddingValues(bottom = 155.dp)) {
            items(displayed, key = { "${mode}-${it.id}" }) { track ->
                if (mode == "Songs") TrackRow(track) { onPlay(tracks.indexOf(track)) } else LibraryEntityRow(track, mode)
            }
        }
    }
}

@Composable
private fun SearchScreen(query: String, onQuery: (String) -> Unit, tracks: List<Track>, onPlay: (Int) -> Unit, modifier: Modifier) {
    val results = remember(query, tracks) {
        if (query.isBlank()) emptyList() else tracks.filter {
            it.title.contains(query, true) || it.artist.contains(query, true) || it.album.contains(query, true)
        }
    }
    Column(modifier.fillMaxSize().padding(horizontal = 22.dp)) {
        Spacer(Modifier.height(28.dp))
        Text("Search", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold), color = MgText)
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
            query.isBlank() -> Text("Search is local. Results come from your device library.", color = MgMuted)
            results.isEmpty() -> EmptySearchState()
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp), contentPadding = PaddingValues(bottom = 155.dp)) {
                items(results, key = { it.id }) { track -> TrackRow(track) { onPlay(tracks.indexOf(track)) } }
            }
        }
    }
}

@Composable
private fun PodcastScreen(modifier: Modifier) {
    LazyColumn(modifier.fillMaxSize().padding(horizontal = 22.dp), verticalArrangement = Arrangement.spacedBy(14.dp), contentPadding = PaddingValues(top = 28.dp, bottom = 155.dp)) {
        item {
            Text("Podcasts", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold), color = MgText)
            Spacer(Modifier.height(6.dp))
            Text("Podcast discovery and subscriptions appear here when backed by real RSS data.", color = MgMuted)
        }
        item { FlowFeatureCard(Icons.Rounded.Podcasts, "RSS-first", "Only connected feeds and real shows become podcast content.") }
        item { FlowFeatureCard(Icons.Rounded.Subscriptions, "Subscriptions", "Subscribed shows and episode progress belong here once persisted.") }
        item { FlowFeatureCard(Icons.Rounded.Download, "Offline", "Downloaded episodes can be surfaced when download support is enabled.") }
    }
}

@Composable
private fun FlowMiniPlayer(track: Track, playing: Boolean, progress: Float, onOpen: () -> Unit, onPlayPause: () -> Unit, onNext: () -> Unit) {
    val height by animateDpAsState(if (playing) 62.dp else 58.dp, label = "miniHeight")
    Surface(
        modifier = Modifier.fillMaxWidth().height(height).clickable(onClick = onOpen),
        shape = RoundedCornerShape(22.dp),
        tonalElevation = 0.dp,
        color = MgGlass
    ) {
        Box {
            Row(Modifier.fillMaxSize().padding(7.dp), verticalAlignment = Alignment.CenterVertically) {
                Artwork(track.artworkUri, 48.dp)
                Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                    Text(track.title, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.titleSmall, color = MgText)
                    Text(track.artist, maxLines = 1, overflow = TextOverflow.Ellipsis, color = MgMuted, style = MaterialTheme.typography.bodySmall)
                }
                IconButton(onClick = onPlayPause) { Icon(if (playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, "Play or pause", tint = MgText) }
                IconButton(onClick = onNext) { Icon(Icons.Rounded.SkipNext, "Next", tint = MgText) }
            }
            LinearProgressIndicator(progress = { progress.coerceIn(0f, 1f) }, modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(2.dp), color = MgAccent, trackColor = Color.Transparent)
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun FlowNowPlaying(tracks: List<Track>, currentIndex: Int, track: Track, playing: Boolean, controller: PlaybackController, onClose: () -> Unit, onPageSelected: (Int) -> Unit) {
    if (tracks.isEmpty()) return
    val pagerState = rememberPagerState(initialPage = currentIndex.coerceIn(0, tracks.lastIndex), pageCount = { tracks.size })
    var position by remember(track.id) { mutableLongStateOf(0L) }
    var duration by remember(track.id) { mutableLongStateOf(track.durationMs) }
    var dragging by remember { mutableStateOf(false) }

    LaunchedEffect(pagerState.currentPage) {
        val page = pagerState.currentPage
        if (page in tracks.indices && tracks[page].id != track.id) onPageSelected(page)
    }
    LaunchedEffect(track.id, playing) {
        while (true) {
            if (!dragging) {
                position = controller.currentPosition().coerceAtLeast(0L)
                duration = controller.duration().takeIf { it > 0 } ?: track.durationMs
            }
            delay(300)
        }
    }
    LaunchedEffect(track.id) {
        val page = tracks.indexOfFirst { it.id == track.id }
        if (page >= 0 && pagerState.currentPage != page) pagerState.scrollToPage(page)
    }

    val progress = if (duration > 0) (position.toFloat() / duration).coerceIn(0f, 1f) else 0f
    Box(Modifier.fillMaxSize().background(MgBackground)) {
        AsyncImage(model = track.artworkUri, contentDescription = null, modifier = Modifier.fillMaxSize().blur(64.dp).graphicsLayer { alpha = 0.22f }, contentScale = ContentScale.Crop)
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, MgBackground.copy(0.58f), MgBackground))))
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onClose) { Icon(Icons.Rounded.KeyboardArrowDown, "Close player", tint = MgText) }
                Text("NOW PLAYING", style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 2.sp), color = MgText)
                Spacer(Modifier.size(48.dp))
            }
            Spacer(Modifier.height(18.dp))
            HorizontalPager(state = pagerState, contentPadding = PaddingValues(horizontal = 42.dp), pageSpacing = 14.dp, modifier = Modifier.fillMaxWidth().height(380.dp)) { page ->
                val pageTrack = tracks[page]
                val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                val absoluteOffset = pageOffset.absoluteValue.coerceIn(0f, 1f)
                val scale = lerp(0.82f, 1f, 1f - absoluteOffset)
                val alpha = lerp(0.42f, 1f, 1f - absoluteOffset)
                val rotationY = (pageOffset * 25f).coerceIn(-25f, 25f)
                Box(
                    Modifier.fillMaxSize().graphicsLayer { scaleX = scale; scaleY = scale; this.alpha = alpha; this.rotationY = rotationY; cameraDistance = 16f * density }
                        .clip(RoundedCornerShape(30.dp)).border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(30.dp))
                ) {
                    Artwork(pageTrack.artworkUri, 380.dp, Modifier.fillMaxSize())
                    Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f)))).padding(22.dp)) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(pageTrack.title, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = MgText, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(pageTrack.artist, style = MaterialTheme.typography.bodyMedium, color = Color.LightGray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
            Spacer(Modifier.weight(1f))
            Surface(Modifier.fillMaxWidth().padding(bottom = 22.dp), shape = RoundedCornerShape(34.dp), color = MgGlass, tonalElevation = 0.dp) {
                Column(Modifier.padding(horizontal = 22.dp, vertical = 18.dp)) {
                    Slider(
                        value = progress,
                        onValueChange = { dragging = true; position = (it * duration).toLong() },
                        onValueChangeFinished = { dragging = false; controller.seekTo(position) },
                        colors = SliderDefaults.colors(thumbColor = MgText, activeTrackColor = MgAccent, inactiveTrackColor = Color.White.copy(alpha = 0.16f))
                    )
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(formatDuration(position), color = MgMuted, style = MaterialTheme.typography.labelSmall)
                        Text(formatDuration(duration), color = MgMuted, style = MaterialTheme.typography.labelSmall)
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { controller.toggleShuffle() }) { Icon(Icons.Rounded.Shuffle, "Shuffle", tint = MgText) }
                        IconButton(onClick = { controller.previous() }) { Icon(Icons.Rounded.SkipPrevious, "Previous", tint = MgText, modifier = Modifier.size(34.dp)) }
                        Box(Modifier.size(68.dp).clip(CircleShape).background(MgAccent), contentAlignment = Alignment.Center) {
                            IconButton(onClick = { if (playing) controller.pause() else controller.resume() }, modifier = Modifier.fillMaxSize()) { Icon(if (playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, "Play or pause", tint = MgBackground, modifier = Modifier.size(34.dp)) }
                        }
                        IconButton(onClick = { controller.next() }) { Icon(Icons.Rounded.SkipNext, "Next", tint = MgText, modifier = Modifier.size(34.dp)) }
                        IconButton(onClick = { controller.toggleRepeat() }) { Icon(Icons.Rounded.Repeat, "Repeat", tint = MgText) }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickPickCard(track: Track, onClick: () -> Unit) {
    Surface(Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(24.dp), color = MgGlass, tonalElevation = 0.dp) {
        Row(Modifier.fillMaxSize().padding(9.dp), verticalAlignment = Alignment.CenterVertically) {
            Artwork(track.artworkUri, 76.dp)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(track.title, maxLines = 2, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = MgText)
                Text(track.artist, maxLines = 1, overflow = TextOverflow.Ellipsis, color = MgMuted, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun TrackRow(track: Track, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
        Artwork(track.artworkUri, 58.dp)
        Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
            Text(track.title, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold), color = MgText)
            Text("${track.artist} · ${track.album}", maxLines = 1, overflow = TextOverflow.Ellipsis, color = MgMuted, style = MaterialTheme.typography.bodySmall)
        }
        Icon(Icons.Rounded.PlayCircleOutline, "Play", tint = MgText)
    }
}

@Composable
private fun LibraryEntityRow(track: Track, mode: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
        Artwork(track.artworkUri, 58.dp)
        Column(Modifier.padding(horizontal = 14.dp)) {
            Text(if (mode == "Albums") track.album else track.artist, color = MgText, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(if (mode == "Albums") track.artist else track.album, color = MgMuted, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun FlowFeatureCard(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, body: String) {
    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), color = MgGlass, tonalElevation = 0.dp) {
        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(50.dp).clip(CircleShape).background(MgAccent.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) { Icon(icon, null, tint = MgAccent) }
            Spacer(Modifier.width(14.dp))
            Column { Text(title, color = MgText, fontWeight = FontWeight.Bold); Spacer(Modifier.height(3.dp)); Text(body, color = MgMuted, style = MaterialTheme.typography.bodyMedium) }
        }
    }
}

@Composable
private fun EmptyState() {
    Box(Modifier.fillMaxWidth().padding(vertical = 70.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Rounded.LibraryMusic, null, tint = MgMuted, modifier = Modifier.size(44.dp))
            Spacer(Modifier.height(12.dp))
            Text("No local music found", color = MgText, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(5.dp))
            Text("Add music to the device and reopen Music Gallery.", color = MgMuted)
        }
    }
}

@Composable
private fun EmptySearchState() {
    Box(Modifier.fillMaxWidth().padding(vertical = 70.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Rounded.SearchOff, null, tint = MgMuted, modifier = Modifier.size(44.dp))
            Spacer(Modifier.height(12.dp))
            Text("No matches in your library", color = MgText, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String) {
    Column {
        Text(title, color = MgText, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(subtitle, color = MgMuted, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun Artwork(uri: String?, size: Dp, modifier: Modifier = Modifier) {
    if (uri.isNullOrBlank()) {
        Box(
            modifier.size(size).clip(RoundedCornerShape(16.dp)).background(
                Brush.linearGradient(listOf(Color.White.copy(alpha = 0.08f), Color.White.copy(alpha = 0.02f)))
            ),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.MusicNote, null, tint = MgMuted, modifier = Modifier.size(size / 3))
        }
    } else {
        AsyncImage(
            model = uri,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier.size(size).clip(RoundedCornerShape(16.dp))
        )
    }
}

private fun formatDuration(ms: Long): String {
    if (ms <= 0L) return "0:00"
    val totalSeconds = ms / 1000L
    return "%d:%02d".format(totalSeconds / 60L, totalSeconds % 60L)
}
