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

    @Test
    fun `Enhanced content card should support state animations for loading, playing, and paused`() = runTest {
        // Arrange
        val testItem = ContentCardItem(
            id = "test-card-3",
            title = "Animated Song",
            subtitle = "Animation Artist",
            artworkUrl = "https://example.com/animated-art.jpg"
        )
        
        // Act
        val cardComponent = EnhancedContentCard.create(testItem)
        
        // Test loading state
        cardComponent.setAnimationState(CardAnimationState.LOADING)
        assertEquals("Loading state should be set", CardAnimationState.LOADING, cardComponent.getAnimationState())
        assertTrue("Card should be animated when in loading state", cardComponent.isAnimated())
        
        // Test playing state
        cardComponent.setAnimationState(CardAnimationState.PLAYING)
        assertEquals("Playing state should be set", CardAnimationState.PLAYING, cardComponent.getAnimationState())
        assertTrue("Card should be animated when playing", cardComponent.isAnimated())
        
        // Test paused state
        cardComponent.setAnimationState(CardAnimationState.PAUSED)
        assertEquals("Paused state should be set", CardAnimationState.PAUSED, cardComponent.getAnimationState())
        assertTrue("Card should be animated when paused", cardComponent.isAnimated())
        
        // Test idle state (no animation)
        cardComponent.setAnimationState(CardAnimationState.IDLE)
        assertEquals("Idle state should be set", CardAnimationState.IDLE, cardComponent.getAnimationState())
    }
}