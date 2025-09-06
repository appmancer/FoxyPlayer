package com.foxy.player.music

import com.foxy.player.music.ui.ContentSectionComponent
import com.foxy.player.music.ui.LayoutType
import com.foxy.player.music.ui.RecentlyPlayedSection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * PLY-141: Test suite for ContentSectionComponent modular UI structure
 * Tests ContentSectionComponent with multiple layout types (list, grid, carousel)
 */
class ContentSectionComponentTest {

    @Test
    fun `ContentSectionComponent should support list layout type`() {
        // Arrange
        val recentItems = listOf(
            RecentItem(
                id = "recent_001",
                title = "Bohemian Rhapsody",
                artist = "Queen",
                lastPlayed = System.currentTimeMillis(),
                artwork = "https://example.com/queen.jpg"
            ),
            RecentItem(
                id = "recent_002",
                title = "Hotel California",
                artist = "Eagles",
                lastPlayed = System.currentTimeMillis() - 1000,
                artwork = "https://example.com/eagles.jpg"
            )
        )
        val section = ContentSection(title = "Recently Played", items = recentItems)

        // Act
        val component = ContentSectionComponent(
            section = section,
            layoutType = LayoutType.LIST
        )

        // Assert
        assertEquals(LayoutType.LIST, component.layoutType)
        assertEquals(section, component.section)
        assertTrue(component.canRenderContent())
    }

    @Test
    fun `ContentSectionComponent should support grid layout type`() {
        // Arrange
        val recommendationItems = listOf(
            RecommendationItem(
                id = "rec_001",
                title = "Stairway to Heaven",
                artist = "Led Zeppelin",
                reason = "Based on your rock preferences",
                confidence = 0.85,
                artwork = "https://example.com/led-zeppelin.jpg"
            )
        )
        val section = ContentSection(title = "Recommended for You", items = recommendationItems)

        // Act
        val component = ContentSectionComponent(
            section = section,
            layoutType = LayoutType.GRID
        )

        // Assert
        assertEquals(LayoutType.GRID, component.layoutType)
        assertEquals(section, component.section)
        assertTrue(component.canRenderContent())
    }

    @Test
    fun `ContentSectionComponent should support carousel layout type`() {
        // Arrange
        val recentItems = listOf(
            RecentItem(
                id = "recent_001",
                title = "Bohemian Rhapsody",
                artist = "Queen",
                lastPlayed = System.currentTimeMillis(),
                artwork = "https://example.com/queen.jpg"
            )
        )
        val section = ContentSection(title = "Recently Played", items = recentItems)

        // Act
        val component = ContentSectionComponent(
            section = section,
            layoutType = LayoutType.CAROUSEL
        )

        // Assert
        assertEquals(LayoutType.CAROUSEL, component.layoutType)
        assertEquals(section, component.section)
        assertTrue(component.canRenderContent())
    }

    @Test
    fun `ContentSectionComponent should handle empty content gracefully`() {
        // Arrange
        val emptySection = ContentSection<RecentItem>(
            title = "Empty Section",
            items = emptyList()
        )

        // Act
        val component = ContentSectionComponent(
            section = emptySection,
            layoutType = LayoutType.LIST
        )

        // Assert
        assertEquals(LayoutType.LIST, component.layoutType)
        assertEquals(emptySection, component.section)
        assertTrue(!component.canRenderContent()) // Should return false for empty content
    }

    @Test
    fun `RecentlyPlayedSection should create component with recent items and horizontal layout`() {
        // Arrange
        val recentItems = listOf(
            RecentItem(
                id = "recent_001",
                title = "Bohemian Rhapsody",
                artist = "Queen",
                lastPlayed = System.currentTimeMillis(),
                artwork = "https://example.com/queen.jpg"
            ),
            RecentItem(
                id = "recent_002",
                title = "Hotel California",
                artist = "Eagles",
                lastPlayed = System.currentTimeMillis() - 1000,
                artwork = "https://example.com/eagles.jpg"
            )
        )

        // Act
        val recentlyPlayedSection = RecentlyPlayedSection(recentItems)

        // Assert
        assertEquals("Recently Played", recentlyPlayedSection.getTitle())
        assertEquals(LayoutType.CAROUSEL, recentlyPlayedSection.getLayoutType()) // Horizontal layout
        assertEquals(recentItems, recentlyPlayedSection.getRecentItems())
        assertTrue(recentlyPlayedSection.hasContent())
    }

