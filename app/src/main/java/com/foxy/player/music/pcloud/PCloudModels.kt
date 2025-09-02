package com.foxy.player.music.pcloud

import com.foxy.player.music.entities.FolderListing
import com.google.gson.annotations.SerializedName

/**
 * Interface for pCloud API operations needed for synchronization.
 * Provides abstraction for testing and different API implementations.
 */
interface PCloudApiInterface {
    /**
     * Lists contents of a pCloud folder.
     * @param folderId The folder ID to list (use "0" for root)
     * @return PCloudListFolderResponse with folder contents
     */
    suspend fun listFolderContents(folderId: String): PCloudListFolderResponse
}

/**
 * Represents the response from the pCloud API for a folder listing request.
 *
 * @property result The result code of the API call (0 for success, non-zero for errors).
 * @property metadata Metadata about the folder, if available.
 * @property contents List of items (files and folders) contained in the folder, if available.
 */
data class PCloudListFolderResponse(
    val result: Int,
    val metadata: PCloudMetadata?,
    val contents: List<PCloudItem>?
)

/**
 * Metadata information for a pCloud folder.
 *
 * @property name The name of the folder.
 * @property created The creation date/time of the folder (ISO 8601 format).
 * @property isFolder Whether this item is a folder (should always be true for folders).
 * @property folderId The unique identifier for the folder.
 * @property parentFolderId The unique identifier of the parent folder.
 */
data class PCloudMetadata(
    val name: String,
    val created: String,
    @SerializedName("isfolder") val isFolder: Boolean,
    @SerializedName("folderid") val folderId: Long,
    @SerializedName("parentfolderid") val parentFolderId: Long
)

/**
 * Represents a file or folder item in a pCloud directory listing.
 *
 * @property name The name of the file or folder.
 * @property created The creation date/time (ISO 8601 format).
 * @property modified The last modification date/time (ISO 8601 format).
 * @property isFolder Whether this item is a folder (true) or file (false).
 * @property folderId The unique identifier for the folder (if this is a folder).
 * @property fileId The unique identifier for the file (if this is a file).
 * @property parentFolderId The unique identifier of the parent folder.
 * @property size The size of the file in bytes (null for folders).
 * @property contentType The MIME type of the file (e.g., "audio/mpeg" for MP3 files).
 * @property category The pCloud category identifier for the file type.
 * @property id The unique string identifier for the item.
 */
data class PCloudItem(
    val name: String,
    val created: String,
    val modified: String,
    @SerializedName("isfolder") val isFolder: Boolean,
    @SerializedName("folderid") val folderId: Long?,
    @SerializedName("fileid") val fileId: Long?,
    @SerializedName("parentfolderid") val parentFolderId: Long,
    val size: Long?,
    @SerializedName("contenttype") val contentType: String?,
    val category: Int?,
    val id: String
)

/**
 * API Response wrapper to support containsAuthToken method.
 * Used for pCloud API authentication and response validation.
 */
data class PCloudAPIResponse(
    val authToken: String,
    val folderListing: FolderListing
) {
    fun containsAuthToken(token: String): Boolean {
        return authToken == token
    }
}

/**
 * Response model for recursive directory traversal operations.
 * Tracks the progress and results of deep folder scanning.
 */
data class RecursiveDirectoryResponse(
    val authToken: String,
    val totalDirectoriesTraversed: Int,
    val allFolders: List<String>
)

/**
 * Response model for audio files filtering operations.
 * Contains the filtered results of audio file discovery.
 */
data class AudioFilesResponse(
    val authToken: String,
    val audioFiles: List<String>
)

/**
 * Utility functions for pCloud item processing.
 */
object PCloudUtils {
    /**
     * Checks if a pCloud item is an audio file based on content type and file extension.
     * @param item PCloudItem to check
     * @param audioFileExtensions Set of supported audio file extensions
     * @return true if item is an audio file
     */
    fun isAudioFile(item: PCloudItem, audioFileExtensions: Set<String>): Boolean {
        // Check content type first (most reliable)
        if (item.contentType?.startsWith("audio/") == true) {
            return true
        }

        // Fallback to file extension check
        val fileName = item.name.lowercase()
        return audioFileExtensions.any { extension ->
            fileName.endsWith(".$extension")
        }
    }

    /**
     * Generates a standardized file path for a track.
     * @param item PCloudItem representing the track
     * @return Standardized file path string
     */
    fun generateTrackPath(item: PCloudItem): String {
        return "/pcloud/${item.name}"
    }

    /**
     * Extracts track title from filename, removing file extension.
     * @param fileName Original filename
     * @return Clean track title
     */
    fun extractTrackTitle(fileName: String): String {
        return fileName.substringBeforeLast(".")
    }
}
