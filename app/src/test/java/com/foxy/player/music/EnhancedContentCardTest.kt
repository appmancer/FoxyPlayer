package com.foxy.player.music

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for EnhancedContentCard components
 * 
 * PLY-144: Enhanced Content Cards
 * Tests enhanced card components with artwork integration, progress indicators, and animations
 */
class EnhancedContentCardTest {

    @Test
    fun `Enhanced content card should display artwork from URL`() = runTest {
        // Arrange
        val testItem = ContentCardItem(
            id = "test-card-1",
            title = "Test Album",
            subtitle = "Test Artist",
            artworkUrl = "https://example.com/artwork.jpg"
        )
        
        // Act
        val cardComponent = EnhancedContentCard.create(testItem)
        val artworkResult = cardComponent.loadArtwork()
        
        // Assert
        assertNotNull("Enhanced card should be created successfully", cardComponent)
        assertEquals("Test Album", cardComponent.title)
        assertEquals("Test Artist", cardComponent.subtitle)
        assertEquals("https://example.com/artwork.jpg", cardComponent.artworkUrl)
        assertTrue("Artwork should be loaded from URL", artworkResult.isSuccess)
    }

    @Test
    fun `Enhanced content card should display playback progress indicator`() = runTest {
        // Arrange
        val testItem = ContentCardItem(
            id = "test-card-2",
            title = "Playing Song",
            subtitle = "Current Artist",
            artworkUrl = "https://example.com/song-art.jpg"
        )
        val playbackProgress = 0.35f // 35% progress
        
        // Act
        val cardComponent = EnhancedContentCard.create(testItem)
        cardComponent.setPlaybackProgress(playbackProgress)
        val progressValue = cardComponent.getPlaybackProgress()
        val hasProgressIndicator = cardComponent.hasProgressIndicator()
        
        // Assert
        assertEquals("Progress should be set correctly", 0.35f, progressValue, 0.001f)
        assertTrue("Card should have progress indicator when progress is set", hasProgressIndicator)
    }
}