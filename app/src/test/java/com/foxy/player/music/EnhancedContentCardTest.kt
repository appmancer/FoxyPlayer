package com.foxy.player.music

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for EnhancedContentCard components
 * * PLY-144: Enhanced Content Cards
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
        cardComponent.setProgressIndicatorVisibility(true) // Explicitly show indicator
        val progressValue = cardComponent.getPlaybackProgress()
        val hasProgressIndicator = cardComponent.hasProgressIndicator()

        // Assert
        assertEquals("Progress should be set correctly", 0.35f, progressValue, 0.001f)
        assertTrue("Card should have progress indicator when explicitly enabled", hasProgressIndicator)
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

    @Test
    fun `Progress indicator visibility should be independent of progress value`() = runTest {
        // Arrange
        val testItem = ContentCardItem(
            id = "test-card-4",
            title = "Progress Test Song",
            subtitle = "Test Artist",
            artworkUrl = "https://example.com/test-art.jpg"
        )

        // Act & Assert
        val cardComponent = EnhancedContentCard.create(testItem)

        // Initially no progress indicator should be shown
        assertFalse("Progress indicator should be hidden by default", cardComponent.hasProgressIndicator())

        // Setting progress without enabling indicator should not show it
        cardComponent.setPlaybackProgress(0.5f)
        assertFalse(
            "Progress indicator should remain hidden even with progress set",
            cardComponent.hasProgressIndicator()
        )

        // Explicitly showing indicator should work regardless of progress
        cardComponent.setProgressIndicatorVisibility(true)
        assertTrue("Progress indicator should be visible when explicitly enabled", cardComponent.hasProgressIndicator())

        // Progress can be reset but indicator can still be visible
        cardComponent.setPlaybackProgress(0.0f)
        assertTrue(
            "Progress indicator should remain visible even with zero progress",
            cardComponent.hasProgressIndicator()
        )

        // Hiding indicator should work
        cardComponent.setProgressIndicatorVisibility(false)
        assertFalse(
            "Progress indicator should be hidden when explicitly disabled",
            cardComponent.hasProgressIndicator()
        )
    }
}
