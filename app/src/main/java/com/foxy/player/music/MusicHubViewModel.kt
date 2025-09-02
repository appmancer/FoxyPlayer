package com.foxy.player.music

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foxy.player.authentication.AuthRepository
import com.foxy.player.authentication.AuthenticatedApiClient
import com.foxy.player.music.network.MusicDiscoveryService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// UI state for hub
data class HubState(
    val isLoading: Boolean = true,
    val nowPlayingExpanded: Boolean = false,
    val cards: Map<String, CardInfo> = emptyMap()
)

class MusicHubViewModel(
    private val authRepository: AuthRepository,
    private val apiClient: AuthenticatedApiClient,
    private val discoveryService: MusicDiscoveryService,
    private val heuristic: HeuristicMusicDiscovery,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _state = MutableStateFlow(HubState())
    val state: StateFlow<HubState> = _state.asStateFlow()

    private var loaded = false

    init {
        loadCountsOnce()
    }

    fun loadCountsOnce() {
        if (loaded) return
        loaded = true
        _state.value = _state.value.copy(isLoading = true)
        viewModelScope.launch(ioDispatcher) {
            val songs = heuristic.listSongs().getOrNull().orEmpty()
            val artists = heuristic.listArtists().getOrNull().orEmpty()
            val albums = heuristic.listAlbums().getOrNull().orEmpty()
            val folders = discoveryService.listPCloudFolders("/").getOrNull()?.folders ?: emptyList()
            val cards = mapOf(
                CardIds.Songs to CardInfo(CardIds.Songs, songs.size),
                CardIds.Albums to CardInfo(CardIds.Albums, albums.size),
                CardIds.Artists to CardInfo(CardIds.Artists, artists.size),
                CardIds.Folders to CardInfo(CardIds.Folders, folders.size)
            )
            _state.value = HubState(isLoading = false, nowPlayingExpanded = false, cards = cards)
        }
    }

    fun fabClick() {
        _state.value = _state.value.copy(nowPlayingExpanded = true)
    }
}
