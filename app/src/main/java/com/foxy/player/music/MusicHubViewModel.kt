package com.foxy.player.music

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foxy.player.authentication.network.AuthRepository
import com.foxy.player.authentication.network.AuthenticatedApiClient
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
        try {
            Log.d("FoxyPlayer", "🎵 MusicHubViewModel CREATED - loading counts")
        } catch (e: Exception) {
            // Ignore logging errors in test environment
        }
        loadCountsOnce()
    }

    fun loadCountsOnce() {
        if (loaded) return
        loaded = true

        System.out.println("🎵 MUSICHUB_DEBUG: loadData() called - starting music discovery")
        try {
            Log.d(
                "FoxyPlayer",
                "📊 LOADING COUNTS: calling heuristic.listSongs(), listArtists(), listAlbums(), listPCloudFolders()"
            )
        } catch (e: Exception) {
            // Ignore logging errors in test environment
        }
        _state.value = _state.value.copy(isLoading = true)
        viewModelScope.launch(ioDispatcher) {
            System.out.println("🎵 MUSICHUB_DEBUG: Starting music discovery calls in coroutine...")

            // 🔍 DEBUG: Test pCloud API authentication before any discovery
            System.out.println("🔍 MUSICHUB_DEBUG: Testing pCloud API authentication...")
            try {
                val debugResult = discoveryService.debugPCloudAPIAuthentication()
                if (debugResult.isSuccess) {
                    val debugInfo = debugResult.getOrNull()!!
                    System.out.println("🔍 MUSICHUB_DEBUG: ✅ pCloud API authentication SUCCESSFUL")
                    System.out.println("🔍 MUSICHUB_DEBUG:   - Auth token length: ${debugInfo.authTokenUsed.length}")
                    System.out.println("🔍 MUSICHUB_DEBUG:   - Response size: ${debugInfo.responseSize} bytes")
                    System.out.println("🔍 MUSICHUB_DEBUG:   - Request duration: ${debugInfo.requestDurationMs}ms")
                    System.out.println(
                        "🔍 MUSICHUB_DEBUG:   - Raw response preview: ${debugInfo.rawApiResponse.take(300)}..."
                    )
                } else {
                    val error = debugResult.exceptionOrNull()
                    System.out.println("🚨 MUSICHUB_DEBUG: ❌ pCloud API authentication FAILED: ${error?.message}")
                    System.out.println("🚨 MUSICHUB_DEBUG: This explains why music discovery returns empty results!")
                }
            } catch (e: Exception) {
                System.out.println("🚨 MUSICHUB_DEBUG: ❌ Debug API call threw exception: ${e.message}")
            }

            val songs = heuristic.listSongs().getOrNull().orEmpty()
            System.out.println("🎵 MUSICHUB_DEBUG: Songs discovery completed: ${songs.size} songs")

            val artists = heuristic.listArtists().getOrNull().orEmpty()
            System.out.println("🎵 MUSICHUB_DEBUG: Artists discovery completed: ${artists.size} artists")

            val albums = heuristic.listAlbums().getOrNull().orEmpty()
            System.out.println("🎵 MUSICHUB_DEBUG: Albums discovery completed: ${albums.size} albums")

            val folders = discoveryService.listPCloudFolders("/").getOrNull()?.folders ?: emptyList()
            System.out.println("🎵 MUSICHUB_DEBUG: Folders discovery completed: ${folders.size} folders")

            try {
                Log.d(
                    "FoxyPlayer",
                    "📊 COUNTS LOADED: songs=${songs.size}, artists=${artists.size}, " +
                        "albums=${albums.size}, folders=${folders.size}"
                )
            } catch (e: Exception) {
                // Ignore logging errors in test environment
            }
            val cards = mapOf(
                CardIds.Songs to CardInfo(CardIds.Songs, songs.size),
                CardIds.Albums to CardInfo(CardIds.Albums, albums.size),
                CardIds.Artists to CardInfo(CardIds.Artists, artists.size),
                CardIds.Folders to CardInfo(CardIds.Folders, folders.size)
            )
            _state.value = HubState(isLoading = false, nowPlayingExpanded = false, cards = cards)
            try {
                Log.d("FoxyPlayer", "✅ HUB STATE UPDATED: loading=false, cards created")
            } catch (e: Exception) {
                // Ignore logging errors in test environment
            }
        }
    }

    fun fabClick() {
        _state.value = _state.value.copy(nowPlayingExpanded = true)
    }
}
