package com.foxy.player.music

import com.foxy.player.music.business.MetadataValidator
import com.foxy.player.music.entities.AudioMetadata
import com.foxy.player.music.entities.MetadataValidationResult
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
        assertTrue("Should detect empty title error", 
            result.validationErrors.any { it.contains("title") })
        assertTrue("Should detect invalid duration error",
            result.validationErrors.any { it.contains("duration") })
        assertTrue("Should detect invalid bitrate error", 
            result.validationErrors.any { it.contains("bitrate") })
    }
}