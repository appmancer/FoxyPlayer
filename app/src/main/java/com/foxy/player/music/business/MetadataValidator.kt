package com.foxy.player.music.business

import com.foxy.player.music.entities.AudioMetadata
import com.foxy.player.music.entities.MetadataValidationResult

/**
 * Validates audio metadata quality and provides confidence scoring.
 * Implements comprehensive validation logic for metadata completeness and consistency.
 */
class MetadataValidator {

    /**
     * Validates audio metadata and provides comprehensive quality assessment.
     * @param metadata The audio metadata to validate
     * @return MetadataValidationResult with validation results and confidence score
     */
    fun validateMetadata(metadata: AudioMetadata): MetadataValidationResult {
        val validationErrors = mutableListOf<String>()
        var confidenceScore = 0.0

        // Validate title
        if (metadata.title.isBlank() || metadata.title == "Unknown") {
            validationErrors.add("title is missing or unknown")
        } else {
            confidenceScore += 0.3
        }

        // Validate artist
        if (metadata.artist.isBlank() || metadata.artist == "Unknown Artist") {
            validationErrors.add("artist is missing or unknown")
        } else {
            confidenceScore += 0.3
        }

        // Validate album
        if (metadata.album.isBlank() || metadata.album == "Unknown Album") {
            validationErrors.add("album is missing or unknown")
        } else {
            confidenceScore += 0.2
        }

        // Validate duration
        if (metadata.durationMs < 0) {
            validationErrors.add("duration cannot be negative")
        } else if (metadata.durationMs > 0) {
            confidenceScore += 0.1
        }

        // Validate bitrate
        if (metadata.bitrate < 0) {
            validationErrors.add("bitrate cannot be negative")
        } else if (metadata.bitrate > 0) {
            confidenceScore += 0.1
        }

        // Validate track number
        if (metadata.trackNumber != null && metadata.trackNumber <= 0) {
            validationErrors.add("Track number must be greater than 0")
        }

        // Determine if metadata has required fields
        val hasRequiredFields = metadata.title.isNotBlank() && 
                               metadata.title != "Unknown" &&
                               metadata.artist.isNotBlank() && 
                               metadata.artist != "Unknown Artist"

        // Determine if metadata is high quality
        val isHighQuality = confidenceScore >= 0.8

        // Determine overall validity
        val isValid = validationErrors.isEmpty()

        return MetadataValidationResult(
            isValid = isValid,
            confidenceScore = confidenceScore,
            validationErrors = validationErrors,
            hasRequiredFields = hasRequiredFields,
            isHighQuality = isHighQuality
        )
    }
}