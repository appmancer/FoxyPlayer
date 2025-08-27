package com.foxy.player.authentication

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthNavigationTest {

    @After
    fun cleanup() {
        // Clear authentication state after each test to prevent state pollution
        val authRepository = AuthRepository()
        authRepository.clearAuthenticationState()
    }

    @Test
    fun `AuthGuard should redirect to login when not authenticated`() {
        // Arrange
        val authRepository = AuthRepository()
        authRepository.clearAuthenticationState() // Ensure clean state
        val authGuard = AuthGuard(authRepository)

        // Act & Assert
        assertTrue("Should redirect to login when not authenticated", authGuard.shouldRedirectToLogin())
        assertEquals("Start destination should be Login", AuthRoutes.Login, authGuard.getStartDestination())
    }

    @Test
    fun `AuthGuard should not redirect when authenticated`() {
        // Arrange
        val authRepository = AuthRepository()
        authRepository.clearAuthenticationState() // Ensure clean state
        val authGuard = AuthGuard(authRepository)

        // Simulate authentication
        authRepository.saveAuthenticationState("test-token", UserInfo("test@example.com"))

        // Act & Assert
        assertFalse("Should not redirect to login when authenticated", authGuard.shouldRedirectToLogin())
        assertEquals("Start destination should be Home", AuthRoutes.Home, authGuard.getStartDestination())
    }

    @Test
    fun `TestAuthNavigator should track navigation correctly`() {
        // Arrange
        val navigator = TestAuthNavigator()

        // Act
        navigator.navigateToLogin()
        navigator.navigateToHome()
        navigator.navigateToRoute("CustomRoute")

        // Assert
        assertEquals("Should track login navigation", 1, navigator.loginNavigationCount)
        assertEquals("Should track home navigation", 1, navigator.homeNavigationCount)
        assertEquals("Should track last route", "CustomRoute", navigator.lastNavigatedRoute)
    }

    @Test
    fun `AuthGuard should update when authentication state changes`() {
        // Arrange
        val authRepository = AuthRepository()
        authRepository.clearAuthenticationState() // Ensure clean state
        val authGuard = AuthGuard(authRepository)

        // Initially not authenticated
        assertTrue("Should redirect to login initially", authGuard.shouldRedirectToLogin())

        // Act - Authenticate
        authRepository.saveAuthenticationState("test-token", UserInfo("test@example.com"))

        // Assert - Now authenticated
        assertFalse("Should not redirect after authentication", authGuard.shouldRedirectToLogin())
        assertEquals("Start destination should be Home", AuthRoutes.Home, authGuard.getStartDestination())

        // Act - Clear authentication
        authRepository.clearAuthenticationState()

        // Assert - Back to unauthenticated
        assertTrue("Should redirect to login after logout", authGuard.shouldRedirectToLogin())
        assertEquals("Start destination should be Login", AuthRoutes.Login, authGuard.getStartDestination())
    }

    @Test
    fun `AuthRoutes should have correct route constants`() {
        // Assert
        assertEquals("Login route should be 'Login'", "Login", AuthRoutes.Login)
        assertEquals("Home route should be 'Home'", "Home", AuthRoutes.Home)
    }
}
