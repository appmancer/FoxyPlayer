package com.foxy.player.music

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.foxy.player.music.database.DatabaseProvider
import com.foxy.player.music.database.MusicDatabaseInterface
import com.foxy.player.music.database.MusicDatabaseProvider
import com.foxy.player.music.entities.EnhancedAlbumEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// ===== ALBUM RESULT TYPES =====

/**
 * Result wrapper for album repository operations with proper error handling.
 * Provides type-safe result handling following modern Android patterns.
 */
sealed class AlbumResult<T> {
    data class Success<T>(val data: T) : AlbumResult<T>()
    data class Error<T>(val exception: Throwable, val message: String) : AlbumResult<T>()

    fun isSuccess(): Boolean = this is Success
    fun isError(): Boolean = this is Error

    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Error -> null
    }

    fun getOrDefault(defaultValue: T): T = getOrNull() ?: defaultValue
}

// ===== ALBUM REPOSITORY LAYER =====

/**
 * Repository interface for album data operations.
 * Provides abstraction layer for album-related data access.
 * Based on PLY-127 architecture research.
 */
interface AlbumRepositoryInterface {
    suspend fun getAllAlbums(): AlbumResult<List<EnhancedAlbumEntity>>
    suspend fun getAlbumById(albumId: String): AlbumResult<EnhancedAlbumEntity?>
    suspend fun getAlbumsByArtist(artist: String): AlbumResult<List<EnhancedAlbumEntity>>
    suspend fun getAlbumsPage(offset: Int, limit: Int): AlbumResult<List<EnhancedAlbumEntity>>
    suspend fun searchAlbums(query: String): AlbumResult<List<EnhancedAlbumEntity>>
}

/**
 * Repository implementation for album data operations.
 * Integrates with Room database through PLY-124 enhanced entities.
 * Implements PLY-127 architecture specifications with proper error handling.
 */
class AlbumRepository(
    private val databaseProvider: DatabaseProvider = MusicDatabaseProvider()
) : AlbumRepositoryInterface {

    private val database: MusicDatabaseInterface by lazy {
        databaseProvider.getDatabase()
    }

    override suspend fun getAllAlbums(): AlbumResult<List<EnhancedAlbumEntity>> {
        return try {
            val albumDao = database.enhancedAlbumDao()
                ?: return AlbumResult.Error(
                    IllegalStateException("Album DAO not available"),
                    "Database not properly initialized"
                )

            val albums = albumDao.getAllEnhancedAlbums()
            AlbumResult.Success(albums)
        } catch (e: Exception) {
            AlbumResult.Error(e, "Failed to retrieve albums: ${e.message}")
        }
    }

    override suspend fun getAlbumById(albumId: String): AlbumResult<EnhancedAlbumEntity?> {
        return try {
            val albumDao = database.enhancedAlbumDao()
                ?: return AlbumResult.Error(
                    IllegalStateException("Album DAO not available"),
                    "Database not properly initialized"
                )

            val album = albumDao.getEnhancedAlbumById(albumId)
            AlbumResult.Success(album)
        } catch (e: Exception) {
            AlbumResult.Error(e, "Failed to retrieve album by ID: ${e.message}")
        }
    }

    override suspend fun getAlbumsByArtist(artist: String): AlbumResult<List<EnhancedAlbumEntity>> {
        return try {
            val albumDao = database.enhancedAlbumDao()
                ?: return AlbumResult.Error(
                    IllegalStateException("Album DAO not available"),
                    "Database not properly initialized"
                )

            val albums = albumDao.getAlbumsByArtist(artist)
            AlbumResult.Success(albums)
        } catch (e: Exception) {
            AlbumResult.Error(e, "Failed to retrieve albums by artist: ${e.message}")
        }
    }

    override suspend fun getAlbumsPage(offset: Int, limit: Int): AlbumResult<List<EnhancedAlbumEntity>> {
        return try {
            if (offset < 0 || limit <= 0) {
                return AlbumResult.Error(
                    IllegalArgumentException("Invalid pagination parameters"),
                    "Offset must be >= 0 and limit must be > 0"
                )
            }

            val allAlbumsResult = getAllAlbums()
            when (allAlbumsResult) {
                is AlbumResult.Success -> {
                    val paginatedAlbums = allAlbumsResult.data.drop(offset).take(limit)
                    AlbumResult.Success(paginatedAlbums)
                }
                is AlbumResult.Error -> allAlbumsResult
            }
        } catch (e: Exception) {
            AlbumResult.Error(e, "Failed to retrieve albums page: ${e.message}")
        }
    }

    override suspend fun searchAlbums(query: String): AlbumResult<List<EnhancedAlbumEntity>> {
        return try {
            if (query.isBlank()) {
                return AlbumResult.Success(emptyList())
            }

            val allAlbumsResult = getAllAlbums()
            when (allAlbumsResult) {
                is AlbumResult.Success -> {
                    val filteredAlbums = allAlbumsResult.data.filter { album ->
                        album.title.contains(query, ignoreCase = true) ||
                            album.artist.contains(query, ignoreCase = true)
                    }
                    AlbumResult.Success(filteredAlbums)
                }
                is AlbumResult.Error -> allAlbumsResult
            }
        } catch (e: Exception) {
            AlbumResult.Error(e, "Failed to search albums: ${e.message}")
        }
    }
}