    @Test
    fun `RecentlyPlayedSection should handle empty items gracefully`() {
        // Arrange
        val emptyItems = emptyList<RecentItem>()

        // Act
        val recentlyPlayedSection = RecentlyPlayedSection(emptyItems)

        // Assert
        assertEquals("Recently Played", recentlyPlayedSection.getTitle())
        assertEquals(LayoutType.CAROUSEL, recentlyPlayedSection.getLayoutType())
        assertEquals(emptyItems, recentlyPlayedSection.getRecentItems())
        assertTrue(!recentlyPlayedSection.hasContent()) // Should return false for empty content
    }

    @Test
    fun `Integration test - RecentlyPlayedSection should create valid ContentSectionComponent`() {
        // Arrange
        val recentItems = listOf(
            RecentItem(
                id = "recent_001",
                title = "Bohemian Rhapsody",
                artist = "Queen",
                lastPlayed = System.currentTimeMillis(),
                artwork = "https://example.com/queen.jpg"
            ),
            RecentItem(
                id = "recent_002",
                title = "Hotel California",
                artist = "Eagles",
                lastPlayed = System.currentTimeMillis() - 1000,
                artwork = "https://example.com/eagles.jpg"
            )
        )

        // Act
        val recentlyPlayedSection = RecentlyPlayedSection(recentItems)
        val component = recentlyPlayedSection.asContentSectionComponent()

        // Assert - Integration validation
        assertEquals("Recently Played", component.section.title)
        assertEquals(LayoutType.CAROUSEL, component.layoutType)
        assertEquals(recentItems, component.section.items)
        assertTrue(component.canRenderContent())

        // Verify that the component maintains consistency with the section
        assertEquals(recentlyPlayedSection.getTitle(), component.section.title)
        assertEquals(recentlyPlayedSection.getLayoutType(), component.layoutType)
        assertEquals(recentlyPlayedSection.getRecentItems(), component.section.items)
        assertEquals(recentlyPlayedSection.hasContent(), component.canRenderContent())
    }

    @Test
    fun `Integration test - Multiple content sections with different layout types`() {
        // Arrange
        val recentItems = listOf(
            RecentItem(
                id = "recent_001",
                title = "Bohemian Rhapsody",
                artist = "Queen",
                lastPlayed = System.currentTimeMillis(),
                artwork = "https://example.com/queen.jpg"
            )
        )

        val recommendationItems = listOf(
            RecommendationItem(
                id = "rec_001",
                title = "Stairway to Heaven",
                artist = "Led Zeppelin",
                reason = "Based on your rock preferences",
                confidence = 0.85,
                artwork = "https://example.com/led-zeppelin.jpg"
            )
        )

        // Act - Create different components with different layout types
        val recentSection = ContentSection(title = "Recently Played", items = recentItems)
        val recentComponent = ContentSectionComponent(
            section = recentSection,
            layoutType = LayoutType.CAROUSEL
        )

        val recommendationSection = ContentSection(
            title = "Recommended for You",
            items = recommendationItems
        )
        val recommendationComponent = ContentSectionComponent(
            section = recommendationSection,
            layoutType = LayoutType.GRID
        )

        // Assert - All components should be functional
        assertTrue(recentComponent.canRenderContent())
        assertEquals(LayoutType.CAROUSEL, recentComponent.layoutType)
        assertEquals("Recently Played", recentComponent.section.title)

        assertTrue(recommendationComponent.canRenderContent())
        assertEquals(LayoutType.GRID, recommendationComponent.layoutType)
        assertEquals("Recommended for You", recommendationComponent.section.title)

        // Verify items are preserved
        assertEquals(recentItems, recentComponent.section.items)
        assertEquals(recommendationItems, recommendationComponent.section.items)
    }
}
