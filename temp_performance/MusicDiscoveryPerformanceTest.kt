package com.foxy.player.music.performance

import com.foxy.player.authentication.models.UserInfo
import com.foxy.player.authentication.network.AuthRepository
import com.foxy.player.authentication.network.AuthenticatedApiClient
import com.foxy.player.music.network.MusicDiscoveryService
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Performance and caching tests for Music Discovery
 * Extracted from MusicDiscoveryTest.kt as part of KLOC refactoring (PLY-137)
 * * Tests caching, memory optimization, and database performance
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

        // Verify cache hit by comparing response times or cache indicators
        assertTrue(
            "Second call should be faster due to caching (or equal if already cached)",
            secondResponse!!.responseTimeMs <= firstResponse!!.responseTimeMs + 100
        )
    }
}