// ===== ALBUM UI MODELS =====

/**
 * UI model for album presentation layer.
 * Transforms database entities for UI consumption.
 * Follows PLY-127 architecture specifications.
 */
data class AlbumUIModel(
    val id: String,
    val title: String,
    val artist: String,
    val trackCount: Int = 0,
    val artworkUrl: String? = null,
    val path: String? = null,
    val lastModified: Long = 0L
) {
    /**
     * Validates that the UI model contains valid data for display.
     */
    fun isValid(): Boolean {
        return id.isNotBlank() && title.isNotBlank() && artist.isNotBlank()
    }
}

/**
 * Converts EnhancedAlbumEntity to AlbumUIModel for presentation layer.
 * Implements proper data transformation following PLY-127 architecture.
 */
fun EnhancedAlbumEntity.toUIModel(): AlbumUIModel {
    return AlbumUIModel(
        id = this.id,
        title = this.title,
        artist = this.artist,
        path = this.path,
        lastModified = this.lastModified
    )
}

/**
 * Extension function to convert list of entities to UI models.
 */
fun List<EnhancedAlbumEntity>.toUIModels(): List<AlbumUIModel> {
    return this.map { it.toUIModel() }
}

// ===== ALBUM UI STATES =====

/**
 * Sealed class representing different states of album list UI.
 * Implements PLY-127 architecture with proper state management.
 */
sealed class AlbumListState {
    object Loading : AlbumListState()
    data class Success(val albums: List<AlbumUIModel>) : AlbumListState()
    data class Error(val message: String) : AlbumListState()
}

// ===== ALBUM VIEW MODELS =====

/**
 * ViewModel for album list screen.
 * Manages album list state and business logic following PLY-127 architecture.
 */
class AlbumListViewModel(
    private val albumRepository: AlbumRepositoryInterface = AlbumRepository()
) : ViewModel() {

    private val _albumsState = MutableStateFlow<AlbumListState>(AlbumListState.Loading)
    val albumsState: StateFlow<AlbumListState> = _albumsState

    /**
     * Loads albums from repository and updates state accordingly.
     * Uses real repository to fetch album data.
     */
    fun loadAlbums() {
        viewModelScope.launch {
            _albumsState.value = AlbumListState.Loading
            
            when (val result = albumRepository.getAllAlbums()) {
                is AlbumResult.Success -> {
                    val uiModels = result.data.toUIModels()
                    _albumsState.value = AlbumListState.Success(uiModels)
                }
                is AlbumResult.Error -> {
                    _albumsState.value = AlbumListState.Error(result.message)
                }
            }
        }
    }

    /**
     * Searches albums with the given query.
     * TODO: Implement search functionality when needed
     */
    fun searchAlbums(query: String) {
        _albumsState.value = AlbumListState.Loading
    }

    /**
     * Loads albums by artist.
     * TODO: Implement artist filtering when needed
     */
    fun loadAlbumsByArtist(artist: String) {
        _albumsState.value = AlbumListState.Loading
    }
}

