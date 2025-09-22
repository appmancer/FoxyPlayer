package com.foxy.player.music

import com.foxy.player.music.business.HeuristicFilePathMetadataExtractor
import com.foxy.player.music.business.MetadataRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Test for PLY-122: Heuristic File Path Metadata Extraction
 * Tests the enhanced heuristic file path extraction with Room database integration
 */
class HeuristicFilePathMetadataExtractionTest {

    @Test
    fun `heuristic_file_path_metadata_extraction_should_create_track_and_album_entities_from_file_path_structure`() = runBlocking {
        // Arrange - setup heuristic file path metadata extractor
        val heuristicExtractor = HeuristicFilePathMetadataExtractor()

        // Test file path with typical music folder structure: /Artist/Album/Track.mp3
        val filePath = "/Music/The Beatles/Abbey Road/01 - Come Together.mp3"
        val expectedArtist = "The Beatles"
        val expectedAlbum = "Abbey Road"
        val expectedTitle = "01 - Come Together"

        // Act - extract metadata from file path and create Room entities
        val result = heuristicExtractor.extractMetadataFromFilePath(filePath)

        // Assert - verify successful extraction and Room entity creation
        assertTrue("Should return successful result with heuristic extraction", result.isSuccess)
        val extractionResult = result.getOrNull()
        assertNotNull("Extraction result should not be null", extractionResult)

        // Verify enhanced track entity creation
        assertNotNull("Should create enhanced track entity", extractionResult!!.trackEntity)
        assertEquals("Track title should match file name", expectedTitle, extractionResult.trackEntity.title)
        assertEquals("Track artist should match folder structure", expectedArtist, extractionResult.trackEntity.artist)
        assertEquals("Track file path should match input", filePath, extractionResult.trackEntity.filePath)

        // Verify enhanced album entity creation
        assertNotNull("Should create enhanced album entity", extractionResult.albumEntity)
        assertEquals("Album title should match folder name", expectedAlbum, extractionResult.albumEntity.title)
        assertEquals("Album artist should match folder structure", expectedArtist, extractionResult.albumEntity.artist)
        assertEquals(
            "Album path should be derived from file path",
            "/Music/The Beatles/Abbey Road",
            extractionResult.albumEntity.path
        )

        // Verify album-track junction entity creation
        assertNotNull("Should create album-track junction entity", extractionResult.albumTrackEntity)
        assertEquals(
            "Junction album ID should match album entity",
            extractionResult.albumEntity.id,
            extractionResult.albumTrackEntity.albumId
        )
        assertEquals(
            "Junction track ID should match track entity",
            extractionResult.trackEntity.id,
            extractionResult.albumTrackEntity.trackId
        )

        // Verify confidence scoring for heuristic extraction
        assertTrue(
            "Confidence score should be between 0.0 and 1.0",
            extractionResult.confidenceScore >= 0.0 && extractionResult.confidenceScore <= 1.0
        )
        assertTrue(
            "Heuristic extraction should have reasonable confidence (> 0.3)",
            extractionResult.confidenceScore > 0.3
        )
    }

    @Test
    fun `heuristic_extraction_should_handle_different_path_structures_gracefully`() = runBlocking {
        // Arrange
        val heuristicExtractor = HeuristicFilePathMetadataExtractor()

        // Test various path structures
        val testCases = listOf(
            "/Artist/Song.mp3" to Triple("Artist", "Unknown Album", "Song"),
            "/Song.mp3" to Triple("Unknown Artist", "Unknown Album", "Song"),
            "/Artist/Album/02-Track Name.flac" to Triple("Artist", "Album", "02-Track Name"),
            "/Music/Various Artists/Compilation/03. Song Title.m4a" to Triple(
                "Various Artists",
                "Compilation",
                "03. Song Title"
            )
        )

        // Act & Assert
        testCases.forEach { (filePath, expected) ->
            val result = heuristicExtractor.extractMetadataFromFilePath(filePath)
            assertTrue("Should handle path structure: $filePath", result.isSuccess)

            val extractionResult = result.getOrNull()!!
            assertEquals("Artist should match for path: $filePath", expected.first, extractionResult.trackEntity.artist)
            assertEquals("Title should match for path: $filePath", expected.third, extractionResult.trackEntity.title)
            assertEquals(
                "Album title should match for path: $filePath",
                expected.second,
                extractionResult.albumEntity.title
            )
        }
    }

    @Test
    fun `heuristic_metadata_extractor_should_persist_entities_through_repository_pattern_with_transactional_support`() = runBlocking {
        // Arrange - setup heuristic extractor with repository integration
        val mockRepository = MetadataRepository()
        val heuristicExtractor = HeuristicFilePathMetadataExtractor()

        val filePath = "/Music/Pink Floyd/The Wall/01 - Another Brick in the Wall.mp3"

        // Act - extract metadata and persist through repository pattern
        val extractionResult = heuristicExtractor.extractMetadataFromFilePath(filePath).getOrThrow()
        val persistResult = heuristicExtractor.persistMetadataWithRepository(extractionResult, mockRepository)

        // Assert - verify transactional persistence through repository
        assertTrue("Should successfully persist through repository", persistResult.isSuccess)
        val persistedData = persistResult.getOrNull()
        assertNotNull("Persisted data should not be null", persistedData)

        // Verify repository received all entities in single transaction
        assertTrue("Repository should persist album entity", persistedData!!.albumPersisted)
        assertTrue("Repository should persist track entity", persistedData.trackPersisted)
        assertTrue("Repository should persist album-track junction", persistedData.junctionPersisted)
        assertTrue("Repository should execute in single transaction", persistedData.transactional)

        // Verify data integrity after persistence
        assertEquals(
            "Persisted album ID should match extraction",
            extractionResult.albumEntity.id,
            persistedData.persistedAlbumId
        )
        assertEquals(
            "Persisted track ID should match extraction",
            extractionResult.trackEntity.id,
            persistedData.persistedTrackId
        )
    }
}
