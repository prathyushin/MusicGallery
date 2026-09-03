package com.prathyushin.musicgallery

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.prathyushin.musicgallery.library.MusicScanner
import com.prathyushin.musicgallery.model.Track
import com.prathyushin.musicgallery.playback.PlaybackController
import com.prathyushin.musicgallery.ui.MusicGalleryTheme

class MainActivity : ComponentActivity() {
    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted -> if (granted) recreate() }
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

@Composable
fun MusicGalleryApp(activity: ComponentActivity) {
    var page by remember { mutableIntStateOf(0) }
    var settings by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var mood by remember { mutableStateOf("All") }
    var tracks by remember { mutableStateOf(emptyList<Track>()) }
    var current by remember { mutableStateOf<Track?>(null) }
    var playing by remember { mutableStateOf(false) }
    val controller = remember { PlaybackController(activity.applicationContext) }
    LaunchedEffect(Unit) { tracks = MusicScanner(activity.contentResolver).scan() }
    DisposableEffect(Unit) { onDispose { controller.release() } }
    fun play(track: Track) { track.contentUri?.let { controller.play(track, it); current = track; playing = true } }

    MusicGalleryTheme {
        Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
            Box(Modifier.fillMaxSize().padding(padding)) {
                when {
                    settings -> SettingsScreen(onBack = { settings = false })
                    page == 0 -> HomeScreen(tracks, mood, { mood = it }, ::play, onSettings = { settings = true })
                    page == 1 -> LibraryScreen(tracks, ::play)
                    page == 2 -> PodcastScreen()
                    else -> SearchScreen(query, { query = it }, tracks, ::play)
                }
                Column(Modifier.align(Alignment.BottomCenter).padding(horizontal = 12.dp, vertical = 10.dp)) {
                    current?.let { MiniPlayerBar(it, playing, { if (playing) controller.pause() else controller.resume(); playing = !playing }, { val i = tracks.indexOfFirst { t -> t.id == it.id }; if (i >= 0 && i + 1 < tracks.size) play(tracks[i + 1]) }) }
                    Spacer(Modifier.height(8.dp))
                    FloatingDock(page) { page = it; settings = false }
                }
            }
        }
    }
}

@Composable
private fun HomeScreen(tracks: List<Track>, mood: String, onMood: (String) -> Unit, onPlay: (Track) -> Unit, onSettings: () -> Unit) {
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 20.dp), contentPadding = PaddingValues(bottom = 150.dp)) {
        item {
            Spacer(Modifier.height(24.dp))
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Column { Text("Good listening", style = MaterialTheme.typography.labelLarge); Text("Music Gallery", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold)); Text("${AppVersion.NAME} · your music, your way", style = MaterialTheme.typography.bodySmall) }
                IconButton(onClick = onSettings) { Icon(Icons.Rounded.Settings, "Settings") }
            }
            Spacer(Modifier.height(22.dp)); Text("Quick picks", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)); Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { listOf("All", "Focus", "Chill", "Energy").forEach { FilterChip(selected = mood == it, onClick = { onMood(it) }, label = { Text(it) }) } }
            Spacer(Modifier.height(18.dp))
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest)) {
                Column(Modifier.padding(20.dp)) { Text("Made for your listening habit", style = MaterialTheme.typography.titleMedium); Spacer(Modifier.height(5.dp)); Text("Music Gallery will learn from recent plays, favorites and artists to shape better quick picks.", style = MaterialTheme.typography.bodyMedium); Spacer(Modifier.height(14.dp)); Text("Create a mix", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary) }
            }
            Spacer(Modifier.height(18.dp)); Text("Listening insights", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)); Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) { Insight("Today", "—", Icons.Rounded.Headphones); Insight("Top artist", "—", Icons.Rounded.Person); Insight("Favorites", "—", Icons.Rounded.Favorite) }
            Spacer(Modifier.height(24.dp)); Text(if (tracks.isEmpty()) "Your library" else "Recently added", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)); Spacer(Modifier.height(8.dp))
        }
        if (tracks.isEmpty()) item { EmptyLibraryCard() } else items(tracks.take(10), key = { it.id }) { TrackRow(it, onPlay) }
    }
}

@Composable private fun Insight(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) { Card(Modifier.weight(1f), shape = RoundedCornerShape(20.dp)) { Column(Modifier.padding(14.dp)) { Icon(icon, null, Modifier.size(20.dp)); Spacer(Modifier.height(8.dp)); Text(value, style = MaterialTheme.typography.titleMedium); Text(title, style = MaterialTheme.typography.labelSmall) } } }

@Composable private fun LibraryScreen(tracks: List<Track>, onPlay: (Track) -> Unit) { LazyColumn(Modifier.fillMaxSize().padding(20.dp), contentPadding = PaddingValues(bottom = 150.dp)) { item { Spacer(Modifier.height(24.dp)); Text("Library", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold)); Text("Songs · Albums · Artists · Playlists", style = MaterialTheme.typography.bodyMedium); Spacer(Modifier.height(18.dp)) }; if (tracks.isEmpty()) item { EmptyLibraryCard() } else items(tracks, key = { it.id }) { TrackRow(it, onPlay) } } }

@Composable private fun PodcastScreen() { LazyColumn(Modifier.fillMaxSize().padding(20.dp), contentPadding = PaddingValues(bottom = 150.dp)) { item { Spacer(Modifier.height(24.dp)); Text("Podcasts", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold)); Text("Your shows and episodes", style = MaterialTheme.typography.bodyMedium); Spacer(Modifier.height(20.dp)); Feature("Continue listening", "Resume your latest episode", Icons.Rounded.PlayCircle); Feature("Your shows", "Subscriptions and new episodes", Icons.Rounded.Podcasts); Feature("Downloads", "Reliable offline listening", Icons.Rounded.Download); Feature("Discover", "Find your next favorite show", Icons.Rounded.Explore) } } }

