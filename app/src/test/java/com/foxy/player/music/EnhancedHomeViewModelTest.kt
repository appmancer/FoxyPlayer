package com.foxy.player.music

import com.foxy.player.authentication.network.AuthRepository
import com.foxy.player.authentication.network.AuthenticatedApiClient
import com.foxy.player.music.network.MusicDiscoveryService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Test for EnhancedHomeViewModel with reactive StateFlow state management
 * PLY-140: EnhancedHomeViewModel Implementation
 *
 * Tests cover reactive state management, backward compatibility with MusicHubViewModel,
 * content loading from HeuristicMusicDiscovery, and error handling.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class EnhancedHomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Test
    fun `EnhancedHomeViewModel should initialize with loading state`() = runTest(testDispatcher) {
        // Arrange - prepare to create EnhancedHomeViewModel
        // This test expects the ViewModel to exist and have initial loading state

        // Act - create the EnhancedHomeViewModel
        val viewModel = EnhancedHomeViewModel()

        // Assert - verify it initializes with loading state
        val initialState = viewModel.enhancedState.first()
        assertTrue("ViewModel should initialize with loading state", initialState.isLoading)
        assertEquals("Initial content should be empty", emptyList<Any>(), initialState.content)
        assertNull("Error should be null initially", initialState.error)
    }

    @Test
    fun `EnhancedHomeViewModel should provide backward compatibility with hubState property`() =
        runTest(testDispatcher) {
            // Arrange - create EnhancedHomeViewModel
            // This test expects REAL backward compatibility with legacy MusicHubViewModel

            // Act - create the ViewModel and access the hubState property
            val viewModel = EnhancedHomeViewModel()

            // Assert - verify hubState property exists and works for legacy compatibility
            val hubState = viewModel.hubState.first()
            assertNotNull("hubState should exist for backward compatibility", hubState)
            assertTrue("hubState should reflect loading state from enhancedState", hubState.isLoading)

            // Verify the hubState property is actually a StateFlow (real reactive property)
            val hubStateType = viewModel.hubState::class.java
            assertTrue(
                "hubState should be a StateFlow",
                hubStateType.name.contains("StateFlow") || hubStateType.interfaces.any { it.name.contains("StateFlow") }
            )
        }

    @Test
    fun `EnhancedHomeViewModel should load real content from HeuristicMusicDiscovery`() = runTest(testDispatcher) {
        // Arrange - create real HeuristicMusicDiscovery with real MusicDiscoveryService
        // This test expects REAL content loading functionality, not mocks
        val authRepository = AuthRepository()
        val authenticatedApiClient = AuthenticatedApiClient(authRepository)
        val musicService = MusicDiscoveryService(authenticatedApiClient)
        val heuristicDiscovery = HeuristicMusicDiscovery(musicService)
        val viewModel = EnhancedHomeViewModel(heuristicDiscovery, ioDispatcher = testDispatcher)

        // Act - call loadContent() method to load real content
        viewModel.loadContent()
        advanceUntilIdle() // Allow coroutines to complete

        // Assert - verify real content was loaded
        val finalState = viewModel.enhancedState.first()
        assertFalse("Loading should be complete after loadContent()", finalState.isLoading)
        assertNotNull("Content should be loaded from real discovery service", finalState.content)
        assertTrue("Content should contain real sections with actual data", finalState.content.isNotEmpty())
        assertNull("Error should be null when loading succeeds", finalState.error)

        // Verify we got real content sections (not empty placeholders)
        assertTrue(
            "Should have loaded actual content sections",
            finalState.content.any { section ->
                section.title.contains("Recently Added") || section.toString().contains("ContentSection")
            }
        )
    }

    @Test
    fun `EnhancedHomeViewModel should integrate with recommendation engine for personalized content`() = runTest(testDispatcher) {
        // PLY-146: Test recommendation engine integration
        // Arrange - create ViewModel with real recommendation engine components
        val authRepository = AuthRepository()
        val authenticatedApiClient = AuthenticatedApiClient(authRepository)
        val musicService = MusicDiscoveryService(authenticatedApiClient)
        val albumDiscoveryService = AlbumDiscoveryService(musicService)
        val userHistoryService = UserHistoryService()
        val contentOrganizer = ContentSectionOrganizer(userHistoryService, albumDiscoveryService)
        
        // Create enhanced ViewModel with recommendation engine
        val viewModel = EnhancedHomeViewModel(
            heuristicDiscovery = null, // Use new recommendation engine instead
            contentOrganizer = contentOrganizer,
            ioDispatcher = testDispatcher
        )

        // Act - load personalized content
        viewModel.loadPersonalizedContent()
        advanceUntilIdle()

        // Assert - verify personalized recommendations are loaded
        val finalState = viewModel.enhancedState.first()
        assertFalse("Loading should complete", finalState.isLoading)
        assertNotNull("Personalized content should be loaded", finalState.content)
        
        // Verify we have recommendation sections
        val hasRecommendations = finalState.content.any { section ->
            section.title.contains("Recommended") || section.title.contains("Recently Played")
        }
        assertTrue("Should have personalized recommendation sections", hasRecommendations)
        assertNull("Should not have errors", finalState.error)
    }

    @Test
    fun `EnhancedHomeViewModel should support content refresh functionality`() = runTest(testDispatcher) {
        // PLY-146: Test content refresh and background updates
        // Arrange
        val authRepository = AuthRepository()
        val authenticatedApiClient = AuthenticatedApiClient(authRepository)
        val musicService = MusicDiscoveryService(authenticatedApiClient)
        val albumDiscoveryService = AlbumDiscoveryService(musicService)
        val userHistoryService = UserHistoryService()
        val contentOrganizer = ContentSectionOrganizer(userHistoryService, albumDiscoveryService)
        
        val viewModel = EnhancedHomeViewModel(
            heuristicDiscovery = null,
            contentOrganizer = contentOrganizer,
            ioDispatcher = testDispatcher
        )

        // Act - initial load then refresh
        viewModel.loadPersonalizedContent()
        advanceUntilIdle()
        
        val initialState = viewModel.enhancedState.first()
        
        // Refresh content
        viewModel.refreshContent()
        advanceUntilIdle()

        // Assert - verify refresh works
        val refreshedState = viewModel.enhancedState.first()
        assertFalse("Refresh should complete loading", refreshedState.isLoading)
        assertNotNull("Refreshed content should exist", refreshedState.content)
        assertNull("Refresh should not have errors", refreshedState.error)
    }
}
