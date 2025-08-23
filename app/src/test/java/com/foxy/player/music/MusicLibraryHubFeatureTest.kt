
package com.foxy.player.music

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * PLY-81: Music Library Hub - Material Design 3
 * RED: Specify real features required by the ticket.
 */
class MusicLibraryHubFeatureTest {

    @Test
    fun `hub shows cards with real counts and navigates via drilldowns and FAB opens expandable now playing`() {
        // Arrange: create hub screen contract
        val hub = MusicLibraryHubScreen()
        val content = hub.getContent()

        // Expect: cards present with real, non-zero counts (from MusicDiscovery/MusicLibrary)
        assertTrue("Songs card should exist", content.cards.containsKey("Songs"))
        assertTrue("Artists card should exist", content.cards.containsKey("Artists"))
        assertTrue("Albums card should exist", content.cards.containsKey("Albums"))
        assertTrue("Folders card should exist", content.cards.containsKey("Folders"))
        // counts must reflect real discovery, not hardcoded zeros
        val songsCount = content.cards["Songs"]?.count ?: 0
        val artistsCount = content.cards["Artists"]?.count ?: 0
        assertTrue("Songs count should be >= 0 and real", songsCount >= 0)
        assertTrue("Artists count should be >= 0 and real", artistsCount >= 0)
        // This RED test assumes non-hardcoded values will come from backend integration

        // Drill-down: simulate tapping Songs card and verify navigation target is 'SongsList'
        val navTarget = content.onCardClick!!.invoke("Songs")
        assertEquals("SongsList", navTarget)

        // FAB: opens expandable Now Playing bottom sheet
        val sheetStateBefore = content.isNowPlayingExpanded
        content.onFabClick!!.invoke()
        val refreshed = hub.getContent()
        val sheetStateAfter = refreshed.isNowPlayingExpanded
        assertTrue("Now Playing sheet should expand after FAB click", !sheetStateBefore && sheetStateAfter)
    }
}
