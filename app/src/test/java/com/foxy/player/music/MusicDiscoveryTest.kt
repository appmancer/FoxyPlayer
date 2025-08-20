package com.foxy.player.music

import org.junit.Test
import org.junit.Assert.*
import com.foxy.player.authentication.AuthenticatedApiClient
import com.foxy.player.authentication.AuthRepository
import com.foxy.player.authentication.UserInfo
import com.foxy.player.authentication.AuthResponse
import java.time.LocalDateTime

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
        
        // Act - call the search method that doesn't exist yet
        val result = musicSearchService.searchTracks("Queen", testTracks)
        
        // Assert - verify search functionality
        assertTrue("Should return successful result with filtered tracks", result.isSuccess)
        val searchResponse = result.getOrNull()
        assertNotNull("Search response should not be null", searchResponse)
        assertTrue("Should contain tracks matching 'Queen'", 
            searchResponse!!.tracks.any { it.artist == "Queen" })
        assertFalse("Should not contain tracks that don't match 'Queen'", 
            searchResponse.tracks.any { it.artist == "Led Zeppelin" })
        assertEquals("Should return exactly 1 track for 'Queen' search", 1, searchResponse.tracks.size)
        assertEquals("Should return the correct Queen track", "Bohemian Rhapsody", searchResponse.tracks[0].title)
    }
    
    @Test
    fun `should filter music tracks by multiple search criteria`() {
        // Arrange - setup test data with authenticated state and music search service
        val authRepository = AuthRepository("https://eapi.pcloud.com")
        authRepository.saveAuthenticationState("test_auth_token", UserInfo("test@example.com"))
        val authenticatedApiClient = AuthenticatedApiClient(authRepository)
        val musicSearchService = MusicSearchService(authenticatedApiClient)
        
        // Create test music tracks with more variety
        val testTracks = listOf(
            MusicTrack("1", "Bohemian Rhapsody", "Queen", "A Night at the Opera", "rock"),
            MusicTrack("2", "We Will Rock You", "Queen", "News of the World", "rock"),
            MusicTrack("3", "Hotel California", "Eagles", "Hotel California", "rock"),
            MusicTrack("4", "Imagine", "John Lennon", "Imagine", "pop"),
            MusicTrack("5", "Like a Rolling Stone", "Bob Dylan", "Highway 61 Revisited", "folk"),
            MusicTrack("6", "Hotel California Live", "Eagles", "Hell Freezes Over", "rock")
        )
        
        // Act - call the enhanced search method that supports multiple criteria
        val searchCriteria = SearchCriteria(
            query = "Hotel",
            searchInTitle = true,
            searchInArtist = true,
            searchInAlbum = true,
            genre = "rock"
        )
        val result = musicSearchService.searchTracksWithCriteria(searchCriteria, testTracks)
        
        // Assert - verify enhanced search functionality
        assertTrue("Should return successful result with filtered tracks", result.isSuccess)
        val searchResponse = result.getOrNull()
        assertNotNull("Search response should not be null", searchResponse)
        assertEquals("Should return exactly 2 tracks matching 'Hotel' and 'rock' genre", 2, searchResponse!!.tracks.size)
        assertTrue("Should contain 'Hotel California' track", 
            searchResponse.tracks.any { it.title == "Hotel California" })
        assertTrue("Should contain 'Hotel California Live' track", 
            searchResponse.tracks.any { it.title == "Hotel California Live" })
        assertFalse("Should not contain non-rock tracks", 
            searchResponse.tracks.any { it.genre != "rock" })
    }
    
    @Test
    fun `should browse music tracks by artist grouping`() {
        // Arrange - setup test data with authenticated state and music browse service
        val authRepository = AuthRepository("https://eapi.pcloud.com")
        authRepository.saveAuthenticationState("test_auth_token", UserInfo("test@example.com"))
        val authenticatedApiClient = AuthenticatedApiClient(authRepository)
        val musicSearchService = MusicSearchService(authenticatedApiClient)
        
        // Create test music tracks with multiple artists
        val testTracks = listOf(
            MusicTrack("1", "Bohemian Rhapsody", "Queen", "A Night at the Opera", "rock"),
            MusicTrack("2", "We Will Rock You", "Queen", "News of the World", "rock"),
            MusicTrack("3", "Hotel California", "Eagles", "Hotel California", "rock"),
            MusicTrack("4", "Take It Easy", "Eagles", "Eagles", "rock"),
            MusicTrack("5", "Imagine", "John Lennon", "Imagine", "pop"),
            MusicTrack("6", "Like a Rolling Stone", "Bob Dylan", "Highway 61 Revisited", "folk")
        )
        
        // Act - call the browse by artist method that doesn't exist yet
        val result = musicSearchService.browseByArtist(testTracks)
        
        // Assert - verify browse functionality
        assertTrue("Should return successful result with artist groups", result.isSuccess)
        val browseResponse = result.getOrNull()
        assertNotNull("Browse response should not be null", browseResponse)
        assertEquals("Should group tracks by 4 different artists", 4, browseResponse!!.artistGroups.size)
        
        // Verify Queen group
        val queenGroup = browseResponse.artistGroups.find { it.artist == "Queen" }
        assertNotNull("Should contain Queen group", queenGroup)
        assertEquals("Queen should have 2 tracks", 2, queenGroup!!.tracks.size)
        assertTrue("Queen group should contain Bohemian Rhapsody", 
            queenGroup.tracks.any { it.title == "Bohemian Rhapsody" })
        assertTrue("Queen group should contain We Will Rock You", 
            queenGroup.tracks.any { it.title == "We Will Rock You" })
        
        // Verify Eagles group
        val eaglesGroup = browseResponse.artistGroups.find { it.artist == "Eagles" }
        assertNotNull("Should contain Eagles group", eaglesGroup)
        assertEquals("Eagles should have 2 tracks", 2, eaglesGroup!!.tracks.size)
    }
    
    @Test
    fun `should sort music tracks by different criteria`() {
        // Arrange - setup test data with authenticated state and music search service
        val authRepository = AuthRepository("https://eapi.pcloud.com")
        authRepository.saveAuthenticationState("test_auth_token", UserInfo("test@example.com"))
        val authenticatedApiClient = AuthenticatedApiClient(authRepository)
        val musicSearchService = MusicSearchService(authenticatedApiClient)
        
        // Create test music tracks with sorting metadata
        val testTracks = listOf(
            MusicTrackWithMetadata("1", "Zebra Song", "Artist A", "Album Z", "rock", 
                durationMs = 180000, fileSizeBytes = 5000000, dateAdded = LocalDateTime.of(2023, 1, 1, 0, 0)),
            MusicTrackWithMetadata("2", "Alpha Song", "Artist B", "Album A", "pop", 
                durationMs = 240000, fileSizeBytes = 3000000, dateAdded = LocalDateTime.of(2023, 3, 1, 0, 0)),
            MusicTrackWithMetadata("3", "Beta Song", "Artist A", "Album B", "rock", 
                durationMs = 120000, fileSizeBytes = 8000000, dateAdded = LocalDateTime.of(2023, 2, 1, 0, 0))
        )
        
        // Test alphabetical sorting by title
        val alphabeticalResult = musicSearchService.sortTracks(testTracks, SortCriteria.ALPHABETICAL_TITLE)
        assertTrue("Should return successful result for alphabetical sort", alphabeticalResult.isSuccess)
        val alphabeticalResponse = alphabeticalResult.getOrNull()!!
        assertEquals("Should sort alphabetically by title", "Alpha Song", alphabeticalResponse.tracks[0].title)
        assertEquals("Should sort alphabetically by title", "Beta Song", alphabeticalResponse.tracks[1].title)
        assertEquals("Should sort alphabetically by title", "Zebra Song", alphabeticalResponse.tracks[2].title)
        
        // Test duration sorting
        val durationResult = musicSearchService.sortTracks(testTracks, SortCriteria.DURATION)
        assertTrue("Should return successful result for duration sort", durationResult.isSuccess)
        val durationResponse = durationResult.getOrNull()!!
        assertEquals("Should sort by duration (shortest first)", 120000, durationResponse.tracks[0].durationMs)
        assertEquals("Should sort by duration", 180000, durationResponse.tracks[1].durationMs)
        assertEquals("Should sort by duration (longest last)", 240000, durationResponse.tracks[2].durationMs)
        
        // Test file size sorting
        val fileSizeResult = musicSearchService.sortTracks(testTracks, SortCriteria.FILE_SIZE)
        assertTrue("Should return successful result for file size sort", fileSizeResult.isSuccess)
        val fileSizeResponse = fileSizeResult.getOrNull()!!
        assertEquals("Should sort by file size (smallest first)", 3000000, fileSizeResponse.tracks[0].fileSizeBytes)
        assertEquals("Should sort by file size", 5000000, fileSizeResponse.tracks[1].fileSizeBytes)
        assertEquals("Should sort by file size (largest last)", 8000000, fileSizeResponse.tracks[2].fileSizeBytes)
    }
    
    @Test
    fun `should display search interface with text input and results`() {
        // Arrange - setup test data with authenticated state and UI state
        val authRepository = AuthRepository("https://eapi.pcloud.com")
        authRepository.saveAuthenticationState("test_auth_token", UserInfo("test@example.com"))
        val authenticatedApiClient = AuthenticatedApiClient(authRepository)
        val musicSearchService = MusicSearchService(authenticatedApiClient)
        
        // Create test music search state manager
        val searchStateManager = MusicSearchStateManager(musicSearchService)
        
        // Act - initialize the search interface state
        val uiState = searchStateManager.getInitialSearchState()
        
        // Assert - verify UI state for search interface
        assertTrue("Should return successful UI state", uiState.isSuccess)
        val searchState = uiState.getOrNull()
        assertNotNull("Search state should not be null", searchState)
        assertEquals("Should have empty search query initially", "", searchState!!.searchQuery)
        assertTrue("Should show search input field", searchState.showSearchInput)
        assertTrue("Should have empty results initially", searchState.searchResults.isEmpty())
        assertFalse("Should not show loading initially", searchState.isLoading)
        
        // Test search input handling
        val searchQuery = "test query"
        val updatedState = searchStateManager.updateSearchQuery(searchQuery)
        assertTrue("Should update search query successfully", updatedState.isSuccess)
        val newState = updatedState.getOrNull()!!
        assertEquals("Should update search query", searchQuery, newState.searchQuery)
        assertTrue("Should show loading when search query is updated", newState.isLoading)
    }
    
    @Test
    fun `MusicTrack_should_use_proper_date_type_for_dateAdded_field`() {
        // Arrange - setup test data using LocalDateTime instead of String
        val testDate = LocalDateTime.of(2023, 3, 15, 10, 30, 0)
        
        // Act - create MusicTrackWithMetadata with LocalDateTime dateAdded
        val track = MusicTrackWithMetadata(
            id = "1", 
            title = "Test Song", 
            artist = "Test Artist", 
            album = "Test Album", 
            genre = "rock",
            durationMs = 180000, 
            fileSizeBytes = 5000000, 
            dateAdded = testDate  // This should be LocalDateTime, not String
        )
        
        // Assert - verify date type functionality
        assertTrue("Should accept LocalDateTime for dateAdded", track.dateAdded is LocalDateTime)
        assertEquals("Should preserve date value correctly", testDate, track.dateAdded)
        assertEquals("Should allow date comparison", 2023, track.dateAdded.year)
        assertEquals("Should allow date comparison", 3, track.dateAdded.monthValue)
    }
    
    @Test
    fun `MusicSearchService_should_sort_tracks_by_dateAdded_in_chronological_order`() {
        // Arrange - setup test data with dates in non-chronological order
        val authRepository = AuthRepository("https://eapi.pcloud.com")
        authRepository.saveAuthenticationState("test_auth_token", UserInfo("test@example.com"))
        val authenticatedApiClient = AuthenticatedApiClient(authRepository)
        val musicSearchService = MusicSearchService(authenticatedApiClient)
        
        // Create tracks with dates that would sort incorrectly lexicographically
        val testTracks = listOf(
            MusicTrackWithMetadata("1", "Song A", "Artist A", "Album A", "rock", 
                durationMs = 180000, fileSizeBytes = 5000000, 
                dateAdded = LocalDateTime.of(2023, 12, 15, 10, 0)), // Latest
            MusicTrackWithMetadata("2", "Song B", "Artist B", "Album B", "pop", 
                durationMs = 240000, fileSizeBytes = 3000000, 
                dateAdded = LocalDateTime.of(2023, 2, 5, 14, 30)),  // Earliest  
            MusicTrackWithMetadata("3", "Song C", "Artist C", "Album C", "rock", 
                durationMs = 120000, fileSizeBytes = 8000000, 
                dateAdded = LocalDateTime.of(2023, 11, 20, 9, 15))  // Middle
        )
        
        // Act - sort tracks by dateAdded chronologically (earliest first)
        val result = musicSearchService.sortTracks(testTracks, SortCriteria.DATE_ADDED)
        
        // Assert - verify chronological sorting (not lexicographic)
        assertTrue("Should return successful result for date sorting", result.isSuccess)
        val sortedResponse = result.getOrNull()!!
        
        // Verify chronological order: Feb 5 -> Nov 20 -> Dec 15
        assertEquals("Should sort chronologically (earliest first)", "Song B", sortedResponse.tracks[0].title)
        assertEquals("Should sort chronologically (middle)", "Song C", sortedResponse.tracks[1].title)  
        assertEquals("Should sort chronologically (latest last)", "Song A", sortedResponse.tracks[2].title)
        
        // Verify specific dates are in chronological order
        assertTrue("First track should be earliest", 
            sortedResponse.tracks[0].dateAdded.isBefore(sortedResponse.tracks[1].dateAdded))
        assertTrue("Second track should be before third", 
            sortedResponse.tracks[1].dateAdded.isBefore(sortedResponse.tracks[2].dateAdded))
    }
    
    @Test
    fun `should_cache_music_metadata_efficiently_for_fast_retrieval`() {
        // Arrange - setup test data with authenticated state and metadata caching service
        val authRepository = AuthRepository("https://eapi.pcloud.com")
        authRepository.saveAuthenticationState("test_auth_token", UserInfo("test@example.com"))
        val authenticatedApiClient = AuthenticatedApiClient(authRepository)
        val musicMetadataCacheService = MusicMetadataCacheService(authenticatedApiClient)
        
        val audioFileUrl = "https://sample.com/test.mp3"
        val audioFileName = "test.mp3"
        
        // Act - call metadata extraction twice to test caching efficiency
        val firstCallResult = musicMetadataCacheService.getMetadataWithCache(audioFileUrl, audioFileName)
        val secondCallResult = musicMetadataCacheService.getMetadataWithCache(audioFileUrl, audioFileName)
        
        // Assert - verify caching efficiency and fast retrieval
        assertTrue("First call should return successful result", firstCallResult.isSuccess)
        assertTrue("Second call should return successful result", secondCallResult.isSuccess)
        
        val firstResponse = firstCallResult.getOrNull()
        val secondResponse = secondCallResult.getOrNull()
        
        assertNotNull("First response should not be null", firstResponse)
        assertNotNull("Second response should not be null", secondResponse)
        
        // Verify cache efficiency behavior
        assertFalse("First call should NOT be served from cache", firstResponse!!.servedFromCache)
        assertTrue("Second call should be served from cache for efficiency", secondResponse!!.servedFromCache)
        
        // Verify metadata is cached and identical
        assertEquals("Cached metadata should be identical to original", 
            firstResponse.metadata, secondResponse.metadata)
        
        // Verify caching improves performance
        assertTrue("First call should take longer (needs metadata extraction)", 
            firstResponse.processingTimeMs > 0)
        assertTrue("Second call should be faster (served from cache)", 
            secondResponse.processingTimeMs < firstResponse.processingTimeMs)
        
        // Verify cache reduces metadata extraction operations
        assertEquals("Should perform metadata extraction only once despite two calls", 
            1, secondResponse.totalMetadataExtractions)
    }
    
    @Test
    fun `should_implement_database_indexing_for_fast_search_queries_under_100ms`() {
        // Arrange - setup test data with authenticated state and database indexing service
        val authRepository = AuthRepository("https://eapi.pcloud.com")
        authRepository.saveAuthenticationState("test_auth_token", UserInfo("test@example.com"))
        val authenticatedApiClient = AuthenticatedApiClient(authRepository)
        val musicDatabaseIndexService = MusicDatabaseIndexService(authenticatedApiClient)
        
        // Create test music tracks for indexing
        val testTracks = listOf(
            MusicTrackWithMetadata("1", "Bohemian Rhapsody", "Queen", "A Night at the Opera", "rock", 
                durationMs = 355000, fileSizeBytes = 8500000, dateAdded = LocalDateTime.of(2023, 1, 1, 0, 0)),
            MusicTrackWithMetadata("2", "Stairway to Heaven", "Led Zeppelin", "Led Zeppelin IV", "rock", 
                durationMs = 482000, fileSizeBytes = 11200000, dateAdded = LocalDateTime.of(2023, 2, 1, 0, 0)),
            MusicTrackWithMetadata("3", "Hotel California", "Eagles", "Hotel California", "rock", 
                durationMs = 391000, fileSizeBytes = 9100000, dateAdded = LocalDateTime.of(2023, 3, 1, 0, 0))
        )
        
        // Act - initialize database indexing for fast searches
        val indexingResult = musicDatabaseIndexService.createIndexes(testTracks)
        
        // Assert - verify database indexing functionality
        assertTrue("Should successfully create database indexes", indexingResult.isSuccess)
        val indexingResponse = indexingResult.getOrNull()
        assertNotNull("Indexing response should not be null", indexingResponse)
        assertTrue("Should create indexes for title field", indexingResponse!!.titleIndexCreated)
        assertTrue("Should create indexes for artist field", indexingResponse.artistIndexCreated)
        assertTrue("Should create indexes for album field", indexingResponse.albumIndexCreated)
        assertTrue("Should create indexes for genre field", indexingResponse.genreIndexCreated)
        
        // Test fast search query performance with indexes
        val searchQuery = "Queen"
        val searchStartTime = System.currentTimeMillis()
        val searchResult = musicDatabaseIndexService.searchWithIndexes(searchQuery, testTracks)
        val searchEndTime = System.currentTimeMillis()
        val searchDurationMs = searchEndTime - searchStartTime
        
        // Assert - verify search performance under 100ms
        assertTrue("Should return successful search result", searchResult.isSuccess)
        val searchResponse = searchResult.getOrNull()
        assertNotNull("Search response should not be null", searchResponse)
        assertTrue("Search should complete under 100ms for performance", searchDurationMs < 100)
        assertTrue("Should use database indexes for search", searchResponse!!.usedDatabaseIndexes)
        assertTrue("Should find matching tracks efficiently", searchResponse.tracks.isNotEmpty())
        assertEquals("Should find Queen track", "Bohemian Rhapsody", searchResponse.tracks[0].title)
        
        // Verify index optimization metrics
        assertTrue("Should show performance improvement with indexes", 
            searchResponse.indexOptimizationMs >= 0)
        assertTrue("Should be faster than linear search", 
            searchResponse.indexOptimizationMs < 50) // Should be very fast with proper indexing
    }
    
    @Test
    fun `should_optimize_memory_usage_for_large_music_libraries`() {
        // Arrange - setup test data with authenticated state and memory optimization service
        val authRepository = AuthRepository("https://eapi.pcloud.com")
        authRepository.saveAuthenticationState("test_auth_token", UserInfo("test@example.com"))
        val authenticatedApiClient = AuthenticatedApiClient(authRepository)
        val musicMemoryOptimizationService = MusicMemoryOptimizationService(authenticatedApiClient)
        
        // Create large music library test data (simulating 10,000+ tracks)
        val largeMusicLibrary = mutableListOf<MusicTrackWithMetadata>()
        for (i in 1..10000) {
            largeMusicLibrary.add(
                MusicTrackWithMetadata(
                    id = i.toString(), 
                    title = "Track $i", 
                    artist = "Artist ${i % 100}", 
                    album = "Album ${i % 50}", 
                    genre = "Genre ${i % 10}",
                    durationMs = (180000 + (i % 120000)).toLong(), 
                    fileSizeBytes = (5000000 + (i % 3000000)).toLong(), 
                    dateAdded = LocalDateTime.of(2023, (i % 12) + 1, (i % 28) + 1, 0, 0)
                )
            )
        }
        
        // Act - optimize memory usage for large library
        val memoryOptimizationResult = musicMemoryOptimizationService.optimizeMemoryForLargeLibrary(largeMusicLibrary)
        
        // Assert - verify memory optimization functionality
        assertTrue("Should successfully optimize memory for large library", memoryOptimizationResult.isSuccess)
        val optimizationResponse = memoryOptimizationResult.getOrNull()
        assertNotNull("Memory optimization response should not be null", optimizationResponse)
        assertTrue("Should reduce memory footprint significantly", optimizationResponse!!.memoryReductionPercent > 50)
        assertTrue("Should maintain data integrity during optimization", optimizationResponse.dataIntegrityMaintained)
        assertTrue("Should use memory-efficient data structures", optimizationResponse.usedMemoryEfficientStructures)
        
        // Test garbage collection optimization
        val gcOptimizationResult = musicMemoryOptimizationService.optimizeGarbageCollection(largeMusicLibrary)
        assertTrue("Should optimize garbage collection for large library", gcOptimizationResult.isSuccess)
        val gcResponse = gcOptimizationResult.getOrNull()
        assertNotNull("GC optimization response should not be null", gcResponse)
        assertTrue("Should reduce GC pressure", gcResponse!!.gcPressureReduced)
        assertTrue("Should improve memory allocation patterns", gcResponse.memoryAllocationOptimized)
        
        // Test memory monitoring and limits
        val memoryMonitoringResult = musicMemoryOptimizationService.monitorMemoryUsage(largeMusicLibrary)
        assertTrue("Should monitor memory usage effectively", memoryMonitoringResult.isSuccess)
        val monitoringResponse = memoryMonitoringResult.getOrNull()
        assertNotNull("Memory monitoring response should not be null", monitoringResponse)
        assertTrue("Should stay within memory limits", monitoringResponse!!.withinMemoryLimits)
        assertTrue("Should provide memory usage metrics", monitoringResponse.memoryUsageBytes > 0)
        assertTrue("Should detect memory leaks", monitoringResponse.memoryLeakDetectionEnabled)
    }
}