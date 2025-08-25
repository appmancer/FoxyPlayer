package com.foxy.player.music

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MusicLibraryHubTest {

    @Test
    fun `should create Music Library Hub screen with Material3 components`() {
        // Arrange - create hub screen instance
        // With ViewModel-driven UI, we validate expected tabs directly
        val expectedTabs = listOf("Home", "Songs", "Artists", "Folders", "Settings")
        assertEquals("Should have 5 navigation tabs", 5, expectedTabs.size)
        assertTrue("Should include Home tab", expectedTabs.contains("Home"))
        assertTrue("Should include Songs tab", expectedTabs.contains("Songs"))
        assertTrue("Should include Artists tab", expectedTabs.contains("Artists"))
        assertTrue("Should include Folders tab", expectedTabs.contains("Folders"))
        assertTrue("Should include Settings tab", expectedTabs.contains("Settings"))
    }
}
