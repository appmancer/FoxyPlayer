package com.foxy.player.music

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MusicLibraryHubTest {

    @Test
    fun `should create Music Library Hub screen with Material3 components`() {
        // Arrange - create hub screen instance
        val hubScreen = MusicLibraryHubScreen()

        // Act - get the screen content
        val content = hubScreen.getContent()

        // Assert - verify Material3 components are present
        assertNotNull("Hub screen content should not be null", content)
        assertTrue("Should have Material3 cards", content.hasMaterial3Cards)
        assertTrue("Should have bottom navigation", content.hasBottomNavigation)
        assertTrue("Should have top app bar", content.hasTopAppBar)
        assertTrue("Should have FAB", content.hasFAB)
        assertEquals("Should have 5 navigation tabs", 5, content.navigationTabs.size)
        assertTrue("Should include Home tab", content.navigationTabs.contains("Home"))
        assertTrue("Should include Songs tab", content.navigationTabs.contains("Songs"))
        assertTrue("Should include Artists tab", content.navigationTabs.contains("Artists"))
        assertTrue("Should include Folders tab", content.navigationTabs.contains("Folders"))
        assertTrue("Should include Settings tab", content.navigationTabs.contains("Settings"))
    }
}
