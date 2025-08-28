package com.foxy.player.music

import com.google.gson.annotations.SerializedName
import java.util.Date

// ===== SQLITE DATABASE FOUNDATION =====

/**
 * Database provider interface for dependency injection
 */
interface DatabaseProvider {
    fun getDatabase(): MusicDatabaseInterface
}

/**
 * SQLite Database Foundation for Music Player
 * Provides Room database abstraction and basic DAO access
 */
class MusicDatabaseProvider : DatabaseProvider {
    override fun getDatabase(): MusicDatabaseInterface {
        return MusicDatabase()
    }
}

/**
 * Database interface for testability and abstraction
 */
interface MusicDatabaseInterface {
    fun isInitialized(): Boolean
    fun trackDao(): TrackDaoInterface
}

/**
 * Main database class for music data storage
 * This will be converted to Room @Database in future iterations
 */
class MusicDatabase : MusicDatabaseInterface {
    private var initialized = true

    override fun isInitialized(): Boolean = initialized

    override fun trackDao(): TrackDaoInterface {
        return TrackDao()
    }
}

/**
 * DAO interface for track operations
 */
interface TrackDaoInterface {
    fun isReady(): Boolean
}

/**
 * Data Access Object for track operations
 * This will be converted to Room @Dao in future iterations
 */
class TrackDao : TrackDaoInterface {
    override fun isReady(): Boolean = true
}

// ===== PCLOUD API MODELS =====

/**
 * Represents the response from the pCloud API for a folder listing request.
 *
 * @property result The result code of the API call (0 for success, non-zero for errors).
 * @property metadata Metadata about the folder, if available.
 * @property contents List of items (files and folders) contained in the folder, if available.
 */
data class PCloudListFolderResponse(
    val result: Int,
    val metadata: PCloudMetadata?,
    val contents: List<PCloudItem>?
)

/**
 * Metadata information for a pCloud folder.
 *
 * @property name The name of the folder.
 * @property created The creation date/time of the folder (ISO 8601 format).
 * @property isFolder Whether this item is a folder (should always be true for folders).
 * @property folderId The unique identifier for the folder.
 * @property parentFolderId The unique identifier of the parent folder.
 */
data class PCloudMetadata(
    val name: String,
    val created: String,
    @SerializedName("isfolder") val isFolder: Boolean,
    @SerializedName("folderid") val folderId: Long,
    @SerializedName("parentfolderid") val parentFolderId: Long
)

/**
 * Represents a file or folder item in a pCloud directory listing.
 *
 * @property name The name of the file or folder.
 * @property created The creation date/time (ISO 8601 format).
 * @property modified The last modification date/time (ISO 8601 format).
 * @property isFolder Whether this item is a folder (true) or file (false).
 * @property folderId The unique identifier for the folder (if this is a folder).
 * @property fileId The unique identifier for the file (if this is a file).
 * @property parentFolderId The unique identifier of the parent folder.
 * @property size The size of the file in bytes (null for folders).
 * @property contentType The MIME type of the file (e.g., "audio/mpeg" for MP3 files).
 * @property category The pCloud category identifier for the file type.
 * @property id The unique string identifier for the item.
 */
data class PCloudItem(
    val name: String,
    val created: String,
    val modified: String,
    @SerializedName("isfolder") val isFolder: Boolean,
    @SerializedName("folderid") val folderId: Long?,
    @SerializedName("fileid") val fileId: Long?,
    @SerializedName("parentfolderid") val parentFolderId: Long,
    val size: Long?,
    @SerializedName("contenttype") val contentType: String?,
    val category: Int?,
    val id: String
)

// ===== BASIC DATA MODELS =====

data class FolderListing(
    val folders: List<String> = emptyList(),
    val files: List<String> = emptyList()
)

// API Response wrapper to support containsAuthToken method
data class PCloudAPIResponse(
    val authToken: String,
    val folderListing: FolderListing
) {
    fun containsAuthToken(token: String): Boolean {
        return authToken == token
    }
}

// Recursive directory traversal response
data class RecursiveDirectoryResponse(
    val authToken: String,
    val totalDirectoriesTraversed: Int,
    val allFolders: List<String>
)

// Audio files filtering response
data class AudioFilesResponse(
    val authToken: String,
    val audioFiles: List<String>
)

// ===== AUDIO METADATA MODELS =====

// Audio metadata model
data class AudioMetadata(
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val format: String,
    val bitrate: Int
)

// Audio file model for caching
data class AudioFile(
    val fileId: String,
    val fileName: String,
    val filePath: String,
    val fileSizeBytes: Long,
    val pCloudUrl: String
)

// Cached metadata response
data class CachedMetadataResponse(
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val format: String,
    val bitrate: Int,
    val servedFromCache: Boolean,
    val processingTimeMs: Long,
    val totalApiCalls: Int
)

// ===== MUSIC SEARCH AND BROWSE MODELS =====

data class MusicTrack(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val genre: String
)

// Enhanced music track with sorting metadata
data class MusicTrackWithMetadata(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val genre: String,
    val durationMs: Long,
    val fileSizeBytes: Long,
    val dateAdded: Date
)

