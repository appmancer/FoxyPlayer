package com.foxy.player.music

/** Simple navigation contract to decouple UI from Android NavController in unit tests. */
interface Navigator {
    fun navigate(route: String)
}

object Routes {
    const val Home = "Home"
    const val SongsList = "SongsList"
    const val ArtistsList = "ArtistsList"
    const val AlbumsList = "AlbumsList"
    const val FoldersView = "FoldersView"
    val all = setOf(Home, SongsList, ArtistsList, AlbumsList, FoldersView)
}

object CardIds {
    const val Songs = "Songs"
    const val Artists = "Artists"
    const val Albums = "Albums"
    const val Folders = "Folders"
}
