package com.foxy.player.music

import com.foxy.player.authentication.models.UserInfo
import com.foxy.player.authentication.network.AuthRepository
import com.foxy.player.authentication.network.AuthenticatedApiClient
import com.foxy.player.music.business.MusicMetadataExtractor
import com.foxy.player.music.network.MusicDiscoveryService
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EnhancedMetadataExtractionTest {

    @Test
    fun `Enhanced_metadata_extraction_with_album-track_relationship_persistence`() = runBlocking {
        // Arrange - setup enhanced metadata extractor with Room database integration
        val authRepository = AuthRepository("https://eapi.pcloud.com")
        authRepository.saveAuthenticationState("test_auth_token", UserInfo("test@example.com"))
        val authenticatedApiClient = AuthenticatedApiClient(authRepository)
        val musicDiscoveryService = MusicDiscoveryService(authenticatedApiClient)

        // Create enhanced metadata extractor that doesn't exist yet
        val enhancedExtractor = MusicMetadataExtractor()

        // Test audio file with comprehensive metadata
        val audioFileUrl = "https://filesamples.com/samples/audio/mp3/SampleAudio_0.4mb_mp3.mp3"
        val audioFileName = "Come Together.mp3"
        val filePath = "/Artists/The Beatles/Abbey Road/Come Together.mp3"

        // Act - call enhanced metadata extraction with Room persistence (method doesn't exist yet)
        val result = enhancedExtractor.extractMetadataWithRoomPersistence(
            audioFileUrl = audioFileUrl,
            audioFileName = audioFileName,
            filePath = filePath,
            musicDiscoveryService = musicDiscoveryService
        )

        // Assert - verify comprehensive metadata extraction and Room integration
        assertTrue("Should return successful result with enhanced extraction", result.isSuccess)
        val enhancedResult = result.getOrNull()
        assertNotNull("Enhanced result should not be null", enhancedResult)

        // Verify complete metadata extraction
        assertNotNull("Should extract enhanced track entity", enhancedResult!!.trackEntity)
        assertNotNull("Should extract album entity", enhancedResult.albumEntity)
        assertNotNull("Should create album-track junction", enhancedResult.albumTrackEntity)

        // Verify Room entity structure
        val trackEntity = enhancedResult.trackEntity
        val albumEntity = enhancedResult.albumEntity
        val junctionEntity = enhancedResult.albumTrackEntity

        // Verify track entity completeness
        assertTrue("Track should have valid ID", trackEntity.id.isNotBlank())
        assertEquals("Track title should match extracted metadata", "Come Together", trackEntity.title)
        assertEquals("Track artist should match extracted metadata", "The Beatles", trackEntity.artist)
        assertTrue("Track should have duration from metadata", trackEntity.durationMs > 0)
        assertEquals("Track file path should be preserved", filePath, trackEntity.filePath)
        assertTrue("Track should have valid album relationship", trackEntity.hasAlbumRelationship())

        // Verify album entity completeness
        assertTrue("Album should have valid ID", albumEntity.id.isNotBlank())
        assertEquals("Album title should match extracted metadata", "Abbey Road", albumEntity.title)
        assertEquals("Album artist should match extracted metadata", "The Beatles", albumEntity.artist)

        // Verify junction table relationship
        assertEquals("Junction should link correct album", albumEntity.id, junctionEntity.albumId)
        assertEquals("Junction should link correct track", trackEntity.id, junctionEntity.trackId)
        assertTrue("Junction should have valid track order", junctionEntity.trackOrder > 0)

        // Verify data integrity
        assertTrue("Track entity should pass validation", trackEntity.isValid())
        assertTrue("Album entity should pass validation", albumEntity.isValid())
        assertTrue("Junction entity should pass validation", junctionEntity.isValid())

        // Verify relationship consistency
        assertEquals("Track albumId should match album ID", albumEntity.id, trackEntity.albumId)
    }
}
