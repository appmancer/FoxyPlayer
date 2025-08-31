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

    @Test
    fun `should implement exponential backoff retry mechanism for failed sync operations`() = runBlocking {
        // Arrange - Create intermittently failing API that succeeds on 3rd retry
        val testRepository = TestTrackRepository()
        val retryableFailingApi = RetryableFailingTestPCloudApi(failureCount = 2) // Fail twice, then succeed
        val syncServiceWithRetry = BackgroundSyncService(testRepository, retryableFailingApi)

        val startTime = System.currentTimeMillis()

        // Act - Attempt sync with retry mechanism
        val syncResult = syncServiceWithRetry.performIncrementalSyncWithRetry(
            maxRetries = 3,
            baseDelayMs = 100L
        )

        val totalTime = System.currentTimeMillis() - startTime

        // Assert - Verify retry logic works with exponential backoff
        assertTrue("Sync should eventually succeed after retries", syncResult is SyncResult.Success)

        when (syncResult) {
            is SyncResult.Success -> {
                val response = syncResult.data
                assertEquals("Should show 3 total attempts (2 failures + 1 success)", 3, response.totalAttempts)
                assertEquals("Should show 2 retry attempts", 2, response.retryAttempts)
                assertTrue("Should indicate retry was used", response.usedRetryMechanism)
                assertTrue("Total time should be at least base delay sum (100 + 200 = 300ms)", totalTime >= 300)
            }
            is SyncResult.Error -> {
                fail("Sync should succeed after retries: ${syncResult.message}")
            }
        }

        // Verify the API was called the expected number of times
        assertEquals("API should be called 3 times total", 3, retryableFailingApi.callCount)
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

    override suspend fun getTracksPage(offset: Int, limit: Int): List<TrackEntity> {
        return tracks.drop(offset).take(limit)
    }

    override suspend fun getTracksForRange(startIndex: Int, count: Int): List<TrackEntity> {
        return tracks.drop(startIndex).take(count)
    }
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

class RetryableFailingTestPCloudApi(private val failureCount: Int) : PCloudApiInterface {
    var callCount = 0

    override suspend fun listFolderContents(folderId: String): PCloudListFolderResponse {
        callCount++

        return if (callCount <= failureCount) {
            // Return failure for first 'failureCount' calls
            PCloudListFolderResponse(
                result = 2003, // Network error code (simulating network issues)
                metadata = null,
                contents = null
            )
        } else {
            // Return success after failures
            PCloudListFolderResponse(
                result = 0,
                metadata = null,
                contents = listOf(
                    PCloudItem(
                        name = "retry-test-song.mp3",
                        created = "2024-01-01T00:00:00Z",
                        modified = "2024-01-01T00:00:00Z",
                        isFolder = false,
                        folderId = null,
                        fileId = 456L,
                        parentFolderId = 0L,
                        size = 4000000L,
                        contentType = "audio/mpeg",
                        category = 2,
                        id = "retry456"
                    )
                )
            )
        }
    }
}
