package com.foxy.player.music.entities

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

/**
 * Room entity representing a music track in the database.
 * This entity maps to the 'tracks' table and contains track metadata
 * for music files stored in pCloud.
 */
@Entity(tableName = "tracks")
data class TrackEntity(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val filePath: String
)

/**
 * Room entity representing an album in the database.
 * This entity maps to the 'albums' table and contains album metadata
 * for organizing music tracks by album.
 */
@Entity(tableName = "albums")
data class AlbumEntity(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String
)

/**
 * Enhanced Room entity for album with additional fields for PLY-130.
 * Includes path and lastModified fields for comprehensive album tracking.
 * * This entity extends the basic AlbumEntity with file system integration
 * for pCloud music library management. Performance indices are included
 * for efficient querying based on PLY-87 database architecture research.
 */
@Entity(
    tableName = "enhanced_albums",
    indices = [
        Index(value = ["title", "artist"]), // Query performance for title+artist (non-unique)
        Index(value = ["path"], unique = true), // Unique file paths
        Index(value = ["last_modified"]), // Recent albums query - use column name
        Index(value = ["artist"]) // Artist-based filtering
    ]
)
data class EnhancedAlbumEntity(
    /**
     * Unique identifier for the album.
     * Should use collision-free UUID generation in production.
     */
    @PrimaryKey val id: String,

    /**
     * Album title for display and search functionality.
     */
    @ColumnInfo(name = "title")
    val title: String,

    /**
     * Artist name associated with this album.
     * In normalized database, this would be a foreign key to ArtistEntity.
     */
    @ColumnInfo(name = "artist") val artist: String,

    /**
     * File system path where the album is stored in pCloud.
     * Must be unique to prevent duplicate album entries.
     */
    @ColumnInfo(name = "path")
    val path: String,

    /**
     * Last modification timestamp in milliseconds since epoch.
     * Used for sync operations and determining recent changes.
     */
    @ColumnInfo(name = "last_modified")
    val lastModified: Long
) {
    /**
     * Validates that the entity contains valid data.
     * @return true if all required fields are properly set
     */
    fun isValid(): Boolean {
        // Path must be non-blank and start with '/'
        val isPathValid = path.isNotBlank() && path.startsWith("/")
        // lastModified must be > 0 and not in the future (allow 5 min clock skew)
        val now = System.currentTimeMillis()
        val maxFutureSkewMs = 5 * 60 * 1000 // 5 minutes
        val isLastModifiedValid = lastModified > 0 && lastModified <= now + maxFutureSkewMs
        return id.isNotBlank() &&
            title.isNotBlank() &&
            artist.isNotBlank() &&
            isPathValid &&
            isLastModifiedValid
    }

    /**
     * Creates a display-friendly string representation.
     * @return formatted string for UI display
     */
    fun getDisplayName(): String = "$title by $artist"
}

/**
 * Audio metadata data class for track information.
 * Contains extracted metadata from audio files including
 * duration, format, and bitrate information.
 */
data class AudioMetadata(
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long, // Keep original field name for consistency
    val format: String,
    val bitrate: Int,
    val trackNumber: Int? = null // Track number from metadata, null if not available
)

/**
 * Audio file model for caching.
 * Represents a cached audio file with pCloud integration.
 */
data class AudioFile(
    val fileId: String,
    val fileName: String,
    val filePath: String,
    val fileSizeBytes: Long,
    val pCloudUrl: String
)

/**
 * Folder listing data class for directory contents.
 * Used for organizing music files in hierarchical structure.
 */
data class FolderListing(
    val folders: List<String> = emptyList(),
    val files: List<String> = emptyList()
)

/**
 * Junction table entity for album-track many-to-many relationships.
 * This entity maps to the 'album_tracks' table and represents
 * the relationship between albums and tracks with ordering information.
 * * Uses composite primary key (albumId, trackId) to ensure uniqueness
 * and includes foreign key constraints for data integrity.
 */
