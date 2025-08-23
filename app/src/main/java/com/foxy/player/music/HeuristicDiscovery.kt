package com.foxy.player.music

data class Song(val path: String, val title: String, val artist: String, val album: String)

data class ArtistSummary(val name: String, val songCount: Int)

data class AlbumSummary(val name: String, val artist: String, val songCount: Int)

private val AUDIO_EXTS = setOf("mp3", "flac", "m4a", "ogg", "wav", "aac")

class HeuristicMusicDiscovery(private val service: MusicDiscoveryService) {
    fun listSongs(): Result<List<Song>> = runCatching {
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

    fun listArtists(): Result<List<ArtistSummary>> = listSongs().map { songs ->
        songs.groupBy { it.artist.ifBlank { "Unknown Artist" } }
            .map { (artist, s) -> ArtistSummary(artist, s.size) }
            .sortedBy { it.name.lowercase() }
    }

    fun listAlbums(): Result<List<AlbumSummary>> = listSongs().map { songs ->
        songs.groupBy { it.artist to it.album.ifBlank { "Unknown Album" } }
            .map { (aa, s) -> AlbumSummary(name = aa.second, artist = aa.first, songCount = s.size) }
            .sortedWith(compareBy({ it.artist.lowercase() }, { it.name.lowercase() }))
    }

    // Utilities
    private fun listAllFilesRecursively(root: String): List<String> {
        val files = mutableListOf<String>()
        val visited = mutableSetOf<String>()
        fun walk(path: String, depth: Int) {
            if (depth > 5) return
            if (!visited.add(path)) return
            val listing = service.listPCloudFolders(path).getOrNull() ?: return
            listing.files.forEach { name ->
                val full = if (path == "/") "/$name" else "$path/$name"
                files += full
            }
            listing.folders.forEach { folderName ->
                val child = if (path == "/") "/$folderName" else "$path/$folderName"
                walk(child, depth + 1)
            }
        }
        walk(root, 0)
        return files
    }
}