// Music track with indexing metadata
data class MusicTrackIndexed(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val genre: String,
    val filePath: String,
    val durationMs: Long,
    val fileSizeBytes: Long,
    val bitrate: Int,
    val dateAdded: Date
)

// Database index statistics
data class IndexStats(
    val totalIndexes: Int,
    val indexHits: Int,
    val indexMisses: Int = 0
)

// Database indexed search response
data class DatabaseIndexedSearchResponse(
    val tracks: List<MusicTrackIndexed>,
    val usedDatabaseIndex: Boolean,
    val indexedSearchTimeMs: Long,
    val indexStats: IndexStats
)

// Sort criteria enum
enum class SortCriteria {
    ALPHABETICAL_TITLE,
    ALPHABETICAL_ARTIST,
    DURATION,
    FILE_SIZE,
    DATE_ADDED
}

data class MusicSearchResponse(
    val tracks: List<MusicTrack>
)

// Enhanced search response with metadata tracks
data class MusicSortResponse(
    val tracks: List<MusicTrackWithMetadata>
)

// Enhanced search criteria model
data class SearchCriteria(
    val query: String,
    val searchInTitle: Boolean = true,
    val searchInArtist: Boolean = true,
    val searchInAlbum: Boolean = true,
    val genre: String? = null
)

// Browse functionality models
data class ArtistGroup(
    val artist: String,
    val tracks: List<MusicTrack>
)

data class MusicBrowseResponse(
    val artistGroups: List<ArtistGroup>
)

// ===== UI STATE MANAGEMENT MODELS =====

data class MusicSearchUIState(
    val searchQuery: String = "",
    val searchResults: List<MusicTrack> = emptyList(),
    val showSearchInput: Boolean = true,
    val isLoading: Boolean = false
)

// ===== ERROR HANDLING MODELS =====

// Error handling metadata extraction response
data class MetadataExtractionErrorResponse(
    val metadata: AudioMetadata? = null,
    val hasFileCorruption: Boolean = false,
    val hasMissingMetadata: Boolean = false,
    val hasUnsupportedFormat: Boolean = false,
    val hasFallbackMetadata: Boolean = false,
    val errorMessage: String = ""
)

// Paginated audio files response
data class PaginatedAudioFilesResponse(
    val authToken: String,
    val audioFiles: List<String>,
    val hasNextPage: Boolean,
    val nextPageToken: String?
)

// Cached audio files response
data class CachedAudioFilesResponse(
    val authToken: String,
    val audioFiles: List<String>,
    val servedFromCache: Boolean,
    val totalApiCallsMade: Int
)

// Error handling audio files response
data class ErrorHandlingAudioFilesResponse(
    val authToken: String = "",
    val audioFiles: List<String> = emptyList(),
    val hasNetworkError: Boolean = false,
    val hasHttpError: Boolean = false,
    val hasAuthError: Boolean = false,
    val httpErrorCode: Int = 0,
    val errorMessage: String = "",
    val retriesPerformed: Int = 0,
    val usedCachedFallback: Boolean = false
)

// ===== CACHE AND STATUS ENUMS =====

// Android architecture support - Cache status for UI state management
enum class CacheStatus {
    Ready,
    Processing,
    Error
}

// ===== MEMORY OPTIMIZATION MODELS =====

// Memory optimization data models
data class MusicTrackMemoryOptimized(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val genre: String,
    val filePath: String,
    val durationMs: Long,
    val fileSizeBytes: Long,
    val bitrate: Int,
    val dateAdded: Date
)

data class MemoryOptimizationStats(
    val usedEfficientDataStructures: Boolean,
    val usedStringInterning: Boolean,
    val usedObjectPooling: Boolean,
    val enabledGCOptimization: Boolean
)

data class MemoryOptimizationResponse(
    val usedMemoryOptimization: Boolean,
    val memoryReductionPercent: Double,
    val dataIntegrityVerified: Boolean,
    val optimizations: MemoryOptimizationStats,
    val loadingTimeMs: Long,
    val totalTracksLoaded: Int,
    val peakMemoryUsageMB: Long
)

// ===== SYNC AND PROGRESS MODELS =====

// PLY-62 Progress Indicators for Library Scanning - Domain Models
data class ScanProgressUpdate(
    val percentComplete: Double,
    val currentOperation: String,
    val itemsProcessed: Int,
    val totalItems: Int,
    val estimatedTimeRemainingMs: Long?
)

data class LibraryScanResponse(
    val usedProgressTracking: Boolean,
    val directoriesScanned: Int,
    val totalFilesFound: Int,
    val scanDurationMs: Long,
    val realTimeUpdatesProvided: Boolean
)

// PLY-62 Background Sync and Incremental Updates - Domain Models
enum class SyncStatus {
    PENDING,
    SYNCED,
    COMPLETED,
    FAILED
}

data class MusicTrackSyncable(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val filePath: String,
    val lastModified: Long,
    val syncStatus: SyncStatus
)

data class BackgroundSyncResponse(
    val usedIncrementalSync: Boolean,
    val tracksProcessed: Int,
    val backgroundExecution: Boolean,
    val nonBlockingOperation: Boolean,
    val scalableForLargeDatasets: Boolean,
    val finalSyncStatus: SyncStatus,
    val syncDurationMs: Long,
    val dataIntegrityVerified: Boolean
)
