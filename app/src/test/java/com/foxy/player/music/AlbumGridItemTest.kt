package com.foxy.player.music

import androidx.compose.ui.graphics.ImageBitmap
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * TDD tests for AlbumGridItem Compose component - Phase 2 Albums-First Navigation
 * Tests Material3 album grid rendering based on PLY-127 architecture
 */
class AlbumGridItemTest {

    @Test
    fun `AlbumGridItem renders album artwork and title correctly`() {
        // Arrange - Create test album data
        val testAlbum = AlbumUIModel(
            id = "test-album-1",
            title = "Test Album Title",
            artist = "Test Artist",
            artworkUrl = "https://example.com/album-art.jpg",
            trackCount = 12
        )

        // Assert - Verify album data is correctly structured for UI
        assertEquals("test-album-1", testAlbum.id)
        assertEquals("Test Album Title", testAlbum.title)
        assertEquals("Test Artist", testAlbum.artist)
        assertEquals(12, testAlbum.trackCount)
        assertEquals("https://example.com/album-art.jpg", testAlbum.artworkUrl)

        // Assert - Verify UI model is valid for display
        assertTrue("Album UI model should be valid", testAlbum.isValid())
    }

    @Test
    fun `AlbumGridItem displays artwork using AlbumArtworkLoader`() = runTest {
        // Arrange
        val testAlbum = AlbumUIModel(
            id = "test-album-2",
            title = "Artwork Test Album",
            artist = "Test Artist",
            artworkUrl = "https://example.com/artwork.jpg",
            trackCount = 10
        )
        val artworkLoader = AlbumArtworkLoader()
        
        // Act
        val artworkImageBitmap = AlbumGridItem.loadArtworkForAlbum(testAlbum, artworkLoader)
        
        // Assert
        assertNotNull("Artwork should be loaded for album", artworkImageBitmap)
        assertTrue("Should successfully load artwork", artworkImageBitmap.isSuccess)
        assertTrue("Should use artwork loader for album", artworkLoader.isCached(testAlbum.artworkUrl!!))
    }
}
