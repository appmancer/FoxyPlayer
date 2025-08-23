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
        val hub = MusicLibraryHubScreen()
        val routeByLabel = hub.getContent().onCardClick?.invoke("Songs")
        val routeById = hub.getContent().onCardClick?.invoke(CardIds.Songs)
        assertEquals(Routes.SongsList, routeByLabel)
        assertEquals(Routes.SongsList, routeById)
    }

    @Test
    fun `onCardClick maps Artists to ArtistsList`() {
        val hub = MusicLibraryHubScreen()
        val routeByLabel = hub.getContent().onCardClick?.invoke("Artists")
        val routeById = hub.getContent().onCardClick?.invoke(CardIds.Artists)
        assertEquals(Routes.ArtistsList, routeByLabel)
        assertEquals(Routes.ArtistsList, routeById)
    }

    @Test
    fun `onCardClick maps Albums to AlbumsList`() {
        val hub = MusicLibraryHubScreen()
        val routeByLabel = hub.getContent().onCardClick?.invoke("Albums")
        val routeById = hub.getContent().onCardClick?.invoke(CardIds.Albums)
        assertEquals(Routes.AlbumsList, routeByLabel)
        assertEquals(Routes.AlbumsList, routeById)
    }

    @Test
    fun `onCardClick maps Folders to FoldersView`() {
        val hub = MusicLibraryHubScreen()
        val routeByLabel = hub.getContent().onCardClick?.invoke("Folders")
        val routeById = hub.getContent().onCardClick?.invoke(CardIds.Folders)
        assertEquals(Routes.FoldersView, routeByLabel)
        assertEquals(Routes.FoldersView, routeById)
    }
}
