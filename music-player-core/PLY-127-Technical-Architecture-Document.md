# Albums-First Navigation Foundation - Technical Architecture Document

**Research Ticket**: PLY-127  
**Date**: September 3, 2025  
**Status**: Architecture Research Complete  

## Executive Summary

This document presents the technical architecture for implementing Albums-First Navigation Foundation in the pCloud Music Player Android app. The research builds upon the enhanced album database schema delivered in PLY-124 and defines a comprehensive approach for transitioning from track-centric to album-centric user experience.

## Current State Analysis

### Existing Album Infrastructure (Post PLY-124)

**✅ Database Layer (STRONG FOUNDATION)**
- Enhanced album entities with proper relationships (`EnhancedAlbumEntity`, `EnhancedTrackEntity`, `EnhancedAlbumTrackEntity`)
- Room database version 2 with foreign key constraints and performance indices
- Junction table pattern for album-track relationships
- Database migration framework supporting schema evolution

**🔄 UI Layer (NEEDS ENHANCEMENT)**
- Basic album list view (`AlbumsListScreen`) with simple text display
- Hub-style navigation with album cards showing counts
- Track-first navigation patterns in place
- Material3 components ready for enhancement

**⚠️ Architecture Gaps**
- No dedicated album repository layer
- No album-specific ViewModels
- No album detail/playback screens
- Missing album artwork integration
- No album-first navigation flows

## Industry Best Practices Research

### Album-First Navigation Patterns

**1. Hierarchical Album Navigation**
```
Albums Grid → Album Detail → Track List → Now Playing
     ↓
Album Artwork + Metadata + Play Controls
```

**2. Album Discovery Patterns**
- **Grid Layout**: Primary album browsing (Spotify, Apple Music standard)
- **Artwork Prominence**: Large album covers as primary navigation elements
- **Metadata Rich**: Artist, year, track count immediately visible
- **Quick Actions**: Play, shuffle, add to library without drilling down

**3. Performance Considerations**
- **Lazy Loading**: Albums loaded on-demand with pagination
- **Image Caching**: Album artwork cached aggressively
- **Background Preloading**: Next page/related albums prefetched
- **Memory Management**: Large album collections handled via windowing

## Technical Architecture Recommendations

### 1. Repository Layer Enhancement

**New: AlbumRepository**
```kotlin
interface AlbumRepositoryInterface {
    suspend fun getAllAlbums(): List<EnhancedAlbumEntity>
    suspend fun getAlbumById(albumId: String): EnhancedAlbumEntity?
    suspend fun getAlbumsByArtist(artist: String): List<EnhancedAlbumEntity>
    suspend fun getAlbumsPage(offset: Int, limit: Int): List<EnhancedAlbumEntity>
    suspend fun getAlbumWithTracks(albumId: String): AlbumWithTracksOrdered?
    suspend fun searchAlbums(query: String): List<EnhancedAlbumEntity>
}
```

**Integration with Existing Infrastructure**:
- Leverage PLY-124 enhanced entities and DAOs
- Follow existing `TrackRepository` patterns for consistency
- Implement `RepositoryResult<T>` wrapper for error handling

### 2. ViewModel Architecture

**AlbumListViewModel**
```kotlin
class AlbumListViewModel(
    private val albumRepository: AlbumRepositoryInterface,
    private val imageCache: AlbumArtworkCache
) : ViewModel() {
    
    sealed class AlbumListState {
        object Loading : AlbumListState()
        data class Success(val albums: List<AlbumUIModel>) : AlbumListState()
        data class Error(val message: String) : AlbumListState()
    }
    
    val albumsState: StateFlow<AlbumListState>
    
    fun loadAlbums()
    fun searchAlbums(query: String)
    fun loadNextPage()
}
```

**AlbumDetailViewModel**
```kotlin
class AlbumDetailViewModel(
    private val albumRepository: AlbumRepositoryInterface,
    private val playbackController: MediaController
) : ViewModel() {
    
    data class AlbumDetailState(
        val album: EnhancedAlbumEntity?,
        val tracks: List<EnhancedTrackEntity>,
        val isPlaying: Boolean,
        val currentTrack: EnhancedTrackEntity?
    )
    
    fun loadAlbumDetail(albumId: String)
    fun playAlbum()
    fun playTrack(trackEntity: EnhancedTrackEntity)
}
```

### 3. UI Component Architecture

**Screen Hierarchy**
```
AlbumListScreen (Grid of albums)
├── AlbumDetailScreen (Album info + track list)
│   ├── AlbumArtworkView (Large album art)
│   ├── AlbumMetadataView (Title, artist, year)
│   ├── AlbumActionsView (Play, shuffle, add)
│   └── TrackListView (Ordered track listing)
└── AlbumPlayerScreen (Full-screen playback)
```

