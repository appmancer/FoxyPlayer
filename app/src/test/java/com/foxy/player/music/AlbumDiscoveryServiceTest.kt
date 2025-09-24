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

    @Test
    fun `MusicDiscoveryService should have discoverAlbumsByPath method that groups tracks by album metadata`() = runBlocking {
        // Arrange - Create MusicDiscoveryService instance
        val authRepository = AuthRepository("")
        val apiClient = AuthenticatedApiClient(authRepository)
        val musicDiscoveryService = MusicDiscoveryService(apiClient)

        // Act - Call the new album discovery method that should exist
        val albumDiscoveryResult = musicDiscoveryService.discoverAlbumsByPath("/Music")

        // Assert - Verify the method exists and returns proper album-grouped results
        assertTrue("Should successfully discover albums by path", albumDiscoveryResult.isSuccess)
        val albumResults = albumDiscoveryResult.getOrNull()
        assertNotNull("Should return album discovery results", albumResults)
        assertNotNull("Should group tracks by album metadata", albumResults?.albumGroups)
    }

    @Test
    fun `discoverAlbumsByPath should scan pCloud directory and extract real album metadata from audio files`() = runBlocking {
        // Arrange - Create MusicDiscoveryService instance with real pCloud integration
        val authRepository = AuthRepository("https://eapi.pcloud.com")
        val apiClient = AuthenticatedApiClient(authRepository)
        val musicDiscoveryService = MusicDiscoveryService(apiClient)

        // Act - Call album discovery on a test path that should have audio files
        val albumDiscoveryResult = musicDiscoveryService.discoverAlbumsByPath("/TestMusic")

        // Assert - Verify that it actually scans pCloud and extracts real metadata
        assertTrue("Should successfully scan pCloud directory", albumDiscoveryResult.isSuccess)
        val albumResults = albumDiscoveryResult.getOrNull()
        assertNotNull("Should return album discovery results", albumResults)
        
        val albumGroups = albumResults?.albumGroups
        assertNotNull("Should discover album groups from audio files", albumGroups)
        assertTrue("Should find at least one album from scanned audio files", albumGroups!!.isNotEmpty())
        
        // Verify that each album group contains real metadata extracted from files
        val firstAlbum = albumGroups.first()
        assertTrue("Album name should not be empty", firstAlbum.albumName.isNotBlank())
        assertTrue("Artist name should not be empty", firstAlbum.artistName.isNotBlank())
        assertTrue("Should have at least one track", firstAlbum.trackCount > 0)
        assertTrue("Track list should not be empty", firstAlbum.tracks.isNotEmpty())
        
        // Verify track filenames are realistic (contain file extensions)
        val firstTrack = firstAlbum.tracks.first()
        assertTrue("Track should have audio file extension", 
            firstTrack.endsWith(".mp3") || firstTrack.endsWith(".flac") || firstTrack.endsWith(".wav"))
    }

    @Test
    fun `discoverAlbumsByPath should use real pCloud file discovery instead of mock data`() = runBlocking {
        // Arrange - Create MusicDiscoveryService instance with real pCloud integration
        val authRepository = AuthRepository("https://eapi.pcloud.com")
        val apiClient = AuthenticatedApiClient(authRepository)
        val musicDiscoveryService = MusicDiscoveryService(apiClient)

        // Act - Call album discovery on a test path
        val albumDiscoveryResult = musicDiscoveryService.discoverAlbumsByPath("/TestMusic")

        // Assert - Verify that it uses real pCloud file discovery, not hardcoded mock data
        assertTrue("Should successfully discover albums using real pCloud API", albumDiscoveryResult.isSuccess)
        val albumResults = albumDiscoveryResult.getOrNull()
        assertNotNull("Should return album discovery results", albumResults)
        
        val albumGroups = albumResults?.albumGroups
        assertNotNull("Should discover album groups from real pCloud files", albumGroups)
        
        // This test should fail because current implementation returns hardcoded mock data
        // We expect the real implementation to call pCloud API and extract real metadata
        // The test will verify that we're NOT getting the hardcoded "Test Album" / "Test Artist"
        if (albumGroups!!.isNotEmpty()) {
            val firstAlbum = albumGroups.first()
            assertFalse("Should not return hardcoded mock data - album name should not be 'Test Album'", 
                firstAlbum.albumName == "Test Album")
            assertFalse("Should not return hardcoded mock data - artist name should not be 'Test Artist'", 
                firstAlbum.artistName == "Test Artist")
            assertFalse("Should not return hardcoded mock tracks", 
                firstAlbum.tracks.contains("track1.mp3") && firstAlbum.tracks.contains("track2.mp3"))
        }
    }

    @Test
    fun `discoverAlbumsByPath should extract real metadata from audio files instead of using placeholder data`() = runBlocking {
        // Arrange - Create MusicDiscoveryService instance
        val authRepository = AuthRepository("https://eapi.pcloud.com")
        val apiClient = AuthenticatedApiClient(authRepository)
        val musicDiscoveryService = MusicDiscoveryService(apiClient)

        // Act - Call album discovery on a test path
        val albumDiscoveryResult = musicDiscoveryService.discoverAlbumsByPath("/TestMusic")

        // Assert - Verify that it extracts real metadata, not placeholder data
        assertTrue("Should successfully discover albums", albumDiscoveryResult.isSuccess)
        val albumResults = albumDiscoveryResult.getOrNull()
        assertNotNull("Should return album discovery results", albumResults)
        
        val albumGroups = albumResults?.albumGroups
        assertNotNull("Should have album groups", albumGroups)
        
        // This test should fail because current implementation uses placeholder "Discovered Album"/"Discovered Artist"
        // We expect the real implementation to extract actual metadata from audio files
        if (albumGroups!!.isNotEmpty()) {
            val firstAlbum = albumGroups.first()
            assertFalse("Should not use placeholder album name 'Discovered Album'", 
                firstAlbum.albumName == "Discovered Album")
            assertFalse("Should not use placeholder artist name 'Discovered Artist'", 
                firstAlbum.artistName == "Discovered Artist")
            assertTrue("Album name should be extracted from real metadata", 
                firstAlbum.albumName.isNotBlank() && firstAlbum.albumName != "Discovered Album")
            assertTrue("Artist name should be extracted from real metadata", 
                firstAlbum.artistName.isNotBlank() && firstAlbum.artistName != "Discovered Artist")
        }
    }
}
