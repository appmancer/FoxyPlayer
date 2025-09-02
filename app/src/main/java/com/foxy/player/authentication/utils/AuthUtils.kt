package com.foxy.player.authentication.utils

import com.foxy.player.authentication.models.TokenValidationException
import com.foxy.player.authentication.network.AuthRepository

// ===== NAVIGATION UTILITIES =====

// PLY-82: Authentication Navigation Integration
object AuthRoutes {
    const val Login = "Login"
    const val Home = "Home"
}

// Navigation guard for authentication
class AuthGuard(private val authRepository: AuthRepository) {
    fun shouldRedirectToLogin(): Boolean {
        return !authRepository.isAuthenticated()
    }

    fun getStartDestination(): String {
        return if (shouldRedirectToLogin()) {
            AuthRoutes.Login
        } else {
            AuthRoutes.Home
        }
    }
}

// Authentication-aware navigator interface
interface AuthNavigator {
    fun navigateToLogin()
    fun navigateToHome()
    fun navigateToRoute(route: String)
}

// Implementation for testing
class TestAuthNavigator : AuthNavigator {
    var lastNavigatedRoute: String? = null
    var loginNavigationCount: Int = 0
    var homeNavigationCount: Int = 0

    override fun navigateToLogin() {
        lastNavigatedRoute = AuthRoutes.Login
        loginNavigationCount++
    }

    override fun navigateToHome() {
        lastNavigatedRoute = AuthRoutes.Home
        homeNavigationCount++
    }

    override fun navigateToRoute(route: String) {
        lastNavigatedRoute = route
    }
}

// ===== SECURE TOKEN STORAGE =====

// PLY-43: Secure Token Storage with Time-based Expiration
class SecureTokenStorage {
    // Storage mappings for different token types
    private val secureStorage = mutableMapOf<String, String>()
    private val expirationStorage = mutableMapOf<String, Long>()
    private val activityTimeoutStorage = mutableMapOf<String, ActivityInfo>()
    private val refreshStorage = mutableMapOf<String, RefreshInfo>()
    private val validationStorage = mutableMapOf<String, (String) -> Boolean>()

    // Data classes for storage management
    data class ActivityInfo(
        val timeoutDurationMs: Long,
        var lastAccessTimeMs: Long
    )

    data class RefreshInfo(
        val refreshThresholdMs: Long,
        val expirationDurationMs: Long,
        val refreshFunction: (String) -> Result<String>
    )

    fun storeToken(alias: String, token: String): Result<Unit> {
        return try {
            secureStorage[alias] = encryptToken(token)
            expirationStorage.remove(alias)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun storeTokenWithExpiration(alias: String, token: String, expirationDurationMs: Long): Result<Unit> {
        return try {
            secureStorage[alias] = encryptToken(token)
            expirationStorage[alias] = System.currentTimeMillis() + expirationDurationMs
            activityTimeoutStorage.remove(alias)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun storeTokenWithActivityTimeout(alias: String, token: String, inactivityTimeoutMs: Long): Result<Unit> {
        return try {
            secureStorage[alias] = encryptToken(token)
            activityTimeoutStorage[alias] = ActivityInfo(inactivityTimeoutMs, System.currentTimeMillis())
            expirationStorage.remove(alias)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun storeTokenWithAutoRefresh(
        alias: String,
        token: String,
        expirationDurationMs: Long,
        refreshThresholdMs: Long,
        refreshFunction: (String) -> Result<String>
    ): Result<Unit> {
        return try {
            secureStorage[alias] = encryptToken(token)
            expirationStorage[alias] = System.currentTimeMillis() + expirationDurationMs
            refreshStorage[alias] = RefreshInfo(
                refreshThresholdMs,
                expirationDurationMs,
                refreshFunction
            )
            activityTimeoutStorage.remove(alias)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun storeTokenWithValidation(
        alias: String,
        token: String,
        validationFunction: (String) -> Boolean
    ): Result<Unit> {
        return try {
            if (!validationFunction(token)) {
                return Result.failure(
                    TokenValidationException("Token validation failed for alias: $alias")
                )
            }
            secureStorage[alias] = encryptToken(token)
            validationStorage[alias] = validationFunction
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun retrieveToken(alias: String): Result<String?> {
        return try {
            val encryptedToken = secureStorage[alias] ?: return Result.success(null)

            // Check expiration
            val expirationTime = expirationStorage[alias]
            if (expirationTime != null && System.currentTimeMillis() > expirationTime) {
                removeToken(alias)
                return Result.success(null)
            }

            // Check activity timeout
            val activityInfo = activityTimeoutStorage[alias]
            if (activityInfo != null) {
                val timeSinceLastAccess = System.currentTimeMillis() - activityInfo.lastAccessTimeMs
                if (timeSinceLastAccess > activityInfo.timeoutDurationMs) {
                    removeToken(alias)
                    return Result.success(null)
                }
                // Update last access time
                activityInfo.lastAccessTimeMs = System.currentTimeMillis()
            }

            val decryptedToken = decryptToken(encryptedToken)
            Result.success(decryptedToken)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun retrieveTokenWithAutoRefresh(alias: String): Result<String?> {
        return try {
            val refreshInfo = refreshStorage[alias]
            if (refreshInfo != null) {
                val expirationTime = expirationStorage[alias]
                if (expirationTime != null) {
                    val timeUntilExpiration = expirationTime - System.currentTimeMillis()
                    if (timeUntilExpiration <= refreshInfo.refreshThresholdMs) {
                        // Attempt to refresh token
                        val currentToken = secureStorage[alias]?.let { decryptToken(it) }
                        if (currentToken != null) {
                            val refreshResult = refreshInfo.refreshFunction(currentToken)
                            if (refreshResult.isSuccess) {
                                val newToken = refreshResult.getOrNull()!!
                                storeTokenWithAutoRefresh(
                                    alias,
                                    newToken,
                                    refreshInfo.expirationDurationMs,
                                    refreshInfo.refreshThresholdMs,
                                    refreshInfo.refreshFunction
                                )
                                return Result.success(newToken)
                            }
                        }
                    }
                }
            }

            retrieveToken(alias)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun removeToken(alias: String): Result<Unit> {
        return try {
            secureStorage.remove(alias)
            expirationStorage.remove(alias)
            activityTimeoutStorage.remove(alias)
            refreshStorage.remove(alias)
            validationStorage.remove(alias)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun isTokenExpired(alias: String): Boolean {
        val expirationTime = expirationStorage[alias] ?: return false
        return System.currentTimeMillis() > expirationTime
    }

    fun hasToken(alias: String): Boolean {
        return secureStorage.containsKey(alias) && !isTokenExpired(alias)
    }

    // Simple encryption/decryption for testing (in production, use Android Keystore)
    private fun encryptToken(token: String): String {
        return "encrypted_$token"
    }

    private fun decryptToken(encryptedToken: String): String {
        return encryptedToken.removePrefix("encrypted_")
    }
}
