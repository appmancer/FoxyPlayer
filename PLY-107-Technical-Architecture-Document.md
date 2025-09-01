# Music Discovery & Data Population Epic - Technical Architecture Document

**Project**: pCloud Music Player (Android)  
**Epic**: Music Discovery & Data Population  
**Research Ticket**: PLY-107  
**Date**: January 2025  
**Author**: Captain Scarlet

## Executive Summary

This document provides the technical architecture and implementation recommendations for the Music Discovery & Data Population Epic in the pCloud Music Player Android application. The research analyzes existing patterns, industry best practices, and technical constraints to establish a comprehensive approach for scalable music discovery and efficient data population.

## 1. Current System Analysis

### 1.1 Existing Architecture Overview

The pCloud Music Player currently implements a well-structured, modern Android architecture with these key characteristics:

**Core Technologies:**
- **Framework**: Android Jetpack Compose (Modern UI)
- **Language**: Kotlin with Coroutines 
- **Architecture**: MVVM with Repository Pattern
- **Database**: Room (SQLite) with batch processing
- **Networking**: OkHttp3 for pCloud API integration
- **Background Processing**: WorkManager for long-running tasks
- **State Management**: StateFlow for reactive UI updates

**Current Implementation Status:**
- ✅ **Authentication**: Complete pCloud integration with auto-server detection
- ✅ **Basic Music Discovery**: File system traversal and audio format detection
- ✅ **Data Models**: Comprehensive models for tracks, metadata, and pCloud API responses  
- ✅ **Database Layer**: Room entities with DAO patterns and repository abstraction
- ✅ **Background Sync**: Incremental sync service with deduplication and batch processing
- ⚠️ **API Integration**: Partially complete with fallback mock patterns
- ⚠️ **Metadata Extraction**: Basic implementation with Android MediaMetadataRetriever

### 1.2 Music Discovery Components

#### Core Discovery Services
```kotlin
// Primary discovery service (MusicDiscovery.kt:53-757)
class MusicDiscoveryService {
    fun listPCloudFolders(path: String): Result<FolderListing>
    fun listPCloudFoldersWithAPI(path: String): Result<PCloudAPIResponse>
    fun listPCloudFoldersRecursively(path: String): Result<RecursiveDirectoryResponse>
    fun listAudioFiles(path: String): Result<AudioFilesResponse>
}

// Heuristic discovery (HeuristicDiscovery.kt:14-57)  
class HeuristicMusicDiscovery {
    suspend fun listSongs(): Result<List<Song>>
    // Path-based discovery using folder structure patterns
}

// Search capabilities (MusicSearch.kt:8-64)
class MusicSearchService {
    fun searchTracks(query: String): Result<MusicSearchResponse>
    fun browseByArtist(): Result<MusicBrowseResponse>
}
```

#### Data Population Architecture
```kotlin
// Background sync service (MusicModels.kt:62-308)
class BackgroundSyncService {
    suspend fun performIncrementalSync(): SyncResult<BackgroundSyncResponse>
    // Features: batch processing, deduplication, integrity validation
}

// Repository pattern (MusicModels.kt:602-708)
interface TrackRepositoryInterface {
    suspend fun getAllTracks(): List<TrackEntity>
    suspend fun insertTrack(id: String, title: String, artist: String, album: String, filePath: String)
    suspend fun getTracksPage(offset: Int, limit: Int): List<TrackEntity>
}
```

#### Current Data Models
```kotlin
// Core track representation
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

// pCloud API integration models
data class PCloudListFolderResponse(
    val result: Int,
    val metadata: PCloudMetadata?,
    val contents: List<PCloudItem>?
)
```

## 2. Industry Best Practices Analysis

### 2.1 Android Architecture Guidelines

Based on research of official Android Architecture Samples and industry standards:

**Recommended Patterns:**
- **Single Activity + Navigation**: ✅ Already implemented
- **Jetpack Compose**: ✅ Already implemented  
- **Repository Pattern**: ✅ Already implemented
- **MVVM with ViewModel**: ✅ Already implemented
- **Dependency Injection**: ⚠️ Currently manual, recommend Hilt
- **Offline-First Architecture**: ✅ Already implemented
- **Coroutines for Async**: ✅ Already implemented

