package com.foxy.player.music

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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

    @Test
    fun `should create ContentSection with title and items list`() {
        // Arrange
        val sectionTitle = "Recently Played"
        val recentItem1 = RecentItem(
            id = "recent_001",
            title = "Bohemian Rhapsody",
            artist = "Queen",
            lastPlayed = System.currentTimeMillis(),
            artwork = "https://example.com/queen.jpg"
        )
        val recentItem2 = RecentItem(
            id = "recent_002",
            title = "Hotel California",
            artist = "Eagles",
            lastPlayed = System.currentTimeMillis() - 1000,
            artwork = "https://example.com/eagles.jpg"
        )
        val items = listOf(recentItem1, recentItem2)

        // Act
        val contentSection = ContentSection(
            title = sectionTitle,
            items = items
        )

        // Assert
        assertEquals(sectionTitle, contentSection.title)
        assertEquals(items, contentSection.items)
        assertEquals(2, contentSection.items.size)
        assertTrue(contentSection.items.contains(recentItem1))
        assertTrue(contentSection.items.contains(recentItem2))
    }

    @Test
    fun `should create RecommendationItem with confidence score`() {
        // Arrange
        val id = "rec_001"
        val title = "Stairway to Heaven"
        val artist = "Led Zeppelin"
        val reason = "Based on your recent rock music listening"
        val confidence = 0.85
        val artwork = "https://example.com/led-zeppelin.jpg"

        // Act
        val recommendationItem = RecommendationItem(
            id = id,
            title = title,
            artist = artist,
            reason = reason,
            confidence = confidence,
            artwork = artwork
        )

        // Assert
        assertEquals(id, recommendationItem.id)
        assertEquals(title, recommendationItem.title)
        assertEquals(artist, recommendationItem.artist)
        assertEquals(reason, recommendationItem.reason)
        assertEquals(confidence, recommendationItem.confidence, 0.001)
        assertEquals(artwork, recommendationItem.artwork)
        assertTrue(recommendationItem.confidence >= 0.0)
        assertTrue(recommendationItem.confidence <= 1.0)
    }
}
