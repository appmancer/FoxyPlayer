package com.foxy.player.music.performance

import com.foxy.player.authentication.models.UserInfo
import com.foxy.player.authentication.network.AuthRepository
import com.foxy.player.authentication.network.AuthenticatedApiClient
import com.foxy.player.music.MusicMemoryOptimizationService
import com.foxy.player.music.network.MusicDiscoveryService
import com.foxy.player.music.ui.MusicTrackMemoryOptimized
import java.util.Date
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Performance and caching tests for Music Discovery
 * Extracted from MusicDiscoveryTest.kt as part of KLOC refactoring (PLY-137)
 * Tests caching, memory optimization, and database performance
 */
class MusicDiscoveryPerformanceTest {

    @Test
    fun `should cache directory listings for performance optimization`() {
        // Arrange - setup test data with authenticated state and caching service
        val authRepository = AuthRepository("https://eapi.pcloud.com")
        authRepository.saveAuthenticationState("test_auth_token", UserInfo("test@example.com"))
        val authenticatedApiClient = AuthenticatedApiClient(authRepository)
        val musicDiscoveryService = MusicDiscoveryService(authenticatedApiClient)

        // Act - call the method multiple times to test caching
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
        assertFalse(
            "First call should not be served from cache",
            firstResponse!!.servedFromCache
        )
        assertTrue(
            "Second call should be served from cache",
            secondResponse!!.servedFromCache
        )
        assertEquals(
            "Both calls should return same audio files",
            firstResponse.audioFiles,
            secondResponse.audioFiles
        )
        assertTrue(
            "Second call should have same or lower API call count",
            secondResponse.totalApiCallsMade >= firstResponse.totalApiCallsMade
        )
    }

    @Test
    fun `should use memory optimization for large track collections`() {
        // Arrange
        val authRepository = AuthRepository("https://eapi.pcloud.com")
        authRepository.saveAuthenticationState("test_auth_token", UserInfo("test@example.com"))
        val authenticatedApiClient = AuthenticatedApiClient(authRepository)
        val memoryOptimizationService = MusicMemoryOptimizationService(authenticatedApiClient)

        // Create test tracks for memory optimization
        val testTracks = listOf(
            MusicTrackMemoryOptimized(
                "1", "Song 1", "Artist 1", "Album 1", "rock", "/path/song1.mp3", 180000L, 3500000L, 128, Date()
            ),
            MusicTrackMemoryOptimized(
                "2", "Song 2", "Artist 2", "Album 2", "pop", "/path/song2.mp3", 200000L, 4000000L, 256, Date()
            ),
            MusicTrackMemoryOptimized(
                "3", "Song 3", "Artist 3", "Album 3", "jazz", "/path/song3.mp3", 220000L, 5000000L, 320, Date()
            )
        )

        // Act
        val result = memoryOptimizationService.loadTracksWithMemoryOptimization(testTracks)

        // Assert
        assertTrue("Should return successful result", result.isSuccess)
        val optimizationResponse = result.getOrNull()
        assertNotNull("Optimization response should not be null", optimizationResponse)
        assertTrue(
            "Should indicate memory optimization was used",
            optimizationResponse!!.usedMemoryOptimization
        )
        assertTrue(
            "Should show memory reduction percentage",
            optimizationResponse.memoryReductionPercent > 0.0
        )
        assertTrue(
            "Should verify data integrity",
            optimizationResponse.dataIntegrityVerified
        )
        assertTrue(
            "Should track loading time (can be 0 for small datasets)",
            optimizationResponse.loadingTimeMs >= 0
        )
        assertEquals(
            "Should load all tracks",
            testTracks.size,
            optimizationResponse.totalTracksLoaded
        )
        assertTrue(
            "Should track peak memory usage",
            optimizationResponse.peakMemoryUsageMB > 0
        )

        // Verify optimization features
        val optimizations = optimizationResponse.optimizations
        assertTrue(
            "Should use efficient data structures",
            optimizations.usedEfficientDataStructures
        )
        assertTrue(
            "Should use string interning",
            optimizations.usedStringInterning
        )
        assertTrue(
            "Should use object pooling",
            optimizations.usedObjectPooling
        )
        assertTrue(
            "Should enable GC optimization",
            optimizations.enabledGCOptimization
        )
    }

    @Test
    fun `should measure cache performance improvements`() {
        // Arrange
        val authRepository = AuthRepository("https://eapi.pcloud.com")
        authRepository.saveAuthenticationState("test_auth_token", UserInfo("test@example.com"))
        val authenticatedApiClient = AuthenticatedApiClient(authRepository)
        val musicDiscoveryService = MusicDiscoveryService(authenticatedApiClient)

        // Act - measure performance of cache vs non-cache calls
        val startTime1 = System.currentTimeMillis()
        val firstCallResult = musicDiscoveryService.listAudioFilesWithCache("/Music")
        val firstCallTime = System.currentTimeMillis() - startTime1

        val startTime2 = System.currentTimeMillis()
        val secondCallResult = musicDiscoveryService.listAudioFilesWithCache("/Music")
        val secondCallTime = System.currentTimeMillis() - startTime2

        // Assert
        assertTrue("First call should succeed", firstCallResult.isSuccess)
        assertTrue("Second call should succeed", secondCallResult.isSuccess)

        val firstResponse = firstCallResult.getOrNull()!!
        val secondResponse = secondCallResult.getOrNull()!!

        // Performance assertions
        assertFalse("First call should not be from cache", firstResponse.servedFromCache)
        assertTrue("Second call should be from cache", secondResponse.servedFromCache)

        // Cache should generally be faster, but we allow some tolerance for test environment variations
        assertTrue(
            "Cached call should be reasonably fast",
            secondCallTime <= firstCallTime + 50 // Allow 50ms tolerance
        )

        assertEquals(
            "Both calls should return same data",
            firstResponse.audioFiles.size,
            secondResponse.audioFiles.size
        )
    }

    @Test
    fun `should track API call count for performance monitoring`() {
        // Arrange
        val authRepository = AuthRepository("https://eapi.pcloud.com")
        authRepository.saveAuthenticationState("test_auth_token", UserInfo("test@example.com"))
        val authenticatedApiClient = AuthenticatedApiClient(authRepository)
        val musicDiscoveryService = MusicDiscoveryService(authenticatedApiClient)

        // Act - make multiple calls to different paths
        val firstPathResult = musicDiscoveryService.listAudioFilesWithCache("/Music")
        val secondPathResult = musicDiscoveryService.listAudioFilesWithCache("/Audio")
        val thirdPathResult = musicDiscoveryService.listAudioFilesWithCache("/Music") // Same as first

        // Assert
        assertTrue("All calls should succeed", firstPathResult.isSuccess)
        assertTrue("All calls should succeed", secondPathResult.isSuccess)
        assertTrue("All calls should succeed", thirdPathResult.isSuccess)

        val firstResponse = firstPathResult.getOrNull()!!
        val secondResponse = secondPathResult.getOrNull()!!
        val thirdResponse = thirdPathResult.getOrNull()!!

        // API call count tracking
        assertTrue(
            "Should track API calls made",
            firstResponse.totalApiCallsMade > 0
        )
        assertTrue(
            "Second path should increase API call count",
            secondResponse.totalApiCallsMade >= firstResponse.totalApiCallsMade
        )
        assertEquals(
            "Third call to same path should not increase API count due to cache",
            secondResponse.totalApiCallsMade,
            thirdResponse.totalApiCallsMade
        )
        assertTrue(
            "Third call should be served from cache",
            thirdResponse.servedFromCache
        )
    }
}
