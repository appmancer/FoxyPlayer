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
    
    @Test
    fun `should handle network and API errors with proper error recovery`() {
        // Arrange - setup test data with authenticated state
        val authRepository = AuthRepository("https://eapi.pcloud.com")
        authRepository.saveAuthenticationState("test_auth_token", UserInfo("test@example.com"))
        val authenticatedApiClient = AuthenticatedApiClient(authRepository)
        val musicDiscoveryService = MusicDiscoveryService(authenticatedApiClient)
        
        // Act & Assert - Test network timeout handling
        val timeoutResult = musicDiscoveryService.listAudioFilesWithErrorHandling("/", networkTimeout = true)
        assertTrue("Should gracefully handle network timeout", timeoutResult.isSuccess)
        val timeoutResponse = timeoutResult.getOrNull()
        assertNotNull("Timeout response should not be null", timeoutResponse)
        assertTrue("Should indicate network timeout error", timeoutResponse!!.hasNetworkError)
        assertEquals("Should have proper error message for timeout", 
            "Network timeout - using cached data or retry mechanism", timeoutResponse.errorMessage)
        
        // Act & Assert - Test HTTP error handling (404, 500, etc.)
        val httpErrorResult = musicDiscoveryService.listAudioFilesWithErrorHandling("/nonexistent", httpError = 404)
        assertTrue("Should gracefully handle HTTP 404 error", httpErrorResult.isSuccess)
        val httpErrorResponse = httpErrorResult.getOrNull()
        assertNotNull("HTTP error response should not be null", httpErrorResponse)
        assertTrue("Should indicate HTTP error", httpErrorResponse!!.hasHttpError)
        assertEquals("Should have proper HTTP error code", 404, httpErrorResponse.httpErrorCode)
        assertEquals("Should have proper error message for HTTP 404", 
            "Resource not found - verify path exists", httpErrorResponse.errorMessage)
        
        // Act & Assert - Test API authentication error handling
        val authErrorResult = musicDiscoveryService.listAudioFilesWithErrorHandling("/", authError = true)
        assertTrue("Should gracefully handle authentication error", authErrorResult.isSuccess)
        val authErrorResponse = authErrorResult.getOrNull()
        assertNotNull("Auth error response should not be null", authErrorResponse)
        assertTrue("Should indicate authentication error", authErrorResponse!!.hasAuthError)
        assertEquals("Should have proper error message for auth failure", 
            "Authentication failed - please re-login", authErrorResponse.errorMessage)
        
        // Act & Assert - Test retry mechanism with eventual success
        val retryResult = musicDiscoveryService.listAudioFilesWithErrorHandling("/", retryScenario = true)
        assertTrue("Should succeed after retry attempts", retryResult.isSuccess)
        val retryResponse = retryResult.getOrNull()
        assertNotNull("Retry response should not be null", retryResponse)
        assertTrue("Should indicate retry was performed", retryResponse!!.retriesPerformed > 0)
        assertTrue("Should have successful data after retry", retryResponse.audioFiles.isNotEmpty())
        assertEquals("Should show proper retry count", 3, retryResponse.retriesPerformed)
        
        // Act & Assert - Test fallback to cached data during errors
        val fallbackResult = musicDiscoveryService.listAudioFilesWithErrorHandling("/Music", useCachedFallback = true)
        assertTrue("Should fall back to cached data during errors", fallbackResult.isSuccess)
        val fallbackResponse = fallbackResult.getOrNull()
        assertNotNull("Fallback response should not be null", fallbackResponse)
        assertTrue("Should indicate fallback to cached data", fallbackResponse!!.usedCachedFallback)
        assertTrue("Should have cached data available", fallbackResponse.audioFiles.isNotEmpty())
        assertEquals("Should have proper fallback message", 
            "Using cached data due to network error", fallbackResponse.errorMessage)
    }
    
    @Test
    fun `should extract basic metadata from MP3 file`() {
        // Arrange - setup test data with authenticated state and metadata extractor
        val authRepository = AuthRepository("https://eapi.pcloud.com")
        authRepository.saveAuthenticationState("test_auth_token", UserInfo("test@example.com"))
        val authenticatedApiClient = AuthenticatedApiClient(authRepository)
        val musicDiscoveryService = MusicDiscoveryService(authenticatedApiClient)
        
        // Create test audio file data
        val audioFileUrl = "https://filesamples.com/samples/audio/mp3/SampleAudio_0.4mb_mp3.mp3"
        val audioFileName = "SampleAudio_0.4mb_mp3.mp3"
        
        // Act - call the metadata extraction method that doesn't exist yet
        val result = musicDiscoveryService.extractMetadata(audioFileUrl, audioFileName)
        
        // Assert - verify metadata extraction functionality
        assertTrue("Should return successful result with extracted metadata", result.isSuccess)
        val metadata = result.getOrNull()
        assertNotNull("Metadata should not be null", metadata)
        assertNotNull("Should extract title from MP3 file", metadata!!.title)
        assertNotNull("Should extract artist from MP3 file", metadata.artist)
        assertNotNull("Should extract album from MP3 file", metadata.album)
        assertTrue("Should extract duration from MP3 file", metadata.durationMs > 0)
        assertEquals("Should identify correct file format", "MP3", metadata.format)
        assertTrue("Should extract bitrate from MP3 file", metadata.bitrate > 0)
    }
    
    @Test
    fun `should extract metadata from different audio formats FLAC WAV AAC`() {
        // Arrange - setup test data with authenticated state and metadata extractor
        val authRepository = AuthRepository("https://eapi.pcloud.com")
        authRepository.saveAuthenticationState("test_auth_token", UserInfo("test@example.com"))
        val authenticatedApiClient = AuthenticatedApiClient(authRepository)
        val musicDiscoveryService = MusicDiscoveryService(authenticatedApiClient)
        
        // Test FLAC format
        val flacResult = musicDiscoveryService.extractMetadata("https://sample.com/test.flac", "test.flac")
        assertTrue("Should successfully extract FLAC metadata", flacResult.isSuccess)
        val flacMetadata = flacResult.getOrNull()
        assertNotNull("FLAC metadata should not be null", flacMetadata)
        assertEquals("Should identify FLAC format correctly", "FLAC", flacMetadata!!.format)
        
        // Test WAV format
        val wavResult = musicDiscoveryService.extractMetadata("https://sample.com/test.wav", "test.wav")
        assertTrue("Should successfully extract WAV metadata", wavResult.isSuccess)
        val wavMetadata = wavResult.getOrNull()
        assertNotNull("WAV metadata should not be null", wavMetadata)
        assertEquals("Should identify WAV format correctly", "WAV", wavMetadata!!.format)
        
        // Test AAC format
        val aacResult = musicDiscoveryService.extractMetadata("https://sample.com/test.aac", "test.aac")
        assertTrue("Should successfully extract AAC metadata", aacResult.isSuccess)
        val aacMetadata = aacResult.getOrNull()
        assertNotNull("AAC metadata should not be null", aacMetadata)
        assertEquals("Should identify AAC format correctly", "AAC", aacMetadata!!.format)
        
        // All formats should have basic metadata fields
        assertTrue("FLAC should have valid duration", flacMetadata.durationMs > 0)
        assertTrue("WAV should have valid duration", wavMetadata.durationMs > 0)
        assertTrue("AAC should have valid duration", aacMetadata.durationMs > 0)
    }
    
    @Test
    fun `should handle corrupted files and missing metadata gracefully`() {
        // Arrange - setup test data with authenticated state and metadata extractor
        val authRepository = AuthRepository("https://eapi.pcloud.com")
        authRepository.saveAuthenticationState("test_auth_token", UserInfo("test@example.com"))
        val authenticatedApiClient = AuthenticatedApiClient(authRepository)
        val musicDiscoveryService = MusicDiscoveryService(authenticatedApiClient)
        
        // Test corrupted file handling
        val corruptedResult = musicDiscoveryService.extractMetadataWithErrorHandling(
            "https://sample.com/corrupted.mp3", 
            "corrupted.mp3",
            simulateCorruption = true
        )
        assertTrue("Should handle corrupted files gracefully", corruptedResult.isSuccess)
        val corruptedResponse = corruptedResult.getOrNull()
        assertNotNull("Corrupted file response should not be null", corruptedResponse)
        assertTrue("Should indicate file corruption", corruptedResponse!!.hasFileCorruption)
        assertTrue("Should provide fallback metadata", corruptedResponse.hasFallbackMetadata)
        assertEquals("Should have proper error message", 
            "File corrupted - using fallback metadata", corruptedResponse.errorMessage)
        
        // Test missing metadata handling
        val missingMetadataResult = musicDiscoveryService.extractMetadataWithErrorHandling(
            "https://sample.com/nometa.mp3", 
            "nometa.mp3",
            simulateMissingMetadata = true
        )
        assertTrue("Should handle missing metadata gracefully", missingMetadataResult.isSuccess)
        val missingResponse = missingMetadataResult.getOrNull()
        assertNotNull("Missing metadata response should not be null", missingResponse)
        assertTrue("Should indicate missing metadata", missingResponse!!.hasMissingMetadata)
        assertTrue("Should provide fallback metadata", missingResponse.hasFallbackMetadata)
        assertEquals("Should have proper error message", 
            "Metadata not found - using fallback values", missingResponse.errorMessage)
        
        // Test unsupported format handling  
        val unsupportedResult = musicDiscoveryService.extractMetadataWithErrorHandling(
            "https://sample.com/video.mp4", 
            "video.mp4",
            simulateUnsupportedFormat = true
        )
        assertTrue("Should handle unsupported format gracefully", unsupportedResult.isSuccess)
        val unsupportedResponse = unsupportedResult.getOrNull()
        assertNotNull("Unsupported format response should not be null", unsupportedResponse)
        assertTrue("Should indicate unsupported format", unsupportedResponse!!.hasUnsupportedFormat)
        assertFalse("Should not provide metadata for unsupported format", unsupportedResponse.hasFallbackMetadata)
        assertEquals("Should have proper error message", 
            "Unsupported audio format", unsupportedResponse.errorMessage)
    }
}