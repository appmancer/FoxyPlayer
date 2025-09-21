package com.foxy.player.music

import com.foxy.player.authentication.network.AuthRepository
import com.foxy.player.authentication.network.AuthenticatedApiClient
import com.foxy.player.music.network.MusicDiscoveryService
import java.io.File
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Debug test to verify pCloud API authentication and response structure.
 * This test directly calls the pCloud API to understand why music discovery returns empty results.
 */
class PCloudAPIAuthenticationDebugTest {

    @Test
    fun `Test direct pCloud API call to verify authentication and response structure`() {
        println("🔍 Testing pCloud API Authentication Debug")
        println("=" + "=".repeat(50))

        try {
            // Check for real credentials file
            val credentialsFile = File("pcloud.txt")
            if (!credentialsFile.exists()) {
                println("⚠️ pcloud.txt not found - skipping debug test")
                println("💡 Create pcloud.txt with your credentials to test debug functionality")
                return
            }

            val credentials = credentialsFile.readLines()
            val email = credentials[2].trim()
            val password = credentials[3].trim()

            println("📧 Email: $email")
            println("🔐 Password: ${password.take(3)}...")

            // Setup authentication with REAL pCloud API calls
            val authRepository = AuthRepository("https://eapi.pcloud.com")

            // Authenticate first
            println("\n🔐 Authenticating...")
            val realAuthResult = authRepository.authenticateWithRealHTTP(email, password)
            assertTrue("Authentication should succeed", realAuthResult.isSuccess)

            // Now test our debug method
            val apiClient = AuthenticatedApiClient(authRepository)
            val discoveryService = MusicDiscoveryService(apiClient)

            println("\n🔍 Testing debug API call...")
            val debugResult = discoveryService.debugPCloudAPIAuthentication()

            // Assert - Verify the debug functionality provides insights
            assertNotNull("Debug result should not be null", debugResult)
            assertTrue("Debug result should indicate authentication success", debugResult.isSuccess)

            val debugInfo = debugResult.getOrNull()!!
            assertTrue("Auth token length should be > 0", debugInfo.authTokenLength > 0)
            assertTrue("Response size should be > 0", debugInfo.responseSize > 0)
            assertNotNull("Response parsing info should be available", debugInfo.responseParsingInfo)
            assertNotNull("Response type should be available", debugInfo.responseType)
            assertNotNull("Endpoint should be available", debugInfo.endpoint)

            println("✅ Debug test completed successfully!")
            println("🔍 Auth token length: ${debugInfo.authTokenLength}")
            println("🔍 Response size: ${debugInfo.responseSize} bytes")
            println("🔍 Request duration: ${debugInfo.requestDurationMs}ms")
            println("🔍 Response type: ${debugInfo.responseType}")
            println("🔍 Endpoint: ${debugInfo.endpoint}")
        } catch (e: Exception) {
            println("❌ Debug test failed: ${e.message}")
            throw e
        }
    }
}