**Compose UI Components**
```kotlin
@Composable
fun AlbumGridItem(
    album: AlbumUIModel,
    onClick: (String) -> Unit
)

@Composable  
fun AlbumDetailHeader(
    album: EnhancedAlbumEntity,
    onPlayClick: () -> Unit,
    onShuffleClick: () -> Unit
)

@Composable
fun AlbumTrackList(
    tracks: List<EnhancedTrackEntity>,
    onTrackClick: (EnhancedTrackEntity) -> Unit
)
```

### 4. Navigation Architecture Updates

**Enhanced Route Definitions**
```kotlin
object AlbumRoutes {
    const val AlbumList = "albums"
    const val AlbumDetail = "albums/{albumId}"
    const val AlbumPlayer = "albums/{albumId}/player"
    
    fun albumDetail(albumId: String) = "albums/$albumId"
    fun albumPlayer(albumId: String) = "albums/$albumId/player"
}
```

**Navigation Graph Enhancement**
```kotlin
// In MusicNavHost.kt
composable(AlbumRoutes.AlbumList) { 
    AlbumListScreen(viewModel, navigator)
}
composable(AlbumRoutes.AlbumDetail) { backStackEntry ->
    val albumId = backStackEntry.arguments?.getString("albumId")
    AlbumDetailScreen(albumId, viewModel, navigator)
}
```

### 5. Data Integration Strategy

**Album Data Population**
- Extend existing `HeuristicMusicDiscovery` for album grouping
- Integrate with `MusicDiscoveryService` for pCloud metadata
- Use PLY-124 migration framework for data consistency

**Performance Optimization**
- Implement album pagination (50 albums per page)
- Cache album artwork using Coil/Glide
- Background sync for album metadata updates

## Integration Points with Current Architecture

### 1. Authentication Integration
- **Existing**: `AuthRepository` and `AuthenticatedApiClient`
- **Integration**: Album repository uses same auth patterns
- **Navigation**: Album screens protected by auth guards

### 2. Music Discovery Integration  
- **Existing**: `MusicDiscoveryService` and `HeuristicMusicDiscovery`
- **Enhancement**: Add album-specific discovery methods
- **Performance**: Leverage existing caching mechanisms

### 3. Navigation Integration
- **Existing**: `Navigator` interface and `Routes` object
- **Enhancement**: Add album-specific routes
- **Consistency**: Follow existing nav patterns

### 4. Database Integration
- **Foundation**: PLY-124 enhanced album entities and DAOs
- **Extension**: New album repository layer
- **Migration**: Use existing migration framework for future updates

## Technical Constraints and Considerations

### 1. Performance Constraints
- **Album Count**: Support 10,000+ albums without performance degradation
- **Image Loading**: Album artwork must load within 300ms on average
- **Memory Usage**: Maximum 50MB additional memory for album views
- **Navigation**: Sub-100ms navigation between album screens

### 2. Platform Constraints
- **Android API**: Minimum API 24 (Android 7.0) compatibility
- **Storage**: Album artwork cache limited to 100MB
- **Network**: Graceful handling of offline/low-bandwidth scenarios
- **Battery**: Background album sync must be battery-efficient

### 3. Integration Constraints
- **pCloud API**: Rate limiting (100 requests/minute)
- **Authentication**: OAuth token refresh handling
- **Database**: Room migration compatibility
- **Media Playback**: Media3/ExoPlayer integration requirements

### 4. User Experience Constraints
- **Loading States**: Maximum 2-second load time for album list
- **Accessibility**: Full VoiceOver/TalkBack support
- **Offline Mode**: Core album navigation works offline
- **Search**: Album search results within 500ms

## Risk Assessment

### High Risk
- **Album Artwork Loading**: Large images may cause memory issues
  - *Mitigation*: Implement aggressive image compression and caching
- **Database Performance**: Large album collections may slow queries
  - *Mitigation*: Use PLY-124 performance indices and pagination

### Medium Risk  
- **pCloud API Changes**: Album metadata structure changes
  - *Mitigation*: Robust error handling and fallback mechanisms
- **Navigation Complexity**: Deep album navigation may confuse users
  - *Mitigation*: Clear breadcrumbs and back navigation

### Low Risk
- **Testing Complexity**: Additional UI test surface area
  - *Mitigation*: Leverage existing testing patterns and TDD approach

## Success Metrics

### Technical Metrics
- **Performance**: Album list loads in <2 seconds
- **Memory**: <50MB additional memory usage
- **Reliability**: 99.9% crash-free album navigation
- **Coverage**: 90%+ unit test coverage for album features

### User Experience Metrics
- **Adoption**: 60%+ of users access album-first navigation
- **Engagement**: 30% increase in album-based music discovery
- **Retention**: Maintained or improved app retention rates
- **Satisfaction**: 4.5+ star rating for album features

## Next Steps

This architecture research provides the foundation for implementing Albums-First Navigation. The recommended approach leverages existing infrastructure while introducing focused enhancements for album-centric user experiences.

**Immediate Next Phase**: Implementation planning and task breakdown based on this architecture.

---

**Research completed by**: Captain Scarlet  
**Review status**: Ready for design phase  
**Implementation readiness**: HIGH - Clear architecture with defined integration points