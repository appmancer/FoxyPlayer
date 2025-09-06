package com.foxy.player.music

import com.foxy.player.music.ui.ContentSectionComponent
import com.foxy.player.music.ui.LayoutType
import com.foxy.player.music.ui.RecentlyPlayedSection
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * PLY-142: Test suite for Dynamic Content Sections
 * Tests UserHistoryService, RecentlyPlayedGenerator, and RecommendationGenerator
 */
class DynamicContentSectionsTest {

    @Test
    fun `UserHistoryService should track and retrieve recently played items`() = runTest {
        // Arrange
        val userHistoryService = UserHistoryService()
        val testTracks = listOf(
            Song("/music/Queen/Bohemian Rhapsody.mp3", "Bohemian Rhapsody", "Queen", "A Night at the Opera"),
            Song("/music/Eagles/Hotel California.mp3", "Hotel California", "Eagles", "Hotel California"),
            Song("/music/Led Zeppelin/Stairway to Heaven.mp3", "Stairway to Heaven", "Led Zeppelin", "Led Zeppelin IV")
        )

        // Act - Simulate user playing tracks with timestamps
        val baseTime = System.currentTimeMillis()
        userHistoryService.recordPlayEvent(testTracks[0], baseTime - 3600000) // 1 hour ago
        userHistoryService.recordPlayEvent(testTracks[1], baseTime - 1800000) // 30 minutes ago
        userHistoryService.recordPlayEvent(testTracks[2], baseTime - 900000) // 15 minutes ago

        // Assert
        val recentItems = userHistoryService.getRecentlyPlayed(limit = 5)
        assertEquals(3, recentItems.size)

        // Should be ordered by most recent first
        assertEquals("Stairway to Heaven", recentItems[0].title)
        assertEquals("Hotel California", recentItems[1].title)
        assertEquals("Bohemian Rhapsody", recentItems[2].title)

        // Verify timestamps
        assertTrue(recentItems[0].lastPlayed > recentItems[1].lastPlayed)
        assertTrue(recentItems[1].lastPlayed > recentItems[2].lastPlayed)
    }

    @Test
    fun `RecentlyPlayedGenerator should create ContentSection from user history`() = runTest {
        // Arrange
        val userHistoryService = UserHistoryService()
        val recentlyPlayedGenerator = RecentlyPlayedGenerator(userHistoryService)

        // Add some test history
        val baseTime = System.currentTimeMillis()
        userHistoryService.recordPlayEvent(
            Song("/music/Queen/Bohemian Rhapsody.mp3", "Bohemian Rhapsody", "Queen", "A Night at the Opera"),
            baseTime - 1000
        )

        // Act
        val recentlyPlayedSection = recentlyPlayedGenerator.generateSection(limit = 10)

        // Assert
        assertTrue(recentlyPlayedSection.isSuccess)
        val section = recentlyPlayedSection.getOrNull()!!
        assertEquals("Recently Played", section.title)
        assertEquals(1, section.items.size)
        assertEquals("Bohemian Rhapsody", section.items[0].title)
        assertEquals("Queen", section.items[0].artist)
    }

    @Test
    fun `RecommendationGenerator should create recommendations from album discovery`() = runTest {
        // Arrange
        val mockAlbumDiscoveryService = createMockAlbumDiscoveryService()
        val recommendationGenerator = RecommendationGenerator(mockAlbumDiscoveryService)

        // Act
        val recommendationSection = recommendationGenerator.generateSection(limit = 5)

        // Assert
        assertTrue(recommendationSection.isSuccess)
        val section = recommendationSection.getOrNull()!!
        assertEquals("Recommended for You", section.title)
        assertTrue(section.items.isNotEmpty())

        // Verify recommendation items have confidence scores
        section.items.forEach { recommendation ->
            assertTrue(recommendation.confidence >= 0.0)
            assertTrue(recommendation.confidence <= 1.0)
            assertTrue(recommendation.reason.isNotBlank())
        }
    }

    @Test
    fun `ContentSectionOrganizer should order sections by priority and relevance`() = runTest {
        // Arrange
        val userHistoryService = UserHistoryService()
        val mockAlbumDiscoveryService = createMockAlbumDiscoveryService()
        val organizer = ContentSectionOrganizer(userHistoryService, mockAlbumDiscoveryService)

        // Add some history to make recently played relevant
        userHistoryService.recordPlayEvent(
            Song("/music/test.mp3", "Test Song", "Test Artist", "Test Album"),
            System.currentTimeMillis() - 1000
        )

        // Act
        val organizedSections = organizer.generateOrderedSections()

        // Assert
        assertTrue(organizedSections.isSuccess)
        val sections = organizedSections.getOrNull()!!
        assertTrue(sections.isNotEmpty())

        // Recently played should come first when user has history
        assertEquals("Recently Played", sections[0].title)

        // Should have multiple section types
        val sectionTitles = sections.map { it.title }
        assertTrue(sectionTitles.contains("Recently Played"))
        assertTrue(sectionTitles.contains("Recommended for You"))
    }

