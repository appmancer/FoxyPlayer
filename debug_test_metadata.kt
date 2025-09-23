package com.foxy.player.music

import com.foxy.player.music.business.MetadataValidator
import com.foxy.player.music.entities.AudioMetadata
import org.junit.Test

class DebugMetadataValidationTest {

    @Test
    fun `debug metadata validation errors`() {
        val inconsistentMetadata = AudioMetadata(
            title = "",
            artist = "Queen",
            album = "A Night at the Opera", 
            durationMs = -100L,
            format = "INVALID_FORMAT",
            bitrate = -1,
            trackNumber = 0
        )
        val validator = MetadataValidator()
        val result = validator.validateMetadata(inconsistentMetadata)
        
        println("Validation errors: ${result.validationErrors}")
        println("Has title error: ${result.validationErrors.any { it.contains("title") }}")
        println("Has duration error: ${result.validationErrors.any { it.contains("duration") }}")
        println("Has bitrate error: ${result.validationErrors.any { it.contains("bitrate") }}")
        println("Error count: ${result.validationErrors.size}")
    }
}