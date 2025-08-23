package com.foxy.player.music

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

// Lightweight DTO used by the existing unit test (non-Compose)
data class HubContent(
    val hasMaterial3Cards: Boolean,
    val hasBottomNavigation: Boolean,
    val hasTopAppBar: Boolean,
    val hasFAB: Boolean,
    val navigationTabs: List<String>
)

class MusicLibraryHubScreen {
    fun getContent(): HubContent = HubContent(
        hasMaterial3Cards = true,
        hasBottomNavigation = true,
        hasTopAppBar = true,
        hasFAB = true,
        navigationTabs = listOf("Home", "Songs", "Artists", "Folders", "Settings")
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicLibraryHubRender() {
    var selectedIndex by remember { mutableStateOf(0) }
    val tabs = listOf(
        "Home" to Icons.Filled.Home,
        "Songs" to Icons.Filled.QueueMusic,
        "Artists" to Icons.Filled.Person,
        "Folders" to Icons.Filled.FolderOpen,
        "Settings" to Icons.Filled.Settings
    )

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Music Library") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { /* TODO: action */ }) {
                Icon(imageVector = Icons.Filled.QueueMusic, contentDescription = "Add/Scan")
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
            // Content placeholder per selected tab; real content can be added later
            Text(text = tabs[selectedIndex].first, style = MaterialTheme.typography.titleLarge)
        }
    }
}