    @Test
    fun `Integration test - Dynamic sections should work with ContentSectionComponent from PLY-141`() = runTest {
        // Arrange
        val userHistoryService = UserHistoryService()
        val organizer = ContentSectionOrganizer(userHistoryService, createMockAlbumDiscoveryService())

        // Add test history
        userHistoryService.recordPlayEvent(
            Song("/music/Queen/Bohemian Rhapsody.mp3", "Bohemian Rhapsody", "Queen", "A Night at the Opera"),
            System.currentTimeMillis() - 1000
        )

        // Act - Generate dynamic sections and convert to UI components
        val sectionsResult = organizer.generateOrderedSections()
        assertTrue(sectionsResult.isSuccess)

        val sections = sectionsResult.getOrNull()!!
        val uiComponents = sections.map { section ->
            when {
                section.title == "Recently Played" -> {
                    @Suppress("UNCHECKED_CAST")
                    val recentSection = section as ContentSection<RecentItem>
                    ContentSectionComponent(section = recentSection, layoutType = LayoutType.CAROUSEL)
                }
                section.title == "Recommended for You" -> {
                    @Suppress("UNCHECKED_CAST")
                    val recommendationSection = section as ContentSection<RecommendationItem>
                    ContentSectionComponent(section = recommendationSection, layoutType = LayoutType.GRID)
                }
                else -> null
            }
        }.filterNotNull()

        // Assert - Verify integration with PLY-141 UI components
        assertEquals(2, uiComponents.size)

        // Verify Recently Played section uses carousel layout
        val recentComponent = uiComponents.find { it.section.title == "Recently Played" }!!
        assertEquals(LayoutType.CAROUSEL, recentComponent.layoutType)
        assertTrue(recentComponent.canRenderContent())
        assertEquals("Bohemian Rhapsody", (recentComponent.section.items[0] as RecentItem).title)

        // Verify Recommendations section uses grid layout
        val recommendationComponent = uiComponents.find { it.section.title == "Recommended for You" }!!
        assertEquals(LayoutType.GRID, recommendationComponent.layoutType)
        assertTrue(recommendationComponent.canRenderContent())
    }

    @Test
    fun `Integration test - RecentlyPlayedSection from PLY-141 should work with dynamic content`() = runTest {
        // Arrange
        val userHistoryService = UserHistoryService()
        val recentlyPlayedGenerator = RecentlyPlayedGenerator(userHistoryService)

        // Add test history
        val baseTime = System.currentTimeMillis()
        userHistoryService.recordPlayEvent(
            Song("/music/Queen/Bohemian Rhapsody.mp3", "Bohemian Rhapsody", "Queen", "A Night at the Opera"),
            baseTime - 2000
        )
        userHistoryService.recordPlayEvent(
            Song("/music/Eagles/Hotel California.mp3", "Hotel California", "Eagles", "Hotel California"),
            baseTime - 1000
        )

        // Act - Generate section and create PLY-141 RecentlyPlayedSection component
        val sectionResult = recentlyPlayedGenerator.generateSection(limit = 5)
        assertTrue(sectionResult.isSuccess)

        val dynamicRecentItems = sectionResult.getOrNull()!!.items
        val recentlyPlayedSection = RecentlyPlayedSection(dynamicRecentItems)

        // Assert - Verify integration between PLY-142 dynamic generation and PLY-141 UI component
        assertEquals("Recently Played", recentlyPlayedSection.getTitle())
        assertEquals(LayoutType.CAROUSEL, recentlyPlayedSection.getLayoutType())
        assertEquals(2, recentlyPlayedSection.getRecentItems().size)
        assertTrue(recentlyPlayedSection.hasContent())

        // Verify ordering (most recent first)
        val items = recentlyPlayedSection.getRecentItems()
        assertEquals("Hotel California", items[0].title)
        assertEquals("Bohemian Rhapsody", items[1].title)

        // Verify it converts properly to ContentSectionComponent
        val component = recentlyPlayedSection.asContentSectionComponent()
        assertEquals(LayoutType.CAROUSEL, component.layoutType)
        assertTrue(component.canRenderContent())
    }

    private fun createMockAlbumDiscoveryService(): AlbumDiscoveryService {
        // For now, use real components with mock auth - this will be refactored later
        val authRepository = com.foxy.player.authentication.network.AuthRepository()
        val apiClient = com.foxy.player.authentication.network.AuthenticatedApiClient(authRepository)
        val musicService = com.foxy.player.music.network.MusicDiscoveryService(apiClient)
        return AlbumDiscoveryService(musicService)
    }
}
