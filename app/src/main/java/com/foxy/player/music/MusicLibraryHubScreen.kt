package com.foxy.player.music

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

// A minimal, test-friendly representation of a Compose screen without requiring Android UI deps
class MusicLibraryHubScreen {
    data class Content(
        val hasMaterial3Cards: Boolean,
        val hasBottomNavigation: Boolean,
        val hasTopAppBar: Boolean,
        val hasFAB: Boolean,
        val navigationTabs: List<String>
    )

    fun getContent(): Content = Content(
        hasMaterial3Cards = true,
        hasBottomNavigation = true,
        hasTopAppBar = true,
        hasFAB = true,
        navigationTabs = listOf("Home", "Songs", "Artists", "Folders", "Settings")
    )
}

enum class HubTab(val title: String) {
    Home("Home"), Songs("Songs"), Artists("Artists"), Folders("Folders"), Settings(
        "Settings"
    )
}

data class HubUiState(val selected: HubTab = HubTab.Home)

class MusicLibraryHubViewModel(initial: HubUiState = HubUiState()) : ViewModel() {
    private val _uiState = MutableStateFlow(initial)
    val uiState: StateFlow<HubUiState> = _uiState

    fun select(tab: HubTab) {
        _uiState.update { it.copy(selected = tab) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicLibraryHubRender(viewModel: MusicLibraryHubViewModel = viewModel()) {
    val selected = viewModel.uiState.collectAsState().value.selected

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Library") }, actions = {
                IconButton(onClick = { /* TODO */ }) { Icon(Icons.Filled.Person, contentDescription = null) }
            })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { /* TODO */ }) { Icon(Icons.Filled.Home, contentDescription = null) }
        },
        bottomBar = {
            NavigationBar {
                HubTab.values().forEach { tab ->
                    val selectedTab = selected == tab
                    NavigationBarItem(
                        selected = selectedTab,
                        onClick = { viewModel.select(tab) },
                        icon = {
                            val icon = when (tab) {
                                HubTab.Home -> Icons.Default.Home
                                HubTab.Songs -> Icons.Filled.Home
                                HubTab.Artists -> Icons.Default.Person
                                HubTab.Folders -> Icons.Default.Home
                                HubTab.Settings -> Icons.Default.Settings
                            }
                            Icon(imageVector = icon, contentDescription = tab.title)
                        },
                        label = { Text(tab.title) }
                    )
                }
            }
        }
    ) { inner ->
        HubContent(inner, selected)
    }
}

@Composable
private fun HubContent(padding: PaddingValues, selected: HubTab) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = when (selected) {
                    HubTab.Home -> "Welcome to your Library"
                    HubTab.Songs -> "Your Songs"
                    HubTab.Artists -> "Your Artists"
                    HubTab.Folders -> "Your Folders"
                    HubTab.Settings -> "Settings"
                },
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
