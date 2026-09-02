package com.prathyushin.musicgallery

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Podcasts
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
    val navItems = listOf(
        NavItem("Home") { Icon(Icons.Rounded.Home, null) },
        NavItem("Library") { Icon(Icons.Rounded.LibraryMusic, null) },
        NavItem("Podcasts") { Icon(Icons.Rounded.Podcasts, null) },
        NavItem("Search") { Icon(Icons.Rounded.Search, null) }
    )

    MaterialTheme {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
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
        ) { padding ->
            when (selected) {
                2 -> PodcastHome(Modifier.padding(padding))
                1 -> LibraryHome(Modifier.padding(padding))
                else -> HomeScreen(Modifier.padding(padding))
            }
        }
    }
}

@Composable
private fun HomeScreen(modifier: Modifier = Modifier) {
    val mixes = listOf("Recently Played", "Favorites", "Daily Mix", "Made For You")
    Column(modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(24.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("Good morning", style = MaterialTheme.typography.labelLarge)
                Text("Music Gallery", style = MaterialTheme.typography.headlineLarge)
            }
            Surface(shape = RoundedCornerShape(50), tonalElevation = 3.dp) {
                Icon(Icons.Rounded.Headphones, "Music", Modifier.padding(12.dp))
            }
        }
        Spacer(Modifier.height(28.dp))
        Text("Your listening", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            items(mixes) { title -> MixCard(title) }
        }
        Spacer(Modifier.height(30.dp))
        Text("Made for you", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))
        FeatureCard("Daily Mix", "A fresh mix built around your library")
        Spacer(Modifier.height(14.dp))
        FeatureCard("Continue listening", "Pick up exactly where you left off")
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
    Column(modifier.fillMaxSize().padding(20.dp)) {
        Spacer(Modifier.height(24.dp))
        Text("Library", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(20.dp))
        listOf("Songs", "Albums", "Artists", "Playlists", "Genres", "Folders").forEach { item ->
            Card(Modifier.fillMaxWidth().padding(vertical = 5.dp), shape = RoundedCornerShape(18.dp)) {
                Text(item, Modifier.padding(18.dp), style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
private fun PodcastHome(modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().padding(20.dp)) {
        Spacer(Modifier.height(24.dp))
        Text("Podcasts", style = MaterialTheme.typography.headlineLarge)
        Text("Discover, follow and continue listening", style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(22.dp))
        FeatureCard("Continue listening", "Resume your latest episode")
        Spacer(Modifier.height(14.dp))
        Text("Your shows", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))
        FeatureCard("Subscriptions", "Your followed podcasts will appear here")
        Spacer(Modifier.height(14.dp))
        FeatureCard("Downloads", "Offline episodes and automatic downloads")
    }
}
