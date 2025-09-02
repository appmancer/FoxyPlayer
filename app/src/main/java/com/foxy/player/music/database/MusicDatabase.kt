package com.foxy.player.music.database

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import com.foxy.player.music.entities.AlbumEntity
import com.foxy.player.music.entities.TrackEntity

// ===== SQLITE DATABASE FOUNDATION =====

/**
 * Database provider interface for dependency injection
 */
interface DatabaseProvider {
    fun getDatabase(): MusicDatabaseInterface
}

/**
 * SQLite Database Foundation for Music Player
 * Provides Room database abstraction and basic DAO access with proper Android context management.
 * * This provider implements the Singleton pattern with thread-safe initialization
 * and provides a clean abstraction over Room database operations.
 */
class MusicDatabaseProvider : DatabaseProvider {
    companion object {
        @Volatile
        private var roomDatabaseInstance: MusicRoomDatabase? = null

        // Context to be injected for Room database creation
        @Volatile
        private var applicationContext: android.content.Context? = null

        /**
         * Initialize the provider with Android application context.
         * Must be called before using the database, typically in Application.onCreate().
         * @param context The application context (will be converted to applicationContext automatically)
         * @throws IllegalArgumentException if context is null
         */
        fun initialize(context: android.content.Context?) {
            require(context != null) { "Application context cannot be null" }
            applicationContext = context.applicationContext
        }

        /**
         * Get the Room database instance using thread-safe singleton pattern.
         * @return Room database instance or null if initialization failed
         * @throws IllegalStateException if not initialized with context
         */
        fun getRoomDatabase(): MusicRoomDatabase? {
            return try {
                roomDatabaseInstance ?: synchronized(this) {
                    roomDatabaseInstance ?: createRoomDatabase().also { roomDatabaseInstance = it }
                }
            } catch (e: IllegalStateException) {
                // Re-throw initialization errors
                throw e
            } catch (e: Exception) {
                // Log error in production - for now return null to maintain backward compatibility
                null
            }
        }

        /**
         * Create Room database instance with proper error handling.
         * @return Room database instance
         * @throws IllegalStateException if context not initialized
         */
        private fun createRoomDatabase(): MusicRoomDatabase {
            val context = applicationContext
                ?: throw IllegalStateException("Database not initialized. Call initialize(context) first.")

            return Room.databaseBuilder(
                context,
                MusicRoomDatabase::class.java,
                "music_database"
            ).build()
        }

        /**
         * Reset the database instance for testing purposes.
         * This method should only be used in unit tests.
         */
        fun resetForTesting() {
            roomDatabaseInstance?.close()
            roomDatabaseInstance = null
            applicationContext = null
        }
    }

    override fun getDatabase(): MusicDatabaseInterface {
        return MusicDatabase()
    }
}

/**
 * High-level interface for music database operations.
 * Provides abstraction over Room database implementation.
 */
interface MusicDatabaseInterface {
    suspend fun getAllTracks(): List<TrackEntity>
    suspend fun insertTrack(track: TrackEntity)
    suspend fun getTracksPage(offset: Int, limit: Int): List<TrackEntity>
    suspend fun getTracksForRange(startIndex: Int, count: Int): List<TrackEntity>
    fun isReady(): Boolean
    fun isInitialized(): Boolean
    fun trackDao(): RoomTrackDao?
}

/**
 * Concrete implementation of music database operations.
 * Delegates to Room database with proper error handling.
 */
class MusicDatabase : MusicDatabaseInterface {

    override suspend fun getAllTracks(): List<TrackEntity> {
        val roomDao = getRoomTrackDao() ?: return emptyList()
        return roomDao.getAllTracks()
    }

    override suspend fun insertTrack(track: TrackEntity) {
        val roomDao = getRoomTrackDao() ?: return
        roomDao.insertTrack(track)
    }

    override suspend fun getTracksPage(offset: Int, limit: Int): List<TrackEntity> {
        val roomDao = getRoomTrackDao() ?: return emptyList()
        return roomDao.getTracksPage(offset, limit)
    }

