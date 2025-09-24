package com.foxy.player.music

import com.foxy.player.music.business.MetadataRepository
import com.foxy.player.music.business.MetadataValidator
import com.foxy.player.music.business.MusicMetadataExtractor
import com.foxy.player.music.entities.AudioMetadata
import com.foxy.player.music.entities.ValidatedMetadataResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Test suite for metadata validation framework and quality assessment.
 * Validates metadata completeness, consistency, and confidence scoring.
 */
class MetadataValidationTest {

    @Test
    fun `should validate complete metadata with high confidence score`() {
        // Arrange - setup complete metadata with all fields
        val completeMetadata = AudioMetadata(
            title = "Bohemian Rhapsody",
            artist = "Queen",
            album = "A Night at the Opera",
            durationMs = 355000L,
            format = "MP3",
            bitrate = 320,
            trackNumber = 11
        )
        val validator = MetadataValidator()

        // Act - validate the metadata
        val result = validator.validateMetadata(completeMetadata)

        // Assert - verify high confidence score for complete metadata
        assertNotNull("Validation result should not be null", result)
        assertTrue("Complete metadata should be valid", result.isValid)
        assertTrue("Confidence score should be high for complete metadata", result.confidenceScore >= 0.8)
        assertEquals("Should have no validation errors for complete metadata", 0, result.validationErrors.size)
        assertTrue("Should have all required fields", result.hasRequiredFields)
        assertTrue("Should have high quality metadata", result.isHighQuality)
    }

    @Test
    fun `should identify incomplete metadata with appropriate confidence score`() {
        // Arrange - setup incomplete metadata missing key fields
        val incompleteMetadata = AudioMetadata(
            title = "Unknown",
            artist = "Unknown Artist",
            album = "Unknown Album",
            durationMs = 0L,
            format = "MP3",
            bitrate = 0,
            trackNumber = null
        )
        val validator = MetadataValidator()

        // Act - validate the incomplete metadata
        val result = validator.validateMetadata(incompleteMetadata)

        // Assert - verify low confidence score for incomplete metadata
        assertNotNull("Validation result should not be null", result)
        assertFalse("Incomplete metadata should not be considered high quality", result.isHighQuality)
        assertTrue("Confidence score should be low for incomplete metadata", result.confidenceScore < 0.4)
        assertTrue("Should have validation errors for incomplete metadata", result.validationErrors.isNotEmpty())
        assertFalse("Should not have all required fields", result.hasRequiredFields)
    }

    @Test
    fun `should detect metadata inconsistencies and conflicts`() {
        // Arrange - setup metadata with inconsistent/conflicting information
        val inconsistentMetadata = AudioMetadata(
            title = "", // Empty title is inconsistent
            artist = "Queen",
            album = "A Night at the Opera",
            durationMs = -100L, // Negative duration is invalid
            format = "INVALID_FORMAT",
            bitrate = -1, // Negative bitrate is invalid
            trackNumber = 0 // Track number should start from 1
        )
        val validator = MetadataValidator()

        // Act - validate the inconsistent metadata
        val result = validator.validateMetadata(inconsistentMetadata)

        // Assert - verify detection of inconsistencies
        assertNotNull("Validation result should not be null", result)
        assertFalse("Inconsistent metadata should not be valid", result.isValid)
        assertTrue("Should detect multiple validation errors", result.validationErrors.size >= 3)
        assertTrue(
            "Should detect empty title error",
            result.validationErrors.any { it.contains("title") }
        )
        assertTrue(
            "Should detect invalid duration error",
            result.validationErrors.any { it.contains("duration") }
        )
        assertTrue(
            "Should detect invalid bitrate error",
            result.validationErrors.any { it.contains("bitrate") }
        )
    }

