package com.foxy.player.music

import com.foxy.player.authentication.AuthRepository
import com.foxy.player.authentication.AuthenticatedApiClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MusicSearchTest {

    @Test
    fun `should filter tracks by artist name when searching with real track data`() {
        // Arrange - real track data
        val authRepository = AuthRepository("https://eapi.pcloud.com")
        val authenticatedApiClient = AuthenticatedApiClient(authRepository)
        val musicSearchService = MusicSearchService(authenticatedApiClient)

        val realTracks = listOf(
            MusicTrack("1", "Song One", "The Beatles", "Abbey Road", "Rock"),
            MusicTrack("2", "Song Two", "Queen", "Bohemian Rhapsody", "Rock"),
            MusicTrack("3", "Song Three", "The Beatles", "Let It Be", "Rock"),
            MusicTrack("4", "Song Four", "Led Zeppelin", "IV", "Rock")
        )

        // Act - search for tracks by "Beatles"
        val result = musicSearchService.searchTracks("Beatles", realTracks)

        // Assert - should find real Beatles tracks
        assertTrue("Search should be successful", result.isSuccess)
        val searchResponse = result.getOrNull()!!
        assertEquals("Should find 2 Beatles tracks", 2, searchResponse.tracks.size)
        assertTrue(
            "All results should contain Beatles",
            searchResponse.tracks.all { it.artist.contains("Beatles") }
        )
    }
}