    override suspend fun getTracksForRange(startIndex: Int, count: Int): List<TrackEntity> {
        val roomDao = getRoomTrackDao() ?: return emptyList()
        return roomDao.getTracksForRange(startIndex, count)
    }

    override fun isReady(): Boolean {
        return MusicDatabaseProvider.getRoomDatabase() != null
    }

    override fun isInitialized(): Boolean {
        return isReady()
    }

    override fun trackDao(): RoomTrackDao? {
        return getRoomTrackDao()
    }

    private fun getRoomTrackDao(): RoomTrackDao? {
        return MusicDatabaseProvider.getRoomDatabase()?.trackDao()
    }
}

/**
 * Room database wrapper that implements the high-level database interface.
 * Provides a bridge between Room-generated code and our abstraction layer.
 */
class RoomDatabaseWrapper : MusicDatabaseInterface {
    private val roomDatabase: MusicRoomDatabase?
        get() = MusicDatabaseProvider.getRoomDatabase()

    override suspend fun getAllTracks(): List<TrackEntity> {
        return roomDatabase?.trackDao()?.getAllTracks() ?: emptyList()
    }

    override suspend fun insertTrack(track: TrackEntity) {
        roomDatabase?.trackDao()?.insertTrack(track)
    }

    override suspend fun getTracksPage(offset: Int, limit: Int): List<TrackEntity> {
        return roomDatabase?.trackDao()?.getTracksPage(offset, limit) ?: emptyList()
    }

    override suspend fun getTracksForRange(startIndex: Int, count: Int): List<TrackEntity> {
        return roomDatabase?.trackDao()?.getTracksForRange(startIndex, count) ?: emptyList()
    }

    override fun isReady(): Boolean {
        return roomDatabase != null
    }

    override fun isInitialized(): Boolean {
        return isReady()
    }

    override fun trackDao(): RoomTrackDao? {
        return roomDatabase?.trackDao()
    }
}

// ===== ROOM DATABASE IMPLEMENTATION =====

/**
 * Room Data Access Object for track operations.
 * Provides type-safe access to track data with compile-time SQL validation.
 * Uses suspend functions for non-blocking database operations.
 */
@Dao
interface RoomTrackDao {
    /**
     * Retrieves all tracks from the database.
     * @return List of all track entities. Returns empty list if no tracks exist.
     */
    @Query("SELECT * FROM tracks")
    suspend fun getAllTracks(): List<TrackEntity>

    /**
     * Inserts a new track into the database.
     * @param track The track entity to insert
     */
    @Insert
    suspend fun insertTrack(track: TrackEntity)

    /**
     * Inserts a new track into the database using individual parameters.
     * @param id The unique identifier for the track
     * @param title The title of the track
     * @param artist The artist name
     * @param album The album name
     * @param filePath The file path where the track is stored
     */
    suspend fun insertTrack(id: String, title: String, artist: String, album: String, filePath: String) {
        insertTrack(TrackEntity(id, title, artist, album, filePath))
    }

    /**
     * Get the total number of tracks in the database.
     * @return Total track count
     */
    @Query("SELECT COUNT(*) FROM tracks")
    suspend fun getTrackCount(): Int

    /**
     * Retrieves a page of tracks from the database.
     * @param offset Starting position (0-based index)
     * @param limit Maximum number of tracks to return
     * @return List of track entities for the requested page
     */
    @Query("SELECT * FROM tracks LIMIT :limit OFFSET :offset")
    suspend fun getTracksPage(offset: Int, limit: Int): List<TrackEntity>

    /**
     * Retrieves tracks for a specific range.
     * @param startIndex Starting index (0-based)
     * @param count Number of tracks to retrieve
     * @return List of track entities in the specified range
     */
    @Query("SELECT * FROM tracks LIMIT :count OFFSET :startIndex")
    suspend fun getTracksForRange(startIndex: Int, count: Int): List<TrackEntity>
}