@Composable private fun SearchScreen(query: String, onQuery: (String) -> Unit, tracks: List<Track>, onPlay: (Track) -> Unit) { val results = tracks.filter { query.isNotBlank() && listOf(it.title, it.artist, it.album).any { v -> v.contains(query, true) } }; Column(Modifier.fillMaxSize().padding(20.dp)) { Spacer(Modifier.height(24.dp)); Text("Search", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold)); Spacer(Modifier.height(14.dp)); OutlinedTextField(query, onQuery, Modifier.fillMaxWidth(), singleLine = true, leadingIcon = { Icon(Icons.Rounded.Search, null) }, placeholder = { Text("Songs, artists, albums, podcasts") }); Spacer(Modifier.height(18.dp)); if (query.isBlank()) Text("Search across your music and podcast library.", style = MaterialTheme.typography.bodyLarge) else if (results.isEmpty()) Text("No music results", style = MaterialTheme.typography.titleMedium) else LazyColumn(contentPadding = PaddingValues(bottom = 150.dp)) { items(results, key = { it.id }) { TrackRow(it, onPlay) } } } }

@Composable private fun SettingsScreen(onBack: () -> Unit) { LazyColumn(Modifier.fillMaxSize().padding(horizontal = 20.dp), contentPadding = PaddingValues(bottom = 120.dp)) { item { Spacer(Modifier.height(20.dp)); Row(verticalAlignment = Alignment.CenterVertically) { IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, "Back") }; Text("Settings", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold)) }; Spacer(Modifier.height(18.dp)); SettingsGroup("Experience", listOf("Appearance" to "White, dark and custom themes", "Equalizer" to "Sound shaping", "Playback" to "Queue, crossfade and behavior")); SettingsGroup("Library", listOf("Storage" to "Scan and storage behavior", "Recommendations" to "Listening habits and mixes", "Listing habit" to "Listening history and insights")); SettingsGroup("System", listOf("Notifications" to "Player and episode alerts", "About" to "Music Gallery ${AppVersion.NAME}")) } } }

@Composable private fun SettingsGroup(title: String, rows: List<Pair<String,String>>) { Text(title.uppercase(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary); Spacer(Modifier.height(6.dp)); rows.forEach { (a,b) -> ListItem(headlineContent = { Text(a) }, supportingContent = { Text(b) }, leadingContent = { Icon(Icons.Rounded.ChevronRight, null) }); HorizontalDivider() }; Spacer(Modifier.height(20.dp)) }

@Composable private fun FloatingDock(selected: Int, onSelect: (Int) -> Unit) { Surface(shape = RoundedCornerShape(28.dp), tonalElevation = 8.dp, shadowElevation = 8.dp, modifier = Modifier.fillMaxWidth()) { Row(Modifier.fillMaxWidth().padding(6.dp), horizontalArrangement = Arrangement.SpaceEvenly) { val items = listOf("Home" to Icons.Rounded.Home, "Library" to Icons.Rounded.LibraryMusic, "Podcasts" to Icons.Rounded.Podcasts, "Search" to Icons.Rounded.Search); items.forEachIndexed { i, item -> NavigationBarItem(selected == i, { onSelect(i) }, icon = { Icon(item.second, item.first) }, label = { Text(item.first, fontSize = 10.sp) }) } } } }

@Composable private fun Feature(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector) { Card(Modifier.fillMaxWidth().padding(bottom = 12.dp), shape = RoundedCornerShape(24.dp)) { ListItem(headlineContent = { Text(title, fontWeight = FontWeight.SemiBold) }, supportingContent = { Text(subtitle) }, leadingContent = { Icon(icon, null, Modifier.size(28.dp)) }, trailingContent = { Icon(Icons.Rounded.ChevronRight, null) }) } }

@Composable private fun TrackRow(track: Track, onPlay: (Track) -> Unit) { ListItem(headlineContent = { Text(track.title, maxLines = 1, overflow = TextOverflow.Ellipsis) }, supportingContent = { Text("${track.artist} · ${track.album}", maxLines = 1, overflow = TextOverflow.Ellipsis) }, leadingContent = { Surface(Modifier.size(52.dp), shape = RoundedCornerShape(14.dp), tonalElevation = 2.dp) { Icon(Icons.Rounded.MusicNote, "Artwork", Modifier.padding(15.dp)) } }, trailingContent = { IconButton(onClick = { onPlay(track) }) { Icon(Icons.Rounded.PlayArrow, "Play") } }) }

@Composable private fun MiniPlayerBar(track: Track, playing: Boolean, onPlayPause: () -> Unit, onNext: () -> Unit) { Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), tonalElevation = 7.dp, shadowElevation = 4.dp) { Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) { Surface(Modifier.size(44.dp), shape = RoundedCornerShape(10.dp)) { Icon(Icons.Rounded.Album, "Album", Modifier.padding(11.dp)) }; Column(Modifier.weight(1f).padding(horizontal = 10.dp)) { Text(track.title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Medium); Text(track.artist, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall) }; IconButton(onClick = onPlayPause) { Icon(if (playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, "Play or pause") }; IconButton(onClick = onNext) { Icon(Icons.Rounded.SkipNext, "Next") } } }

@Composable private fun EmptyLibraryCard() { Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(26.dp)) { Column(Modifier.padding(22.dp)) { Text("No music found yet", style = MaterialTheme.typography.titleLarge); Spacer(Modifier.height(6.dp)); Text("Allow Music Gallery to access audio, then reopen the Library.") } } }
