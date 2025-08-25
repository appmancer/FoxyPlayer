package com.foxy.player.music

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Extended DTOs used by unit tests (non-Compose)
data class CardInfo(
    val id: String,
    val count: Int
)

data class HubContent(
    val hasMaterial3Cards: Boolean,
    val hasBottomNavigation: Boolean,
    val hasTopAppBar: Boolean,
    val hasFAB: Boolean,
    val navigationTabs: List<String>,
    val cards: Map<String, CardInfo> = emptyMap(),
    val isNowPlayingExpanded: Boolean = false,
    val onFabClick: (() -> Unit)? = null,
    val onCardClick: ((String) -> String)? = null
)

// Legacy imperative screen removed; logic now handled by ViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicLibraryHubRender(viewModel: MusicHubViewModel, navigator: Navigator? = null) {
    var selectedIndex by remember { mutableStateOf(0) }
    val tabs = listOf(
        "Home" to Icons.Filled.Home,
        "Songs" to Icons.Filled.QueueMusic,
        "Artists" to Icons.Filled.Person,
        "Folders" to Icons.Filled.FolderOpen,
        "Settings" to Icons.Filled.Settings
    )

    val state = viewModel.state
    val showNowPlaying = state.value.nowPlayingExpanded

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Music Library") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.fabClick() }) {
                Icon(imageVector = Icons.Filled.QueueMusic, contentDescription = "Now Playing")
            }
        },
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, (label, icon) ->
                    NavigationBarItem(
                        selected = selectedIndex == index,
                        onClick = { selectedIndex = index },
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label) }
                    )
                }
            }
        }
    ) { inner ->
        Surface(modifier = Modifier.padding(inner)) {
            when (tabs[selectedIndex].first) {
                "Home" -> HubCardsGrid(state.value.cards, navigator)
                "Songs" -> Text("SongsList", style = MaterialTheme.typography.titleLarge)
                "Artists" -> Text("ArtistsList", style = MaterialTheme.typography.titleLarge)
                "Albums" -> Text("AlbumsList", style = MaterialTheme.typography.titleLarge)
                "Folders" -> Text("FoldersView", style = MaterialTheme.typography.titleLarge)
                else -> Text(tabs[selectedIndex].first, style = MaterialTheme.typography.titleLarge)
            }
        }

        if (showNowPlaying) {
            ModalBottomSheet(
                onDismissRequest = { /* TODO: collapse via VM if needed */ },
                sheetState = rememberModalBottomSheetState()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text("Now Playing", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.padding(8.dp))
                    Text("Track details and controls coming soon")
                }
            }
        }
    }
}

@Composable
private fun HubCardsGrid(cards: Map<String, CardInfo>, navigator: Navigator? = null) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(cards.keys.toList()) { key ->
            val info = cards[key]
            HubCard(title = key, count = info?.count ?: 0) {
                val route = when (key) {
                    CardIds.Songs, "Songs" -> Routes.SongsList
                    CardIds.Artists, "Artists" -> Routes.ArtistsList
                    CardIds.Albums, "Albums" -> Routes.AlbumsList
                    CardIds.Folders, "Folders" -> Routes.FoldersView
                    else -> key
                }
                navigator?.navigate(route)
            }
        }
    }
}

@Composable
private fun HubCard(title: String, count: Int, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.padding(4.dp))
            Text("$count", style = MaterialTheme.typography.headlineSmall)
        }
    }
}
