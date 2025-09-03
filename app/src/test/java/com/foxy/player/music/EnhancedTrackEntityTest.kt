package com.foxy.player.music

import com.foxy.player.music.entities.EnhancedTrackEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Test for enhanced TrackEntity with album relationship fields
 * PLY-129: Enhanced Album Data Models
 *
 * Tests cover enhanced track entity with album relationships, foreign keys, and validation.
 */
class EnhancedTrackEntityTest {

    @Test
    fun `should create EnhancedTrackEntity with album relationship fields for Room integration`() {
        // Arrange - prepare test data for enhanced track with album relationship
        val trackId = "track_enhanced_001"
        val title = "Bohemian Rhapsody"
        val artist = "Queen"
        val albumId = "album_001"
        val filePath = "/music/Queen/A Night at the Opera/01 - Bohemian Rhapsody.mp3"
        val durationMs = 354000L
        val lastModified = System.currentTimeMillis() - 86400000L // 1 day ago

        // Act - create enhanced track entity with album relationship
        val enhancedTrackEntity = EnhancedTrackEntity(
            id = trackId,
            title = title,
            artist = artist,
            albumId = albumId,
            filePath = filePath,
            durationMs = durationMs,
            lastModified = lastModified
        )

        // Assert - verify enhanced track entity is properly created with album relationship
        assertEquals("Track ID should match", trackId, enhancedTrackEntity.id)
        assertEquals("Track title should match", title, enhancedTrackEntity.title)
        assertEquals("Track artist should match", artist, enhancedTrackEntity.artist)
        assertEquals("Album ID foreign key should match", albumId, enhancedTrackEntity.albumId)
        assertEquals("File path should match", filePath, enhancedTrackEntity.filePath)
        assertEquals("Duration should match", durationMs, enhancedTrackEntity.durationMs)
        assertEquals("Last modified should match", lastModified, enhancedTrackEntity.lastModified)
    }

    @Test
    fun `should validate enhanced track entity with proper album relationship constraints`() {
        // Arrange - create enhanced track entity with required album relationship
        val validTrackEntity = EnhancedTrackEntity(
            id = "track_002",
            title = "We Will Rock You",
            artist = "Queen",
            albumId = "album_001",
            filePath = "/music/Queen/News of the World/02 - We Will Rock You.mp3",
            durationMs = 122000L,
            lastModified = System.currentTimeMillis() - 3600000L // 1 hour ago
        )

        // Act & Assert - validate enhanced track entity
        assertTrue("Enhanced track should be valid", validTrackEntity.isValid())
        assertTrue("Should have album relationship", validTrackEntity.hasAlbumRelationship())
        assertEquals(
            "Display name should include artist and title",
            "We Will Rock You by Queen",
            validTrackEntity.getDisplayName()
        )
    }

    @Test
    fun `should handle invalid enhanced track entity with missing album relationship`() {
        // Arrange - create track entities with invalid album relationships
        val missingAlbumId = EnhancedTrackEntity(
            id = "track_003",
            title = "Test Track",
            artist = "Test Artist",
            albumId = "", // Invalid: empty album ID
            filePath = "/valid/path.mp3",
            durationMs = 180000L,
            lastModified = System.currentTimeMillis()
        )

        val invalidFilePath = EnhancedTrackEntity(
            id = "track_004",
            title = "Another Track",
            artist = "Another Artist",
            albumId = "album_002",
            filePath = "invalid_path", // Invalid: doesn't start with /
            durationMs = 200000L,
            lastModified = System.currentTimeMillis()
        )

        // Act & Assert - validate invalid enhanced track entities
        assertFalse("Track with empty album ID should be invalid", missingAlbumId.isValid())
        assertFalse("Track should not have album relationship", missingAlbumId.hasAlbumRelationship())
        assertFalse("Track with invalid path should be invalid", invalidFilePath.isValid())
    }
}