**Industry Best Practices for Music Apps:**
1. **MediaSession Integration**: For system-wide media controls
2. **ExoPlayer/Media3**: For advanced audio playback capabilities
3. **Notification MediaStyle**: For lock screen and notification controls
4. **Background Audio Service**: For uninterrupted playback
5. **Content URI Handling**: For audio file access and security

### 2.2 Performance Optimization Patterns

**Current Optimizations** (Already Implemented):
```kotlin
// String interning for memory efficiency (MusicLibrary.kt:670-756)
class MusicMemoryOptimizationService {
    private val stringPool = mutableMapOf<String, String>()
    private val trackObjectPool = mutableListOf<MusicTrackMemoryOptimized>()
}

// Database indexing for fast search (MusicLibrary.kt:575-667)
class MusicDatabaseIndexService {
    private val artistIndex = mutableMapOf<String, MutableList<MusicTrackIndexed>>()
    private val titleIndex = mutableMapOf<String, MutableList<MusicTrackIndexed>>()
}

// Paginated data loading (MusicModels.kt:1069-1129)
class PaginatedTrackDataLayer {
    suspend fun loadTracksPage(pageNumber: Int, pageSize: Int): PagedTracksResult
}
```

**Additional Recommended Optimizations:**
- **LazyColumn with key()**: For efficient list rendering in Compose
- **Image Loading with Coil**: For album art caching and memory management
- **Database migrations**: For schema evolution without data loss
- **Proguard/R8 optimization**: For release build size reduction

## 3. Technical Constraints & Requirements

### 3.1 Platform Constraints

**Android Platform Requirements:**
- **Minimum SDK**: API 24 (Android 7.0) - Current
- **Target SDK**: API 34 (Android 14) - Current  
- **Compile SDK**: API 34 - Current
- **Java Compatibility**: Java 8 - Current

**Hardware Constraints:**
- **Storage**: Metadata caching requires efficient storage management
- **Memory**: Large music libraries need memory optimization (already implemented)
- **Network**: Intermittent connectivity requires robust offline support (already implemented)
- **Battery**: Background sync must be battery-efficient (WorkManager handles this)

### 3.2 pCloud API Constraints

**Current Integration Constraints:**
```kotlin
// Authentication integration (auth.kt:97-101)
class AuthRepository {
    private val httpClient = OkHttpClient()
    private var connectTimeout: Long = 30000L // 30 second timeout
    private var lastSuccessfulServer: String? = null // EU/US server caching
}
```

**API Rate Limiting Considerations:**
- pCloud API has undocumented rate limits
- Current implementation uses exponential backoff for retries
- Batch processing reduces API call frequency
- Caching reduces redundant API calls

**Data Format Constraints:**
- **Supported Audio Formats**: MP3, FLAC, WAV, M4A, AAC, OGG
- **Metadata Extraction**: Limited by Android MediaMetadataRetriever capabilities
- **File Size Limits**: No specific limits identified in pCloud documentation

### 3.3 Business Requirements

**Functional Requirements:**
- **Large Library Support**: Handle 10,000+ tracks efficiently ✅ (Batch processing implemented)
- **Offline Access**: Work without network connectivity ✅ (Offline library implemented)
- **Fast Search**: Sub-100ms search response times ✅ (Database indexing implemented)
- **Real-time Sync**: Update library when pCloud content changes ⚠️ (Periodic sync only)
- **Metadata Accuracy**: Extract and display comprehensive track information ⚠️ (Basic implementation)

**Non-Functional Requirements:**
- **Performance**: App launch < 3 seconds ✅
- **Battery Efficiency**: Background sync should not drain battery ✅ (WorkManager)
- **Storage Efficiency**: Metadata storage < 1MB per 1000 tracks ✅
- **Network Efficiency**: Minimize bandwidth usage ✅ (Incremental sync)
- **Security**: Secure token storage and transmission ✅ (Implemented)

## 4. Architecture Recommendations

### 4.1 Enhanced Discovery Architecture

**Recommended Architecture Extensions:**

