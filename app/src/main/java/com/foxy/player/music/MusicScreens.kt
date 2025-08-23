package com.foxy.player.music

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

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
                    Column { Text(s.title); Text("${s.artist} – ${s.album}", style = MaterialTheme.typography.bodySmall) }
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
                items(albums) { a -> Column { Text(a.name); Text(a.artist, style = MaterialTheme.typography.bodySmall) } }
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
            LazyColumn(modifier = Modifier.fillMaxSize()) { items(folders) { f -> Text(f) } }
        }
        else -> Text("Failed to load folders", style = MaterialTheme.typography.bodyLarge)
    }
}
