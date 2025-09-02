package com.foxy.player.music.metadata

import com.foxy.player.authentication.models.UserInfo
import com.foxy.player.authentication.network.AuthRepository
import com.foxy.player.authentication.network.AuthenticatedApiClient
import com.foxy.player.music.network.MusicDiscoveryService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Metadata extraction tests for Music Discovery
 * Extracted from MusicDiscoveryTest.kt as part of KLOC refactoring (PLY-137)
 * * Tests audio file metadata extraction and processing
 */
class MusicDiscoveryMetadataTest {

    @Test
    fun `should extract basic metadata from MP3 file`() {
        // Arrange - setup test data with authenticated state and metadata extraction service
        val authRepository = AuthRepository("https://eapi.pcloud.com")
        authRepository.saveAuthenticationState("test_auth_token", UserInfo("test@example.com"))
        val authenticatedApiClient = AuthenticatedApiClient(authRepository)
        val musicDiscoveryService = MusicDiscoveryService(authenticatedApiClient)

        // Act - call the metadata extraction method
        val result = musicDiscoveryService.extractBasicMetadata("/test/path/song.mp3")

        // Assert - verify metadata extraction functionality
        assertTrue("Should return successful result with metadata", result.isSuccess)
        val metadataResponse = result.getOrNull()
        assertNotNull("Metadata response should not be null", metadataResponse)
        assertTrue(
            "Should extract title from MP3 file",
            metadataResponse!!.title.isNotEmpty()
        )
        assertTrue(
            "Should extract artist from MP3 file",
            metadataResponse.artist.isNotEmpty()
        )
        assertEquals(
            "Should identify MP3 format correctly",
            "mp3",
            metadataResponse.format
        )
    }
}
