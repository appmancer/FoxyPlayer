package com.foxy.player.music

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.foxy.player.music.entities.FolderListing
import com.foxy.player.music.network.MusicDiscoveryService
import com.foxy.player.music.ui.ContentSectionComponent
import com.foxy.player.music.ui.LayoutType

@Composable
fun SongsListScreen(service: MusicDiscoveryService) {
    val h = remember { HeuristicMusicDiscovery(service) }
    val state = remember { mutableStateOf<Result<List<Song>>?>(null) }
    LaunchedEffect(Unit) { state.value = h.listSongs() }
    val result = state.value
    when {
        result == null -> Text("Loading songs...", style = MaterialTheme.typography.bodyLarge)
        result.isSuccess -> {
            val songs = result.getOrNull().orEmpty()
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(songs) { s ->
                    Column {
                        Text(s.title)
                        Text(
                            "${s.artist} – ${s.album}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
        else -> Text("Failed to load songs", style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun ArtistsListScreen(service: MusicDiscoveryService) {
    val h = remember { HeuristicMusicDiscovery(service) }
    val state = remember { mutableStateOf<Result<List<ArtistSummary>>?>(null) }
    LaunchedEffect(Unit) { state.value = h.listArtists() }
    val result = state.value
    when {
        result == null -> Text("Loading artists...", style = MaterialTheme.typography.bodyLarge)
        result.isSuccess -> {
            val artists = result.getOrNull().orEmpty()
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(artists) { a ->
                    Column {
                        Text(a.name); Text(
                            "${a.songCount} songs",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
        else -> Text("Failed to load artists", style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun AlbumsListScreen(service: MusicDiscoveryService) {
    val h = remember { HeuristicMusicDiscovery(service) }
    val state = remember { mutableStateOf<Result<List<AlbumSummary>>?>(null) }
    LaunchedEffect(Unit) { state.value = h.listAlbums() }
    val result = state.value
    when {
        result == null -> Text("Loading albums...", style = MaterialTheme.typography.bodyLarge)
        result.isSuccess -> {
            val albums = result.getOrNull().orEmpty()
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(albums) { a ->
                    Column {
                        Text(a.name)
                        Text(
                            a.artist,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
        else -> Text("Failed to load albums", style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun FoldersViewScreen(service: MusicDiscoveryService) {
    val state = remember { mutableStateOf<Result<FolderListing>?>(null) }
    LaunchedEffect(Unit) { state.value = service.listPCloudFolders("/") }
    val result = state.value
    when {
        result == null -> Text("Loading folders...", style = MaterialTheme.typography.bodyLarge)
        result.isSuccess -> {
            val folders = result.getOrNull()?.folders.orEmpty()
            LazyColumn(modifier = Modifier.fillMaxSize()) { items(folders) { f -> Text(f)     }
}

// ===== PLY-141: MODULAR UI COMPONENT STRUCTURE - COMPOSE LAYOUTS =====

@Composable
fun <T> ContentSectionRenderer(
    component: ContentSectionComponent<T>,
    itemRenderer: @Composable (T) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Section title
        Text(
            text = component.section.title,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // Adaptive layout based on layout type
        when (component.layoutType) {
            LayoutType.LIST -> {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(component.section.items) { item ->
                        itemRenderer(item)
                    }
                }
            }
            LayoutType.GRID -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(component.section.items) { item ->
                        itemRenderer(item)
                    }
                }
            }
            LayoutType.CAROUSEL -> {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(component.section.items) { item ->
                        itemRenderer(item)
                    }
                }
            }
        }
    }
}

@Composable
fun RecentItemCard(recentItem: RecentItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = recentItem.title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = recentItem.artist,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun RecommendationItemCard(recommendationItem: RecommendationItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = recommendationItem.title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = recommendationItem.artist,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Confidence: ${(recommendationItem.confidence * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
        }
        else -> Text("Failed to load folders", style = MaterialTheme.typography.bodyLarge)
    }
}
