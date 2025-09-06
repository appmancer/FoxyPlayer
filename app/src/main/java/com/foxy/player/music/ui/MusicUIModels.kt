package com.foxy.player.music.ui

import com.foxy.player.music.entities.AudioMetadata
import com.foxy.player.music.entities.TrackEntity
import java.util.Date

// ===== UI STATE MANAGEMENT MODELS =====

data class MusicSearchUIState(
    val searchQuery: String = "",
    val searchResults: List<MusicTrack> = emptyList(),
    val showSearchInput: Boolean = true,
    val isLoading: Boolean = false
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

data class SpecificApiErrorResponse(
    val errorType: String,
    val httpStatusCode: Int,
    val errorMessage: String,
    val suggestedRetryDelayMs: Long? = null
)

data class CircuitBreakerState(
    val state: String, // "CLOSED", "OPEN", "HALF_OPEN"
    val failureCount: Int,
    val lastFailureTimeMs: Long?
)

data class CircuitBreakerResponse(
    val circuitOpen: Boolean,
    val message: String
)

data class DetailedErrorReport(
    val endpoint: String,
    val errorType: String,
    val httpStatusCode: Int,
    val attemptNumber: Int,
    val timestamp: Long,
    val exceptionDetails: String,
    val debugContext: Map<String, String>
)

// ===== MULTI-STRATEGY METADATA EXTRACTION MODELS =====

interface MetadataStrategy {
    suspend fun extractMetadata(audioFileUrl: String, audioFileName: String, filePath: String): Result<AudioMetadata>
    fun getConfidenceScore(metadata: AudioMetadata): Double
    fun getStrategyName(): String
}

data class StrategyResult(
    val strategyName: String,
    val metadata: AudioMetadata,
    val confidence: Double
)

data class MultiStrategyMetadataResult(
    val metadata: AudioMetadata,
    val overallConfidence: Double,
    val strategyResults: Map<String, StrategyResult>,
    val usedCrossValidation: Boolean,
    val usedWeightedAveraging: Boolean
)

// ===== ERROR HANDLING AND LOGGING MODELS =====

data class StrategyError(
    val strategyName: String,
    val errorMessage: String,
    val errorType: String,
    val timestamp: Long
)

data class ErrorLog(
    val strategyErrors: List<StrategyError>,
    val errorsByCategory: Map<String, List<StrategyError>>
)

data class PerformanceMetrics(
    val executionTimeMs: Long,
    val strategyAttempts: Int
)

data class MultiStrategyErrorResult(
    val errorLog: ErrorLog,
    val usedFallbackMetadata: Boolean,
    val fallbackMetadata: AudioMetadata?,
    val performanceMetrics: PerformanceMetrics
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

// ===== PAGINATED UI DATA LAYER =====

/**
 * Result container for paginated track loading.
 * Provides track data with pagination metadata.
 */
data class PagedTracksResult(
    val tracks: List<TrackEntity>,
    val hasMorePages: Boolean,
    val totalCount: Int
)

// ===== OFFLINE UI MODELS =====

/**
 * Result wrapper for offline music library operations.
 * Provides type-safe error handling for offline access scenarios.
 */
sealed class OfflineResult<out T> {
    data class Success<out T>(val data: T, val fromCache: Boolean = false) : OfflineResult<T>()
    data class Error(val exception: Throwable, val message: String) : OfflineResult<Nothing>()
    object NetworkUnavailable : OfflineResult<Nothing>()
}

// ===== AUDIO METADATA MODELS =====

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

// ===== PLY-141: MODULAR UI COMPONENT STRUCTURE MODELS =====

// Layout types for ContentSectionComponent 
enum class LayoutType {
    LIST,
    GRID,
    CAROUSEL
}

// ContentSectionComponent for modular UI structure
data class ContentSectionComponent<T>(
    val section: com.foxy.player.music.ContentSection<T>,
    val layoutType: LayoutType
) {
    fun canRenderContent(): Boolean {
        return section.items.isNotEmpty()
    }
}

// RecentlyPlayedSection - specialized component for recent items with horizontal layout
class RecentlyPlayedSection(private val recentItems: List<com.foxy.player.music.RecentItem>) {
    fun getTitle(): String = "Recently Played"
    
    fun getLayoutType(): LayoutType = LayoutType.CAROUSEL // Horizontal layout for recent items
    
    fun getRecentItems(): List<com.foxy.player.music.RecentItem> = recentItems
    
    fun hasContent(): Boolean = recentItems.isNotEmpty()
    
    fun asContentSectionComponent(): ContentSectionComponent<com.foxy.player.music.RecentItem> {
        val section = com.foxy.player.music.ContentSection(
            title = getTitle(),
            items = recentItems
        )
        return ContentSectionComponent(section, getLayoutType())
    }
}
