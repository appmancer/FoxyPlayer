package com.foxy.player.authentication

import org.junit.Assert.assertTrue
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class AuthToken401Test {
    
    // PLY-119: Test for 401 authentication failure with token refresh logic
    @Test
    fun `Handle_401_authentication_failure_with_token_refresh_logic`() {
        // Arrange
        val authRepository = AuthRepository()
        val expiredToken = "expired_token_12345"
        
        // Set up a scenario where we have an expired token that triggers 401
        authRepository.setCurrentToken(expiredToken)
        
        // Act - attempt to make an authenticated request that should trigger 401 and refresh
        val result = authRepository.makeAuthenticatedRequest("/api/userinfo")
        
        // Assert - verify that 401 was handled with token refresh
        assertTrue("Should handle 401 error and refresh token", result.isSuccess)
        assertTrue("Should have refreshed token", authRepository.getCurrentToken() != expiredToken)
        assertNotNull("Should have new valid token after refresh", authRepository.getCurrentToken())
    }
}