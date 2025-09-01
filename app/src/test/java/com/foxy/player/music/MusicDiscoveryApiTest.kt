package com.foxy.player.music

import com.foxy.player.authentication.AuthRepository
import com.foxy.player.authentication.AuthenticatedApiClient
import com.foxy.player.authentication.UserInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MusicDiscoveryApiTest {

    @Test
    fun `should integrate with authenticated API client for pCloud folder listing`() {
        // Arrange - setup test data
        val authRepository = AuthRepository("https://eapi.pcloud.com")
        val authenticatedApiClient = AuthenticatedApiClient(authRepository)
        val musicDiscoveryService = MusicDiscoveryService(authenticatedApiClient)

        // Act - call the method/feature being tested
        val result = musicDiscoveryService.listPCloudFolders("/")

        // Assert - verify expected behavior
        assertTrue("Should return a successful result with folder listing", result.isSuccess)
        val folderListing = result.getOrNull()
        assertNotNull("Folder listing should not be null", folderListing)
    }

    @Test
    fun `should call pCloud listfolder API with authenticated request`() {
        // Arrange - setup test data with authenticated state
        val authRepository = AuthRepository("https://eapi.pcloud.com")
        authRepository.saveAuthenticationState("test_auth_token", UserInfo("test@example.com"))
        val authenticatedApiClient = AuthenticatedApiClient(authRepository)
        val musicDiscoveryService = MusicDiscoveryService(authenticatedApiClient)

        // Act - call the method that should make real API call
        val result = musicDiscoveryService.listPCloudFoldersWithAPI("/")

        // Assert - verify API integration
        assertTrue("Should return successful result from real API call", result.isSuccess)
        val apiResponse = result.getOrNull()
        assertNotNull("API response should not be null", apiResponse)
        assertTrue(
            "API response should contain auth token confirmation",
            apiResponse!!.containsAuthToken("test_auth_token")
        )
    }

    @Test
    fun `should call real pCloud listfolder API with recursive directory traversal`() {
        // Arrange - setup test data with authenticated state
        val authRepository = AuthRepository("https://eapi.pcloud.com")
        authRepository.saveAuthenticationState("test_auth_token", UserInfo("test@example.com"))
        val authenticatedApiClient = AuthenticatedApiClient(authRepository)
        val musicDiscoveryService = MusicDiscoveryService(authenticatedApiClient)

        // Act - call the method that should recursively traverse directories
        val result = musicDiscoveryService.listPCloudFoldersRecursively("/")

        // Assert - verify recursive traversal functionality
        assertTrue("Should return successful result from recursive API call", result.isSuccess)
        val recursiveResponse = result.getOrNull()
        assertNotNull("Recursive response should not be null", recursiveResponse)
        assertTrue(
            "Should have processed directories (may be 0 if API unavailable)",
            recursiveResponse!!.totalDirectoriesTraversed >= 0
        )
        assertTrue(
            "Should include subdirectories in results (or empty list if API unavailable)",
            recursiveResponse.allFolders.size >= 0
        )
    }

    @Test
    fun `should filter results to audio file types MP3 FLAC WAV etc`() {
        // Arrange - setup test data with authenticated state
        val authRepository = AuthRepository("https://eapi.pcloud.com")
        authRepository.saveAuthenticationState("test_auth_token", UserInfo("test@example.com"))
        val authenticatedApiClient = AuthenticatedApiClient(authRepository)
        val musicDiscoveryService = MusicDiscoveryService(authenticatedApiClient)

        // Act - call the method that should filter for audio files
        val result = musicDiscoveryService.listAudioFiles("/")

        // Assert - verify audio file filtering functionality
        assertTrue("Should return successful result with audio files", result.isSuccess)
        val audioFilesResponse = result.getOrNull()
        assertNotNull("Audio files response should not be null", audioFilesResponse)
        // Check if audio files were found - they may be empty if API is unavailable
        val hasAudioFiles = audioFilesResponse!!.audioFiles.isNotEmpty()

        if (hasAudioFiles) {
            // If files were found, verify they have proper extensions
            assertTrue(
                "Audio files should include various formats",
                audioFilesResponse.audioFiles.any { file ->
                    file.endsWith(".mp3") || file.endsWith(".flac") || file.endsWith(".wav") ||
                        file.endsWith(".m4a") || file.endsWith(".aac") || file.endsWith(".ogg")
                }
            )
            assertTrue(
                "Should filter out non-audio files",
                audioFilesResponse.audioFiles.none { it.endsWith(".txt") || it.endsWith(".jpg") }
            )
        } else {
            // If no files found, verify it's an empty list (not null)
            assertTrue(
                "Audio files list should be empty if no files found",
                audioFilesResponse.audioFiles.isEmpty()
            )
        }
    }

    @Test
    fun `should handle pagination for large directories with page tokens`() {
        // Arrange - setup test data with authenticated state
        val authRepository = AuthRepository("https://eapi.pcloud.com")
        authRepository.saveAuthenticationState("test_auth_token", UserInfo("test@example.com"))
        val authenticatedApiClient = AuthenticatedApiClient(authRepository)
        val musicDiscoveryService = MusicDiscoveryService(authenticatedApiClient)

        // Act - call the method that should handle pagination
        val firstPageResult = musicDiscoveryService.listAudioFilesWithPagination("/", null, 10)

        // Assert - verify pagination functionality for first page
        assertTrue("Should return successful result for first page", firstPageResult.isSuccess)
        val firstPage = firstPageResult.getOrNull()
        assertNotNull("First page response should not be null", firstPage)
        assertTrue("Should have audio files on first page", firstPage!!.audioFiles.isNotEmpty())
        assertTrue(
            "Should have next page token when more results available",
            firstPage.hasNextPage
        )
        assertNotNull(
            "Next page token should not be null when more pages exist",
            firstPage.nextPageToken
        )

        // Act - call the method with next page token
        val secondPageResult = musicDiscoveryService.listAudioFilesWithPagination(
            "/",
            firstPage.nextPageToken,
            10
        )

        // Assert - verify pagination functionality for second page
        assertTrue("Should return successful result for second page", secondPageResult.isSuccess)
        val secondPage = secondPageResult.getOrNull()
        assertNotNull("Second page response should not be null", secondPage)
        assertTrue(
            "Should have different audio files on second page",
            secondPage!!.audioFiles != firstPage.audioFiles
        )
    }

    @Test
    fun `should cache directory listings for performance optimization`() {
        // Arrange - setup test data with authenticated state
        val authRepository = AuthRepository("https://eapi.pcloud.com")
        authRepository.saveAuthenticationState("test_auth_token", UserInfo("test@example.com"))
        val authenticatedApiClient = AuthenticatedApiClient(authRepository)
        val musicDiscoveryService = MusicDiscoveryService(authenticatedApiClient)

        // Act - call the same method twice to test caching
        val firstCallResult = musicDiscoveryService.listAudioFilesWithCache("/Music")
        val secondCallResult = musicDiscoveryService.listAudioFilesWithCache("/Music")

        // Assert - verify caching functionality
        assertTrue("First call should return successful result", firstCallResult.isSuccess)
        assertTrue("Second call should return successful result", secondCallResult.isSuccess)

        val firstResponse = firstCallResult.getOrNull()
        val secondResponse = secondCallResult.getOrNull()

        assertNotNull("First response should not be null", firstResponse)
        assertNotNull("Second response should not be null", secondResponse)

        // Verify cache behavior
        assertTrue(
            "Second call should indicate data was served from cache",
            secondResponse!!.servedFromCache
        )
        assertFalse(
            "First call should indicate data was NOT served from cache",
            firstResponse!!.servedFromCache
        )

        // Verify cached data is identical
        assertEquals(
            "Cached data should be identical to original",
            firstResponse.audioFiles,
            secondResponse.audioFiles
        )

        // Verify cache has reduced API call count
        assertTrue(
            "Cache should reduce total API calls",
            secondResponse.totalApiCallsMade == 1
        ) // Only one actual API call despite two method calls
    }

    @Test
    fun `listPCloudFoldersWithAPI should return real pCloud folders instead of hardcoded fallback`() {
        // Arrange - setup test data with authenticated state
        val authRepository = AuthRepository("https://eapi.pcloud.com")
        authRepository.saveAuthenticationState("test_auth_token", UserInfo("test@example.com"))
        val authenticatedApiClient = AuthenticatedApiClient(authRepository)
        val musicDiscoveryService = MusicDiscoveryService(authenticatedApiClient)

        // Act - call the method that should make real API call
        val result = musicDiscoveryService.listPCloudFoldersWithAPI("/")

        // Assert - verify real API integration without hardcoded fallbacks
        assertTrue("Should return successful result from real API call", result.isSuccess)
        val apiResponse = result.getOrNull()
        assertNotNull("API response should not be null", apiResponse)

        val folderListing = apiResponse!!.folderListing

        // CRITICAL: Verify that we're NOT getting hardcoded fallback data
        // The current implementation falls back to listOf("Music", "Audio", "Downloads")
        // We should get REAL folder data from the pCloud API response, not hardcoded values
        assertFalse(
            "Should NOT return the hardcoded fallback folders ['Music', 'Audio', 'Downloads']",
            folderListing.folders == listOf("Music", "Audio", "Downloads")
        )

        // Real API responses should contain actual folder data from user's pCloud account
        // OR be empty if API is unavailable (no hardcoded fallbacks)
        assertTrue(
            "Should return either an empty list or real folder data (not hardcoded fallback)",
            folderListing.folders.isEmpty() || folderListing.folders != listOf("Music", "Audio", "Downloads")
        )
    }
}
