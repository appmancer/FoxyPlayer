# Albums-First Navigation Epic - Technical Architecture Document

## Executive Summary

This document presents the technical architecture and implementation strategy for the Albums-First Navigation Epic in the pCloud Music Player Android application. Based on comprehensive analysis of the existing codebase and industry best practices, this epic will transform the user experience to prioritize album-based music discovery and navigation.

## 1. Current State Analysis

### 1.1 Existing Navigation Architecture

**Framework**: Android Jetpack Navigation Component with Compose
- **Navigation Pattern**: Single-activity architecture using `NavHostController`
- **Route Structure**: Flat navigation with basic routes (Home, Songs, Artists, Albums, Folders)
- **State Management**: `MusicHubViewModel` with `StateFlow<HubState>` for reactive UI
- **Authentication**: Integrated `AuthGuard` for login flow protection

**Current Navigation Limitations**:
- Flat routing structure without nested album navigation
- Albums treated as secondary content (card-based hub doesn't prioritize albums)
- No album detail views or drill-down capabilities
- Missing album-specific state management

### 1.2 Music Library Architecture

**Data Layer**:
- **Database**: Room with single `TrackEntity` table
- **Discovery**: `HeuristicMusicDiscovery` extracts albums from pCloud folder structure
- **API Integration**: pCloud API via `MusicDiscoveryService` and `AuthenticatedApiClient`
- **Models**: Basic `AlbumSummary` with limited metadata (name, artist, song count)

**Current Gaps for Albums-First**:
- No dedicated album entity or rich album metadata
- Limited album artwork support
- No album-specific data queries or caching
- Missing track-to-album relationship modeling

## 2. Albums-First Vision & Requirements

### 2.1 User Experience Goals

**Primary Navigation Flow**:
1. **Albums Hub**: Home screen prioritizes albums with rich visual presentation
2. **Album Discovery**: Browse albums by artist, year, genre, recently added
3. **Album Detail**: Immersive album view with artwork, track listing, metadata
4. **Seamless Playback**: Album-aware play queue and track progression

**Key UX Principles**:
- **Visual Priority**: Album artwork as primary visual element
- **Contextual Navigation**: Deep linking between albums, artists, tracks
- **Progressive Disclosure**: Album → Track → Artist drill-down patterns
- **Cohesive Experience**: Album-centric organization throughout the app

### 2.2 Functional Requirements

**Core Features**:
- Album-centric home screen layout
- Nested navigation with album detail screens
- Rich album metadata display (artwork, year, genre, duration)
- Album-specific search and filtering
- Album-based playback queue management

**Performance Requirements**:
- Sub-200ms navigation transitions
- Lazy loading for large album collections (>1000 albums)
- Efficient artwork caching and loading
- Offline album browsing capability

## 3. Technical Architecture Design

### 3.1 Enhanced Navigation Architecture

**Nested Navigation Structure**:
```kotlin
// Enhanced navigation routes
object AlbumsRoutes {
    const val AlbumsHub = "albums_hub"
    const val AlbumDetail = "album_detail/{albumId}"
    const val AlbumTracks = "album_tracks/{albumId}"
    const val AlbumsByArtist = "albums_by_artist/{artistId}"
    const val AlbumsByGenre = "albums_by_genre/{genre}"
    const val AlbumsByYear = "albums_by_year/{year}"
}

// Navigation coordinator for complex flows
class AlbumsNavigationCoordinator {
    fun navigateToAlbum(albumId: String, source: NavigationSource)
    fun navigateToAlbumTrack(albumId: String, trackId: String)
    fun navigateToArtistAlbums(artistId: String)
    fun navigateBack(): Boolean // Handle deep navigation stack
}
```

**Navigation State Management**:
```kotlin
data class AlbumsNavigationState(
    val currentAlbum: AlbumEntity?,
    val navigationHistory: List<NavigationEntry>,
    val breadcrumbs: List<BreadcrumbItem>,
    val canNavigateBack: Boolean
)
```

### 3.2 Enhanced Data Architecture

**Core Album Entity**:
```kotlin
@Entity(tableName = "albums")
data class AlbumEntity(
    @PrimaryKey val id: String,
    val name: String,
    val artist: String,
    val artistId: String,
    val year: Int?,
    val genre: String?,
    val coverArtPath: String?,
    val coverArtUrl: String?,
    val trackCount: Int,
    val totalDuration: Long,
    val dateAdded: Date,
    val dateModified: Date,
    val pCloudPath: String,
    val sortKey: String // For efficient sorting
)

@Entity(tableName = "album_tracks")
data class AlbumTrackEntity(
    @PrimaryKey val id: String,
    val albumId: String,
    val trackId: String,
    val trackNumber: Int,
    val discNumber: Int = 1,
    @ForeignKey(entity = AlbumEntity::class, parentColumns = ["id"], childColumns = ["albumId"])
    val album: AlbumEntity
)
```

**Album-Specific Data Access**:
```kotlin
@Dao
interface AlbumDao {
    @Query("SELECT * FROM albums ORDER BY sortKey")
    fun getAllAlbums(): Flow<List<AlbumEntity>>
    
    @Query("SELECT * FROM albums WHERE artistId = :artistId")
    fun getAlbumsByArtist(artistId: String): Flow<List<AlbumEntity>>
    
    @Query("SELECT * FROM albums WHERE genre = :genre")
    fun getAlbumsByGenre(genre: String): Flow<List<AlbumEntity>>
    
    @Transaction
    @Query("SELECT * FROM albums WHERE id = :albumId")
    fun getAlbumWithTracks(albumId: String): Flow<AlbumWithTracks>
}

data class AlbumWithTracks(
    @Embedded val album: AlbumEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "albumId",
        entity = AlbumTrackEntity::class
    )
    val tracks: List<TrackEntity>
)
```

### 3.3 Albums-First UI Architecture

**Album Hub Screen**:
```kotlin
@Composable
fun AlbumsHubScreen(
    viewModel: AlbumsHubViewModel,
    navigator: AlbumsNavigationCoordinator
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LazyVerticalGrid(
        columns = GridCells.Adaptive(160.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(uiState.featuredAlbums) { album ->
            AlbumCard(
                album = album,
                onClick = { navigator.navigateToAlbum(album.id, NavigationSource.Hub) }
            )
        }
    }
}

@Composable
fun AlbumDetailScreen(
    albumId: String,
    viewModel: AlbumDetailViewModel,
    navigator: AlbumsNavigationCoordinator
) {
    val album by viewModel.getAlbum(albumId).collectAsState(null)
    val tracks by viewModel.getAlbumTracks(albumId).collectAsState(emptyList())
    
    LazyColumn {
        item {
            AlbumHeader(
                album = album,
                onPlayAlbum = { viewModel.playAlbum(albumId) },
                onShuffleAlbum = { viewModel.shuffleAlbum(albumId) }
            )
        }
        items(tracks) { track ->
            AlbumTrackItem(
                track = track,
                onPlayTrack = { viewModel.playTrack(track.id, albumId) }
            )
        }
    }
}
```

**State Management Architecture**:
```kotlin
class AlbumsHubViewModel(
    private val albumRepository: AlbumRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AlbumsHubUiState())
    val uiState: StateFlow<AlbumsHubUiState> = _uiState.asStateFlow()
    
    data class AlbumsHubUiState(
        val isLoading: Boolean = true,
        val featuredAlbums: List<AlbumEntity> = emptyList(),
        val recentAlbums: List<AlbumEntity> = emptyList(),
        val sortOrder: AlbumSortOrder = AlbumSortOrder.RECENT,
        val viewMode: AlbumViewMode = AlbumViewMode.GRID,
        val error: String? = null
    )
}

enum class AlbumSortOrder { NAME, ARTIST, YEAR, RECENT, MOST_PLAYED }
enum class AlbumViewMode { GRID, LIST, DETAILED }
```

## 4. Integration Strategy

### 4.1 Music Discovery Integration

**Enhanced Album Metadata Extraction**:
```kotlin
// Extend MusicDiscoveryService
class AlbumsFirstMusicDiscovery(
    private val basicDiscovery: MusicDiscoveryService,
    private val artworkExtractor: AlbumArtworkExtractor
) {
    suspend fun discoverAlbumsWithRichMetadata(path: String): Result<List<EnrichedAlbum>> {
        // 1. Use existing HeuristicMusicDiscovery for basic album detection
        // 2. Extract embedded artwork from audio files
        // 3. Download/cache album artwork from pCloud
        // 4. Extract year, genre from ID3 tags
        // 5. Calculate total duration and track count
    }
}

class AlbumArtworkExtractor {
    suspend fun extractArtworkFromTrack(trackPath: String): ByteArray?
    suspend fun findAlbumArtworkFile(albumPath: String): String?
    suspend fun downloadArtworkFromPCloud(artworkPath: String): Result<ByteArray>
}
```

### 4.2 pCloud API Integration

**Album-Specific API Extensions**:
```kotlin
class AlbumsPCloudService(
    private val authenticatedClient: AuthenticatedApiClient
) {
    suspend fun getAlbumArtwork(albumPath: String): Result<ByteArray>
    suspend fun getAlbumMetadata(albumPath: String): Result<AlbumMetadata>
    suspend fun syncAlbumChanges(lastSyncTime: Long): Result<List<AlbumChange>>
}

data class AlbumMetadata(
    val name: String,
    val artist: String,
    val year: Int?,
    val genre: String?,
    val artworkFiles: List<String>,
    val trackFiles: List<TrackFile>
)
```

### 4.3 Database Migration Strategy

**Migration Path**:
```kotlin
@Database(
    entities = [TrackEntity::class, AlbumEntity::class, AlbumTrackEntity::class],
    version = 2,
    autoMigrations = [
        AutoMigration(from = 1, to = 2)
    ]
)
abstract class MusicDatabase : RoomDatabase() {
    // Migration will:
    // 1. Create albums table
    // 2. Create album_tracks junction table
    // 3. Add albumId foreign key to tracks table
    // 4. Populate albums from existing track data
}
```

## 5. Technical Constraints & Considerations

### 5.1 Platform Constraints

**Android API Requirements**:
- **Minimum SDK**: 24 (Android 7.0) - Current constraint
- **Target SDK**: 34 (Android 14) - Current target
- **Compose**: BOM 2024.10.00 - Supports advanced grid layouts
- **Navigation**: Compose Navigation 2.7.7 - Supports nested navigation

**Device Performance Considerations**:
- **Memory**: Efficient album artwork caching (max 50MB cache)
- **Storage**: Local album metadata cache for offline browsing
- **Network**: Progressive artwork loading and caching strategy

### 5.2 pCloud API Constraints

**Rate Limiting**:
- pCloud API rate limits: 1000 requests/hour per user
- Batch artwork downloads to minimize API calls
- Implement exponential backoff for failed requests

**Data Transfer**:
- Large album collections (>1000 albums) require progressive sync
- Artwork files can be 1-5MB each - implement compression

### 5.3 User Experience Constraints

**Performance Targets**:
- Album grid loading: <500ms for 50 albums
- Album detail screen: <200ms navigation transition
- Artwork loading: Progressive with placeholders
- Search/filter: <100ms response time for local data

**Accessibility Requirements**:
- Full TalkBack support for album navigation
- High contrast album artwork handling
- Text scaling support for album metadata

## 6. Implementation Roadmap

### Phase 1: Foundation (2-3 weeks)
**Database & Models**:
- [ ] Create AlbumEntity and AlbumTrackEntity models
- [ ] Implement AlbumDao with core queries
- [ ] Create database migration from v1 to v2
- [ ] Update TrackEntity with album relationship

**Basic Album Discovery**:
- [ ] Enhance HeuristicMusicDiscovery for albums
- [ ] Implement basic album metadata extraction
- [ ] Create AlbumRepository with caching

### Phase 2: Core Navigation (2-3 weeks)
**Navigation Architecture**:
- [ ] Implement AlbumsNavigationCoordinator
- [ ] Create album-specific routes and navigation
- [ ] Add navigation state management
- [ ] Implement deep linking for albums

**UI Foundation**:
- [ ] Create AlbumsHubScreen with grid layout
- [ ] Implement AlbumDetailScreen
- [ ] Add AlbumCard component with artwork support

### Phase 3: Rich Features (3-4 weeks)
**Album Artwork**:
- [ ] Implement AlbumArtworkExtractor
- [ ] Add artwork caching strategy
- [ ] Create placeholder and loading states

**Enhanced Metadata**:
- [ ] Extract year, genre from ID3 tags
- [ ] Implement album duration calculation
- [ ] Add album sorting and filtering

**Integration**:
- [ ] Integrate with existing authentication
- [ ] Update BackgroundSyncService for albums
- [ ] Add album-specific progress indicators

### Phase 4: Optimization & Polish (2-3 weeks)
**Performance**:
- [ ] Implement lazy loading for large collections
- [ ] Optimize artwork loading and caching
- [ ] Add search performance optimizations

**UX Refinements**:
- [ ] Add smooth navigation transitions
- [ ] Implement album-specific error handling
- [ ] Add accessibility improvements

**Testing & Quality**:
- [ ] Comprehensive unit test coverage
- [ ] UI testing for album navigation flows
- [ ] Performance testing with large datasets

## 7. Success Metrics

### 7.1 Technical Metrics
- **Navigation Performance**: <200ms album detail transitions
- **Loading Times**: <500ms for 50-album grid rendering
- **Memory Usage**: <50MB album artwork cache
- **Database Performance**: <100ms for album queries

### 7.2 Quality Metrics
- **Test Coverage**: >90% for albums-related code
- **Code Quality**: Pass all ktlint and detekt checks
- **Accessibility**: 100% TalkBack compatibility
- **Stability**: Zero crashes in album navigation flows

## 8. Risk Mitigation

### 8.1 Technical Risks
**Large Dataset Performance**:
- *Risk*: Slow loading with >1000 albums
- *Mitigation*: Implement pagination and lazy loading from Phase 1

**pCloud API Limitations**:
- *Risk*: Rate limiting affects album sync
- *Mitigation*: Implement intelligent batching and caching

**Database Migration Issues**:
- *Risk*: Data loss during schema migration
- *Mitigation*: Comprehensive migration testing and backup strategy

### 8.2 User Experience Risks
**Artwork Loading Performance**:
- *Risk*: Slow album grid rendering
- *Mitigation*: Progressive loading with placeholders, image compression

**Navigation Complexity**:
- *Risk*: Users confused by nested navigation
- *Mitigation*: Clear breadcrumbs and back navigation patterns

## 9. Conclusion

The Albums-First Navigation Epic represents a significant enhancement to the pCloud Music Player that will transform the user experience while building upon the existing solid architectural foundation. The proposed architecture leverages modern Android development patterns (Compose, Navigation Component, Room) and integrates seamlessly with the current pCloud API and authentication systems.

Key benefits of this approach:
- **Incremental Implementation**: Phases allow for iterative development and testing
- **Backward Compatibility**: Existing functionality remains intact during development
- **Scalable Architecture**: Design supports future enhancements and growing music libraries
- **User-Centric Design**: Albums-first approach aligns with natural music discovery patterns

The implementation roadmap provides a clear path from foundation to polished user experience, with defined success metrics and risk mitigation strategies to ensure successful delivery.

---

*Document prepared by Captain Scarlet - Spectrum Development Team*  
*Analysis based on comprehensive codebase review and industry best practices*  
*Ready for design phase and implementation planning*