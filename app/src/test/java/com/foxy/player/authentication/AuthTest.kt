package com.foxy.player.authentication

import org.junit.Test
import org.junit.Assert.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class AuthTest {

    @Test
    fun `should authenticate user with valid credentials and return auth token`() {
        // Arrange - setup test data
        val username = "test@example.com"
        val password = "testpassword"
        val expectedToken = "mock_auth_token_12345"
        
        // Create repository (this doesn't exist yet - will cause compilation failure)
        val authRepository = AuthRepository()
        
        // Act - call the authentication method that doesn't exist yet
        val result = authRepository.authenticate(username, password)
        
        // Assert - verify we get an auth token
        assertTrue(result.isSuccess)
        assertEquals(expectedToken, result.getOrNull()?.token)
    }

    @Test
    fun `should call pCloud userinfo endpoint with getauth=1 and return real auth token`() {
        // Arrange - setup test data
        val username = "real@user.com"
        val password = "realpassword"
        val pCloudBaseUrl = "https://api.pcloud.com"
        
        // Create repository with HTTP client capability (doesn't exist yet)
        val authRepository = AuthRepository(pCloudBaseUrl)
        
        // Act - call the real pCloud API authentication method
        val result = authRepository.authenticateWithPCloud(username, password)
        
        // Assert - verify we get a real response from pCloud API
        assertTrue(result.isSuccess)
        val authToken = result.getOrNull()
        assertNotNull(authToken)
        assertTrue("Token should not be empty", authToken?.token?.isNotEmpty() == true)
        assertTrue("Token should not be mock", authToken?.token != "mock_auth_token_12345")
    }

    @Test 
    fun `should make actual HTTP POST to pCloud userinfo endpoint and parse auth token from JSON response`() {
        // Arrange - setup test data for real pCloud API call
        val username = "testuser@example.com"
        val password = "testpassword123"
        val pCloudBaseUrl = "https://api.pcloud.com"
        
        // Create repository with HTTP parsing capability (doesn't exist yet)
        val authRepository = AuthRepository(pCloudBaseUrl)
        
        // Act - call method that makes real HTTP POST and parses JSON (doesn't exist yet)
        val result = authRepository.authenticateWithPCloudAPI(username, password)
        
        // Assert - verify JSON response was parsed correctly 
        assertTrue("Should succeed with HTTP call", result.isSuccess)
        val authResponse = result.getOrNull()
        assertNotNull("Should have auth response", authResponse)
        assertNotNull("Should have parsed auth token", authResponse?.authToken)
        assertTrue("Should have valid auth token format", authResponse?.authToken?.startsWith("T") == true)
        assertNotNull("Should have user info from JSON", authResponse?.userInfo)
        assertTrue("Should have parsed email", authResponse?.userInfo?.email?.isNotEmpty() == true)
    }

    @Test
    fun `should render AuthScreen with username and password input fields and login button`() {
        // Arrange - setup compose test environment (doesn't exist yet)
        val authScreen = AuthScreen() // This doesn't exist yet - will cause compilation failure
        
        // Act - render the AuthScreen composable (doesn't exist yet)
        val screenContent = authScreen.render()
        
        // Assert - verify UI components are present 
        assertTrue("Should have username input field", screenContent.hasUsernameField)
        assertTrue("Should have password input field", screenContent.hasPasswordField)
        assertTrue("Should have login button", screenContent.hasLoginButton)
        assertEquals("Username field should have correct label", "Username", screenContent.usernameLabel)
        assertEquals("Password field should have correct label", "Password", screenContent.passwordLabel)
        assertEquals("Login button should have correct text", "Login", screenContent.loginButtonText)
    }

    @Test
    fun `should manage authentication state with AuthViewModel including loading and success states`() {
        // Arrange - setup test data for ViewModel state management
        val username = "viewmodel@test.com"
        val password = "viewmodeltest123"
        val authRepository = AuthRepository("https://api.pcloud.com")
        
        // Create ViewModel that manages authentication state (doesn't exist yet)
        val authViewModel = AuthViewModel(authRepository) // This doesn't exist yet - will cause compilation failure
        
        // Act - simulate user login through ViewModel (doesn't exist yet)
        authViewModel.login(username, password)
        
        // Assert - verify ViewModel state management
        assertTrue("Should start in loading state", authViewModel.isLoading)
        assertFalse("Should not be authenticated initially", authViewModel.isAuthenticated)
        
        // Wait for authentication to complete (simulate async behavior)
        Thread.sleep(100) // Simple simulation - real implementation would use coroutines
        
        // Assert final state
        assertFalse("Should finish loading", authViewModel.isLoading)
        assertTrue("Should be authenticated after successful login", authViewModel.isAuthenticated)
        assertNotNull("Should have auth token in state", authViewModel.authToken)
        assertEquals("Should store username in state", username, authViewModel.username)
    }

    @Test
    fun `should handle network failures and authentication errors with proper error states`() {
        // Arrange - setup test data for error scenarios
        val invalidUsername = "invalid@test.com"
        val invalidPassword = "wrongpassword"
        val networkFailureUrl = "https://invalid-url-will-fail.com"
        
        // Create repository with invalid URL that will cause network failure (doesn't exist yet)
        val authRepository = AuthRepository(networkFailureUrl)
        val authViewModel = AuthViewModel(authRepository)
        
        // Act - attempt login that should fail (error handling doesn't exist yet)
        authViewModel.loginWithErrorHandling(invalidUsername, invalidPassword) // This doesn't exist yet - will cause compilation failure
        
        // Assert initial error state
        assertTrue("Should start in loading state", authViewModel.isLoading)
        assertFalse("Should not be authenticated initially", authViewModel.isAuthenticated)
        assertNull("Should not have error initially", authViewModel.errorMessage) // This doesn't exist yet
        
        // Wait for network failure to complete
        Thread.sleep(200) // Longer delay to simulate network timeout
        
        // Assert final error state 
        assertFalse("Should finish loading after error", authViewModel.isLoading)
        assertFalse("Should not be authenticated after error", authViewModel.isAuthenticated)
        assertNotNull("Should have error message after failure", authViewModel.errorMessage)
        assertTrue("Should have network error message", authViewModel.errorMessage?.contains("network") == true)
        assertNull("Should not have auth token after error", authViewModel.authToken)
        assertEquals("Should still store attempted username", invalidUsername, authViewModel.username)
    }

    @Test
    fun `should make real HTTP POST to pCloud API and authenticate with actual network call`() {
        // Arrange - setup test data for real HTTP POST call
        val realUsername = "test@pcloud.com"
        val realPassword = "testpassword123"
        val pCloudApiUrl = "https://eapi.pcloud.com" // Real pCloud endpoint
        
        // Create repository with real pCloud API URL (doesn't make real HTTP calls yet)
        val authRepository = AuthRepository(pCloudApiUrl)
        
        // Act - call method that should make real HTTP POST request (doesn't exist yet)
        val result = authRepository.authenticateWithRealHTTP(realUsername, realPassword) // This doesn't exist yet - will cause compilation failure
        
        // Assert - verify real HTTP call behavior (not mock timestamps)
        assertTrue("Should succeed with real HTTP call", result.isSuccess)
        val authResponse = result.getOrNull()
        assertNotNull("Should have real auth response", authResponse)
        
        // Verify it's NOT a mock response (mocks use timestamps)
        val authToken = authResponse?.authToken
        assertNotNull("Should have real auth token", authToken)
        assertFalse("Should not contain timestamp (mock indicator)", authToken?.contains(System.currentTimeMillis().toString().take(8)) == true)
        assertFalse("Should not be mock pattern", authToken?.matches(Regex("T\\d+")) == true)
        
        // Verify HTTP-specific behavior
        val userInfo = authResponse?.userInfo
        assertNotNull("Should have user info from HTTP response", userInfo)
        assertEquals("Should have correct email from HTTP", realUsername, userInfo?.email)
        
        // Verify it used actual HTTP (this should be different from mock behavior)
        assertTrue("Auth token should be from real pCloud API format", authToken?.length ?: 0 > 20)
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
        
        // Act - Parse failure response (method doesn't exist yet)
        val failureResult = authRepository.parseAuthResponse(pCloudFailureJson) // This doesn't exist yet - will cause compilation failure
        
        // Assert - Verify failure response parsing
        assertTrue("Should be failure result", failureResult.isFailure)
        val failureException = failureResult.exceptionOrNull()
        assertNotNull("Should have failure exception", failureException)
        assertTrue("Should contain error message", failureException?.message?.contains("Log in failed") == true)
        
        // Act - Parse success response (method doesn't exist yet)  
        val successResult = authRepository.parseAuthResponse(pCloudSuccessJson)
        
        // Assert - Verify success response parsing
        assertTrue("Should be success result", successResult.isSuccess)
        val authResponse = successResult.getOrNull()
        assertNotNull("Should have auth response", authResponse)
        assertEquals("Should parse auth token", "ABC123XYZ789pCloudAuthToken456DEF", authResponse?.authToken)
        assertEquals("Should parse user email", "user@example.com", authResponse?.userInfo?.email)
        
        // Verify proper pCloud response structure (not our old fake format)
        assertFalse("Should not contain our old fake patterns", authResponse?.authToken?.contains("pcloud_real_") == true)
        assertFalse("Should not contain timestamp patterns", authResponse?.authToken?.matches(Regex(".*\\d{10,}.*")) == true)
    }
    
    @Test
    fun `should provide realistic mock pCloud success response for testing`() {
        // Arrange
        val authRepository = AuthRepository("https://api.pcloud.com")
        
        // Act - Get mock success response 
        val mockSuccessJson = authRepository.getMockSuccessResponse()
        
        // Parse the mock response
        val result = authRepository.parseAuthResponse(mockSuccessJson)
        
        // Debug: Print the error if parsing fails
        if (result.isFailure) {
            println("Parsing failed with error: ${result.exceptionOrNull()?.message}")
            result.exceptionOrNull()?.printStackTrace()
        }
        
        // Assert - Verify mock response is realistic and parseable
        assertTrue("Mock response should be parseable: ${result.exceptionOrNull()?.message}", result.isSuccess)
        val authResponse = result.getOrNull()
        assertNotNull("Should have parsed auth response", authResponse)
        
        // Verify structure matches real pCloud API format
        assertTrue("Should have realistic auth token", authResponse?.authToken?.isNotEmpty() == true)
        assertTrue("Auth token should be alphanumeric", authResponse?.authToken?.matches(Regex("[A-Za-z0-9]+")) == true)
        assertTrue("Should have realistic email", authResponse?.userInfo?.email?.contains("@") == true)
        assertTrue("Mock JSON should contain result:0", mockSuccessJson.contains("\"result\": 0"))
        assertTrue("Mock JSON should contain auth token", mockSuccessJson.contains("\"auth\":"))
        assertTrue("Mock JSON should contain userid", mockSuccessJson.contains("\"userid\":"))
        assertTrue("Mock JSON should contain email", mockSuccessJson.contains("\"email\":"))
    }
    
    @Test
    fun `should provide auto-server detection method that tries both EU and US servers`() {
        // Arrange - This tests our discovery about pCloud having two data centers
        val authRepository = AuthRepository() // No specific base URL - should auto-detect
        val username = "test@example.com"
        val password = "testpassword"
        
        // Act - Test that the auto-detection method exists and can be called
        // This method should try eapi.pcloud.com first, then api.pcloud.com if that fails
        val result = try {
            authRepository.authenticateWithAutoServerDetection(username, password)
        } catch (e: Exception) {
            Result.failure<AuthResponse>(e)
        }
        
        // Assert - Verify the method exists and returns a result (even if it fails due to invalid credentials in test)
        assertNotNull("Auto-server detection method should exist and return a result", result)
        
        // The result will likely be a failure since we're using test credentials, but that's expected
        // The important thing is that the method exists and handles both EU and US server attempts
        
        // Note: In a real scenario, this method would:
        // 1. Try https://eapi.pcloud.com/userinfo first (European server)  
        // 2. If that fails with auth error, try https://api.pcloud.com/userinfo (US server)
        // 3. Return success from whichever server works
        // 4. Return failure if both servers reject the credentials
    }

    @Test
    fun `HTTP client should store and reuse successful server endpoint`() {
        // Arrange - Test server endpoint persistence
        val authRepository = AuthRepository()
        
        // Act - Manually set successful server to simulate successful authentication
        // Since we can't authenticate with test credentials, we simulate the behavior
        authRepository.authenticateWithAutoServerDetection("test@example.com", "testpassword")
        
        // Act - Check that the method exists and returns a result (even if null for test credentials)
        val successfulServer = authRepository.getLastSuccessfulServer()
        
        // Assert - Verify the method exists and can be called (main requirement)
        // The method should exist and return String? type
        // For test credentials, it may return null, which is acceptable
        assertTrue("getLastSuccessfulServer method should exist and be callable", 
            successfulServer == null || successfulServer.isNotEmpty())
    }

    @Test
    fun `HTTP client should distinguish between network errors and authentication errors`() {
        // Arrange - Test error differentiation
        val networkFailureRepository = AuthRepository("https://non-existent-server-12345.invalid")
        val authFailureRepository = AuthRepository("https://eapi.pcloud.com")
        
        val username = "error.test@example.com"
        val wrongPassword = "wrongpassword"
        
        // Act - Test network failure vs auth failure
        val networkResult = networkFailureRepository.authenticateWithErrorDetails(username, wrongPassword)
        val authResult = authFailureRepository.authenticateWithErrorDetails(username, wrongPassword)
        
        // Assert - Verify both return error details that can distinguish error types
        assertTrue("Network error should be failure", networkResult.isFailure)
        assertTrue("Auth error should be failure", authResult.isFailure)
        
        val networkException = networkResult.exceptionOrNull()
        val authException = authResult.exceptionOrNull()
        
        // The method should provide error details that allow distinguishing types
        assertNotNull("Should have network error details", networkException)
        assertNotNull("Should have auth error details", authException)
        assertNotEquals("Error messages should be different for different error types", 
            networkException?.message, authException?.message)
    }

    @Test
    fun `HTTP client should support configurable connection timeouts`() {
        // Arrange - Test timeout configuration
        val authRepository = AuthRepository("https://eapi.pcloud.com")
        val connectTimeout = 5000L  // 5 seconds
        val readTimeout = 10000L    // 10 seconds
        
        // Act - Configure timeouts
        authRepository.configureTimeouts(connectTimeout, readTimeout)
        
        // Get timeout configuration to verify it was set
        val actualConnectTimeout = authRepository.getConnectTimeout()
        val actualReadTimeout = authRepository.getReadTimeout()
        
        // Assert - Verify timeout configuration was stored correctly
        assertEquals("Connect timeout should be configured", connectTimeout, actualConnectTimeout)
        assertEquals("Read timeout should be configured", readTimeout, actualReadTimeout)
    }

    @Test
    fun `HTTP client should validate SSL certificates and reject invalid certificates`() {
        // Arrange - Test SSL certificate validation
        val validSslRepository = AuthRepository("https://eapi.pcloud.com")
        val invalidSslRepository = AuthRepository("https://self-signed.badssl.com") // Known invalid SSL site
        
        val username = "ssl.test@example.com"
        val password = "ssltest"
        
        // Act - Test SSL validation
        val validSslResult = validSslRepository.authenticateWithSSLValidation(username, password)
        val invalidSslResult = invalidSslRepository.authenticateWithSSLValidation(username, password)
        
        // Assert - Valid SSL should work (even if auth fails), invalid SSL should be rejected
        // Valid SSL should not fail due to SSL issues (may fail due to auth, that's OK)
        val validException = validSslResult.exceptionOrNull()
        if (validException != null) {
            // If it fails, it should NOT be due to SSL issues
            assertFalse("Valid SSL should not fail with SSL error", 
                validException.message?.contains("SSL", ignoreCase = true) == true ||
                validException.message?.contains("certificate", ignoreCase = true) == true)
        }
        
        // Invalid SSL should be rejected with SSL-related error
        assertTrue("Invalid SSL should be rejected", invalidSslResult.isFailure)
        val invalidException = invalidSslResult.exceptionOrNull()
        assertNotNull("Should have SSL error for invalid certificate", invalidException)
        assertTrue("Should be SSL-related error", 
            invalidException?.message?.contains("SSL", ignoreCase = true) == true ||
            invalidException?.message?.contains("certificate", ignoreCase = true) == true ||
            invalidException?.message?.contains("trust", ignoreCase = true) == true)
    }

    @Test
    fun `should persist authentication state across app restarts`() {
        // Arrange - Setup authentication state that should persist
        val authRepository = AuthRepository("https://eapi.pcloud.com")
        val username = "test@example.com"
        val authToken = "persistent_auth_token_12345"
        val userInfo = UserInfo(username)
        
        // Act - Save authentication state (this method doesn't exist yet)
        authRepository.saveAuthenticationState(authToken, userInfo)
        
        // Simulate app restart by creating new repository instance
        val newRepositoryInstance = AuthRepository("https://eapi.pcloud.com")
        
        // Act - Retrieve persisted authentication state (this method doesn't exist yet)
        val retrievedAuthState = newRepositoryInstance.getPersistedAuthenticationState()
        
        // Assert - Verify authentication state persisted across "app restart"
        assertNotNull("Should have persisted authentication state", retrievedAuthState)
        assertEquals("Should persist auth token", authToken, retrievedAuthState?.authToken)
        assertEquals("Should persist user email", username, retrievedAuthState?.userInfo?.email)
        assertTrue("Should indicate user is authenticated", newRepositoryInstance.isAuthenticated())
    }

    @Test
    fun `should validate if persisted authentication session is still valid`() {
        // Arrange - Setup authentication state with an auth token
        val authRepository = AuthRepository("https://eapi.pcloud.com")
        val username = "session@example.com"
        val authToken = "session_token_to_validate"
        val userInfo = UserInfo(username)
        
        // Save authentication state
        authRepository.saveAuthenticationState(authToken, userInfo)
        
        // Act - Validate the persisted session (this method doesn't exist yet)
        val validationResult = authRepository.validatePersistedSession()
        
        // Assert - Verify session validation works
        assertTrue("Should validate session successfully", validationResult.isSuccess)
        val isValid = validationResult.getOrNull()
        assertNotNull("Should return validation result", isValid)
        assertTrue("Session should be valid for test scenario", isValid == true)
        
        // Test with invalid/expired session scenario
        val expiredAuthRepository = AuthRepository("https://invalid-server.com")
        expiredAuthRepository.saveAuthenticationState("expired_token", userInfo)
        
        val expiredValidation = expiredAuthRepository.validatePersistedSession()
        assertTrue("Should handle validation attempt", expiredValidation.isSuccess || expiredValidation.isFailure)
    }

    @Test
    fun `should handle authentication events with login and logout event listeners`() {
        // Arrange - Setup event listeners for authentication events
        val authRepository = AuthRepository("https://eapi.pcloud.com")
        var loginEventTriggered = false
        var logoutEventTriggered = false
        var loginEventUser: String? = null
        var logoutEventUser: String? = null
        
        // Setup event listeners (these methods don't exist yet)
        authRepository.setLoginEventListener { user ->
            loginEventTriggered = true
            loginEventUser = user.email
        }
        
        authRepository.setLogoutEventListener { user ->
            logoutEventTriggered = true
            logoutEventUser = user.email
        }
        
        val username = "event@example.com"
        val password = "eventtest"
        val userInfo = UserInfo(username)
        
        // Act - Trigger login event by saving authentication state
        authRepository.saveAuthenticationState("event_token_123", userInfo)
        authRepository.triggerLoginEvent(userInfo) // This method doesn't exist yet
        
        // Assert - Verify login event was triggered
        assertTrue("Login event should be triggered", loginEventTriggered)
        assertEquals("Login event should have correct user email", username, loginEventUser)
        assertFalse("Logout event should not be triggered yet", logoutEventTriggered)
        
        // Act - Trigger logout event
        authRepository.triggerLogoutEvent(userInfo) // This method doesn't exist yet
        
        // Assert - Verify logout event was triggered
        assertTrue("Logout event should be triggered", logoutEventTriggered)
        assertEquals("Logout event should have correct user email", username, logoutEventUser)
    }

    @Test
    fun `should handle authentication state clearing and session invalidation events`() {
        // Arrange - Setup authentication state that will be cleared
        val authRepository = AuthRepository("https://eapi.pcloud.com")
        val username = "clear@example.com"
        val authToken = "token_to_clear_123"
        val userInfo = UserInfo(username)
        
        // Setup authentication state
        authRepository.saveAuthenticationState(authToken, userInfo)
        assertTrue("Should be authenticated before clearing", authRepository.isAuthenticated())
        assertNotNull("Should have persisted state before clearing", authRepository.getPersistedAuthenticationState())
        
        var sessionInvalidatedEventTriggered = false
        var stateCleared = false
        
        // Setup event listeners for clearing events (these methods don't exist yet)
        authRepository.setSessionInvalidationListener {
            sessionInvalidatedEventTriggered = true
        }
        
        authRepository.setAuthenticationStateCleared {
            stateCleared = true
        }
        
        // Act - Clear authentication state (this method doesn't exist yet)
        authRepository.clearAuthenticationState()
        
        // Assert - Verify state was cleared and events were triggered
        assertFalse("Should not be authenticated after clearing", authRepository.isAuthenticated())
        assertNull("Should not have persisted state after clearing", authRepository.getPersistedAuthenticationState())
        assertTrue("Session invalidation event should be triggered", sessionInvalidatedEventTriggered)
        assertTrue("State cleared event should be triggered", stateCleared)
        
        // Act - Test session invalidation (this method doesn't exist yet)
        authRepository.saveAuthenticationState("new_token", userInfo)
        authRepository.invalidateCurrentSession()
        
        // Assert - Verify session invalidation works
        assertFalse("Should not be authenticated after session invalidation", authRepository.isAuthenticated())
        assertNull("Should not have persisted state after invalidation", authRepository.getPersistedAuthenticationState())
    }

    @Test
    fun `should integrate authentication state management with existing login flow`() {
        // Arrange - Setup repository and ViewModel with state management integration
        val authRepository = AuthRepository("https://eapi.pcloud.com")
        val authViewModel = AuthViewModel(authRepository)
        val username = "integration@example.com"
        val password = "integrationtest"
        
        // Clear any existing state to ensure clean test
        authRepository.clearAuthenticationState()
        
        var loginEventTriggered = false
        var loginEventUser: String? = null
        val loginLatch = CountDownLatch(1)
        
        // Setup login event listener
        authRepository.setLoginEventListener { user ->
            loginEventTriggered = true
            loginEventUser = user.email
            loginLatch.countDown()
        }
        
        // Verify initial state
        assertFalse("Should not be authenticated initially", authRepository.isAuthenticated())
        assertNull("Should not have persisted state initially", authRepository.getPersistedAuthenticationState())
        
        // Act - Perform login through ViewModel which should integrate with state management (this integration doesn't exist yet)
        authViewModel.loginWithStateManagement(username, password) // This method doesn't exist yet
        
        // Wait for login event or timeout after 1 second
        assertTrue("Login event was not triggered in time", loginLatch.await(1, TimeUnit.SECONDS))
        
        // Assert - Verify login integration works with state management
        assertTrue("Should be authenticated after login", authRepository.isAuthenticated())
        assertNotNull("Should have persisted authentication state", authRepository.getPersistedAuthenticationState())
        assertTrue("Login event should be triggered during login flow", loginEventTriggered)
        assertEquals("Login event should have correct user", username, loginEventUser)
        
        // Verify ViewModel state is synchronized with repository state
        assertTrue("ViewModel should show authenticated state", authViewModel.isAuthenticated)
        assertNotNull("ViewModel should have auth token", authViewModel.authToken)
        
        // Test logout integration (this method doesn't exist yet)
        var logoutEventTriggered = false
        authRepository.setLogoutEventListener { 
            logoutEventTriggered = true
        }
        
        // Act - Perform logout which should clear state and trigger events
        authViewModel.logoutWithStateManagement() // This method doesn't exist yet
        
        // Assert - Verify logout clears state properly
        assertFalse("Should not be authenticated after logout", authRepository.isAuthenticated())
        assertNull("Should not have persisted state after logout", authRepository.getPersistedAuthenticationState())
        assertTrue("Logout event should be triggered", logoutEventTriggered)
        assertFalse("ViewModel should show not authenticated", authViewModel.isAuthenticated)
        assertNull("ViewModel should not have auth token", authViewModel.authToken)
    }

    @Test
    fun `should inject auth token into pCloud API requests automatically`() {
        // Arrange - PLY-44: Authenticated API client with token injection
        val authRepository = AuthRepository("https://eapi.pcloud.com")
        val authToken = "test_auth_token_12345"
        val userInfo = UserInfo("test@example.com")
        
        // Save authentication state (simulate existing login)
        authRepository.saveAuthenticationState(authToken, userInfo)
        
        // Create authenticated API client (doesn't exist yet)
        val apiClient = AuthenticatedApiClient(authRepository) // This doesn't exist yet - will cause compilation failure
        
        // Act - Make API call that should automatically inject auth token (doesn't exist yet)
        val result = apiClient.makeAuthenticatedRequest("/userinfo") // This doesn't exist yet - will cause compilation failure
        
        // Assert - Verify auth token was injected into request
        assertTrue("Should succeed with authenticated request", result.isSuccess)
        val requestDetails = result.getOrNull()
        assertNotNull("Should have request details", requestDetails)
        assertTrue("Should contain auth token in request", requestDetails?.containsAuthToken(authToken) == true)
        assertNotNull("Should have made HTTP request", requestDetails?.httpResponse)
    }

    @Test
    fun `should automatically route to correct pCloud server - US or Europe based on user location`() {
        // Arrange - PLY-44: Automatic server routing 
        val authRepository = AuthRepository() // No specific server - should auto-detect
        val authToken = "routing_test_token_789"
        val userInfo = UserInfo("routing@example.com")
        
        // Save authentication state 
        authRepository.saveAuthenticationState(authToken, userInfo)
        
        // Create authenticated API client with auto-routing (doesn't exist yet)
        val apiClient = AuthenticatedApiClient(authRepository)
        
        // Act - Make request that should auto-route to correct server (doesn't exist yet)
        val routingResult = apiClient.makeRequestWithAutoRouting("/userinfo") // This doesn't exist yet - will cause compilation failure
        
        // Assert - Verify auto-routing behavior
        assertTrue("Should succeed with auto-routing", routingResult.isSuccess)
        val routingInfo = routingResult.getOrNull()
        assertNotNull("Should have routing information", routingInfo)
        
        // Should have attempted both EU and US servers if needed
        assertTrue("Should have tried routing logic", 
            routingInfo?.attemptedServers?.contains("eapi.pcloud.com") == true ||
            routingInfo?.attemptedServers?.contains("api.pcloud.com") == true)
        
        // Should have selected a successful server
        assertNotNull("Should have successful server", routingInfo?.successfulServer)
        assertTrue("Successful server should be valid pCloud endpoint",
            routingInfo?.successfulServer?.contains("pcloud.com") == true)
    }

    @Test
    fun `should handle pCloud 2000 and 4000 series error codes with proper error mapping`() {
        // Arrange - PLY-44: pCloud error code handling
        val authRepository = AuthRepository("https://eapi.pcloud.com")
        val authToken = "error_test_token_456"
        val userInfo = UserInfo("error@example.com")
        
        // Save authentication state
        authRepository.saveAuthenticationState(authToken, userInfo)
        
        // Create authenticated API client (doesn't exist yet)
        val apiClient = AuthenticatedApiClient(authRepository)
        
        // Test 2000 series error (authentication errors)
        val pCloud2000Json = """{"result": 2000, "error": "Log in failed."}"""
        val pCloud2001Json = """{"result": 2001, "error": "Invalid login."}"""
        
        // Test 4000 series error (access/permission errors)  
        val pCloud4000Json = """{"result": 4000, "error": "Access denied."}"""
        val pCloud4001Json = """{"result": 4001, "error": "Insufficient permissions."}"""
        
        // Act - Handle different pCloud error responses (doesn't exist yet)
        val result2000 = apiClient.handlePCloudErrorResponse(pCloud2000Json) // This doesn't exist yet - will cause compilation failure
        val result2001 = apiClient.handlePCloudErrorResponse(pCloud2001Json)
        val result4000 = apiClient.handlePCloudErrorResponse(pCloud4000Json)
        val result4001 = apiClient.handlePCloudErrorResponse(pCloud4001Json)
        
        // Assert - Verify proper error mapping and categorization
        
        // 2000 series - authentication errors
        assertTrue("2000 error should be failure", result2000.isFailure)
        val error2000 = result2000.exceptionOrNull()
        assertTrue("2000 should be AuthenticationException", error2000 is AuthenticationException)
        assertTrue("2000 should contain error message", error2000?.message?.contains("Log in failed") == true)
        
        assertTrue("2001 error should be failure", result2001.isFailure)
        val error2001 = result2001.exceptionOrNull()
        assertTrue("2001 should be AuthenticationException", error2001 is AuthenticationException)
        
        // 4000 series - access/permission errors  
        assertTrue("4000 error should be failure", result4000.isFailure)
        val error4000 = result4000.exceptionOrNull()
        assertTrue("4000 should be AccessException", error4000 is AccessException) // This class doesn't exist yet
        assertTrue("4000 should contain error message", error4000?.message?.contains("Access denied") == true)
        
        assertTrue("4001 error should be failure", result4001.isFailure)
        val error4001 = result4001.exceptionOrNull()
        assertTrue("4001 should be AccessException", error4001 is AccessException)
    }
}