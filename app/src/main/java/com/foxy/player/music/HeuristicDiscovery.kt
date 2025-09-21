package com.foxy.player.music

import com.foxy.player.music.network.MusicDiscoveryService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

data class Song(val path: String, val title: String, val artist: String, val album: String)

data class ArtistSummary(val name: String, val songCount: Int)

data class AlbumSummary(val name: String, val artist: String, val songCount: Int)

private val AUDIO_EXTS = setOf("mp3", "flac", "m4a", "ogg", "wav", "aac")

class HeuristicMusicDiscovery(private val service: MusicDiscoveryService) {
    suspend fun listSongs(): Result<List<Song>> = runCatching {
        val startTime = System.currentTimeMillis()
        com.foxy.player.music.network.MusicDiscoveryLogger.logUiDataFlow("HeuristicMusicDiscovery", "UI", "songs", 0)

        try {
            val allFiles = listAllFilesRecursively("/")
            val songs = allFiles.filter { f -> f.lowercase().substringAfterLast('.', "") in AUDIO_EXTS }
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

            val duration = System.currentTimeMillis() - startTime
            com.foxy.player.music.network.MusicDiscoveryLogger.logUiDataBinding(
                "HeuristicMusicDiscovery",
                "listSongs",
                songs.size
            )
            com.foxy.player.music.network.MusicDiscoveryLogger.logUiPerformance(
                "HeuristicMusicDiscovery",
                "listSongs",
                duration
            )
            songs
        } catch (e: Exception) {
            com.foxy.player.music.network.MusicDiscoveryLogger.logUiError("HeuristicMusicDiscovery", "listSongs", e)
            throw e
        }
    }

    suspend fun listArtists(): Result<List<ArtistSummary>> = listSongs().map { songs ->
        val startTime = System.currentTimeMillis()
        com.foxy.player.music.network.MusicDiscoveryLogger.logUiDataFlow(
            "HeuristicMusicDiscovery",
            "UI",
            "artists_from_songs",
            songs.size
        )

        try {
            val artists = songs.groupBy { it.artist.ifBlank { "Unknown Artist" } }
                .map { (artist, s) -> ArtistSummary(artist, s.size) }
                .sortedBy { it.name.lowercase() }

            val duration = System.currentTimeMillis() - startTime
            com.foxy.player.music.network.MusicDiscoveryLogger.logUiDataBinding(
                "HeuristicMusicDiscovery",
                "listArtists",
                artists.size
            )
            com.foxy.player.music.network.MusicDiscoveryLogger.logUiPerformance(
                "HeuristicMusicDiscovery",
                "listArtists",
                duration
            )
            artists
        } catch (e: Exception) {
            com.foxy.player.music.network.MusicDiscoveryLogger.logUiError("HeuristicMusicDiscovery", "listArtists", e)
            throw e
        }
    }

    suspend fun listAlbums(): Result<List<AlbumSummary>> = listSongs().map { songs ->
        val startTime = System.currentTimeMillis()
        com.foxy.player.music.network.MusicDiscoveryLogger.logUiDataFlow(
            "HeuristicMusicDiscovery",
            "UI",
            "albums_from_songs",
            songs.size
        )

        try {
            val albums = songs.groupBy { it.artist to it.album.ifBlank { "Unknown Album" } }
                .map { (aa, s) -> AlbumSummary(name = aa.second, artist = aa.first, songCount = s.size) }
                .sortedWith(compareBy({ it.artist.lowercase() }, { it.name.lowercase() }))

            val duration = System.currentTimeMillis() - startTime
            com.foxy.player.music.network.MusicDiscoveryLogger.logUiDataBinding(
                "HeuristicMusicDiscovery",
                "listAlbums",
                albums.size
            )
            com.foxy.player.music.network.MusicDiscoveryLogger.logUiPerformance(
                "HeuristicMusicDiscovery",
                "listAlbums",
                duration
            )
            albums
        } catch (e: Exception) {
            com.foxy.player.music.network.MusicDiscoveryLogger.logUiError("HeuristicMusicDiscovery", "listAlbums", e)
            throw e
        }
    }

    // Utilities
    private suspend fun listAllFilesRecursively(root: String): List<String> = coroutineScope {
        System.out.println("🔍 HEURISTIC_DEBUG: listAllFilesRecursively called with root: '$root'")
        android.util.Log.d("HeuristicDiscovery", "🔍 listAllFilesRecursively called with root: '$root'")

        val listingResult = withContext(Dispatchers.IO) {
            service.listPCloudFolders(root)
        }
        val listing = listingResult.getOrNull()

        if (listing == null) {
            System.out.println("🚨 HEURISTIC_DEBUG: listPCloudFolders returned null for root: '$root'")
            android.util.Log.w("HeuristicDiscovery", "⚠️ listPCloudFolders returned null for root: '$root'")
            val error = listingResult.exceptionOrNull()
            System.out.println("🚨 HEURISTIC_DEBUG: Exception: ${error?.message}")
            android.util.Log.w("HeuristicDiscovery", "⚠️ Exception: ${error?.message}", error)
            return@coroutineScope emptyList()
        }

        System.out.println("📂 HEURISTIC_DEBUG: Folder listing for '$root':")
        System.out.println("📂 HEURISTIC_DEBUG:   Files found: ${listing.files.size}")
        System.out.println("📂 HEURISTIC_DEBUG:   Folders found: ${listing.folders.size}")
        System.out.println("📂 HEURISTIC_DEBUG:   Files: ${listing.files}")
        System.out.println("📂 HEURISTIC_DEBUG:   Folders: ${listing.folders}")
        android.util.Log.d("HeuristicDiscovery", "📂 Folder listing for '$root':")
        android.util.Log.d("HeuristicDiscovery", "  Files found: ${listing.files.size}")
        android.util.Log.d("HeuristicDiscovery", "  Folders found: ${listing.folders.size}")
        android.util.Log.d("HeuristicDiscovery", "  Files: ${listing.files}")
        android.util.Log.d("HeuristicDiscovery", "  Folders: ${listing.folders}")

        val files = listing.files.map { name -> if (root == "/") "/$name" else "$root/$name" }
        System.out.println("📂 HEURISTIC_DEBUG:   Mapped files: $files")
        android.util.Log.d("HeuristicDiscovery", "  Mapped files: $files")

        val folderFiles = listing.folders.map { folderName ->
            val child = if (root == "/") "/$folderName" else "$root/$folderName"
            System.out.println("📁 HEURISTIC_DEBUG:   Recursively scanning folder: '$child'")
            android.util.Log.d("HeuristicDiscovery", "  📁 Recursively scanning folder: '$child'")
            async { listAllFilesRecursively(child) }
        }.flatMap { it.await() }

        val allFiles = files + folderFiles
        System.out.println("📋 HEURISTIC_DEBUG: Total files found in '$root': ${allFiles.size}")
        System.out.println("📋 HEURISTIC_DEBUG: All files: $allFiles")
        android.util.Log.d("HeuristicDiscovery", "📋 Total files found in '$root': ${allFiles.size}")
        android.util.Log.d("HeuristicDiscovery", "📋 All files: $allFiles")

        allFiles
    }
}
