package com.foxy.player.music

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class BackgroundSyncServiceTest {

    @Test
    fun `should sync pCloud tracks to local database incrementally`() = runBlocking {
        // Arrange - Create test data and stub implementations
        val pcloudTracks = listOf(
            PCloudItem(
                name = "test-song.mp3",
                created = "2024-01-01T00:00:00Z",
                modified = "2024-01-01T00:00:00Z",
                isFolder = false,
                folderId = null,
                fileId = 123L,
                parentFolderId = 0L,
                size = 3500000L,
                contentType = "audio/mpeg",
                category = 2,
                id = "file123"
            )
        )

        val testRepository = TestTrackRepository()
        val testPCloudApi = TestPCloudApi(pcloudTracks)
        val syncService = BackgroundSyncService(testRepository, testPCloudApi)

        // Act - Perform incremental sync
        val syncResult = syncService.performIncrementalSync()

        // Assert - Verify sync completed successfully
        assertTrue("Sync should be successful", syncResult is SyncResult.Success)

        when (syncResult) {
            is SyncResult.Success -> {
                val response = syncResult.data
                assertTrue("Sync should complete successfully", response.usedIncrementalSync)
                assertEquals("Should process exactly 1 track", 1, response.tracksProcessed)
                assertTrue("Should run in background", response.backgroundExecution)
                assertTrue("Should be non-blocking", response.nonBlockingOperation)
                assertEquals("Final sync status should be COMPLETED", SyncStatus.COMPLETED, response.finalSyncStatus)
                assertTrue("Should verify data integrity", response.dataIntegrityVerified)
            }
            is SyncResult.Error -> {
                fail("Sync should not fail: ${syncResult.message}")
            }
        }

        // Verify track was inserted into repository
        assertEquals("Should have 1 track after sync", 1, testRepository.getTrackCount())
        val insertedTracks = testRepository.getAllTracks()
        assertEquals("Track ID should match", "file123", insertedTracks.first().id)
        assertEquals("Track title should be clean filename", "test-song", insertedTracks.first().title)
    }

    @Test
    fun `should handle pCloud API errors gracefully`() = runBlocking {
        // Arrange - Create failing pCloud API
        val testRepository = TestTrackRepository()
        val failingPCloudApi = FailingTestPCloudApi()
        val syncService = BackgroundSyncService(testRepository, failingPCloudApi)

        // Act - Attempt sync with failing API
        val syncResult = syncService.performIncrementalSync()

        // Assert - Verify error handling
        assertTrue("Sync should fail gracefully", syncResult is SyncResult.Error)

        when (syncResult) {
            is SyncResult.Success -> {
                fail("Sync should not succeed with failing API")
            }
            is SyncResult.Error -> {
                assertTrue("Error message should be informative", syncResult.message.isNotEmpty())
                assertNotNull("Exception should be captured", syncResult.exception)
            }
        }
    }
}

// Test implementations
class TestTrackRepository : TrackRepositoryInterface {
    private val tracks = mutableListOf<TrackEntity>()

    override fun isReady(): Boolean = true

    override suspend fun getAllTracks(): List<TrackEntity> = tracks.toList()

    override suspend fun insertTrack(id: String, title: String, artist: String, album: String, filePath: String) {
        tracks.add(TrackEntity(id, title, artist, album, filePath))
    }

    override suspend fun getTrackCount(): Int = tracks.size
}

class TestPCloudApi(private val tracksToReturn: List<PCloudItem>) : PCloudApiInterface {
    override suspend fun listFolderContents(folderId: String): PCloudListFolderResponse {
        return PCloudListFolderResponse(
            result = 0,
            metadata = null,
            contents = tracksToReturn
        )
    }
}

class FailingTestPCloudApi : PCloudApiInterface {
    override suspend fun listFolderContents(folderId: String): PCloudListFolderResponse {
        return PCloudListFolderResponse(
            result = 1, // Error code
            metadata = null,
            contents = null
        )
    }
}