    @Test
    fun `should integrate validation with multi-strategy metadata extraction`() {
        // Arrange: Create real extractor
        val extractor = MusicMetadataExtractor()

        // Note: This test will fail until extractMetadataWithValidation is implemented
        // This is the expected RED state for TDD Cycle #2

        // Act: Extract metadata with validation integration
        val result = runBlocking {
            extractor.extractMetadataWithValidation(
                audioFileUrl = "https://sample.com/abbey_road/come_together.mp3",
                audioFileName = "come_together.mp3",
                filePath = "/music/The Beatles/Abbey Road/come_together.mp3"
            )
        }

        // Assert: Result includes both multi-strategy extraction AND validation results
        assertTrue("Integration should succeed", result.isSuccess)
        val validatedResult = result.getOrThrow()

        // Multi-strategy extraction results
        assertNotNull("Should have multi-strategy result", validatedResult.multiStrategyResult)
        assertTrue(
            "Should have good extraction confidence",
            validatedResult.multiStrategyResult.overallConfidence > 0.5
        )

        // Validation results integrated
        assertNotNull("Should have validation result", validatedResult.validationResult)
        assertTrue("Should not have validation errors", validatedResult.validationResult.validationErrors.isEmpty())
        assertTrue("Should have high validation confidence", validatedResult.validationResult.confidenceScore > 0.8)

        // Combined confidence score factoring both extraction and validation
        assertTrue("Should have high combined confidence", validatedResult.combinedConfidence > 0.7)
        assertEquals(
            "Should have quality assessment",
            "High quality metadata with validation passed",
            validatedResult.qualityAssessment
        )
    }

    @Test
    fun `should integrate validation with real multi-strategy extraction service`() {
        // Arrange: Create real extractor and mock service
        val extractor = MusicMetadataExtractor()

        // Note: This test will fail until extractMetadataWithRealMultiStrategy is implemented
        // This is the expected RED state for TDD Cycle #3

        // Act: Extract metadata using real multi-strategy extraction with validation
        val result = runBlocking {
            extractor.extractMetadataWithRealMultiStrategy(
                audioFileUrl = "https://sample.com/abbey_road/come_together.mp3",
                audioFileName = "come_together.mp3",
                filePath = "/music/The Beatles/Abbey Road/come_together.mp3"
            )
        }

        // Assert: Result uses real multi-strategy extraction AND validation
        assertTrue("Real integration should succeed", result.isSuccess)
        val validatedResult: ValidatedMetadataResult = result.getOrThrow()

        // Verify it's using real multi-strategy extraction (not test implementation)
        assertTrue("Should use real cross-validation", validatedResult.multiStrategyResult.usedCrossValidation)
        assertTrue("Should use real weighted averaging", validatedResult.multiStrategyResult.usedWeightedAveraging)
        assertTrue("Should have strategy results", validatedResult.multiStrategyResult.strategyResults.isNotEmpty())

        // Combined confidence should be calculated from real extraction and validation
        assertTrue("Should have realistic combined confidence", validatedResult.combinedConfidence > 0.0)
        assertNotNull("Should have quality assessment", validatedResult.qualityAssessment)
    }

    @Test
    fun `should persist validated metadata to Repository with confidence tracking`() {
        // Arrange: Create extractor and repository
        val extractor = MusicMetadataExtractor()
        val repository = MetadataRepository()

        // Note: This test will fail until extractAndPersistValidatedMetadata is implemented
        // This is the expected RED state for TDD Cycle #4

        // Act: Extract metadata with validation AND persist to repository
        val result = runBlocking {
            extractor.extractAndPersistValidatedMetadata(
                audioFileUrl = "https://sample.com/abbey_road/come_together.mp3",
                audioFileName = "come_together.mp3",
                filePath = "/music/The Beatles/Abbey Road/come_together.mp3",
                repository = repository
            )
        }

        // Assert: Result includes validation, extraction AND repository persistence
        assertTrue("Extraction and persistence should succeed", result.isSuccess)
        val validatedResult = result.getOrThrow()

        // Validation results
        assertNotNull("Should have validation result", validatedResult.validationResult)
        assertTrue("Should have high validation confidence", validatedResult.validationResult.confidenceScore > 0.8)

        // Persistence results
        assertNotNull("Should have persistence result", validatedResult.persistenceResult)
        assertTrue("Album should be persisted", validatedResult.persistenceResult!!.albumPersisted)
        assertTrue("Track should be persisted", validatedResult.persistenceResult!!.trackPersisted)
        assertTrue("Junction should be persisted", validatedResult.persistenceResult!!.junctionPersisted)

        // Combined confidence tracking preserved through persistence
        assertTrue("Should maintain combined confidence after persistence", validatedResult.combinedConfidence > 0.7)
        assertNotNull("Should have quality assessment including persistence status", validatedResult.qualityAssessment)
    }
}