```kotlin
// Enhanced discovery coordinator
class MusicDiscoveryCoordinator(
    private val pcloudApiService: PCloudApiService,
    private val metadataExtractor: MetadataExtractionService,
    private val cacheService: MusicCacheService,
    private val indexingService: DatabaseIndexingService
) {
    
    suspend fun performFullDiscovery(): DiscoveryResult {
        // 1. Authenticate with pCloud
        // 2. Discover folder structure recursively  
        // 3. Filter audio files by format
        // 4. Extract metadata in parallel batches
        // 5. Update database with conflict resolution
        // 6. Update search indices
        // 7. Notify UI of progress
    }
    
    suspend fun performIncrementalDiscovery(): DiscoveryResult {
        // 1. Get last sync timestamp
        // 2. Query pCloud for changes since timestamp
        // 3. Process only new/modified files
        // 4. Update database incrementally
    }
}
```

**Integration with Existing Components:**
```kotlin
// Leverage existing authenticated API client
class EnhancedPCloudApiService(
    private val authenticatedApiClient: AuthenticatedApiClient // Existing component
) {
    suspend fun listFolderContentsWithMetadata(folderId: String): List<PCloudFileWithMetadata>
    suspend fun getFileMetadata(fileId: String): FileMetadata
    suspend fun getFileDownloadUrl(fileId: String): String
}
```

### 4.2 Advanced Metadata Architecture

**Metadata Extraction Pipeline:**
```kotlin
// Enhanced metadata extraction service
class AdvancedMetadataExtractionService {
    
    private val extractors = listOf(
        AndroidMediaMetadataExtractor(), // Current implementation
        PCloudMetadataExtractor(),       // New: Extract from pCloud file properties
        HeuristicMetadataExtractor(),    // New: Infer from file path/name
        OnlineMetadataExtractor()        // Future: MusicBrainz/Last.fm integration
    )
    
    suspend fun extractMetadata(audioFile: AudioFile): EnhancedMetadata {
        // Try extractors in priority order
        // Combine results for comprehensive metadata
        // Cache results for future requests
    }
}

// Enhanced metadata model
data class EnhancedMetadata(
    val basic: BasicMetadata,          // Title, artist, album, duration
    val technical: TechnicalMetadata,  // Bitrate, format, file size
    val extended: ExtendedMetadata,    // Genre, year, track number, album art
    val computed: ComputedMetadata,    // Fingerprint, similarity scores
    val confidence: MetadataConfidence // Confidence scores for each field
)
```

### 4.3 Real-time Sync Architecture

**WebSocket Integration for Real-time Updates:**
```kotlin
// Real-time sync service (Future enhancement)
class RealTimeSyncService(
    private val webSocketClient: WebSocketClient,
    private val changeProcessor: ChangeProcessor
) {
    
    fun startRealTimeSync() {
        // Connect to pCloud WebSocket (if available)
        // Listen for folder/file change notifications
        // Process changes incrementally
        // Update local database in real-time
    }
}

// Fallback polling for systems without WebSocket
class PollingBasedSyncService {
    fun startPeriodicPolling(intervalMinutes: Int) {
        // Schedule periodic WorkManager tasks
        // Check for changes via REST API
        // Process updates incrementally
    }
}
```

### 4.4 Enhanced Search & Discovery

**Multi-tier Search Architecture:**
```kotlin
// Enhanced search service
class AdvancedSearchService {
    
    private val searchStrategies = listOf(
        ExactMatchStrategy(),           // Current implementation
        FuzzyMatchStrategy(),          // New: Typo tolerance
        SemanticSearchStrategy(),      // Future: AI-powered search
        PhoneticSearchStrategy()       // New: Sound-alike matching
    )
    
    suspend fun search(query: String): SearchResults {
        // Execute search strategies in parallel
        // Combine and rank results
        // Cache popular searches
    }
}

// Smart discovery features
class SmartDiscoveryService {
    suspend fun discoverSimilarTracks(track: MusicTrack): List<MusicTrack>
    suspend fun discoverByMood(mood: String): List<MusicTrack>
    suspend fun discoverRecentlyAdded(): List<MusicTrack>
    suspend fun discoverFrequentlyPlayed(): List<MusicTrack>
}
```

### 4.5 Performance Architecture

