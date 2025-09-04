package com.foxy.player.music

import org.junit.Assert.*
import org.junit.Test

/**
 * Tests for Enhanced Album Discovery Service functionality.
 * PLY-125: Tests album-specific discovery methods that group songs by album metadata.
 */
class AlbumDiscoveryServiceTest {

    @Test
    fun `AlbumDiscoveryResult data class should work correctly`() {
        // Arrange
        val albumResult = AlbumDiscoveryResult(
            albumName = "Test Album",
            artistName = "Test Artist", trackCount = 10,
            discoveredTracks = listOf("track1.mp3", "track2.mp3")
        )

        // Assert - basic data class functionality
        assertEquals("Test Album", albumResult.albumName)
        assertEquals("Test Artist", albumResult.artistName)
        assertEquals(10, albumResult.trackCount)
        assertEquals(2, albumResult.discoveredTracks.size)
    }

    @Test
    fun `AlbumResult success should work correctly`() {
        // Arrange
        val albums = listOf(
            AlbumDiscoveryResult("Album 1", "Artist 1", 5, emptyList()),
            AlbumDiscoveryResult("Album 2", "Artist 2", 8, emptyList())
        )
        val result = AlbumResult.Success(albums)

        // Assert
        assertTrue("Should be success", result.isSuccess())
        assertFalse("Should not be error", result.isError())
        assertEquals(2, result.getOrNull()?.size)
    }

    @Test
    fun `AlbumResult error should work correctly`() {
        // Arrange
        val exception = RuntimeException("Test error")
        val result = AlbumResult.Error<List<AlbumDiscoveryResult>>(exception, "Test error message")

        // Assert
        assertFalse("Should not be success", result.isSuccess())
        assertTrue("Should be error", result.isError())
        assertNull("Should return null on error", result.getOrNull())
    }
}
