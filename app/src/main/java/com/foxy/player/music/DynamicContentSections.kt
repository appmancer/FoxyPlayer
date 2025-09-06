package com.foxy.player.music

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Internal data class for tracking play history
 */
internal data class PlayHistoryItem(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val path: String,
    val playedAt: Long
)

/**
 * PLY-142: User History Service for tracking recently played items
 * Provides in-memory storage for user play history with timestamp tracking
 */
class UserHistoryService {
    private val _playHistory = MutableStateFlow<List<PlayHistoryItem>>(emptyList())
    internal val playHistory: StateFlow<List<PlayHistoryItem>> = _playHistory.asStateFlow()

    /**
     * Records a play event for a song with timestamp
     */
    fun recordPlayEvent(song: Song, timestamp: Long = System.currentTimeMillis()) {
        val historyItem = PlayHistoryItem(
            id = generateId(song),
            title = song.title,
            artist = song.artist,
            album = song.album,
            path = song.path,
            playedAt = timestamp
        )
        
        val currentHistory = _playHistory.value.toMutableList()
        
        // Remove existing entry if present to avoid duplicates
        currentHistory.removeAll { it.id == historyItem.id }
        
        // Add to beginning (most recent first)
        currentHistory.add(0, historyItem)
        
        // Limit history size to 100 items
        if (currentHistory.size > 100) {
            currentHistory.removeLast()
        }
        
        _playHistory.value = currentHistory
    }

    /**
     * Gets recently played items as RecentItem objects
     */
    fun getRecentlyPlayed(limit: Int = 20): List<RecentItem> {
        return _playHistory.value
            .take(limit)
            .map { historyItem ->
                RecentItem(
                    id = historyItem.id,
                    title = historyItem.title,
                    artist = historyItem.artist,
                    lastPlayed = historyItem.playedAt,
                    artwork = generateArtworkUrl(historyItem.artist, historyItem.album)
                )
            }
    }

    private fun generateId(song: Song): String {
        return "${song.artist}-${song.title}".replace(" ", "-").lowercase()
    }

    private fun generateArtworkUrl(artist: String, album: String): String {
        // Placeholder artwork URL generation
        return "https://example.com/artwork/${artist.replace(" ", "-")}-${album.replace(" ", "-")}.jpg".lowercase()
    }
}

/**
 * PLY-142: Recently Played Section Generator
 * Creates ContentSection<RecentItem> from user history
 */
class RecentlyPlayedGenerator(
    private val userHistoryService: UserHistoryService
) {
    suspend fun generateSection(limit: Int = 10): Result<ContentSection<RecentItem>> {
        return try {
            val recentItems = userHistoryService.getRecentlyPlayed(limit)
            val section = ContentSection(
                title = "Recently Played",
                items = recentItems
            )
            Result.success(section)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * PLY-142: Recommendation Generator
 * Creates ContentSection<RecommendationItem> from album discovery service
 */
class RecommendationGenerator(
    private val albumDiscoveryService: AlbumDiscoveryService
) {
    suspend fun generateSection(limit: Int = 10): Result<ContentSection<RecommendationItem>> {
        return try {
            val albumsResult = albumDiscoveryService.getAlbumCentricData()
            
            if (albumsResult.isSuccess()) {
                val albums = albumsResult.getOrNull() ?: emptyList()
                val recommendations = albums.take(limit).mapIndexed { index, album ->
                    RecommendationItem(
                        id = "rec-${album.albumName.replace(" ", "-").lowercase()}",
                        title = album.albumName,
                        artist = album.artistName,
                        reason = generateRecommendationReason(album, index),
                        confidence = generateConfidenceScore(album, index),
                        artwork = generateArtworkUrl(album.artistName, album.albumName)
                    )
                }
                
                val section = ContentSection(
                    title = "Recommended for You",
                    items = recommendations
                )
                Result.success(section)
            } else {
                // Fallback recommendations
                val fallbackRecommendations = listOf(
                    RecommendationItem(
                        id = "rec-fallback-1",
                        title = "Discover New Music",
                        artist = "Various Artists",
                        reason = "Based on your music library",
                        confidence = 0.75,
                        artwork = "https://example.com/discover-music.jpg"
                    )
                )
                val section = ContentSection(
                    title = "Recommended for You",
                    items = fallbackRecommendations
                )
                Result.success(section)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun generateRecommendationReason(album: AlbumDiscoveryResult, index: Int): String {
        return when (index % 4) {
            0 -> "Popular album with ${album.trackCount} tracks"
            1 -> "From artist ${album.artistName}"
            2 -> "Discovered in your music library"
            else -> "Similar to your recent listening"
        }
    }

    private fun generateConfidenceScore(album: AlbumDiscoveryResult, index: Int): Double {
        // Generate confidence based on track count and position
        val baseConfidence = 0.6
        val trackCountBonus = (album.trackCount.coerceAtMost(20) / 20.0) * 0.3
        val positionPenalty = (index / 10.0) * 0.1
        return (baseConfidence + trackCountBonus - positionPenalty).coerceIn(0.0, 1.0)
    }

    private fun generateArtworkUrl(artist: String, album: String): String {
        return "https://example.com/artwork/${artist.replace(" ", "-")}-${album.replace(" ", "-")}.jpg".lowercase()
    }
}

/**
 * PLY-142: Content Section Organizer
 * Manages ordering and display logic for multiple content sections
 */
class ContentSectionOrganizer(
    private val userHistoryService: UserHistoryService,
    private val albumDiscoveryService: AlbumDiscoveryService
) {
    suspend fun generateOrderedSections(): Result<List<ContentSection<*>>> {
        return try {
            val sections = mutableListOf<ContentSection<*>>()
            
            // Check if user has recent history
            val recentItems = userHistoryService.getRecentlyPlayed(1)
            if (recentItems.isNotEmpty()) {
                // Generate recently played section first
                val recentlyPlayedGenerator = RecentlyPlayedGenerator(userHistoryService)
                val recentSection = recentlyPlayedGenerator.generateSection(10)
                if (recentSection.isSuccess) {
                    sections.add(recentSection.getOrThrow())
                }
            }
            
            // Generate recommendations section
            val recommendationGenerator = RecommendationGenerator(albumDiscoveryService)
            val recommendationSection = recommendationGenerator.generateSection(8)
            if (recommendationSection.isSuccess) {
                sections.add(recommendationSection.getOrThrow())
            }
            
            Result.success(sections)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}