@Entity(
    tableName = "album_tracks",
    primaryKeys = ["album_id", "track_id"], // Use actual column names
    foreignKeys = [
        ForeignKey(
            entity = AlbumEntity::class,
            parentColumns = ["id"],
            childColumns = ["album_id"], // Use actual column name
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TrackEntity::class,
            parentColumns = ["id"],
            childColumns = ["track_id"], // Use actual column name
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["album_id"]), // Use actual column name
        Index(value = ["track_id"]), // Use actual column name
        Index(value = ["track_order"]) // Use actual column name
    ]
)
data class AlbumTrackEntity(
    /**
     * Foreign key reference to AlbumEntity.id.
     * Establishes the album side of the many-to-many relationship.
     */
    @ColumnInfo(name = "album_id")
    val albumId: String,

    /**
     * Foreign key reference to TrackEntity.id.
     * Establishes the track side of the many-to-many relationship.
     */
    @ColumnInfo(name = "track_id")
    val trackId: String,

    /**
     * Order/position of the track within the album.
     * Used for maintaining album track sequencing (1-based indexing).
     */
    @ColumnInfo(name = "track_order")
    val trackOrder: Int
) {
    /**
     * Validates that the junction entity contains valid relationship data.
     * @return true if all required fields are properly set
     */
    fun isValid(): Boolean {
        return albumId.isNotBlank() && trackId.isNotBlank() && trackOrder > 0
    }
}

/**
 * Enhanced Room entity for track with album relationship integration.
 * Includes albumId foreign key for comprehensive music library organization.
 * This entity extends the basic TrackEntity with album relationship fields
 * for enhanced Room integration and data integrity.
 */
@Entity(
    tableName = "enhanced_tracks",
    foreignKeys = [
        ForeignKey(
            entity = AlbumEntity::class,
            parentColumns = ["id"],
            childColumns = ["album_id"], // Use actual column name
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["title"]),
        Index(value = ["artist"]),
        Index(value = ["album_id"]), // Use actual column name
        Index(value = ["file_path"], unique = true), // Use actual column name
        Index(value = ["last_modified"]), // Use actual column name
        Index(value = ["artist", "album_id"]) // Use actual column names
    ]
)
data class EnhancedTrackEntity(
    /**
     * Unique identifier for the track.
     */
    @PrimaryKey val id: String,

    /**
     * Track title for display and search functionality.
     */
    @ColumnInfo(name = "title")
    val title: String,

    /**
     * Artist name associated with this track.
     */
    @ColumnInfo(name = "artist")
    val artist: String,

    /**
     * Foreign key reference to AlbumEntity.id.
     * Establishes the album relationship for this track.
     */
    @ColumnInfo(name = "album_id")
    val albumId: String,

    /**
     * File system path where the track is stored.
     * Must be unique to prevent duplicate track entries.
     */
    @ColumnInfo(name = "file_path")
    val filePath: String,

    /**
     * Track duration in milliseconds.
     */
    @ColumnInfo(name = "duration_ms")
    val durationMs: Long,

    /**
     * Last modification timestamp in milliseconds since epoch.
     */
    @ColumnInfo(name = "last_modified")
    val lastModified: Long
) {
    /**
     * Validates that the entity contains valid data.
     * @return true if all required fields are properly set
     */
    fun isValid(): Boolean {
        val isPathValid = filePath.isNotBlank() && filePath.startsWith("/")
        val now = System.currentTimeMillis()
        val maxFutureSkewMs = 5 * 60 * 1000 // 5 minutes
        val isLastModifiedValid = lastModified > 0 && lastModified <= now + maxFutureSkewMs
        return id.isNotBlank() &&
            title.isNotBlank() &&
            artist.isNotBlank() &&
            albumId.isNotBlank() &&
            isPathValid &&
            durationMs > 0 &&
            isLastModifiedValid
    }

    /**
     * Checks if this track has a valid album relationship.
     * @return true if albumId is not blank
     */
    fun hasAlbumRelationship(): Boolean {
        return albumId.isNotBlank()
    }

    /**
     * Creates a display-friendly string representation.
     * @return formatted string for UI display
     */
    fun getDisplayName(): String = "$title by $artist"
}

/**
 * Room relationship model for querying albums with their tracks.
 * Uses @Relation annotation to establish one-to-many relationship.
 */
data class AlbumWithTracks(
    @Embedded val album: AlbumEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "albumId"
    )
    val tracks: List<EnhancedTrackEntity>
) {
    /**
     * Checks if this album has valid tracks.
     * @return true if tracks list is not empty
     */
    fun hasValidTracks(): Boolean {
        return tracks.isNotEmpty()
    }

    /**
     * Validates relationship consistency between album and tracks.
     * @return list of validation error messages
     */
    fun getValidationErrors(): List<String> {
        val errors = mutableListOf<String>()
        tracks.forEach { track ->
            if (track.albumId != album.id) {
                errors.add("Track ${track.id} has mismatched album ID")
            }
        }
        return errors
    }
}

