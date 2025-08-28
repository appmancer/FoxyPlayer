package com.foxy.player.authentication

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LoginScreenTest {

    @Test
    fun `should render basic login form elements`() {
        // Arrange
        val loginScreen = LoginScreen()

        // Act
        val content = loginScreen.content()

        // Assert - Basic form elements
        assertTrue("Should have username text field", content.hasUsernameTextField)
        assertTrue("Should have password text field", content.hasPasswordTextField)
        assertTrue("Should have login button", content.hasLoginButton)
        assertTrue("Should have server selection field", content.hasServerSelectionField)

        // Assert - Field labels and placeholders (updated for PLY-83)
        assertEquals("Username placeholder should be correct", "Email or Username", content.usernamePlaceholder)
        assertEquals("Password placeholder should be correct", "Password", content.passwordPlaceholder)
        assertEquals("Login button text should be correct", "Login", content.loginButtonText)
        assertEquals("Server placeholder should be correct", "pCloud Server (optional)", content.serverPlaceholder)

        // Assert - Security and UX features
        assertTrue("Password field should be obscured", content.isPasswordFieldObscured)
        assertEquals("Default server should be api.pcloud.com", "api.pcloud.com", content.defaultServerValue)
        assertEquals("Current server should be api.pcloud.com", "api.pcloud.com", content.currentServerValue)
    }

    @Test
    fun `should handle loading state correctly`() {
        // Arrange
        val loginScreen = LoginScreen()

        // Initial state - not loading
        val initialContent = loginScreen.content()
        assertFalse("Should not be loading initially", initialContent.isLoading)
        assertFalse("Login button should not be disabled initially", initialContent.isLoginButtonDisabled)
        assertTrue("Username field should be enabled initially", initialContent.isUsernameEnabled)
        assertTrue("Password field should be enabled initially", initialContent.isPasswordEnabled)
        assertEquals("Login button text should be 'Login'", "Login", initialContent.loginButtonText)

        // Act - Start login process
        loginScreen.onLoginPressed("test@example.com", "password")

        // Assert - Loading state
        val loadingContent = loginScreen.content()
        assertTrue("Should be loading after login pressed", loadingContent.isLoading)
        assertTrue("Login button should be disabled during loading", loadingContent.isLoginButtonDisabled)
        assertFalse("Username field should be disabled during loading", loadingContent.isUsernameEnabled)
        assertFalse("Password field should be disabled during loading", loadingContent.isPasswordEnabled)
        assertEquals(
            "Login button text should change to 'Logging in...'",
            "Logging in...",
            loadingContent.loginButtonText
        )
    }

    @Test
    fun `should handle error state correctly`() {
        // Arrange
        val loginScreen = LoginScreen()
        val errorMessage = "Invalid credentials"

        // Act - Trigger login failure
        loginScreen.onLoginFailed(errorMessage)

        // Assert - Error state
        val errorContent = loginScreen.content()
        assertFalse("Should not be loading after error", errorContent.isLoading)
        assertTrue("Should have error message", errorContent.hasErrorMessage)
        assertEquals("Error message should match", errorMessage, errorContent.errorMessage)
        assertFalse("Login button should not be disabled after error", errorContent.isLoginButtonDisabled)
        assertTrue("Username field should be enabled after error", errorContent.isUsernameEnabled)
        assertTrue("Password field should be enabled after error", errorContent.isPasswordEnabled)
        assertEquals("Login button text should be 'Login' after error", "Login", errorContent.loginButtonText)
    }

    @Test
    fun `should handle server selection changes`() {
        // Arrange
        val loginScreen = LoginScreen()
        val newServerValue = "eapi.pcloud.com"

        // Initial state
        val initialContent = loginScreen.content()
        assertEquals("Initial server should be api.pcloud.com", "api.pcloud.com", initialContent.currentServerValue)

        // Act - Change server
        loginScreen.onServerChanged(newServerValue)

        // Assert - Server change
        val updatedContent = loginScreen.content()
        assertEquals("Server value should be updated", newServerValue, updatedContent.currentServerValue)
        assertTrue("Server field should remain enabled", updatedContent.isServerFieldEnabled)
    }

    @Test
    fun `should clear error message when login is pressed again`() {
        // Arrange
        val loginScreen = LoginScreen()

        // First, set an error state
        loginScreen.onLoginFailed("Previous error")
        val errorContent = loginScreen.content()
        assertTrue("Should have error message", errorContent.hasErrorMessage)

        // Act - Press login again
        loginScreen.onLoginPressed("test@example.com", "password")

        // Assert - Error should be cleared
        val clearedContent = loginScreen.content()
        assertFalse("Should not have error message after new login attempt", clearedContent.hasErrorMessage)
        assertNull("Error message should be null", clearedContent.errorMessage)
        assertTrue("Should be in loading state", clearedContent.isLoading)
    }

    @Test
    fun `should maintain field enablement consistency during state transitions`() {
        // Arrange
        val loginScreen = LoginScreen()

        // Test: Normal -> Loading -> Error -> Normal cycle

        // 1. Normal state
        val normalContent = loginScreen.content()
        assertTrue("Username enabled in normal state", normalContent.isUsernameEnabled)
        assertTrue("Password enabled in normal state", normalContent.isPasswordEnabled)
        assertTrue("Server enabled in normal state", normalContent.isServerFieldEnabled)
        assertFalse("Button not disabled in normal state", normalContent.isLoginButtonDisabled)

        // 2. Loading state
        loginScreen.onLoginPressed("test@example.com", "password")
        val loadingContent = loginScreen.content()
        assertFalse("Username disabled in loading state", loadingContent.isUsernameEnabled)
        assertFalse("Password disabled in loading state", loadingContent.isPasswordEnabled)
        assertTrue("Server remains enabled in loading state", loadingContent.isServerFieldEnabled)
        assertTrue("Button disabled in loading state", loadingContent.isLoginButtonDisabled)

        // 3. Error state
        loginScreen.onLoginFailed("Network error")
        val errorContent = loginScreen.content()
        assertTrue("Username enabled in error state", errorContent.isUsernameEnabled)
        assertTrue("Password enabled in error state", errorContent.isPasswordEnabled)
        assertTrue("Server enabled in error state", errorContent.isServerFieldEnabled)
        assertFalse("Button not disabled in error state", errorContent.isLoginButtonDisabled)
    }

    @Test
    fun `should handle multiple server changes`() {
        // Arrange
        val loginScreen = LoginScreen()

        // Test multiple server changes
        val servers = listOf("api.pcloud.com", "eapi.pcloud.com", "custom.pcloud.server")

        for (server in servers) {
            // Act
            loginScreen.onServerChanged(server)

            // Assert
            val content = loginScreen.content()
            assertEquals("Server should be updated to $server", server, content.currentServerValue)
        }
    }

    @Test
    fun `should maintain server field independence from other states`() {
        // Arrange
        val loginScreen = LoginScreen()

        // Change server, then enter loading state
        loginScreen.onServerChanged("eapi.pcloud.com")
        loginScreen.onLoginPressed("test@example.com", "password")

        val loadingContent = loginScreen.content()
        assertEquals("Server value should persist during loading", "eapi.pcloud.com", loadingContent.currentServerValue)
        assertTrue("Server field should remain enabled during loading", loadingContent.isServerFieldEnabled)

        // Enter error state
        loginScreen.onLoginFailed("Error occurred")

        val errorContent = loginScreen.content()
        assertEquals("Server value should persist during error", "eapi.pcloud.com", errorContent.currentServerValue)
        assertTrue("Server field should remain enabled during error", errorContent.isServerFieldEnabled)
    }

    @Test
    fun `should handle edge cases in error messages`() {
        // Arrange
        val loginScreen = LoginScreen()

        // Test empty error message
        loginScreen.onLoginFailed("")
        val emptyErrorContent = loginScreen.content()
        assertTrue("Should have error flag even with empty message", emptyErrorContent.hasErrorMessage)
        assertEquals("Error message should be empty string", "", emptyErrorContent.errorMessage)

        // Test null-like error message handling through onLoginPressed (clears error)
        loginScreen.onLoginPressed("test", "test")
        val clearedContent = loginScreen.content()
        assertFalse("Error should be cleared", clearedContent.hasErrorMessage)
        assertNull("Error message should be null after clearing", clearedContent.errorMessage)
    }

    @Test
    fun `should preserve security features across all states`() {
        // Arrange
        val loginScreen = LoginScreen()

        // Test password obscuring in different states
        val states = listOf(
            { loginScreen.content() }, // Normal
            { loginScreen.onLoginPressed("test", "test"); loginScreen.content() }, // Loading
            { loginScreen.onLoginFailed("error"); loginScreen.content() } // Error
        )

        for (getState in states) {
            val content = getState()
            assertTrue("Password should always be obscured", content.isPasswordFieldObscured)
        }
    }
}
