package com.foxy.player.authentication.models

// ===== AUTHENTICATION EXCEPTIONS =====

class AuthenticationException(message: String) : Exception(message)
class AccessException(message: String) : Exception(message) // PLY-44: For 4000 series errors
class TokenExpiredException(message: String) : Exception(message) // PLY-43: For expired tokens
class TokenValidationException(message: String) : Exception(message) // PLY-43: For invalid tokens
class RateLimitingException(message: String, val retryAfterSeconds: Int) : Exception(message)

// ===== CORE AUTH DATA MODELS =====

data class AuthToken(val token: String)
data class UserInfo(val email: String)
data class AuthResponse(val authToken: String, val userInfo: UserInfo)

// ===== PCLOUD API RESPONSE MODELS =====

// Real pCloud API Response Models (based on actual API exploration)
data class PCloudResponse(
    val result: Int,
    val error: String? = null,
    val auth: String? = null,
    val userid: Long? = null,
    val email: String? = null
)

// ===== UI MODELS =====

// Auth UI Models
data class AuthScreenContent(
    val hasUsernameField: Boolean,
    val hasPasswordField: Boolean,
    val hasLoginButton: Boolean,
    val usernameLabel: String,
    val passwordLabel: String,
    val loginButtonText: String
)

// Login Screen Content Model (for PLY-45 Compose UI)
data class LoginScreenContent(
    val hasUsernameTextField: Boolean,
    val hasPasswordTextField: Boolean,
    val hasLoginButton: Boolean,
    val usernamePlaceholder: String,
    val passwordPlaceholder: String,
    val usernameLabel: String = usernamePlaceholder,
    val passwordLabel: String = passwordPlaceholder,
    val loginButtonText: String,
    val submitButtonText: String = loginButtonText, // Alias for backwards compatibility
    val hasSubmitButton: Boolean = hasLoginButton, // Alias for backwards compatibility
    val isPasswordFieldObscured: Boolean,
    val isLoading: Boolean = false,
    val isLoginButtonDisabled: Boolean = false,
    val isUsernameEnabled: Boolean = true,
    val isPasswordEnabled: Boolean = true,
    val hasErrorMessage: Boolean = false,
    val errorMessage: String? = null,
    val hasServerSelectionField: Boolean = true,
    val hasServerSelection: Boolean = hasServerSelectionField,
    val serverPlaceholder: String = "pCloud Server (optional)",
    val defaultServerValue: String = "api.pcloud.com",
    val isServerFieldEnabled: Boolean = true,
    val currentServerValue: String = "api.pcloud.com",
    val serverOptions: List<String> = listOf("US", "EU")
)

// ===== AUTHENTICATED API CLIENT MODELS =====

// PLY-44: Authenticated API Client Data Models
data class AuthenticatedRequestResult(
    val httpResponse: String,
    val authTokenUsed: String
) {
    fun containsAuthToken(token: String): Boolean {
        return authTokenUsed == token
    }
}

data class ServerRoutingResult(
    val attemptedServers: List<String>,
    val successfulServer: String?
)

// PLY-118: Rate Limiting Retry Result Models
data class RetryRequestResult(
    val retriesAttempted: Int,
    val backoffIntervalsUsed: List<Long>,
    val finalHttpResponse: String,
    val finalException: Exception?
)

// PLY-118: Request Throttling Result Models
data class ThrottledRequestResult(
    val httpResponse: String,
    val authTokenUsed: String,
    val throttleDelayMs: Long
) {
    fun containsAuthToken(token: String): Boolean {
        return authTokenUsed == token
    }
}

// PLY-118: Circuit Breaker State Models
data class CircuitBreakerState(
    val status: String, // "OPEN", "CLOSED", "HALF_OPEN"
    val consecutiveFailures: Int,
    val openedAtMs: Long
)