/**
 * Implementation of track DAO operations using clean architecture patterns.
 * Provides concrete implementation of track data access with error handling.
 */
class TrackDao : TrackDaoInterface {
    override suspend fun getAllTracks(): List<TrackEntity> {
        val roomDao = MusicDatabaseProvider.getRoomDatabase()?.trackDao() ?: return emptyList()
        return roomDao.getAllTracks()
    }

    override suspend fun insertTrack(track: TrackEntity) {
        val roomDao = MusicDatabaseProvider.getRoomDatabase()?.trackDao() ?: return
        roomDao.insertTrack(track)
    }

    override suspend fun getTracksPage(offset: Int, limit: Int): List<TrackEntity> {
        val roomDao = MusicDatabaseProvider.getRoomDatabase()?.trackDao() ?: return emptyList()
        return roomDao.getTracksPage(offset, limit)
    }

    override suspend fun getTracksForRange(startIndex: Int, count: Int): List<TrackEntity> {
        val roomDao = MusicDatabaseProvider.getRoomDatabase()?.trackDao() ?: return emptyList()
        return roomDao.getTracksForRange(startIndex, count)
    }
}

/**
 * Simple Room track DAO implementation that directly extends the Room DAO.
 * Used for testing and simple use cases where direct Room access is preferred.
 */
class SimpleRoomTrackDao : RoomTrackDao {
    override suspend fun getAllTracks(): List<TrackEntity> {
        val roomDao = MusicDatabaseProvider.getRoomDatabase()?.trackDao() ?: return emptyList()
        return roomDao.getAllTracks()
    }

    override suspend fun insertTrack(track: TrackEntity) {
        val roomDao = MusicDatabaseProvider.getRoomDatabase()?.trackDao() ?: return
        roomDao.insertTrack(track)
    }

    override suspend fun getTrackCount(): Int {
        val roomDao = MusicDatabaseProvider.getRoomDatabase()?.trackDao() ?: return 0
        return roomDao.getTrackCount()
    }

    override suspend fun getTracksPage(offset: Int, limit: Int): List<TrackEntity> {
        val roomDao = MusicDatabaseProvider.getRoomDatabase()?.trackDao() ?: return emptyList()
        return roomDao.getTracksPage(offset, limit)
    }

    override suspend fun getTracksForRange(startIndex: Int, count: Int): List<TrackEntity> {
        val roomDao = MusicDatabaseProvider.getRoomDatabase()?.trackDao() ?: return emptyList()
        return roomDao.getTracksForRange(startIndex, count)
    }
}

/**
 * Wrapper around Room DAO that provides additional abstraction and error handling.
 * This allows for easy mocking in tests and provides a consistent interface.
 */
class RoomTrackDaoWrapper(private val roomDao: RoomTrackDao) : TrackDaoInterface {
    override suspend fun getAllTracks(): List<TrackEntity> {
        return roomDao.getAllTracks()
    }

    override suspend fun insertTrack(track: TrackEntity) {
        roomDao.insertTrack(track)
    }

    override suspend fun getTracksPage(offset: Int, limit: Int): List<TrackEntity> {
        return roomDao.getTracksPage(offset, limit)
    }

    override suspend fun getTracksForRange(startIndex: Int, count: Int): List<TrackEntity> {
        return roomDao.getTracksForRange(startIndex, count)
    }
}

/**
 * Room database configuration with entities and version management.
 */
@Database(
    entities = [TrackEntity::class, AlbumEntity::class],
    version = 1,
    exportSchema = false
)
abstract class MusicRoomDatabase : RoomDatabase() {
    abstract fun trackDao(): RoomTrackDao
}

/**
 * Interface for track data access operations.
 * Provides abstraction layer for dependency injection and testing.
 */
interface TrackDaoInterface {
    suspend fun getAllTracks(): List<TrackEntity>
    suspend fun insertTrack(track: TrackEntity)
    suspend fun getTracksPage(offset: Int, limit: Int): List<TrackEntity>
    suspend fun getTracksForRange(startIndex: Int, count: Int): List<TrackEntity>
}
