package com.foxy.player.music

import org.junit.Test
import org.junit.Assert.*

/**
 * PLY-145: Migrate Existing Home Screen UI
 * Tests for migrating MusicLibraryHubRender to use EnhancedContentCard components
 */
class HomeScreenMigrationTest {

    @Test
    fun `should migrate HubCard to use EnhancedContentCard component`() {
        // Arrange - setup test data for card migration
        val cardData = CardInfo(id = "songs", count = 42)
        val expectedTitle = "Songs"
        val expectedSubtitle = "42 items"
        
        // Act - call the migration function that doesn't exist yet
        val migratedCard = MigratedHubCard.fromCardInfo(expectedTitle, cardData)
        
        // Assert - verify that it creates an EnhancedContentCard
        assertNotNull("Migrated card should not be null", migratedCard)
        assertEquals("Card should use correct title", expectedTitle, migratedCard.title)
        assertEquals("Card should use correct subtitle", expectedSubtitle, migratedCard.subtitle)
        assertTrue("Card should be backed by EnhancedContentCard", migratedCard.usesEnhancedCard())
    }
}

/**
 * Migration wrapper that will be implemented to bridge HubCard to EnhancedContentCard
 * PLY-145: Minimal implementation to pass TDD cycle #1
 */
data class MigratedHubCard(
    val title: String,
    val subtitle: String,
    val enhancedCard: EnhancedContentCard
) {
    companion object {
        fun fromCardInfo(title: String, cardInfo: CardInfo): MigratedHubCard {
            // Minimal implementation - create ContentCardItem and EnhancedContentCard
            val contentItem = ContentCardItem(
                id = cardInfo.id,
                title = title,
                subtitle = "${cardInfo.count} items",
                artworkUrl = "" // Empty URL for now - minimal implementation
            )
            val enhancedCard = EnhancedContentCard.create(contentItem)
            
            return MigratedHubCard(
                title = title,
                subtitle = "${cardInfo.count} items",
                enhancedCard = enhancedCard
            )
        }
    }
    
    fun usesEnhancedCard(): Boolean {
        // Minimal implementation - always true since we have an enhancedCard
        return true
    }
}