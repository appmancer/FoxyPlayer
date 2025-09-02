package com.foxy.player.music.search

import com.foxy.player.authentication.models.UserInfo
import com.foxy.player.authentication.network.AuthRepository
import com.foxy.player.authentication.network.AuthenticatedApiClient
import com.foxy.player.music.MusicSearchService
import com.foxy.player.music.ui.MusicTrack
import com.foxy.player.music.ui.SearchCriteria
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Search and filtering tests for Music Discovery
 * Extracted from MusicDiscoveryTest.kt as part of KLOC refactoring (PLY-137)
 * Tests music search, filtering, sorting, and browsing functionality
 */
class MusicDiscoverySearchTest {

    @Test
    fun `should filter music tracks by search query`() {
        // Arrange - setup test data with authenticated state and music search service
        val authRepository = AuthRepository("https://eapi.pcloud.com")
        authRepository.saveAuthenticationState("test_auth_token", UserInfo("test@example.com"))
        val authenticatedApiClient = AuthenticatedApiClient(authRepository)
        val musicSearchService = MusicSearchService(authenticatedApiClient)

        // Create test music tracks
        val testTracks = listOf(
            MusicTrack("1", "Bohemian Rhapsody", "Queen", "A Night at the Opera", "rock"),
            MusicTrack("2", "Stairway to Heaven", "Led Zeppelin", "Led Zeppelin IV", "rock"),
            MusicTrack("3", "Hotel California", "Eagles", "Hotel California", "rock"),
            MusicTrack("4", "Imagine", "John Lennon", "Imagine", "pop"),
            MusicTrack("5", "Like a Rolling Stone", "Bob Dylan", "Highway 61 Revisited", "folk")
        )

        // Act - call the search method
        val result = musicSearchService.searchTracks("Queen", testTracks)

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

    @Test
    fun `should filter music tracks with advanced search criteria`() {
        // Arrange
        val authRepository = AuthRepository("https://eapi.pcloud.com")
        authRepository.saveAuthenticationState("test_auth_token", UserInfo("test@example.com"))
        val authenticatedApiClient = AuthenticatedApiClient(authRepository)
        val musicSearchService = MusicSearchService(authenticatedApiClient)

        val testTracks = listOf(
            MusicTrack("1", "Bohemian Rhapsody", "Queen", "A Night at the Opera", "rock"),
            MusicTrack("2", "Stairway to Heaven", "Led Zeppelin", "Led Zeppelin IV", "rock"),
            MusicTrack("3", "Hotel California", "Eagles", "Hotel California", "rock"),
            MusicTrack("4", "Imagine", "John Lennon", "Imagine", "pop"),
            MusicTrack("5", "Like a Rolling Stone", "Bob Dylan", "Highway 61 Revisited", "folk")
        )

        // Test searching by genre with text query in title
        val searchCriteria = SearchCriteria(
            query = "Bohemian",
            searchInTitle = true,
            searchInArtist = false,
            searchInAlbum = false,
            genre = "rock"
        )

        // Act
        val result = musicSearchService.searchTracksWithCriteria(searchCriteria, testTracks)

        // Assert
        assertTrue("Should return successful result", result.isSuccess)
        val searchResponse = result.getOrNull()
        assertNotNull("Search response should not be null", searchResponse)
        assertEquals(
            "Should return exactly 1 rock track matching 'Bohemian'",
            1,
            searchResponse!!.tracks.size
        )
        assertTrue(
            "All returned tracks should be rock genre",
            searchResponse.tracks.all { it.genre == "rock" }
        )
        assertTrue(
            "Returned track should contain 'Bohemian' in title",
            searchResponse.tracks.any { it.title.contains("Bohemian") }
        )
    }

    @Test
    fun `should browse music tracks by artist`() {
        // Arrange
        val authRepository = AuthRepository("https://eapi.pcloud.com")
        authRepository.saveAuthenticationState("test_auth_token", UserInfo("test@example.com"))
        val authenticatedApiClient = AuthenticatedApiClient(authRepository)
        val musicSearchService = MusicSearchService(authenticatedApiClient)

        val testTracks = listOf(
            MusicTrack("1", "Bohemian Rhapsody", "Queen", "A Night at the Opera", "rock"),
            MusicTrack("2", "Another One Bites the Dust", "Queen", "The Game", "rock"),
            MusicTrack("3", "Stairway to Heaven", "Led Zeppelin", "Led Zeppelin IV", "rock"),
            MusicTrack("4", "Black Dog", "Led Zeppelin", "Led Zeppelin IV", "rock")
        )

        // Act
        val result = musicSearchService.browseByArtist(testTracks)

        // Assert
        assertTrue("Should return successful result", result.isSuccess)
        val browseResponse = result.getOrNull()
        assertNotNull("Browse response should not be null", browseResponse)
        assertEquals(
            "Should return exactly 2 artist groups",
            2,
            browseResponse!!.artistGroups.size
        )

        val queenGroup = browseResponse.artistGroups.find { it.artist == "Queen" }
        assertNotNull("Should contain Queen artist group", queenGroup)
        assertEquals(
            "Queen group should contain 2 tracks",
            2,
            queenGroup!!.tracks.size
        )

        val ledZepGroup = browseResponse.artistGroups.find { it.artist == "Led Zeppelin" }
        assertNotNull("Should contain Led Zeppelin artist group", ledZepGroup)
        assertEquals(
            "Led Zeppelin group should contain 2 tracks",
            2,
            ledZepGroup!!.tracks.size
        )
    }
}
