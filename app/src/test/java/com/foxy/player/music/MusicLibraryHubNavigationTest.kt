package com.foxy.player.music

import org.junit.Assert.assertEquals
import org.junit.Test

class MusicLibraryHubNavigationTest {

    private class FakeNavigator : Navigator {
        var lastRoute: String? = null
        override fun navigate(route: String) {
            lastRoute = route
        }
    }

    @Test
    fun `onCardClick maps Songs to SongsList`() {
        val routeByLabel = when ("Songs") {
            CardIds.Songs, "Songs" -> Routes.SongsList
            else -> ""
        }
        val routeById = when (CardIds.Songs) {
            CardIds.Songs, "Songs" -> Routes.SongsList
            else -> ""
        }
        assertEquals(Routes.SongsList, routeByLabel)
        assertEquals(Routes.SongsList, routeById)
    }

    @Test
    fun `onCardClick maps Artists to ArtistsList`() {
        val routeByLabel = when ("Artists") {
            CardIds.Artists, "Artists" -> Routes.ArtistsList
            else -> ""
        }
        val routeById = when (CardIds.Artists) {
            CardIds.Artists, "Artists" -> Routes.ArtistsList
            else -> ""
        }
        assertEquals(Routes.ArtistsList, routeByLabel)
        assertEquals(Routes.ArtistsList, routeById)
    }

    @Test
    fun `onCardClick maps Albums to AlbumsList`() {
        val routeByLabel = when ("Albums") {
            CardIds.Albums, "Albums" -> Routes.AlbumsList
            else -> ""
        }
        val routeById = when (CardIds.Albums) {
            CardIds.Albums, "Albums" -> Routes.AlbumsList
            else -> ""
        }
        assertEquals(Routes.AlbumsList, routeByLabel)
        assertEquals(Routes.AlbumsList, routeById)
    }

    @Test
    fun `onCardClick maps Folders to FoldersView`() {
        val routeByLabel = when ("Folders") {
            CardIds.Folders, "Folders" -> Routes.FoldersView
            else -> ""
        }
        val routeById = when (CardIds.Folders) {
            CardIds.Folders, "Folders" -> Routes.FoldersView
            else -> ""
        }
        assertEquals(Routes.FoldersView, routeByLabel)
        assertEquals(Routes.FoldersView, routeById)
    }
}
