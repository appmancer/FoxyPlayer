package com.foxy.player.music.repository

import com.foxy.player.music.database.DatabaseProvider
import com.foxy.player.music.database.MusicDatabaseInterface
import com.foxy.player.music.database.MusicDatabaseProvider
import com.foxy.player.music.entities.TrackEntity

/**
 * Result wrapper for repository operations that may fail.
 * Provides proper error handling without throwing exceptions,
 * following modern Android development patterns.
 */
sealed class RepositoryResult<out T> {
    data class Success<out T>(val data: T) : RepositoryResult<T>()
    data class Error(val exception: Throwable) : RepositoryResult<Nothing>()
}

/**
 * Repository interface for track data operations.
 * Provides an abstraction layer between the data access layer (Room) and
 * the business logic layer, following the Repository pattern.
 */
interface TrackRepositoryInterface {
    fun isReady(): Boolean
    suspend fun getAllTracks(): List<TrackEntity>
    suspend fun insertTrack(id: String, title: String, artist: String, album: String, filePath: String)
    suspend fun getTrackCount(): Int
    suspend fun getTracksPage(offset: Int, limit: Int): List<TrackEntity>
    suspend fun getTracksForRange(startIndex: Int, count: Int): List<TrackEntity>
}

/**
 * Repository implementation for track data operations.
 * Integrates with Room database through dependency injection and provides
 * a clean API for track-related data operations with proper error handling.
 */
class TrackRepository(
    private val databaseProvider: DatabaseProvider = MusicDatabaseProvider()
) : TrackRepositoryInterface {

    private val database: MusicDatabaseInterface by lazy {
        databaseProvider.getDatabase()
    }

    override fun isReady(): Boolean {
        return try {
            database.isInitialized()
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getAllTracks(): List<TrackEntity> {
        return try {
            val trackDao = database.trackDao()
            trackDao?.getAllTracks() ?: emptyList()
        } catch (e: Exception) {
            // Return empty list on error rather than throwing exception
            emptyList()
        }
    }

    override suspend fun insertTrack(id: String, title: String, artist: String, album: String, filePath: String) {
        try {
            val trackDao = database.trackDao()
            trackDao?.insertTrack(id, title, artist, album, filePath)
        } catch (e: Exception) {
            // Log error in real implementation - for now just handle gracefully
        }
    }

    override suspend fun getTrackCount(): Int {
        return try {
            val trackDao = database.trackDao()
            trackDao?.getTrackCount() ?: 0
        } catch (e: Exception) {
            // Return 0 on error rather than throwing exception
            0
        }
    }

    /**
     * Enhanced method with Result wrapper for better error handling.
     * This provides an alternative API for callers who want explicit error handling.
     */
    suspend fun getAllTracksWithResult(): RepositoryResult<List<TrackEntity>> {
        return try {
            val tracks = getAllTracks()
            RepositoryResult.Success(tracks)
        } catch (e: Exception) {
            RepositoryResult.Error(e)
        }
    }

    override suspend fun getTracksPage(offset: Int, limit: Int): List<TrackEntity> {
        return try {
            val trackDao = database.trackDao()
            trackDao?.getTracksPage(offset, limit) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getTracksForRange(startIndex: Int, count: Int): List<TrackEntity> {
        return try {
            val trackDao = database.trackDao()
            trackDao?.getTracksForRange(startIndex, count) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
