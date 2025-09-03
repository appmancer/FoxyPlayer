package com.foxy.player.music

import com.foxy.player.music.entities.AlbumEntity
import com.foxy.player.music.entities.AlbumWithTracks
import com.foxy.player.music.entities.EnhancedTrackEntity
import com.foxy.player.music.entities.TrackWithAlbum
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Test for Room relationship models with @Relation annotations
 * PLY-129: Enhanced Album Data Models
 *
 * Tests cover Room relationship queries for albums with tracks and tracks with albums.
 */
class AlbumTrackRelationshipQueryTest {

    @Test
    fun `should create AlbumWithTracks relationship model for Room queries`() {
        // Arrange - prepare test data for album with tracks relationship
        val albumEntity = AlbumEntity(
            id = "album_queen_001",
            title = "A Night at the Opera",
            artist = "Queen"
        )

        val tracks = listOf(
            EnhancedTrackEntity(
                id = "track_001",
                title = "Bohemian Rhapsody",
                artist = "Queen",
                albumId = "album_queen_001",
                filePath = "/music/Queen/A Night at the Opera/01 - Bohemian Rhapsody.mp3",
                durationMs = 354000L,
                lastModified = System.currentTimeMillis()
            ),
            EnhancedTrackEntity(
                id = "track_002", title = "You're My Best Friend",
                artist = "Queen",
                albumId = "album_queen_001",
                filePath = "/music/Queen/A Night at the Opera/02 - You're My Best Friend.mp3",
                durationMs = 172000L,
                lastModified = System.currentTimeMillis()
            )
        )

        // Act - create album with tracks relationship model
        val albumWithTracks = AlbumWithTracks(
            album = albumEntity,
            tracks = tracks
        )

        // Assert - verify Room relationship model is properly created
        assertEquals("Album should match", albumEntity, albumWithTracks.album)
        assertEquals("Track count should match", 2, albumWithTracks.tracks.size)
        assertTrue(
            "All tracks should belong to album", albumWithTracks.tracks.all { it.albumId == albumEntity.id }
        )
        assertEquals(
            "First track should be Bohemian Rhapsody", "Bohemian Rhapsody",
            albumWithTracks.tracks[0].title
        )
    }

    @Test
    fun `should create TrackWithAlbum relationship model for Room queries`() {
        // Arrange - prepare test data for track with album relationship
        val albumEntity = AlbumEntity(
            id = "album_queen_002",
            title = "News of the World", artist = "Queen"
        )

        val trackEntity = EnhancedTrackEntity(
            id = "track_003",
            title = "We Will Rock You",
            artist = "Queen",
            albumId = "album_queen_002",
            filePath = "/music/Queen/News of the World/01 - We Will Rock You.mp3",
            durationMs = 122000L,
            lastModified = System.currentTimeMillis()
        )

        // Act - create track with album relationship model
        val trackWithAlbum = TrackWithAlbum(
            track = trackEntity,
            album = albumEntity
        )

        // Assert - verify Room relationship model is properly created
        assertEquals("Track should match", trackEntity, trackWithAlbum.track)
        assertEquals("Album should match", albumEntity, trackWithAlbum.album)
        assertEquals(
            "Track album ID should match album ID", albumEntity.id,
            trackWithAlbum.track.albumId
        )
        assertEquals(
            "Album and track artists should match", trackWithAlbum.album.artist,
            trackWithAlbum.track.artist
        )
    }

    @Test
    fun `should validate album-track relationship consistency`() {
        // Arrange - create album with tracks for validation
        val albumEntity = AlbumEntity(
            id = "album_validation_001",
            title = "Test Album",
            artist = "Test Artist"
        )

        val validTrack = EnhancedTrackEntity(
            id = "track_valid",
            title = "Valid Track",
            artist = "Test Artist",
            albumId = "album_validation_001",
            filePath = "/valid/path.mp3",
            durationMs = 180000L,
            lastModified = System.currentTimeMillis()
        )

        val invalidTrack = EnhancedTrackEntity(
            id = "track_invalid",
            title = "Invalid Track", artist = "Test Artist",
            albumId = "different_album_id", // Different album ID
            filePath = "/invalid/path.mp3",
            durationMs = 200000L,
            lastModified = System.currentTimeMillis()
        )

        val albumWithTracks = AlbumWithTracks(
            album = albumEntity,
            tracks = listOf(validTrack, invalidTrack)
        )

        // Act & Assert - validate relationship consistency
        assertTrue("Album with tracks should have tracks", albumWithTracks.hasValidTracks())
        assertEquals(
            "Should have validation errors for mismatched album IDs", 1,
            albumWithTracks.getValidationErrors().size
        )
        assertTrue(
            "Should identify invalid track relationships",
            albumWithTracks.getValidationErrors().contains("Track track_invalid has mismatched album ID")
        )
    }
}
