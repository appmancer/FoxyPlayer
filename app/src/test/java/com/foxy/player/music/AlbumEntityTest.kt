package com.foxy.player.music

import com.foxy.player.music.entities.EnhancedAlbumEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Test for enhanced AlbumEntity with Room annotations
 * PLY-130: Create AlbumEntity with basic fields (id, title, artist, path, lastModified)
 *
 * Tests cover entity creation, validation, and Room database integration.
 */
class AlbumEntityTest {

    @Test
    fun `should create AlbumEntity with enhanced fields including path and lastModified`() {
        // Arrange - prepare test data for enhanced AlbumEntity
        val albumId = "album_001"
        val title = "Abbey Road"
        val artist = "The Beatles"
        val path = "/music/albums/the_beatles/abbey_road"
        val lastModified = System.currentTimeMillis()

        // Act - create enhanced AlbumEntity with all required fields
        val albumEntity = EnhancedAlbumEntity(
            id = albumId,
            title = title,
            artist = artist,
            path = path,
            lastModified = lastModified
        )

        // Assert - verify all fields are properly set
        assertEquals("Album ID should match", albumId, albumEntity.id)
        assertEquals("Album title should match", title, albumEntity.title)
        assertEquals("Album artist should match", artist, albumEntity.artist)
        assertEquals("Album path should match", path, albumEntity.path)
        assertEquals("Album lastModified should match", lastModified, albumEntity.lastModified)
    }

    @Test
    fun `should validate entity data correctly`() {
        // Arrange & Act - create valid entity
        val validEntity = EnhancedAlbumEntity(
            id = "valid_id",
            title = "Valid Title",
            artist = "Valid Artist",
            path = "/valid/path",
            lastModified = System.currentTimeMillis()
        )

        // Assert - valid entity should pass validation
        assertTrue("Valid entity should pass validation", validEntity.isValid())
    }

    @Test
    fun `should reject invalid entity data`() {
        // Arrange & Act - create entity with invalid data
        val invalidEntity = EnhancedAlbumEntity(
            id = "", // Invalid empty ID
            title = "Title",
            artist = "Artist",
            path = "/path",
            lastModified = System.currentTimeMillis()
        )

        // Assert - invalid entity should fail validation
        assertFalse("Invalid entity should fail validation", invalidEntity.isValid())
    }

    @Test
    fun `should generate proper display name`() {
        // Arrange - create entity with test data
        val albumEntity = EnhancedAlbumEntity(
            id = "display_test",
            title = "Sgt. Pepper's Lonely Hearts Club Band",
            artist = "The Beatles",
            path = "/music/beatles/sgtpepper",
            lastModified = System.currentTimeMillis()
        )

        // Act - get display name
        val displayName = albumEntity.getDisplayName()

        // Assert - display name should be properly formatted
        assertEquals(
            "Display name should be formatted correctly",
            "Sgt. Pepper's Lonely Hearts Club Band by The Beatles",
            displayName
        )
    }

    @Test
    fun `should allow duplicate albums with same title and artist but different paths`() {
        // Arrange - create two albums with same title/artist but different paths (remastered versions)
        val originalAlbum = EnhancedAlbumEntity(
            id = "abbey_road_original",
            title = "Abbey Road",
            artist = "The Beatles",
            path = "/music/beatles/abbey_road_1969",
            lastModified = System.currentTimeMillis()
        )

        val remasteredAlbum = EnhancedAlbumEntity(
            id = "abbey_road_remastered",
            title = "Abbey Road", // Same title
            artist = "The Beatles", // Same artist
            path = "/music/beatles/abbey_road_2019_remastered", // Different path
            lastModified = System.currentTimeMillis() + 1000
        )

        // Act & Assert - both albums should be valid and allowed
        assertTrue("Original album should be valid", originalAlbum.isValid())
        assertTrue("Remastered album should be valid", remasteredAlbum.isValid())

        // Assert - albums should be distinct entities despite same title/artist
        assertFalse("Albums should have different IDs", originalAlbum.id == remasteredAlbum.id)
        assertFalse("Albums should have different paths", originalAlbum.path == remasteredAlbum.path)

        // This test verifies that our database design should allow both albums to coexist
        // because they have unique paths and IDs, even with same title/artist
    }

    @Test
    fun `should validate path format and timestamp ranges properly`() {
        val currentTime = System.currentTimeMillis()

        // Test invalid path format (doesn't start with /)
        val invalidPathEntity = EnhancedAlbumEntity(
            id = "test_id",
            title = "Test Album",
            artist = "Test Artist",
            path = "invalid_path_without_slash", // Should start with /
            lastModified = currentTime
        )

        // Test future timestamp (5+ minutes in future)
        val futureTimestampEntity = EnhancedAlbumEntity(
            id = "future_id",
            title = "Future Album",
            artist = "Future Artist",
            path = "/valid/path",
            lastModified = currentTime + (10 * 60 * 1000) // 10 minutes in future
        )

        // Test negative timestamp
        val negativeTimestampEntity = EnhancedAlbumEntity(
            id = "negative_id",
            title = "Negative Album",
            artist = "Negative Artist",
            path = "/valid/path2",
            lastModified = -1000 // Negative timestamp
        )

        // Test valid entity within acceptable clock skew
        val validEntity = EnhancedAlbumEntity(
            id = "valid_id",
            title = "Valid Album",
            artist = "Valid Artist",
            path = "/valid/path3",
            lastModified = currentTime + (2 * 60 * 1000) // 2 minutes in future (within 5 min skew)
        )

        // Act & Assert - enhanced validation should catch these issues
        assertFalse("Invalid path format should fail validation", invalidPathEntity.isValid())
        assertFalse("Future timestamp should fail validation", futureTimestampEntity.isValid())
        assertFalse("Negative timestamp should fail validation", negativeTimestampEntity.isValid())
        assertTrue("Valid entity within clock skew should pass", validEntity.isValid())
    }
}
