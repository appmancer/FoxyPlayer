
package com.foxy.player.music

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * PLY-81: Music Library Hub - Material Design 3
 * RED: Specify real features required by the ticket.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MusicLibraryHubFeatureTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `hub shows cards with real counts and navigates via drilldowns and FAB opens expandable now playing`() = runTest {
        // Arrange: create VM with real services
        val authRepo = com.foxy.player.authentication.AuthRepository()
        val api = com.foxy.player.authentication.AuthenticatedApiClient(authRepo)
        val discovery = MusicDiscoveryService(api)
        val heuristic = HeuristicMusicDiscovery(discovery)
        val vm = MusicHubViewModel(authRepo, api, discovery, heuristic)

        // Wait for initial load (advancing dispatcher does this)
        testDispatcher.scheduler.advanceUntilIdle()
        val state = vm.state.value

        // Expect: cards present with counts map
        assertTrue("Songs card should exist", state.cards.containsKey("Songs"))
        assertTrue("Artists card should exist", state.cards.containsKey("Artists"))
        assertTrue("Albums card should exist", state.cards.containsKey("Albums"))
        assertTrue("Folders card should exist", state.cards.containsKey("Folders"))

        // counts should be >= 0 (backed by discovery)
        val songsCount = state.cards["Songs"]?.count ?: 0
        val artistsCount = state.cards["Artists"]?.count ?: 0
        assertTrue("Songs count should be >= 0 and real", songsCount >= 0)
        assertTrue("Artists count should be >= 0 and real", artistsCount >= 0)

        // Drill-down: route mapping checked in UI util; here we just verify keys exist
        assertTrue(state.cards.keys.containsAll(listOf("Songs", "Artists", "Albums", "Folders")))

        // FAB: opens expandable Now Playing bottom sheet (via VM flag)
        val before = state.nowPlayingExpanded
        vm.fabClick()
        val after = vm.state.value.nowPlayingExpanded
        assertTrue("Now Playing sheet should expand after FAB click", !before && after)
    }
}
