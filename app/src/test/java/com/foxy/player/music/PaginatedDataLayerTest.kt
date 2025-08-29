package com.foxy.player.music

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PaginatedDataLayerTest {

    @Test
    fun `should load tracks in pages instead of loading all tracks into memory`() = runBlocking {
        // Arrange - Create large dataset to simulate unlimited scale
        val testRepository = TestPaginatedTrackRepository()
        val totalTracks = 10000

        // Insert large number of tracks to test pagination
        repeat(totalTracks) { index ->
            testRepository.insertTrack(
                id = "track_$index",
                title = "Song $index",
                artist = "Artist $index", album = "Album $index",
                filePath = "/path/to/song_$index.mp3"
            )
        }

        // Create paginated data layer
        val paginatedDataLayer = PaginatedTrackDataLayer(testRepository)

        // Act - Load first page (should not load all tracks into memory)
        val pageSize = 50
        val firstPage = paginatedDataLayer.loadTracksPage(
            pageNumber = 0,
            pageSize = pageSize
        )

        // Assert - Verify pagination behavior
        assertEquals("Should return exactly page size tracks", pageSize, firstPage.tracks.size)
        assertTrue("Should indicate more pages available", firstPage.hasMorePages)
        assertEquals("Total count should be available", totalTracks, firstPage.totalCount)

        // Verify memory efficiency - should not load all tracks
        val memoryFootprint = paginatedDataLayer.getInMemoryTrackCount()
        assertTrue("Should keep minimal tracks in memory for pagination", memoryFootprint < 200)

        // Test viewport-based loading
        val viewportTracks = paginatedDataLayer.loadTracksForViewport(
            startIndex = 100,
            viewportSize = 20
        )
        assertEquals("Viewport should load exact requested range", 20, viewportTracks.size)
        assertEquals("First track should match start index", "Song 100", viewportTracks.first().title)
    }
}

// Test implementation for pagination
class TestPaginatedTrackRepository : TrackRepositoryInterface {
    private val tracks = mutableListOf<TrackEntity>()

    override fun isReady(): Boolean = true

    override suspend fun getAllTracks(): List<TrackEntity> = tracks.toList()

    override suspend fun insertTrack(id: String, title: String, artist: String, album: String, filePath: String) {
        tracks.add(TrackEntity(id, title, artist, album, filePath))
    }

    override suspend fun getTrackCount(): Int = tracks.size

    // New pagination methods needed
    override suspend fun getTracksPage(offset: Int, limit: Int): List<TrackEntity> {
        return tracks.drop(offset).take(limit)
    }

    override suspend fun getTracksForRange(startIndex: Int, count: Int): List<TrackEntity> {
        return tracks.drop(startIndex).take(count)
    }
}