/**
 * Room relationship model for querying tracks with their album.
 * Uses @Relation annotation to establish many-to-one relationship.
 */
data class TrackWithAlbum(
    @Embedded val track: EnhancedTrackEntity,
    @Relation(
        parentColumn = "albumId",
        entityColumn = "id"
    )
    val album: AlbumEntity
)

/**
 * Enhanced junction table entity for album-track many-to-many relationships.
 * This entity uses consistent foreign key references to enhanced entities
 * and addresses PR review feedback regarding foreign key consistency.
 */
@Entity(
    tableName = "enhanced_album_tracks",
    primaryKeys = ["album_id", "enhanced_track_id"], // Use actual column names
    foreignKeys = [
        ForeignKey(
            entity = AlbumEntity::class,
            parentColumns = ["id"],
            childColumns = ["album_id"], // Use actual column name
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = EnhancedTrackEntity::class,
            parentColumns = ["id"],
            childColumns = ["enhanced_track_id"], // Use actual column name
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["album_id"]), // Use actual column name
        Index(value = ["enhanced_track_id"]), // Use actual column name
        Index(value = ["track_order"]) // Use actual column name
    ]
)
data class EnhancedAlbumTrackEntity(
    /**
     * Foreign key reference to AlbumEntity.id.
     */
    @ColumnInfo(name = "album_id")
    val albumId: String,

    /**
     * Foreign key reference to EnhancedTrackEntity.id.
     * Ensures consistent use of enhanced entities throughout.
     */
    @ColumnInfo(name = "enhanced_track_id")
    val enhancedTrackId: String,

    /**
     * Order/position of the track within the album.
     */
    @ColumnInfo(name = "track_order")
    val trackOrder: Int
) {
    /**
     * Validates that the entity contains valid relationship data.
     */
    fun isValid(): Boolean {
        return albumId.isNotBlank() && enhancedTrackId.isNotBlank() && trackOrder > 0
    }
}

/**
 * Enhanced Room relationship model that properly uses junction table for ordering.
 * Addresses PR review feedback about bypassing track ordering information.
 * This model includes the junction table entities to preserve track order.
 */
data class EnhancedAlbumWithTracksOrdered(
    @Embedded val album: AlbumEntity,
    @Relation(
        entity = EnhancedAlbumTrackEntity::class,
        parentColumn = "id",
        entityColumn = "albumId"
    )
    val albumTracks: List<AlbumTrackWithEnhancedTrack>
) {
    /**
     * Gets the tracks in their proper album order using junction table ordering.
     */
    fun getOrderedTracks(): List<EnhancedTrackEntity> {
        return albumTracks
            .sortedBy { it.albumTrack.trackOrder }
            .map { it.track }
    }

    /**
     * Validates that all tracks belong to this album and have valid ordering.
     */
    fun isValid(): Boolean {
        return albumTracks.all { it.albumTrack.albumId == album.id && it.albumTrack.trackOrder > 0 }
    }
}

/**
 * Junction table relationship that includes both the junction entity and the actual track.
 * Preserves ordering information while providing access to track details.
 */
data class AlbumTrackWithEnhancedTrack(
    @Embedded val albumTrack: EnhancedAlbumTrackEntity,
    @Relation(
        parentColumn = "enhancedTrackId",
        entityColumn = "id"
    )
    val track: EnhancedTrackEntity
)

/**
 * Result of metadata validation containing quality assessment and confidence scoring.
 * Provides comprehensive validation results for audio metadata.
 */
data class MetadataValidationResult(
    val isValid: Boolean,
    val confidenceScore: Double,
    val validationErrors: List<String>,
    val hasRequiredFields: Boolean,
    val isHighQuality: Boolean
)

/**
 * Result of integrated metadata extraction with validation.
 * Combines multi-strategy extraction results with metadata validation for comprehensive quality assessment.
 */
data class ValidatedMetadataResult(
    val multiStrategyResult: com.foxy.player.music.ui.MultiStrategyMetadataResult,
    val validationResult: MetadataValidationResult,
    val combinedConfidence: Double,
    val qualityAssessment: String,
    val persistenceResult: com.foxy.player.music.business.MetadataPersistenceResult? = null
)