// ===== ALBUM DOMAIN OPERATIONS =====

/**
 * Album domain service for business logic operations.
 * Coordinates between repository and presentation layers.
 * Implements PLY-127 architecture with proper error handling.
 */
open class AlbumDomainService(
    private val albumRepository: AlbumRepositoryInterface = AlbumRepository()
) {

    /**
     * Retrieves all albums for UI display with proper error handling.
     */
    open suspend fun getAlbumsForUI(): AlbumResult<List<AlbumUIModel>> {
        return when (val result = albumRepository.getAllAlbums()) {
            is AlbumResult.Success -> AlbumResult.Success(result.data.toUIModels())
            is AlbumResult.Error -> AlbumResult.Error(result.exception, result.message)
        }
    }

    /**
     * Searches albums for UI display with query validation.
     */
    open suspend fun searchAlbumsForUI(query: String): AlbumResult<List<AlbumUIModel>> {
        if (query.isBlank()) {
            return AlbumResult.Success(emptyList())
        }

        return when (val result = albumRepository.searchAlbums(query)) {
            is AlbumResult.Success -> AlbumResult.Success(result.data.toUIModels())
            is AlbumResult.Error -> AlbumResult.Error(result.exception, result.message)
        }
    }

    /**
     * Retrieves albums by artist for UI display.
     */
    open suspend fun getAlbumsByArtistForUI(artist: String): AlbumResult<List<AlbumUIModel>> {
        if (artist.isBlank()) {
            return AlbumResult.Error(
                IllegalArgumentException("Artist name cannot be blank"),
                "Artist name is required for filtering"
            )
        }

        return when (val result = albumRepository.getAlbumsByArtist(artist)) {
            is AlbumResult.Success -> AlbumResult.Success(result.data.toUIModels())
            is AlbumResult.Error -> AlbumResult.Error(result.exception, result.message)
        }
    }

    /**
     * Retrieves paginated albums for UI display with validation.
     */
    open suspend fun getAlbumsPageForUI(offset: Int, limit: Int): AlbumResult<List<AlbumUIModel>> {
        return when (val result = albumRepository.getAlbumsPage(offset, limit)) {
            is AlbumResult.Success -> AlbumResult.Success(result.data.toUIModels())
            is AlbumResult.Error -> AlbumResult.Error(result.exception, result.message)
        }
    }
}

// ===== ALBUM UI COMPONENTS =====

/**
 * Material3 album grid item component for displaying individual albums.
 * Implements PLY-127 UI specifications with proper Material Design.
 */
@Composable
fun AlbumGridItem(
    album: AlbumUIModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Album artwork placeholder (will be enhanced in future iterations)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🎵",
                    style = MaterialTheme.typography.headlineLarge
                )
            }

            // Album title
            Text(
                text = album.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Artist name
            Text(
                text = album.artist,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Track count
            Text(
                text = "${album.trackCount} tracks",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Album list screen component using Material3 LazyVerticalGrid.
 * * Displays albums from ViewModel state following PLY-127 Albums-First Navigation architecture.
 * Handles three states: Loading (progress indicator), Success (grid display), and Error (message).
 * * @param viewModel The ViewModel managing album list state
 * @param modifier Optional modifier for the screen layout
 */
@Composable
fun AlbumListScreen(
    viewModel: AlbumListViewModel,
    modifier: Modifier = Modifier
) {
    val albumsState by viewModel.albumsState.collectAsState()

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (val currentState = albumsState) {
            is AlbumListState.Loading -> {
                CircularProgressIndicator()
            }
            is AlbumListState.Success -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(currentState.albums) { album ->
                        AlbumGridItem(
                            album = album,
                            onClick = { /* TODO: Navigate to album detail */ }
                        )
                    }
                }
            }
            is AlbumListState.Error -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Error: ${currentState.message}",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Button(
                        onClick = { viewModel.loadAlbums() }
                    ) {
                        Text("Retry")
                    }
                }
            }
        }
    }
}