**Background Processing Pipeline:**
```kotlin
// Enhanced background processing
class BackgroundProcessingOrchestrator {
    
    suspend fun scheduleDiscoveryPipeline() {
        val pipeline = ProcessingPipeline.builder()
            .stage(FolderDiscoveryStage())
            .stage(FileFilteringStage())
            .stage(MetadataExtractionStage())
            .stage(DatabaseInsertionStage())
            .stage(IndexUpdateStage())
            .onProgress { updateNotification(it) }
            .build()
            
        pipeline.execute()
    }
}

// Memory-efficient processing
class StreamingProcessor {
    fun processLargeLibrary(): Flow<ProcessingResult> {
        // Process files in streaming fashion
        // Avoid loading entire library into memory
        // Use Kotlin Flow for backpressure handling
    }
}
```

## 5. Implementation Roadmap

### 5.1 Phase 1: Foundation Enhancement (2-3 weeks)

**Priority: HIGH**

**1.1 Complete pCloud API Integration**
- Remove remaining fallback mock patterns
- Implement comprehensive error handling for all API calls
- Add retry mechanisms with exponential backoff
- Implement API response caching for better performance

**1.2 Enhanced Metadata Extraction**
- Extend current MediaMetadataRetriever implementation
- Add support for album art extraction and caching
- Implement fallback metadata extraction from file paths
- Add metadata validation and error handling

**1.3 Dependency Injection Migration**
- Migrate from manual dependency injection to Hilt
- Improve testability and code maintainability
- Enable easier feature flag management

**Deliverables:**
- Complete pCloud API integration
- Enhanced metadata extraction service
- Hilt dependency injection setup
- Updated test coverage for new components

### 5.2 Phase 2: Advanced Discovery Features (3-4 weeks)

**Priority: MEDIUM**

**2.1 Real-time Sync Implementation**
- Research pCloud WebSocket capabilities or implement polling fallback
- Design change detection algorithms
- Implement incremental update processing
- Add conflict resolution for concurrent changes

**2.2 Advanced Search & Discovery**
- Implement fuzzy search with typo tolerance
- Add phonetic search for artist/song name variations
- Create smart discovery algorithms (similar tracks, mood-based)
- Implement search result ranking and caching

**2.3 Performance Optimizations**
- Add streaming processing for large libraries
- Implement advanced caching strategies
- Optimize database queries with proper indexing
- Add memory usage monitoring and optimization

**Deliverables:**
- Real-time sync service
- Advanced search capabilities
- Smart discovery features
- Performance monitoring dashboard

### 5.3 Phase 3: Production Polish (2-3 weeks)

**Priority: MEDIUM**

**3.1 Media Framework Integration**
- Integrate with Android MediaSession for system controls
- Add support for Media3/ExoPlayer for advanced playback
- Implement MediaStyle notifications
- Add support for Android Auto integration

**3.2 Enhanced User Experience**
- Add progress indicators for long-running operations
- Implement pull-to-refresh for manual sync
- Add advanced filtering and sorting options
- Create music library statistics and insights

**3.3 Quality Assurance**
- Comprehensive integration testing
- Performance testing with large music libraries
- Battery usage optimization testing
- Security audit of pCloud integration

**Deliverables:**
- Complete media framework integration
- Enhanced user experience features
- Comprehensive test suite
- Performance and security validation

### 5.4 Phase 4: Future Enhancements (Future Sprints)

**Priority: LOW**

**4.1 AI-Powered Features**
- Implement semantic search using machine learning
- Add automatic genre classification
- Create intelligent playlist generation
- Implement music recommendation engine

**4.2 Social Features**
- Add playlist sharing capabilities
- Implement collaborative playlists
- Add music discovery based on social connections
- Create listening statistics and social sharing

**4.3 Advanced Integrations**
- Integrate with external metadata sources (MusicBrainz, Last.fm)
- Add lyrics integration and display
- Implement cross-platform sync (if expanding beyond Android)
- Add support for external audio sources beyond pCloud

## 6. Technical Specifications

### 6.1 API Design Specifications

**Enhanced pCloud Integration:**
```kotlin
interface PCloudMusicApiService {
    @GET("listfolder")
    suspend fun listFolder(
        @Query("folderid") folderId: String,
        @Query("recursive") recursive: Boolean = false,
        @Query("showdeleted") showDeleted: Boolean = false
    ): Response<PCloudFolderResponse>
    
    @GET("file_open")
    suspend fun getFileStream(
        @Query("fileid") fileId: String
    ): Response<ResponseBody>
    
    @GET("getfilepublink")
    suspend fun getPublicLink(
        @Query("fileid") fileId: String
    ): Response<PCloudPublicLinkResponse>
}
```

