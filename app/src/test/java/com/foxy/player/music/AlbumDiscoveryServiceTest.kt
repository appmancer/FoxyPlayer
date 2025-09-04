package com.foxy.player.music

import com.foxy.player.authentication.network.AuthRepository
import com.foxy.player.authentication.network.AuthenticatedApiClient
import com.foxy.player.music.business.MusicMetadataExtractor
import com.foxy.player.music.entities.AudioMetadata
import com.foxy.player.music.network.MusicDiscoveryService
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
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
            artistName = "Test Artist",
            trackCount = 10,
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

    @Test
    fun `should extract real metadata from audio files using AlbumDiscoveryService`() = runBlocking {
        // Arrange - Create metadata extractor for real metadata functionality
        val metadataExtractor = MusicMetadataExtractor()

        // Create test audio file URLs that would be processed by AlbumDiscoveryService
        val testAudioFiles = listOf(
            "https://sample.com/Artists/Pink Floyd/The Wall/01 - Another Brick in the Wall.mp3",
            "https://sample.com/Artists/Beatles/Abbey Road/01 - Come Together.mp3"
        )

        // Act - Extract real metadata from the files
        val extractedMetadata = mutableListOf<AudioMetadata>()

        for (audioFileUrl in testAudioFiles) {
            val fileName = audioFileUrl.substringAfterLast('/')
            val metadataResult = metadataExtractor.extractMetadata(audioFileUrl, fileName)

            if (metadataResult.isSuccess) {
                metadataResult.getOrNull()?.let { metadata ->
                    extractedMetadata.add(metadata)
                }
            }
        }

        // Assert - Verify real metadata extraction functionality
        assertTrue("Should successfully extract metadata from test files", extractedMetadata.isNotEmpty())

        // Verify that metadata contains meaningful album information for grouping
        val firstMetadata = extractedMetadata.first()
        assertNotNull("Should have title", firstMetadata.title)
        assertNotNull("Should have artist", firstMetadata.artist)
        assertNotNull("Should have album", firstMetadata.album)
        assertTrue("Should have meaningful title", firstMetadata.title.isNotBlank())
        assertTrue("Should have meaningful artist", firstMetadata.artist.isNotBlank())
        assertTrue("Should have meaningful album", firstMetadata.album.isNotBlank())

        // Test that AlbumDiscoveryService would be able to group by this metadata
        // Create valid instances for required dependencies
        val authRepository = AuthRepository("")
        val apiClient = AuthenticatedApiClient(authRepository)
        val musicDiscoveryService = MusicDiscoveryService(apiClient)

        val albumService = AlbumDiscoveryService(musicDiscoveryService)

        // This method now exists and should work with real metadata
        val albumDiscoveryResult = albumService.discoverAlbumsWithRealMetadata(testAudioFiles)
        assertTrue("Should discover albums using real metadata", albumDiscoveryResult.isSuccess())

        val discoveredAlbums = albumDiscoveryResult.getOrNull()
        assertNotNull("Should return discovered albums", discoveredAlbums)
        assertTrue("Should discover at least one album", discoveredAlbums!!.isNotEmpty())
    }
}
