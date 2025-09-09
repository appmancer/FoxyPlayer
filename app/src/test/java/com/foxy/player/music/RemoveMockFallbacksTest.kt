package com.foxy.player.music

import com.foxy.player.authentication.network.AuthenticatedApiClient
import com.foxy.player.music.network.MusicDiscoveryService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Test for PLY-115: Remove Mock Fallbacks from MusicDiscovery
 * 
 * Verifies that MusicDiscoveryService no longer returns hardcoded mock responses
 * and implements proper error handling instead.
 */
class RemoveMockFallbacksTest {

    private lateinit var authenticatedApiClient: AuthenticatedApiClient
    private lateinit var musicDiscoveryService: MusicDiscoveryService

    @Before
    fun setup() {
        // Create a real API client instance for testing
        val authRepository = com.foxy.player.authentication.network.AuthRepository("https://eapi.pcloud.com")
        authenticatedApiClient = AuthenticatedApiClient(authRepository)
        musicDiscoveryService = MusicDiscoveryService(authenticatedApiClient)
    }

    @Test
    fun `listPCloudFolders should not return hardcoded Music Audio Downloads when no authentication`() {
        // Act - Call without proper authentication which should result in empty/proper error handling
        val result = musicDiscoveryService.listPCloudFolders("/")

        // Assert - Should NOT return the hardcoded ["Music", "Audio", "Downloads"] 
        // that existed in the mock fallback implementation
        if (result.isSuccess) {
            val folderListing = result.getOrNull()!!
            assertFalse("Should not return hardcoded 'Music' folder from mock fallback", 
                folderListing.folders.contains("Music"))
            assertFalse("Should not return hardcoded 'Audio' folder from mock fallback", 
                folderListing.folders.contains("Audio"))
            assertFalse("Should not return hardcoded 'Downloads' folder from mock fallback", 
                folderListing.folders.contains("Downloads"))
            
            // If all three hardcoded folders are present, this indicates mock fallback is still active
            val hasAllMockFolders = folderListing.folders.containsAll(listOf("Music", "Audio", "Downloads"))
            assertFalse("Mock fallback still active - returning hardcoded ['Music', 'Audio', 'Downloads']", 
                hasAllMockFolders)
        }
    }

    @Test
    fun `listAudioFilesWithPagination should not return hardcoded page1_song1_mp3 patterns`() {
        // Act - Call pagination without proper auth/setup
        val result = musicDiscoveryService.listAudioFilesWithPagination("/music", null, 10)

        // Assert - Should NOT return the hardcoded pagination patterns
        if (result.isSuccess) {
            val paginatedResponse = result.getOrNull()!!
            assertFalse("Should not return hardcoded 'page1_song1.mp3' from mock fallback", 
                paginatedResponse.audioFiles.contains("page1_song1.mp3"))
            assertFalse("Should not return hardcoded 'page1_track2.flac' from mock fallback", 
                paginatedResponse.audioFiles.contains("page1_track2.flac"))
            assertFalse("Should not return hardcoded 'page1_audio3.wav' from mock fallback", 
                paginatedResponse.audioFiles.contains("page1_audio3.wav"))
            
            // Check for the specific pattern of hardcoded pagination mock data
            val hasMockPattern = paginatedResponse.audioFiles.any { 
                it.startsWith("page1_") || it.startsWith("page2_") 
            }
            assertFalse("Mock pagination fallback still active", hasMockPattern)
        }
    }

    @Test
    fun `listAudioFilesWithCache should not return hardcoded cached_song1_mp3 patterns`() {
        // Act - Call cache functionality
        val result = musicDiscoveryService.listAudioFilesWithCache("/music")

        // Assert - Should NOT return the hardcoded cache patterns
        if (result.isSuccess) {
            val cachedResponse = result.getOrNull()!!
            assertFalse("Should not return hardcoded 'cached_song1.mp3' from mock fallback", 
                cachedResponse.audioFiles.contains("cached_song1.mp3"))
            assertFalse("Should not return hardcoded 'cached_track2.flac' from mock fallback", 
                cachedResponse.audioFiles.contains("cached_track2.flac"))
            assertFalse("Should not return hardcoded 'cached_audio3.wav' from mock fallback", 
                cachedResponse.audioFiles.contains("cached_audio3.wav"))
            
            // Check for the specific pattern of hardcoded cache mock data
            val hasMockCachePattern = cachedResponse.audioFiles.any { it.startsWith("cached_") }
            assertFalse("Mock cache fallback still active", hasMockCachePattern)
        }
    }

    @Test
    fun `discoverAlbums should not return hardcoded Artist_Album_Song patterns`() {
        // Act - Call album discovery
        val result = musicDiscoveryService.discoverAlbums("/music")

        // Assert - Should NOT return the hardcoded album patterns
        if (result.isSuccess) {
            val albumsResponse = result.getOrNull()!!
            val allAudioFiles = albumsResponse.albums.flatMap { it.audioFiles }
            assertFalse("Should not return hardcoded 'Artist - Album - Song1.mp3' from mock fallback", 
                allAudioFiles.contains("Artist - Album - Song1.mp3"))
            assertFalse("Should not return hardcoded 'Artist - Album - Song2.mp3' from mock fallback", 
                allAudioFiles.contains("Artist - Album - Song2.mp3"))
            
            // Check for the specific pattern of hardcoded album mock data
            val hasMockAlbumPattern = allAudioFiles.any { 
                it.matches(Regex("Artist - Album - Song\\d+\\.mp3")) 
            }
            assertFalse("Mock album fallback still active", hasMockAlbumPattern)
        }
    }

    @Test
    fun `listAudioFilesWithErrorHandling should not return hardcoded retry patterns when retryScenario is false`() {
        // Act - Call without retry scenario activated
        val result = musicDiscoveryService.listAudioFilesWithErrorHandling(
            "/music", 
            retryScenario = false  // Explicitly disable retry scenario
        )

        // Assert - Should NOT return hardcoded retry patterns
        if (result.isSuccess) {
            val errorResponse = result.getOrNull()!!
            val audioFiles = errorResponse.audioFiles ?: emptyList()
            
            // Check for hardcoded retry file patterns that shouldn't appear when retryScenario=false
            val hasRetryPattern = audioFiles.any { 
                it.contains("_retry1.mp3") || it.contains("_retry2.flac") 
            }
            assertFalse("Should not return hardcoded retry patterns when retryScenario=false", hasRetryPattern)
            
            // Check for hardcoded default patterns that also shouldn't appear
            val hasDefaultPattern = audioFiles.any { 
                it.contains("_default1.mp3") || it.contains("_default2.flac") 
            }
            assertFalse("Should not return hardcoded default patterns", hasDefaultPattern)
        }
    }
}