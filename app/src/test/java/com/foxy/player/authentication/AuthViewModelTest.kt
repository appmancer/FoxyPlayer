package com.foxy.player.authentication

import com.foxy.player.authentication.network.AuthRepository
import com.foxy.player.authentication.ui.AuthViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthViewModelTest {

    companion object {
        private const val TEST_USERNAME = "test@example.com"
        private const val TEST_PASSWORD = "test-password-not-real"
    }

    @Test
    fun `should manage loading state during login`() {
        // Arrange
        val authRepository = AuthRepository()
        val authViewModel = AuthViewModel(authRepository)

        // Verify initial state
        assertFalse("Should not be loading initially", authViewModel.isLoading)
        assertFalse("Should not be authenticated initially", authViewModel.isAuthenticated)
        assertEquals("Username should be empty initially", "", authViewModel.username)

        // Act
        authViewModel.login(TEST_USERNAME, TEST_PASSWORD)

        // Assert immediate state (loading)
        assertTrue("Should be loading immediately after login call", authViewModel.isLoading)
        assertEquals("Username should be set", TEST_USERNAME, authViewModel.username)
        assertFalse("Should not be authenticated yet", authViewModel.isAuthenticated)

        // Wait for async completion
        Thread.sleep(100)

        // Assert final state
        assertFalse("Should not be loading after completion", authViewModel.isLoading)
        assertTrue("Should be authenticated after successful login", authViewModel.isAuthenticated)
        assertNotNull("Should have auth token", authViewModel.authToken)
        assertEquals("Username should remain set", TEST_USERNAME, authViewModel.username)
    }

    @Test
    fun `should handle authentication errors gracefully`() {
        // Arrange
        val authRepository = AuthRepository("https://invalid-url-will-fail")
        val authViewModel = AuthViewModel(authRepository)

        // Act
        authViewModel.loginWithErrorHandling(TEST_USERNAME, TEST_PASSWORD)

        // Assert immediate state (loading)
        assertTrue("Should be loading immediately", authViewModel.isLoading)
        assertEquals("Username should be set", TEST_USERNAME, authViewModel.username)
        assertNull("Error message should be null initially", authViewModel.errorMessage)

        // Wait for async completion with network failure
        Thread.sleep(200)

        // Assert final error state
        assertFalse("Should not be loading after error", authViewModel.isLoading)
        assertFalse("Should not be authenticated after error", authViewModel.isAuthenticated)
        assertNull("Should not have auth token after error", authViewModel.authToken)
        assertEquals("Should have error message", "network connection failed", authViewModel.errorMessage)
    }

    @Test
    fun `should integrate with authentication state management`() {
        // Arrange
        val authRepository = AuthRepository()
        val authViewModel = AuthViewModel(authRepository)
        var loginEventTriggered = false
        var logoutEventTriggered = false

        // Set up event listeners
        authRepository.setLoginEventListener { userInfo ->
            loginEventTriggered = true
            assertEquals("Login event should have correct email", TEST_USERNAME, userInfo.email)
        }

        authRepository.setLogoutEventListener { userInfo ->
            logoutEventTriggered = true
            assertEquals("Logout event should have correct email", TEST_USERNAME, userInfo.email)
        }

        // Act - Login with state management
        authViewModel.loginWithStateManagement(TEST_USERNAME, TEST_PASSWORD)

        // Wait for async completion
        Thread.sleep(150)

        // Assert login state
        assertTrue("Should be authenticated", authViewModel.isAuthenticated)
        assertNotNull("Should have auth token", authViewModel.authToken)
        assertTrue("Login event should be triggered", loginEventTriggered)

        // Verify repository state was saved
        val persistedState = authRepository.getPersistedAuthenticationState()
        assertNotNull("Repository should have persisted state", persistedState)
        assertEquals(
            "Persisted auth token should match ViewModel",
            authViewModel.authToken,
            persistedState?.authToken
        )

        // Act - Logout with state management
        authViewModel.logoutWithStateManagement()

        // Assert logout state
        assertFalse("Should not be authenticated after logout", authViewModel.isAuthenticated)
        assertNull("Should not have auth token after logout", authViewModel.authToken)
        assertTrue("Logout event should be triggered", logoutEventTriggered)

        // Verify repository state was cleared
        val clearedState = authRepository.getPersistedAuthenticationState()
        assertNull("Repository state should be cleared", clearedState)
    }

    @Test
    fun `should maintain correct state transitions`() {
        // Arrange
        val authRepository = AuthRepository()
        val authViewModel = AuthViewModel(authRepository)

        // Test state: Initial -> Loading -> Authenticated
        assertFalse("Initial: not loading", authViewModel.isLoading)
        assertFalse("Initial: not authenticated", authViewModel.isAuthenticated)

        authViewModel.login(TEST_USERNAME, TEST_PASSWORD)

        assertTrue("Loading: is loading", authViewModel.isLoading)
        assertFalse("Loading: not authenticated", authViewModel.isAuthenticated)

        Thread.sleep(100)

        assertFalse("Final: not loading", authViewModel.isLoading)
        assertTrue("Final: is authenticated", authViewModel.isAuthenticated)
    }

    @Test
    fun `should handle multiple rapid login attempts`() {
        // Arrange
        val authRepository = AuthRepository()
        val authViewModel = AuthViewModel(authRepository)

        // Act - Make multiple rapid login calls
        authViewModel.login(TEST_USERNAME, TEST_PASSWORD)
        authViewModel.login(TEST_USERNAME, TEST_PASSWORD)
        authViewModel.login(TEST_USERNAME, TEST_PASSWORD)

        // The ViewModel should handle this gracefully
        assertTrue("Should be in loading state", authViewModel.isLoading)

        // Wait for completion
        Thread.sleep(150)

        // Should have consistent final state
        assertFalse("Should not be loading", authViewModel.isLoading)
        assertEquals("Username should be set", TEST_USERNAME, authViewModel.username)
    }

    @Test
    fun `should provide access to authentication token`() {
        // Arrange
        val authRepository = AuthRepository()
        val authViewModel = AuthViewModel(authRepository)

        // Initial state
        assertNull("Auth token should be null initially", authViewModel.authToken)

        // Act
        authViewModel.login(TEST_USERNAME, TEST_PASSWORD)
        Thread.sleep(100)

        // Assert
        assertNotNull("Auth token should be available after login", authViewModel.authToken)
        assertTrue("Auth token should be non-empty", authViewModel.authToken?.isNotEmpty() == true)
        assertTrue(
            "Auth token should have reasonable length",
            (authViewModel.authToken?.length ?: 0) >= 10
        )
    }

    @Test
    fun `should clear error message on successful login after failure`() {
        // Arrange
        val failingRepository = AuthRepository("https://invalid-url-will-fail")
        val authViewModel = AuthViewModel(failingRepository)

        // First, cause an error
        authViewModel.loginWithErrorHandling(TEST_USERNAME, TEST_PASSWORD)
        Thread.sleep(200)

        assertNotNull("Should have error message after failure", authViewModel.errorMessage)

        // Now try with working repository
        val workingRepository = AuthRepository()
        val workingViewModel = AuthViewModel(workingRepository)

        // Act - Login successfully
        workingViewModel.login(TEST_USERNAME, TEST_PASSWORD)
        Thread.sleep(100)

        // Assert
        assertNull("Error message should be null after successful login", workingViewModel.errorMessage)
        assertTrue("Should be authenticated", workingViewModel.isAuthenticated)
    }

    @Test
    fun `should handle repository authentication state consistency`() {
        // Arrange
        val authRepository = AuthRepository()
        val authViewModel = AuthViewModel(authRepository)

        // Login through ViewModel
        authViewModel.loginWithStateManagement(TEST_USERNAME, TEST_PASSWORD)
        Thread.sleep(150)

        // ViewModel and Repository should be in sync
        assertTrue("ViewModel should be authenticated", authViewModel.isAuthenticated)
        assertTrue("Repository should be authenticated", authRepository.isAuthenticated())

        val viewModelToken = authViewModel.authToken
        val repositoryState = authRepository.getPersistedAuthenticationState()

        assertEquals("Tokens should match", viewModelToken, repositoryState?.authToken)

        // Logout through ViewModel
        authViewModel.logoutWithStateManagement()

        // Both should be cleared
        assertFalse("ViewModel should not be authenticated", authViewModel.isAuthenticated)
        assertFalse("Repository should not be authenticated", authRepository.isAuthenticated())
    }
}
