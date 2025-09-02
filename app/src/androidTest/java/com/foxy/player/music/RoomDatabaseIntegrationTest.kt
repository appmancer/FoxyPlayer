package com.foxy.player.music

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.foxy.player.music.database.MusicDatabaseProvider
import com.foxy.player.music.repository.TrackRepository
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomDatabaseIntegrationTest {

    private lateinit var repository: TrackRepository

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        // Initialize the database provider with Android context
        MusicDatabaseProvider.initialize(context)

        // Clean up any existing database file
        val dbFile = File(context.filesDir, MusicDatabaseProvider.DATABASE_NAME_FOR_TESTING)
        if (dbFile.exists()) {
            dbFile.delete()
        }

        repository = TrackRepository()
    }

    @After
    fun tearDown() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        // Clean up test database file
        val dbFile = File(context.filesDir, MusicDatabaseProvider.DATABASE_NAME_FOR_TESTING)
        if (dbFile.exists()) {
            dbFile.delete()
        }
    }

    @Test
    fun shouldCreateRealRoomDatabaseWithAndroidContextAndPersistTrackDataToSQLiteFile() = runBlocking {
        // Arrange - verify we have real Android context and repository
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        assertNotNull("Should have real Android context", context)
        assertNotNull("Should have real repository", repository)
        assertTrue("Repository should be ready", repository.isReady())

        // Debug: Check if Room database is actually being created
        val roomDatabase = MusicDatabaseProvider.getRoomDatabase()
        assertNotNull("Room database should be created", roomDatabase)

        // Create test track data
        val testId = "test-track-001"
        val testTitle = "Real Test Song"
        val testArtist = "Real Test Artist"
        val testAlbum = "Real Test Album"
        val testFilePath = "/real/path/test.mp3"

        // Act - insert track through repository (should use real Room database)
        repository.insertTrack(testId, testTitle, testArtist, testAlbum, testFilePath)

        // Retrieve tracks from repository (should come from real database)
        val retrievedTracks = repository.getAllTracks()
        val trackCount = repository.getTrackCount()

        // Assert - verify data was persisted to real SQLite file through repository
        assertTrue("Should have at least one track after insertion", retrievedTracks.isNotEmpty())
        assertEquals("Should have exactly one track", 1, retrievedTracks.size)
        assertEquals("Track count should be 1", 1, trackCount)

        val retrievedTrack = retrievedTracks.first()
        assertEquals("ID should match", testId, retrievedTrack.id)
        assertEquals("Title should match", testTitle, retrievedTrack.title)
        assertEquals("Artist should match", testArtist, retrievedTrack.artist)
        assertEquals("Album should match", testAlbum, retrievedTrack.album)
        assertEquals("File path should match", testFilePath, retrievedTrack.filePath)

        // Verify SQLite file was actually created (check both possible locations)
        val filesDbFile = File(context.filesDir, MusicDatabaseProvider.DATABASE_NAME_FOR_TESTING)
        val databaseDbFile = File(context.getDatabasePath(MusicDatabaseProvider.DATABASE_NAME_FOR_TESTING).absolutePath)

        val dbFileExists = filesDbFile.exists() || databaseDbFile.exists()
        assertTrue("SQLite database file should exist in either filesDir or databaseDir", dbFileExists)

        val actualDbFile = if (databaseDbFile.exists()) databaseDbFile else filesDbFile
        assertTrue("Database file should have content", actualDbFile.length() > 0)
    }
}
