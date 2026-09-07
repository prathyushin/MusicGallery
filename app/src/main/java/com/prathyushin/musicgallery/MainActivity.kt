package com.prathyushin.musicgallery

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import coil.compose.AsyncImage
import kotlin.math.absoluteValue

// Design Tokens (Apple Glass + Spotify Dark)
val MgDarkBase = Color(0xFF09090B)
val MgNeonLime = Color(0xFFD4FF26)
val MgGlassBg = Color(0x1AFFFFFF)
val MgGlassBorder = Color(0x33FFFFFF)
val MgPrimary = Color(0xFFFFFFFF)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(background = MgDarkBase)) {
                MusicGalleryMasterApp()
            }
        }
    }
}

@Composable
fun MusicGalleryMasterApp() {
    val navController = rememberNavController()
    var isNowPlayingExpanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(MgDarkBase)) {
        Scaffold(
            bottomBar = { FloatingGlassNav(navController) },
            containerColor = Color.Transparent,
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            NavHost(navController, startDestination = "home", modifier = Modifier.padding(innerPadding)) {
                composable("home") { HomeScreen { isNowPlayingExpanded = true } }
                composable("library") { Box(Modifier.fillMaxSize()) } // Connect to MediaStore scanner
                composable("podcasts") { PodcastsScreen() }
                composable("search") { Box(Modifier.fillMaxSize()) }
            }
        }

        // Apple-style Fluid Modal Player
        AnimatedVisibility(
            visible = isNowPlayingExpanded,
            enter = slideInVertically(initialOffsetY = { it }, animationSpec = spring(stiffness = Spring.StiffnessLow)),
            exit = slideOutVertically(targetOffsetY = { it }, animationSpec = spring(stiffness = Spring.StiffnessLow))
        ) {
            NowPlayingScreen { isNowPlayingExpanded = false }
        }
    }
}

@Composable
fun FloatingGlassNav(navController: NavHostController) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val items = listOf(
        Pair("home", Icons.Rounded.Home),
        Pair("library", Icons.Rounded.LibraryMusic),
        Pair("podcasts", Icons.Rounded.Mic),
        Pair("search", Icons.Rounded.Search)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .navigationBarsPadding(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(40.dp))
                .background(MgGlassBg)
                .border(1.dp, MgGlassBorder, RoundedCornerShape(40.dp))
                .blur(32.dp)
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { (route, icon) ->
                val isSelected = currentRoute == route
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) MgNeonLime else Color.Transparent)
                        .clickable {
                            navController.navigate(route) {
                                popUpTo(0)
                                launchSingleTop = true
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        null,
                        tint = if (isSelected) MgDarkBase else MgPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun HomeScreen(onOpenPlayer: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        val auraScale by rememberInfiniteTransition().animateFloat(
            1f,
            1.3f,
            infiniteRepeatable(tween(6000), RepeatMode.Reverse)
        )
        Box(
            modifier = Modifier
                .offset((-50).dp, (-50).dp)
                .size(350.dp)
                .graphicsLayer { scaleX = auraScale; scaleY = auraScale }
                .blur(120.dp)
                .background(
                    Brush.radialGradient(
                        listOf(Color(0xFF9C27B0).copy(0.6f), Color.Transparent)
                    ),
                    CircleShape
                )
        )

        LazyColumn(
            contentPadding = PaddingValues(top = 64.dp, bottom = 120.dp, start = 20.dp, end = 20.dp)
        ) {
            item {
                Text(
                    "Hi, Listener",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MgPrimary
                )
                Spacer(Modifier.height(32.dp))
            }
            item {
                Text(
                    "Recently Played",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MgPrimary
                )
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenPlayer() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.DarkGray)
                    )
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Blinding Lights", fontWeight = FontWeight.Bold, color = MgPrimary)
                        Text("The Weeknd", color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun PodcastsScreen() {
    LazyColumn(
        contentPadding = PaddingValues(top = 64.dp, bottom = 120.dp, start = 20.dp, end = 20.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                "Podcasts",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MgPrimary
            )
            Spacer(Modifier.height(32.dp))
        }
        item {
            Text(
                "Up Next",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MgPrimary
            )
            Spacer(Modifier.height(16.dp))
        }
        items(5) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MgGlassBg)
                )
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Design Matters #$it", fontWeight = FontWeight.Bold, color = MgPrimary)
                    Text("45 min left", color = MgNeonLime, style = MaterialTheme.typography.bodySmall)
                }
                Icon(
                    Icons.Rounded.PlayCircleOutline,
                    null,
                    tint = MgPrimary,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NowPlayingScreen(onClose: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    Box(modifier = Modifier.fillMaxSize().background(MgDarkBase)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.DarkGray)
                .blur(100.dp)
                .graphicsLayer { alpha = 0.5f }
        ) // Ambient background

        Column(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
            IconButton(onClick = onClose, modifier = Modifier.padding(16.dp)) {
                Icon(
                    Icons.Rounded.KeyboardArrowDown,
                    null,
                    tint = MgPrimary,
                    modifier = Modifier.size(36.dp)
                )
            }
            Spacer(Modifier.height(20.dp))

            HorizontalPager(
                state = pagerState,
                contentPadding = PaddingValues(horizontal = 70.dp),
                modifier = Modifier.fillMaxWidth().height(360.dp)
            ) { page ->
                val offset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                val scale = lerp(
                    0.85f,
                    1f,
                    1f - offset.absoluteValue.coerceIn(0f, 1f)
                )
                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            rotationY = (offset * 25f).coerceIn(-25f, 25f)
                        }
                        .fillMaxSize()
                        .padding(4.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(Color.Gray)
                )
            }

            Spacer(Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .clip(RoundedCornerShape(40.dp))
                    .background(MgGlassBg)
                    .border(1.dp, MgGlassBorder, RoundedCornerShape(40.dp))
                    .padding(24.dp)
            ) {
                Column {
                    Slider(
                        value = 0.3f,
                        onValueChange = {},
                        colors = SliderDefaults.colors(
                            thumbColor = MgPrimary,
                            activeTrackColor = MgNeonLime
                        )
                    )
                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Rounded.SkipPrevious,
                            null,
                            tint = MgPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(MgPrimary.copy(0.2f))
                                .border(1.dp, MgGlassBorder, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Rounded.PlayArrow,
                                null,
                                tint = MgPrimary,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Icon(
                            Icons.Rounded.SkipNext,
                            null,
                            tint = MgPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }
        }
    }
}
