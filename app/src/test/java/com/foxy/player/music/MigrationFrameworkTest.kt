package com.foxy.player.music

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * TDD tests for Migration Framework - PLY-92
 * Tests migration from RAM-based to database-based architecture
 */
class MigrationFrameworkTest {

    @Test
    fun `Migration framework should detect and migrate in-memory track storage to database`() = runBlocking {
        // Arrange - Create a system with in-memory track storage
        val inMemoryTracks = listOf(
            TrackEntity("1", "Song 1", "Artist 1", "Album 1", "file1.mp3"),
            TrackEntity("2", "Song 2", "Artist 2", "Album 2", "file2.mp3"),
            TrackEntity("3", "Song 3", "Artist 3", "Album 3", "file3.mp3")
        )

        // Create in-memory storage system that needs migration
        val inMemoryStorage = InMemoryTrackStorage().apply {
            tracksList.addAll(inMemoryTracks)
        }

        // Create migration framework
        val migrationFramework = MigrationFramework()

        // Act - Execute migration from in-memory to database
        val migrationResult = migrationFramework.migrateTracksToDatabase(
            sourceStorage = inMemoryStorage,
            targetDatabase = TestDatabaseProvider()
        )

        // Assert - Verify migration was successful
        assertTrue("Migration should succeed", migrationResult is MigrationResult.Success)
        val successResult = migrationResult as MigrationResult.Success
        assertEquals("All tracks should be migrated", 3, successResult.migratedCount)
        assertTrue("Source storage should be empty after migration", inMemoryStorage.isEmpty())

        // Verify tracks are now in database
        val databaseTracks = successResult.targetDao.getAllTracks()
        assertEquals("Database should contain all migrated tracks", 3, databaseTracks.size)
        assertEquals("Track 1 should match", "Song 1", databaseTracks.find { it.id == "1" }?.title)
        assertEquals("Track 2 should match", "Song 2", databaseTracks.find { it.id == "2" }?.title)
        assertEquals("Track 3 should match", "Song 3", databaseTracks.find { it.id == "3" }?.title)
    }
}
