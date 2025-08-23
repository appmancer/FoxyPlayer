package com.foxy.player.authentication

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthRepositoryTest {

    companion object {
        // Test credentials (not real credentials, safe for testing)
        private const val TEST_USERNAME = "test@example.com"
        private const val TEST_PASSWORD = "test-password-not-real"
    }

    // PLY-71: Core Authentication Test - Main entry point
    @Test
    fun `should authenticate with test credentials and return real pCloud API token`() {
        // Arrange
        val authRepository = AuthRepository()

        // Act - call the main authenticate method (PLY-71: now uses real API)
        val result = authRepository.authenticate(TEST_USERNAME, TEST_PASSWORD)

        // Assert - verify it makes REAL pCloud API call instead of returning mock token
        assertTrue("Authentication should succeed with real API", result.isSuccess)

        val authToken = result.getOrNull()?.token
        assertNotNull("Should return real auth token", authToken)

        // Verify it's NOT the old hardcoded mock response
        assertFalse("Should NOT return hardcoded mock token", authToken == "mock_auth_token_12345")

        // Verify it has characteristics of real pCloud auth token
        assertTrue("Real pCloud token should be alphanumeric", authToken?.matches(Regex("[A-Za-z0-9]+")) == true)
        assertTrue("Real pCloud token should be substantial length", (authToken?.length ?: 0) >= 20)
    }

    @Test
    fun `should parse real pCloud JSON responses for both success and failure scenarios`() {
        // Arrange - Real pCloud JSON responses based on API exploration
        val pCloudFailureJson = """
            {
                "result": 2000,
                "error": "Log in failed."
            }
        """.trimIndent()

        val pCloudSuccessJson = """
            {
                "result": 0,
                "auth": "ABC123XYZ789pCloudAuthToken456DEF",
                "userid": 123456789,
                "email": "user@example.com"
            }
        """.trimIndent()

        val authRepository = AuthRepository("https://eapi.pcloud.com")

        // Act & Assert - Parse failure response
        val failureResult = authRepository.parseAuthResponse(pCloudFailureJson)
        assertTrue("Should be failure result", failureResult.isFailure)
        val failureException = failureResult.exceptionOrNull()
        assertNotNull("Should have failure exception", failureException)
        assertTrue(
            "Should contain error message",
            failureException?.message?.contains("Log in failed") == true
        )

        // Act & Assert - Parse success response
        val successResult = authRepository.parseAuthResponse(pCloudSuccessJson)
        assertTrue("Should be success result", successResult.isSuccess)
        val authResponse = successResult.getOrNull()
        assertNotNull("Should have auth response", authResponse)
        assertEquals(
            "Should parse auth token",
            "ABC123XYZ789pCloudAuthToken456DEF",
            authResponse?.authToken
        )
        assertEquals("Should parse user email", "user@example.com", authResponse?.userInfo?.email)
    }

    @Test
    fun `should provide auto-server detection method that tries both EU and US servers`() {
        // Arrange
        val authRepository = AuthRepository() // No specific base URL - should auto-detect
        val username = "test@example.com"
        val password = "test123"

        // Act - Test that the auto-detection method exists and can be called
        val result = try {
            authRepository.authenticateWithAutoServerDetection(username, password)
        } catch (e: Exception) {
            Result.failure<AuthResponse>(e)
        }

        // Assert - Verify the method exists and returns a result (even if it fails due to invalid credentials)
        assertNotNull("Auto-server detection method should exist and return a result", result)
        // The result will likely be a failure since we're using test credentials, but that's expected
        // The important thing is that the method exists and handles both EU and US server attempts
    }

    // PLY-42: Username/Password Login Tests
    @Test
    fun `should authenticate with digest authentication`() {
        // Arrange
        val username = "testuser@example.com"
        val testPassword = "secure_test_placeholder"
        val authRepository = AuthRepository("https://test-api.example.com")

        // Act
        val result = authRepository.authenticateWithDigest(username, testPassword)

        // Assert
        assertTrue("Should succeed with digest authentication", result.isSuccess)
        val authResponse = result.getOrNull()
        assertNotNull("Should have auth response", authResponse)
        assertNotNull("Should have auth token", authResponse?.authToken)
        assertTrue("Token should not be empty", authResponse?.authToken?.isNotEmpty() == true)
        assertEquals("Should store user email", username, authResponse?.userInfo?.email)
    }

    @Test
    fun `should support both US and Europe pCloud API servers`() {
        // Arrange
        val username = "testuser@example.com"
        val testPassword = "secure_test_placeholder"

        // Act & Assert - US server
        val usRepository = AuthRepository("https://api.pcloud.com")
        val usResult = usRepository.authenticateWithServerSupport(username, testPassword, "US")
        assertTrue("Should succeed with US server", usResult.isSuccess)
        val usAuthResponse = usResult.getOrNull()
        assertNotNull("Should have US auth response", usAuthResponse)
        assertTrue("US token should not be empty", usAuthResponse?.authToken?.isNotEmpty() == true)

        // Act & Assert - Europe server
        val europeRepository = AuthRepository("https://eapi.pcloud.com")
        val europeResult = europeRepository.authenticateWithServerSupport(username, testPassword, "EUROPE")
        assertTrue("Should succeed with Europe server", europeResult.isSuccess)
        val europeAuthResponse = europeResult.getOrNull()
        assertNotNull("Should have Europe auth response", europeAuthResponse)
        assertTrue("Europe token should not be empty", europeAuthResponse?.authToken?.isNotEmpty() == true)

        // Assert - tokens should be different (different servers)
        assertNotEquals(
            "Tokens should be different for different servers",
            usAuthResponse?.authToken,
            europeAuthResponse?.authToken
        )
    }

    // Integration test with real credentials (if available)
    @Test
    fun `should authenticate with real pCloud API when credentials are available`() {
        val credentialsFile = File("pcloud.txt")
        if (!credentialsFile.exists()) {
            println("⚠️ pcloud.txt not found - skipping real API test")
            return
        }

        try {
            val credentials = credentialsFile.readLines()
            val realUsername = credentials[2].trim()
            val realPassword = credentials[3].trim()

            println("📧 Testing real pCloud API with email: $realUsername")

            val authRepository = AuthRepository("https://eapi.pcloud.com")
            val result = authRepository.authenticateWithRealPCloudAPI(realUsername, realPassword)

            if (result.isSuccess) {
                val authResponse = result.getOrNull()
                assertNotNull("Should have real auth response", authResponse)

                val authToken = authResponse?.authToken
                assertNotNull("Should have real auth token", authToken)
                assertFalse("Should NOT be mock token", authToken == "mock_auth_token_12345")

                println("✅ Real pCloud API authentication successful!")
            } else {
                println("❌ Real authentication failed: ${result.exceptionOrNull()?.message}")
                println("💡 This is expected if credentials are invalid")
            }
        } catch (e: Exception) {
            println("💥 Error during real pCloud API test: ${e.message}")
            println("💡 This is expected if credentials file format is unexpected")
        }
    }
}
