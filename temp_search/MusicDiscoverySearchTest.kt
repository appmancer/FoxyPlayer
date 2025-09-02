package com.foxy.player.music.search

import com.foxy.player.authentication.models.UserInfo
import com.foxy.player.authentication.network.AuthRepository
import com.foxy.player.authentication.network.AuthenticatedApiClient
import com.foxy.player.music.network.MusicDiscoveryService
import com.foxy.player.music.ui.MusicTrack
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Search and filtering tests for Music Discovery
 * Extracted from MusicDiscoveryTest.kt as part of KLOC refactoring (PLY-137)
 * * Tests music search, filtering, sorting, and browsing functionality
 */
class MusicDiscoverySearchTest {

    @Test
    fun `should filter music tracks by search query`() {
        // Arrange - setup test data with authenticated state and music search service
        val authRepository = AuthRepository("https://eapi.pcloud.com")
        authRepository.saveAuthenticationState("test_auth_token", UserInfo("test@example.com"))
        val authenticatedApiClient = AuthenticatedApiClient(authRepository)
        val musicDiscoveryService = MusicDiscoveryService(authenticatedApiClient)

        // Create test music tracks
        val testTracks = listOf(
            MusicTrack("1", "Bohemian Rhapsody", "Queen", "A Night at the Opera", "rock"),
            MusicTrack("2", "Stairway to Heaven", "Led Zeppelin", "Led Zeppelin IV", "rock"),
            MusicTrack("3", "Hotel California", "Eagles", "Hotel California", "rock"),
            MusicTrack("4", "Imagine", "John Lennon", "Imagine", "pop"),
            MusicTrack("5", "Like a Rolling Stone", "Bob Dylan", "Highway 61 Revisited", "folk")
        )

        // Act - call the search method
        val result = musicDiscoveryService.searchTracks("Queen", testTracks)

        // Assert - verify search functionality
        assertTrue("Should return successful result with filtered tracks", result.isSuccess)
        val searchResponse = result.getOrNull()
        assertNotNull("Search response should not be null", searchResponse)
        assertTrue(
            "Should contain tracks matching 'Queen'",
            searchResponse!!.tracks.any { it.artist == "Queen" }
        )
        assertEquals(
            "Should return exactly 1 track for 'Queen' search",
            1,
            searchResponse.tracks.size
        )
        assertEquals(
            "Should return the correct Queen track",
            "Bohemian Rhapsody",
            searchResponse.tracks[0].title
        )
    }
}
