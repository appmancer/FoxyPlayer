package com.foxy.player.music

import java.util.Date
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * TDD test for offline music library support.
 * Tests the ability to access cached music tracks when network is unavailable.
 */
class OfflineMusicLibraryTest {

    private lateinit var offlineMusicLibrary: OfflineMusicLibrary
    private lateinit var testNetworkConnectivity: DefaultNetworkConnectivity

    @Before
    fun setup() {
        testNetworkConnectivity = DefaultNetworkConnectivity()
        offlineMusicLibrary = OfflineMusicLibrary(testNetworkConnectivity)
    }

    /**
     * PLY-79: Test that cached tracks are returned when network is unavailable.
     * This test expects REAL offline functionality to exist.
     */
    @Test
    fun `should return cached tracks when network is unavailable`() = runBlocking {
        // Arrange - Set up offline scenario with cached tracks
        val cachedTracks = listOf(
            MusicTrackWithMetadata(
                id = "track1",
                title = "Offline Song 1",
                artist = "Cached Artist",
                album = "Offline Album",
                genre = "Rock",
                durationMs = 240000,
                fileSizeBytes = 3500000,
                dateAdded = Date()
            ),
            MusicTrackWithMetadata(
                id = "track2",
                title = "Offline Song 2",
                artist = "Cached Artist",
                album = "Offline Album",
                genre = "Pop",
                durationMs = 180000,
                fileSizeBytes = 2800000,
                dateAdded = Date()
            )
        )

        // Pre-populate cache with tracks (simulate previous online session)
        val populateResult = offlineMusicLibrary.populateCache(cachedTracks)
        assertTrue("Cache population should succeed", populateResult is OfflineResult.Success)

        // Simulate network unavailable
        testNetworkConnectivity.setNetworkAvailable(false)

        // Act - Try to get tracks while offline
        val result = offlineMusicLibrary.getTracks()

        // Assert - Should return cached tracks even when offline
        assertTrue("Should return cached tracks when offline", result is OfflineResult.Success)
        val successResult = result as OfflineResult.Success
        val tracks = successResult.data
        assertNotNull("Tracks should not be null", tracks)
        assertEquals("Should return all cached tracks", 2, tracks.size)
        assertEquals("First track should match cached data", "Offline Song 1", tracks[0].title)
        assertEquals("Second track should match cached data", "Offline Song 2", tracks[1].title)
        assertTrue("Should indicate result from cache", successResult.fromCache)
        assertTrue("Should indicate offline mode", offlineMusicLibrary.isOfflineMode())
    }
}
