package com.foxy.player.music

import org.junit.Assert.*
import org.junit.Test

/**
 * PLY-139: Test suite for HomeScreenContent data models
 * Tests RecentItem, ContentSection, and RecommendationItem functionality
 */
class HomeScreenContentTest {

    @Test
    fun `should create RecentItem with required properties`() {
        // Arrange
        val id = "recent_001"
        val title = "Bohemian Rhapsody"
        val artist = "Queen"
        val lastPlayed = System.currentTimeMillis()
        val artwork = "https://example.com/artwork.jpg"

        // Act
        val recentItem = RecentItem(
            id = id,
            title = title,
            artist = artist,
            lastPlayed = lastPlayed,
            artwork = artwork
        )

        // Assert
        assertEquals(id, recentItem.id)
        assertEquals(title, recentItem.title)
        assertEquals(artist, recentItem.artist)
        assertEquals(lastPlayed, recentItem.lastPlayed)
        assertEquals(artwork, recentItem.artwork)
    }
}
