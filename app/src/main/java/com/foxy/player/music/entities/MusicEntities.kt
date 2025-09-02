package com.foxy.player.music.entities

import androidx.room.Entity
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
