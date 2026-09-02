package com.prathyushin.musicgallery

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Podcasts
import androidx.compose.material.icons.rounded.Search
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.prathyushin.musicgallery.model.PlaybackState
import com.prathyushin.musicgallery.ui.MiniPlayer
import com.prathyushin.musicgallery.ui.MusicGalleryTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MusicGalleryApp() }
    }
}

private data class NavItem(val label: String, val icon: @Composable () -> Unit)

@Composable
fun MusicGalleryApp() {
    var selected by remember { mutableIntStateOf(0) }
    var playing by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    val playback = remember { PlaybackState(title = "Music Gallery", subtitle = "Your personal listening") }
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
                    MiniPlayer(
                        state = playback.copy(isPlaying = playing),
                        onPlayPause = { playing = !playing },
                        onNext = {},
                        onPrevious = {},
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
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
                1 -> LibraryHome(Modifier.padding(padding))
                2 -> PodcastHome(Modifier.padding(padding))
                3 -> SearchHome(query, { query = it }, Modifier.padding(padding))
                else -> HomeScreen(Modifier.padding(padding))
            }
        }
    }
}

@Composable
private fun HomeScreen(modifier: Modifier = Modifier) {
    val mixes = listOf("Recently Played", "Favorites", "Daily Mix", "Made For You")
    LazyColumn(modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        item {
            Spacer(Modifier.height(24.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Good morning", style = MaterialTheme.typography.labelLarge)
                    Text("Music Gallery", style = MaterialTheme.typography.headlineLarge)
                }
                Surface(shape = RoundedCornerShape(50), tonalElevation = 3.dp) {
                    Icon(Icons.Rounded.Headphones, "Music", Modifier.padding(12.dp))
                }
            }
            Spacer(Modifier.height(12.dp))
            VersionPill()
            Spacer(Modifier.height(24.dp))
            ReleaseCard()
            Spacer(Modifier.height(28.dp))
            Text("Your listening", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))
        }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                items(mixes) { MixCard(it) }
            }
            Spacer(Modifier.height(30.dp))
            Text("Made for you", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))
        }
        item { FeatureCard("Daily Mix", "A fresh mix built around your library") }
        item { Spacer(Modifier.height(14.dp)) }
        item { FeatureCard("Continue listening", "Pick up exactly where you left off") }
        item { Spacer(Modifier.height(20.dp)) }
    }
}

@Composable
private fun VersionPill() {
    Surface(
        shape = RoundedCornerShape(50.dp),
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Icon(Icons.Rounded.Upgrade, null, Modifier.size(16.dp))
            Text("Music Gallery ${AppVersion.NAME}", style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun ReleaseCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest)
    ) {
        Column(Modifier.padding(20.dp)) {
            Text("What's new", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(4.dp))
            Text(AppVersion.RELEASE_TITLE, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(10.dp))
            Text(
                "A refreshed listening foundation with clearer navigation, a dedicated podcast space, a persistent mini-player and release-aware versioning.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun MixCard(title: String) {
    Card(shape = RoundedCornerShape(22.dp), modifier = Modifier.size(width = 150.dp, height = 170.dp)) {
        Column(Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.Bottom) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text("Your library", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun FeatureCard(title: String, subtitle: String) {
    Card(
        modifier = Modifier.fillMaxWidth().height(96.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest)
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.Center) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun LibraryHome(modifier: Modifier = Modifier) {
    LazyColumn(modifier.fillMaxSize().padding(20.dp)) {
        item {
            Spacer(Modifier.height(24.dp))
            Text("Library", style = MaterialTheme.typography.headlineLarge)
            Text("Your music, organized your way", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(20.dp))
        }
        items(listOf("Songs", "Albums", "Artists", "Playlists", "Genres", "Folders")) { item ->
            Card(Modifier.fillMaxWidth().padding(vertical = 5.dp), shape = RoundedCornerShape(18.dp)) {
                Text(item, Modifier.padding(18.dp), style = MaterialTheme.typography.titleMedium)
            }
        }
        item {
            Spacer(Modifier.height(18.dp))
            Text("Version ${AppVersion.NAME}", style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun PodcastHome(modifier: Modifier = Modifier) {
    LazyColumn(modifier.fillMaxSize().padding(20.dp)) {
        item {
            Spacer(Modifier.height(24.dp))
            Text("Podcasts", style = MaterialTheme.typography.headlineLarge)
            Text("Discover, follow and continue listening", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(22.dp))
        }
        item { FeatureCard("Continue listening", "Resume your latest episode") }
        item { Spacer(Modifier.height(14.dp)); Text("Your shows", style = MaterialTheme.typography.titleLarge); Spacer(Modifier.height(12.dp)) }
        item { FeatureCard("Subscriptions", "Your followed podcasts will appear here") }
        item { Spacer(Modifier.height(14.dp)); FeatureCard("Downloads", "Offline episodes and automatic downloads") }
        item { Spacer(Modifier.height(18.dp)); Text("Music Gallery ${AppVersion.NAME}", style = MaterialTheme.typography.labelMedium) }
    }
}

@Composable
private fun SearchHome(query: String, onQueryChange: (String) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().padding(20.dp)) {
        Spacer(Modifier.height(24.dp))
        Text("Search", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(16.dp))
        TextField(
            query,
            onQueryChange,
            Modifier.fillMaxWidth(),
            placeholder = { Text("Songs, artists, albums or podcasts") },
            singleLine = true
        )
        Spacer(Modifier.height(22.dp))
        if (query.isBlank()) {
            Text("Search your entire listening world", style = MaterialTheme.typography.titleLarge)
            Text("Music and podcasts will share one fast, unified search.", style = MaterialTheme.typography.bodyLarge)
        } else {
            Text("Results for \"$query\"", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))
            FeatureCard("Music", "Matching songs, albums and artists")
            Spacer(Modifier.height(12.dp))
            FeatureCard("Podcasts", "Matching shows and episodes")
        }
    }
}
