package com.foxy.player.music.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

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
        Index(value = ["title", "artist"]), // Query performance for title+artist (non-unique to allow remastered versions)
        Index(value = ["path"], unique = true), // Unique file paths
        Index(value = ["lastModified"]), // Recent albums query
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
    val bitrate: Int
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
