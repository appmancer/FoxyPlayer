package com.foxy.player.music

import org.junit.Test
import org.junit.Assert.*

/**
 * PLY-145: Migrate Existing Home Screen UI  
 * Tests for using EnhancedContentCard directly in MusicLibraryHubScreen
 */
class HomeScreenMigrationTest {

    @Test
    fun `HomeScreen should use EnhancedContentCard directly without compatibility layers`() {
        // Arrange - setup test data for simplified card usage
        val cardData = CardInfo(id = "songs", count = 42)
        val expectedTitle = "Songs"
        
        // Act - call the simplified hub screen that uses EnhancedContentCard directly
        val hubCards = MusicLibraryHubScreen.createEnhancedCards(mapOf(expectedTitle to cardData))
        
        // Assert - verify direct EnhancedContentCard usage
        assertNotNull("Hub cards should not be null", hubCards)
        assertEquals("Should have one card", 1, hubCards.size)
        assertTrue("Should contain Songs card", hubCards.containsKey(expectedTitle))
        
        val songsCard = hubCards[expectedTitle]
        assertNotNull("Songs card should exist", songsCard)
        assertEquals("Card should have correct title", expectedTitle, songsCard!!.title)
        assertEquals("Card should have correct subtitle", "42 items", songsCard.subtitle)
    }
}

