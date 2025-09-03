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
}