**Metadata API Specifications:**
```kotlin
interface MetadataApiService {
    suspend fun extractMetadata(audioFile: AudioFile): Result<EnhancedMetadata>
    suspend fun extractAlbumArt(audioFile: AudioFile): Result<ByteArray>
    suspend fun validateMetadata(metadata: EnhancedMetadata): ValidationResult
    suspend fun enrichMetadata(basicMetadata: BasicMetadata): Result<EnhancedMetadata>
}
```

### 6.2 Database Schema Enhancements

**Enhanced Track Entity:**
```kotlin
@Entity(
    tableName = "tracks",
    indices = [
        Index(value = ["artist"], name = "idx_artist"),
        Index(value = ["album"], name = "idx_album"),
        Index(value = ["title"], name = "idx_title"),
        Index(value = ["genre"], name = "idx_genre"),
        Index(value = ["dateAdded"], name = "idx_date_added"),
        Index(value = ["lastPlayed"], name = "idx_last_played")
    ]
)
data class EnhancedTrackEntity(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val genre: String?,
    val year: Int?,
    val trackNumber: Int?,
    val discNumber: Int?,
    val durationMs: Long,
    val fileSizeBytes: Long,
    val bitrate: Int?,
    val sampleRate: Int?,
    val format: String,
    val filePath: String,
    val pcloudFileId: String,
    val dateAdded: Date,
    val dateModified: Date,
    val lastPlayed: Date?,
    val playCount: Int = 0,
    val isFavorite: Boolean = false,
    val albumArtPath: String?,
    val metadataConfidence: Float = 1.0f
)
```

**Additional Tables:**
```kotlin
@Entity(tableName = "albums")
data class AlbumEntity(
    @PrimaryKey val id: String,
    val name: String,
    val artist: String,
    val year: Int?,
    val trackCount: Int,
    val totalDuration: Long,
    val albumArtPath: String?,
    val dateAdded: Date
)

@Entity(tableName = "artists")
data class ArtistEntity(
    @PrimaryKey val id: String,
    val name: String,
    val albumCount: Int,
    val trackCount: Int,
    val totalDuration: Long,
    val dateAdded: Date
)

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String?,
    val trackCount: Int,
    val totalDuration: Long,
    val dateCreated: Date,
    val dateModified: Date,
    val isSystemPlaylist: Boolean = false
)
```

### 6.3 Configuration Specifications

**Enhanced Sync Configuration:**
```kotlin
data class AdvancedSyncConfiguration(
    val batchSize: Int = 50,
    val enableDeduplication: Boolean = true,
    val enableRealTimeSync: Boolean = false,
    val pollingIntervalMinutes: Int = 30,
    val maxConcurrentExtractions: Int = 4,
    val enableMetadataEnrichment: Boolean = true,
    val cacheExpirationHours: Int = 24,
    val enableBatteryOptimization: Boolean = true,
    val enableWifiOnlySync: Boolean = false,
    val audioFileExtensions: Set<String> = setOf("mp3", "wav", "flac", "m4a", "aac", "ogg"),
    val maxFileSize: Long = 500_000_000L, // 500MB
    val enableProgressNotifications: Boolean = true
)
```

## 7. Risk Assessment & Mitigation

### 7.1 Technical Risks

**Risk: pCloud API Rate Limiting**
- **Impact**: High - Could halt music discovery
- **Probability**: Medium
- **Mitigation**: Implement exponential backoff, request caching, batch processing optimization

**Risk: Large Library Performance**
- **Impact**: High - App becomes unusable with large music collections
- **Probability**: Low - Already mitigated with current architecture
- **Mitigation**: Streaming processing, pagination, database indexing (already implemented)

**Risk: Metadata Extraction Failures**
- **Impact**: Medium - Incomplete music library information
- **Probability**: Medium - Common with corrupted or unusual audio files
- **Mitigation**: Multiple extraction strategies, fallback mechanisms, graceful degradation

