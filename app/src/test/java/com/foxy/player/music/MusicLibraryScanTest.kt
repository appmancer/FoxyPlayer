package com.foxy.player.music

import com.foxy.player.authentication.AuthRepository
import com.foxy.player.authentication.AuthenticatedApiClient
import java.io.File
import java.nio.file.Files
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class MusicLibraryScanTest {

    @Test
    fun `should scan directory and find real audio files with progress updates`() = runBlocking {
        // Arrange - create temporary directory with real audio files
        val tempDir = Files.createTempDirectory("music_scan_test").toFile()
        try {
            // Create real audio files for testing
            val mp3File = File(tempDir, "test_song.mp3")
            val wavFile = File(tempDir, "test_audio.wav")
            val flacFile = File(tempDir, "test_music.flac")
            val txtFile = File(tempDir, "not_audio.txt") // Non-audio file

            mp3File.createNewFile()
            wavFile.createNewFile()
            flacFile.createNewFile()
            txtFile.createNewFile()

            val authRepository = AuthRepository("https://eapi.pcloud.com")
            val authenticatedApiClient = AuthenticatedApiClient(authRepository)
            val scanProgressService = MusicLibraryScanProgressService(authenticatedApiClient)

            val progressUpdates = mutableListOf<ScanProgressUpdate>()

            // Act - scan the directory with progress tracking
            val result = scanProgressService.scanLibraryWithProgress(listOf(tempDir.absolutePath)) { progress ->
                progressUpdates.add(progress)
            }

            // Assert - verify real audio files were found and progress was tracked
            assertTrue("Scan should be successful", result.isSuccess)
            val scanResponse = result.getOrNull()!!

            assertTrue("Should find at least 3 audio files", scanResponse.totalFilesFound >= 3)
            assertTrue("Should have progress updates", progressUpdates.isNotEmpty())
            assertTrue("Should track progress percentage", progressUpdates.any { it.percentComplete >= 0.0 })
            assertTrue("Should show current operation", progressUpdates.any { it.currentOperation.isNotEmpty() })

            // Verify the scanner correctly identifies audio files
            val audioFileScanner = RealAudioFileScanner()
            assertTrue("Should identify MP3 as audio", audioFileScanner.isAudioFile("test_song.mp3"))
            assertTrue("Should identify WAV as audio", audioFileScanner.isAudioFile("test_audio.wav"))
            assertTrue("Should identify FLAC as audio", audioFileScanner.isAudioFile("test_music.flac"))
            assertFalse("Should not identify TXT as audio", audioFileScanner.isAudioFile("not_audio.txt"))
        } finally {
            // Clean up - delete temporary directory
            tempDir.deleteRecursively()
        }
    }
}
