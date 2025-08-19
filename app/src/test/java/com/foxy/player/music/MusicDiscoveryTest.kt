package com.foxy.player.music

import org.junit.Test
import org.junit.Assert.*
import com.foxy.player.authentication.AuthenticatedApiClient
import com.foxy.player.authentication.AuthRepository
import com.foxy.player.authentication.UserInfo
import com.foxy.player.authentication.AuthResponse

class MusicDiscoveryTest {
    
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
        assertTrue("API response should contain auth token confirmation", 
            apiResponse!!.containsAuthToken("test_auth_token"))
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
        assertTrue("Should have processed multiple directory levels", 
            recursiveResponse!!.totalDirectoriesTraversed > 0)
        assertTrue("Should include subdirectories in results", 
            recursiveResponse.allFolders.isNotEmpty())
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
        assertTrue("Should contain MP3 files", 
            audioFilesResponse!!.audioFiles.any { it.endsWith(".mp3") })
        assertTrue("Should contain FLAC files", 
            audioFilesResponse.audioFiles.any { it.endsWith(".flac") })
        assertTrue("Should contain WAV files", 
            audioFilesResponse.audioFiles.any { it.endsWith(".wav") })
        assertTrue("Should filter out non-audio files", 
            audioFilesResponse.audioFiles.none { it.endsWith(".txt") || it.endsWith(".jpg") })
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
        assertTrue("Should have next page token when more results available", 
            firstPage.hasNextPage)
        assertNotNull("Next page token should not be null when more pages exist", 
            firstPage.nextPageToken)
        
        // Act - call the method with next page token
        val secondPageResult = musicDiscoveryService.listAudioFilesWithPagination("/", firstPage.nextPageToken, 10)
        
        // Assert - verify pagination functionality for second page
        assertTrue("Should return successful result for second page", secondPageResult.isSuccess)
        val secondPage = secondPageResult.getOrNull()
        assertNotNull("Second page response should not be null", secondPage)
        assertTrue("Should have different audio files on second page", 
            secondPage!!.audioFiles != firstPage.audioFiles)
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
        assertTrue("Second call should indicate data was served from cache", 
            secondResponse!!.servedFromCache)
        assertFalse("First call should indicate data was NOT served from cache", 
            firstResponse!!.servedFromCache)
        
        // Verify cached data is identical
        assertEquals("Cached data should be identical to original", 
            firstResponse.audioFiles, secondResponse.audioFiles)
        
        // Verify cache has reduced API call count
        assertTrue("Cache should reduce total API calls", 
            secondResponse.totalApiCallsMade == 1) // Only one actual API call despite two method calls
    }
}