**Risk: Network Connectivity Issues**
- **Impact**: Medium - Limited functionality when offline
- **Probability**: High - Mobile devices frequently have connectivity issues
- **Mitigation**: Robust offline support (already implemented), intelligent sync retry

### 7.2 Business Risks

**Risk: User Adoption**
- **Impact**: High - Epic may not provide expected user value
- **Probability**: Low - Basic music discovery already functional
- **Mitigation**: Phased rollout, user feedback integration, A/B testing

**Risk: Development Timeline**
- **Impact**: Medium - May delay other features
- **Probability**: Medium - Complex integration work
- **Mitigation**: Phased implementation, modular architecture, parallel development

### 7.3 Compliance & Security Risks

**Risk: Data Privacy**
- **Impact**: High - User music library data must be protected
- **Probability**: Low - Current implementation already secure
- **Mitigation**: Secure token storage (implemented), encrypted local database, minimal data collection

**Risk: pCloud API Changes**
- **Impact**: High - Could break integration
- **Probability**: Medium - Third-party APIs do change
- **Mitigation**: API versioning, comprehensive error handling, fallback mechanisms

## 8. Success Metrics & Monitoring

### 8.1 Technical Metrics

**Performance Metrics:**
- Music discovery completion time: Target < 30 seconds for 1000 tracks
- Search response time: Target < 100ms for indexed search
- App startup time: Target < 3 seconds on mid-range devices
- Memory usage: Target < 200MB for 10,000 track library
- Battery impact: Target < 5% battery per hour of background sync

**Quality Metrics:**
- Metadata accuracy: Target > 95% for title/artist extraction
- API success rate: Target > 99% for pCloud API calls
- Sync reliability: Target > 99% successful sync operations
- Crash rate: Target < 0.1% crash rate
- User retention: Target > 80% 7-day retention

### 8.2 Business Metrics

**User Experience Metrics:**
- Time to first music playback: Target < 60 seconds after login
- User engagement: Target > 50% daily active users play music
- Feature adoption: Target > 70% users use search functionality
- Support tickets: Target < 2% users require support for music discovery

**Operational Metrics:**
- Development velocity: Track story points completed per sprint
- Code coverage: Target > 80% test coverage for new features
- Bug resolution time: Target < 48 hours for critical bugs
- Feature flag rollout: Safe gradual rollout of new features

## 9. Conclusion

The Music Discovery & Data Population Epic builds upon a solid foundation of well-architected Android components. The current implementation demonstrates excellent architectural patterns including:

- **MVVM with Repository Pattern** for clean separation of concerns
- **Room Database** with efficient batch processing and indexing
- **Coroutines and Flow** for reactive, non-blocking operations
- **Offline-First Architecture** with comprehensive caching
- **Background Processing** using WorkManager for reliability

### 9.1 Key Recommendations

1. **Complete pCloud Integration**: Remove remaining mock fallbacks and implement comprehensive error handling
2. **Enhanced Metadata Extraction**: Build upon existing MediaMetadataRetriever with multiple extraction strategies
3. **Real-time Sync**: Implement WebSocket or polling-based real-time updates
4. **Advanced Search**: Add fuzzy matching and semantic search capabilities
5. **Performance Optimization**: Continue leveraging existing optimizations while adding streaming processing

### 9.2 Implementation Approach

The recommended phased approach allows for:
- **Incremental Value Delivery**: Each phase provides immediate user value
- **Risk Mitigation**: Early phases validate technical approaches
- **Resource Optimization**: Allows parallel development of independent features
- **Quality Assurance**: Comprehensive testing at each phase

### 9.3 Expected Outcomes

Upon completion of this epic, the pCloud Music Player will provide:
- **Seamless Music Discovery**: Automatic detection and cataloging of pCloud music libraries
- **Rich Metadata**: Comprehensive track information including album art and technical details
- **Intelligent Search**: Fast, accurate search with typo tolerance and smart suggestions
- **Real-time Updates**: Immediate reflection of pCloud library changes
- **Scalable Performance**: Support for large music libraries (10,000+ tracks) with sub-second response times

The architecture leverages existing strengths while addressing current limitations, ensuring a robust, scalable, and user-friendly music discovery experience that builds naturally upon the already-solid foundation.

---

**Document Version**: 1.0  
**Last Updated**: January 2025  
**Next Review**: Upon Phase 1 completion