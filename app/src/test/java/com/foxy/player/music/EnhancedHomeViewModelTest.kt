package com.foxy.player.music

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
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
    fun `EnhancedHomeViewModel should provide backward compatibility with hubState property`() = runTest(testDispatcher) {
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
            "hubState should be a StateFlow", hubStateType.name.contains("StateFlow") || hubStateType.interfaces.any { it.name.contains("StateFlow") }
        )
    }
}
