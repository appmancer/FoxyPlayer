package com.foxy.player.music

import com.foxy.player.music.database.DatabaseProvider
import com.foxy.player.music.database.MusicDatabaseInterface
import com.foxy.player.music.database.MusicDatabaseProvider
import com.foxy.player.music.entities.EnhancedAlbumEntity

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

// ===== ALBUM DOMAIN OPERATIONS =====

/**
 * Album domain service for business logic operations.
 * Coordinates between repository and presentation layers.
 * Implements PLY-127 architecture with proper error handling.
 */
class AlbumDomainService(
    private val albumRepository: AlbumRepositoryInterface = AlbumRepository()
) {

    /**
     * Retrieves all albums for UI display with proper error handling.
     */
    suspend fun getAlbumsForUI(): AlbumResult<List<AlbumUIModel>> {
        return when (val result = albumRepository.getAllAlbums()) {
            is AlbumResult.Success -> AlbumResult.Success(result.data.toUIModels())
            is AlbumResult.Error -> AlbumResult.Error(result.exception, result.message)
        }
    }

    /**
     * Searches albums for UI display with query validation.
     */
    suspend fun searchAlbumsForUI(query: String): AlbumResult<List<AlbumUIModel>> {
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
    suspend fun getAlbumsByArtistForUI(artist: String): AlbumResult<List<AlbumUIModel>> {
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
    suspend fun getAlbumsPageForUI(offset: Int, limit: Int): AlbumResult<List<AlbumUIModel>> {
        return when (val result = albumRepository.getAlbumsPage(offset, limit)) {
            is AlbumResult.Success -> AlbumResult.Success(result.data.toUIModels())
            is AlbumResult.Error -> AlbumResult.Error(result.exception, result.message)
        }
    }
}
