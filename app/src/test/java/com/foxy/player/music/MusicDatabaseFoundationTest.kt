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

    @Test
    fun `should add Room dependencies and database annotations`() {
        // Arrange - verify Room classes exist with proper structure
        val databaseClass = MusicRoomDatabase::class.java
        val daoInterface = RoomTrackDao::class.java
        val entityClass = TrackEntity::class.java

        // Act & Assert - verify Room database extends RoomDatabase
        assertTrue(
            "Database should extend RoomDatabase",
            androidx.room.RoomDatabase::class.java.isAssignableFrom(databaseClass)
        )

        // Verify database has trackDao method
        val trackDaoMethod = databaseClass.declaredMethods.find { it.name == "trackDao" }
        assertNotNull("Database should have trackDao method", trackDaoMethod)
        assertTrue(
            "trackDao should return RoomTrackDao",
            trackDaoMethod!!.returnType == daoInterface
        )

        // Verify DAO has getAllTracks method
        val getAllTracksMethod = daoInterface.declaredMethods.find { it.name == "getAllTracks" }
        assertNotNull("DAO should have getAllTracks method", getAllTracksMethod)

        // Verify DAO has additional methods for database operations
        val insertTrackMethod = daoInterface.declaredMethods.find { it.name == "insertTrack" }
        assertNotNull("DAO should have insertTrack method", insertTrackMethod)

        val getTrackCountMethod = daoInterface.declaredMethods.find { it.name == "getTrackCount" }
        assertNotNull("DAO should have getTrackCount method", getTrackCountMethod)

        // Verify entity has proper fields
        val idField = entityClass.declaredFields.find { it.name == "id" }
        assertNotNull("Entity should have id field", idField)

        val titleField = entityClass.declaredFields.find { it.name == "title" }
        assertNotNull("Entity should have title field", titleField)

        // Verify classes are properly accessible
        assertNotNull("Database class should be accessible", databaseClass)
        assertNotNull("DAO interface should be accessible", daoInterface)
        assertNotNull("Entity class should be accessible", entityClass)
    }
}
