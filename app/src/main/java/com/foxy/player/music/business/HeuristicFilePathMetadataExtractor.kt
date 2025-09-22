package com.foxy.player.music.business

import com.foxy.player.music.entities.AlbumTrackEntity
import com.foxy.player.music.entities.EnhancedAlbumEntity
import com.foxy.player.music.entities.EnhancedTrackEntity
import java.util.UUID

/**
 * Result data class for heuristic file path metadata extraction
 * Contains all Room entities and confidence score for the extraction
 */
data class HeuristicFilePathExtractionResult(
    val trackEntity: EnhancedTrackEntity,
    val albumEntity: EnhancedAlbumEntity,
    val albumTrackEntity: AlbumTrackEntity,
    val confidenceScore: Double
)

/**
 * PLY-122: Heuristic File Path Metadata Extractor
 * Extracts metadata from file paths and creates Room database entities
 */
class HeuristicFilePathMetadataExtractor {

    /**
     * Extracts metadata from file path and creates Room entities
     * @param filePath The full file path to extract metadata from
     * @return Result with extracted metadata and Room entities
     */
    suspend fun extractMetadataFromFilePath(filePath: String): Result<HeuristicFilePathExtractionResult> {
        return try {
            val (artist, album, title) = parsePathForMetadata(filePath)
            val currentTime = System.currentTimeMillis()

            // Generate unique IDs
            val trackId = UUID.randomUUID().toString()
            val albumId = UUID.randomUUID().toString()

            // Create enhanced track entity
            val trackEntity = EnhancedTrackEntity(
                id = trackId,
                title = title,
                artist = artist,
                albumId = albumId,
                filePath = filePath,
                durationMs = 0L, // Default for heuristic extraction
                lastModified = currentTime
            )

            // Create enhanced album entity
            val albumPath = extractAlbumPath(filePath)
            val albumEntity = EnhancedAlbumEntity(
                id = albumId,
                title = album,
                artist = artist,
                path = albumPath,
                lastModified = currentTime
            )

            // Create album-track junction entity
            val albumTrackEntity = AlbumTrackEntity(
                albumId = albumId,
                trackId = trackId,
                trackOrder = 1 // Default track order for heuristic extraction
            )

            // Calculate confidence score based on metadata quality
            val confidenceScore = calculateConfidenceScore(artist, album, title)

            val result = HeuristicFilePathExtractionResult(
                trackEntity = trackEntity,
                albumEntity = albumEntity,
                albumTrackEntity = albumTrackEntity,
                confidenceScore = confidenceScore
            )

            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Parses file path for artist, album, and title metadata
     * Based on typical music folder structure: /Artist/Album/Track.ext
     */
    private fun parsePathForMetadata(filePath: String): Triple<String, String, String> {
        val parts = filePath.trim('/').split('/')
        val (artist, album) = when {
            parts.size >= 3 -> parts[parts.size - 3] to parts[parts.size - 2]
            parts.size >= 2 -> parts[parts.size - 2] to "Unknown Album"
            else -> "Unknown Artist" to "Unknown Album"
        }
        val fileName = parts.lastOrNull() ?: "Unknown"
        val title = fileName.substringBeforeLast('.')

        return Triple(artist, album, title)
    }

    /**
     * Extracts album directory path from file path
     */
    private fun extractAlbumPath(filePath: String): String {
        val parts = filePath.trim('/').split('/')
        return when {
            parts.size >= 3 -> "/" + parts.dropLast(1).joinToString("/")
            parts.size >= 2 -> "/" + parts.dropLast(1).joinToString("/")
            else -> "/"
        }
    }

    /**
     * Calculates confidence score based on metadata quality
     * Higher score for well-structured paths with identifiable components
     */
    private fun calculateConfidenceScore(artist: String, album: String, title: String): Double {
        var score = 0.0

        // Artist confidence
        if (artist.isNotBlank() && artist != "Unknown Artist") score += 0.3

        // Album confidence
        if (album.isNotBlank() && album != "Unknown Album") score += 0.3

        // Title confidence
        if (title.isNotBlank() && title != "Unknown") score += 0.4

        return score
    }

    /**
     * Persists extracted metadata through repository pattern with transactional support
     * @param extractionResult The result of metadata extraction
     * @param repository The repository to persist through
     * @return Result indicating success/failure of persistence operation
     */
    suspend fun persistMetadataWithRepository(
        extractionResult: HeuristicFilePathExtractionResult,
        repository: MetadataRepository
    ): Result<MetadataPersistenceResult> {
        return repository.persistMetadataTransaction(
            extractionResult.albumEntity,
            extractionResult.trackEntity,
            extractionResult.albumTrackEntity
        )
    }
}
