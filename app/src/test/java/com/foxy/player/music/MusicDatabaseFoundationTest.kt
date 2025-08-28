package com.foxy.player.music

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MusicDatabaseFoundationTest {

    @Test
    fun `should create music database with Room foundation`() {
        // Arrange - attempt to get a database instance through provider
        val databaseProvider: DatabaseProvider = MusicDatabaseProvider()

        // Act - attempt to create a database instance
        val database = databaseProvider.getDatabase()

        // Assert - verify database foundation is available
        assertNotNull(database)
        assertTrue("Database should be properly initialized", database.isInitialized())

        // Verify we can access basic database operations
        val trackDao = database.trackDao()
        assertNotNull("Track DAO should be available", trackDao)
    }

    @Test
    fun `should provide track DAO for music data operations`() {
        // Arrange
        val databaseProvider: DatabaseProvider = MusicDatabaseProvider()
        val database = databaseProvider.getDatabase()

        // Act - get track DAO
        val trackDao = database.trackDao()

        // Assert - verify DAO is functional
        assertNotNull(trackDao)
        assertTrue("DAO should be ready for operations", trackDao.isReady())
    }
}
