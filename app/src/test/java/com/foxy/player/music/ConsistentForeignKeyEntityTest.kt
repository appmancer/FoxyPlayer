package com.foxy.player.music

import com.foxy.player.music.entities.AlbumEntity
import com.foxy.player.music.entities.EnhancedAlbumTrackEntity
import com.foxy.player.music.entities.EnhancedTrackEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Test for consistent foreign key references using enhanced entities
 * PLY-129: Enhanced Album Data Models - Addressing PR review feedback
 *
 * Tests ensure foreign key consistency between enhanced entities.
 */
class ConsistentForeignKeyEntityTest {

    @Test
    fun `should create EnhancedAlbumTrackEntity with consistent foreign key references to enhanced entities`() {
        // Arrange - prepare test data using enhanced entities consistently
        val albumEntity = AlbumEntity(
            id = "album_consistency_001",
            title = "Consistency Test Album",
            artist = "Test Artist"
        )

        val enhancedTrackEntity = EnhancedTrackEntity(
            id = "enhanced_track_001",
            title = "Consistent Track",
            artist = "Test Artist",
            albumId = "album_consistency_001",
            filePath = "/music/TestArtist/ConsistencyTest/01-ConsistentTrack.mp3",
            durationMs = 180000L,
            lastModified = System.currentTimeMillis()
        )

        // Act - create enhanced junction table entity using enhanced track entity
        val enhancedJunctionEntity = EnhancedAlbumTrackEntity(
            albumId = albumEntity.id,
            enhancedTrackId = enhancedTrackEntity.id, // Uses enhanced track ID
            trackOrder = 1
        )

        // Assert - verify enhanced junction table references enhanced entities consistently
        assertEquals("Album ID should match", albumEntity.id, enhancedJunctionEntity.albumId)
        assertEquals("Enhanced track ID should match", enhancedTrackEntity.id, enhancedJunctionEntity.enhancedTrackId)
        assertEquals("Track order should be valid", 1, enhancedJunctionEntity.trackOrder)
        assertTrue("Junction entity should be valid", enhancedJunctionEntity.isValid())
    }

    @Test
    fun `should validate enhanced junction table prevents foreign key mismatches`() {
        // Arrange - create entities with mismatched references
        val albumId = "album_002"
        val enhancedTrackId = "enhanced_track_002"

        val validJunction = EnhancedAlbumTrackEntity(
            albumId = albumId,
            enhancedTrackId = enhancedTrackId,
            trackOrder = 1
        )

        val invalidJunction = EnhancedAlbumTrackEntity(
            albumId = "", // Invalid album ID
            enhancedTrackId = enhancedTrackId,
            trackOrder = 0 // Invalid track order
        )

        // Act & Assert - validate foreign key constraints
        assertTrue("Valid junction should pass validation", validJunction.isValid())
        assertTrue("Invalid junction should fail validation", !invalidJunction.isValid())
        assertEquals("Album ID validation should work", albumId, validJunction.albumId)
        assertEquals("Enhanced track ID validation should work", enhancedTrackId, validJunction.enhancedTrackId)
    }

    @Test
    fun `should ensure enhanced album-track relationships preserve ordering through junction table`() {
        // Arrange - create album with multiple enhanced tracks through junction table
        val albumId = "album_ordering_001"
        val tracks = listOf(
            EnhancedTrackEntity(
                id = "track_001",
                title = "First Track",
                artist = "Artist",
                albumId = albumId,
                filePath = "/path1.mp3",
                durationMs = 180000L,
                lastModified = System.currentTimeMillis()
            ),
            EnhancedTrackEntity(
                id = "track_002",
                title = "Second Track",
                artist = "Artist",
                albumId = albumId,
                filePath = "/path2.mp3",
                durationMs = 200000L,
                lastModified = System.currentTimeMillis()
            )
        )

        val junctionEntries = tracks.mapIndexed { index, track ->
            EnhancedAlbumTrackEntity(
                albumId = albumId,
                enhancedTrackId = track.id,
                trackOrder = index + 1
            )
        }

        // Act & Assert - verify ordering preservation through junction table
        assertEquals("Should have correct number of junction entries", 2, junctionEntries.size)
        assertEquals("First track should have order 1", 1, junctionEntries[0].trackOrder)
        assertEquals("Second track should have order 2", 2, junctionEntries[1].trackOrder)
        assertTrue("All junction entries should be valid", junctionEntries.all { it.isValid() })
        assertTrue(
            "All junction entries should reference correct album",
            junctionEntries.all { it.albumId == albumId }
        )
    }
}
