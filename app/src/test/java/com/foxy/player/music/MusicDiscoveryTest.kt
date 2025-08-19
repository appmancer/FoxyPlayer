package com.foxy.player.music

import org.junit.Test
import org.junit.Assert.*
import com.foxy.player.authentication.AuthenticatedApiClient
import com.foxy.player.authentication.AuthRepository
import com.foxy.player.authentication.UserInfo
import com.foxy.player.authentication.AuthResponse

class MusicDiscoveryTest {
    
    @Test
    fun `should integrate with authenticated API client for pCloud folder listing`() {
        // Arrange - setup test data
        val authRepository = AuthRepository("https://eapi.pcloud.com")
        val authenticatedApiClient = AuthenticatedApiClient(authRepository)
        val musicDiscoveryService = MusicDiscoveryService(authenticatedApiClient)
        
        // Act - call the method/feature being tested
        val result = musicDiscoveryService.listPCloudFolders("/")
        
        // Assert - verify expected behavior
        assertTrue("Should return a successful result with folder listing", result.isSuccess)
        val folderListing = result.getOrNull()
        assertNotNull("Folder listing should not be null", folderListing)
    }
    
    @Test
    fun `should call pCloud listfolder API with authenticated request`() {
        // Arrange - setup test data with authenticated state
        val authRepository = AuthRepository("https://eapi.pcloud.com")
        authRepository.saveAuthenticationState("test_auth_token", UserInfo("test@example.com"))
        val authenticatedApiClient = AuthenticatedApiClient(authRepository)
        val musicDiscoveryService = MusicDiscoveryService(authenticatedApiClient)
        
        // Act - call the method that should make real API call
        val result = musicDiscoveryService.listPCloudFoldersWithAPI("/")
        
        // Assert - verify API integration
        assertTrue("Should return successful result from real API call", result.isSuccess)
        val apiResponse = result.getOrNull()
        assertNotNull("API response should not be null", apiResponse)
        assertTrue("API response should contain auth token confirmation", 
            apiResponse!!.containsAuthToken("test_auth_token"))
    }
}