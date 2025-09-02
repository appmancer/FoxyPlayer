package com.foxy.player.music

import com.foxy.player.music.network.MusicDiscoveryService
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

data class Song(val path: String, val title: String, val artist: String, val album: String)

data class ArtistSummary(val name: String, val songCount: Int)

data class AlbumSummary(val name: String, val artist: String, val songCount: Int)

private val AUDIO_EXTS = setOf("mp3", "flac", "m4a", "ogg", "wav", "aac")

class HeuristicMusicDiscovery(private val service: MusicDiscoveryService) {
    suspend fun listSongs(): Result<List<Song>> = runCatching {
        val allFiles = listAllFilesRecursively("/")
        allFiles.filter { f -> f.lowercase().substringAfterLast('.', "") in AUDIO_EXTS }
            .map { path ->
                val name = path.substringAfterLast('/')
                val parts = path.trim('/').split('/')
                val (artist, album) = when {
                    parts.size >= 3 -> parts[parts.size - 3] to parts[parts.size - 2]
                    parts.size >= 2 -> parts[parts.size - 2] to "Unknown Album"
                    else -> "Unknown Artist" to "Unknown Album"
                }
                val title = name.substringBeforeLast('.')
                Song(path = path, title = title, artist = artist, album = album)
            }
    }

    suspend fun listArtists(): Result<List<ArtistSummary>> = listSongs().map { songs ->
        songs.groupBy { it.artist.ifBlank { "Unknown Artist" } }
            .map { (artist, s) -> ArtistSummary(artist, s.size) }
            .sortedBy { it.name.lowercase() }
    }

    suspend fun listAlbums(): Result<List<AlbumSummary>> = listSongs().map { songs ->
        songs.groupBy { it.artist to it.album.ifBlank { "Unknown Album" } }
            .map { (aa, s) -> AlbumSummary(name = aa.second, artist = aa.first, songCount = s.size) }
            .sortedWith(compareBy({ it.artist.lowercase() }, { it.name.lowercase() }))
    }

    // Utilities
    private suspend fun listAllFilesRecursively(root: String): List<String> = coroutineScope {
        val listingResult = service.listPCloudFolders(root)
        val listing = listingResult.getOrNull() ?: return@coroutineScope emptyList()

        val files = listing.files.map { name -> if (root == "/") "/$name" else "$root/$name" }

        val folderFiles = listing.folders.map { folderName ->
            val child = if (root == "/") "/$folderName" else "$root/$folderName"
            async { listAllFilesRecursively(child) }
        }.flatMap { it.await() }

        files + folderFiles
    }
}
