@file:Suppress("ktlint:filename")

package com.foxy.player.authentication

// ===== COMPATIBILITY LAYER FOR EXISTING IMPORTS =====
// This file maintains backward compatibility while delegating to new domain modules

import com.foxy.player.authentication.models.AccessException
import com.foxy.player.authentication.models.AuthResponse
import com.foxy.player.authentication.models.AuthScreenContent
import com.foxy.player.authentication.models.AuthToken
import com.foxy.player.authentication.models.AuthenticatedRequestResult
import com.foxy.player.authentication.models.AuthenticationException
import com.foxy.player.authentication.models.LoginScreenContent
import com.foxy.player.authentication.models.PCloudResponse
import com.foxy.player.authentication.models.ServerRoutingResult
import com.foxy.player.authentication.models.TokenExpiredException
import com.foxy.player.authentication.models.TokenValidationException
import com.foxy.player.authentication.models.UserInfo
import com.foxy.player.authentication.network.AuthRepository
import com.foxy.player.authentication.network.AuthenticatedApiClient
import com.foxy.player.authentication.ui.AuthScreen
import com.foxy.player.authentication.ui.AuthViewModel
import com.foxy.player.authentication.ui.LoginScreen
import com.foxy.player.authentication.utils.AuthGuard
import com.foxy.player.authentication.utils.AuthNavigator
import com.foxy.player.authentication.utils.AuthRoutes
import com.foxy.player.authentication.utils.SecureTokenStorage

// Re-export key classes for backward compatibility
typealias AuthRepositoryAlias = AuthRepository
typealias AuthenticatedApiClientAlias = AuthenticatedApiClient
typealias AuthScreenAlias = AuthScreen
typealias LoginScreenAlias = LoginScreen
typealias AuthViewModelAlias = AuthViewModel
typealias SecureTokenStorageAlias = SecureTokenStorage
typealias AuthGuardAlias = AuthGuard
typealias AuthNavigatorAlias = AuthNavigator

// Re-export data classes
typealias AuthTokenAlias = AuthToken
typealias UserInfoAlias = UserInfo
typealias AuthResponseAlias = AuthResponse
typealias PCloudResponseAlias = PCloudResponse
typealias AuthScreenContentAlias = AuthScreenContent
typealias LoginScreenContentAlias = LoginScreenContent
typealias AuthenticatedRequestResultAlias = AuthenticatedRequestResult
typealias ServerRoutingResultAlias = ServerRoutingResult

// Re-export exceptions
typealias AuthenticationExceptionAlias = AuthenticationException
typealias AccessExceptionAlias = AccessException
typealias TokenExpiredExceptionAlias = TokenExpiredException
typealias TokenValidationExceptionAlias = TokenValidationException

// Re-export constants
object AuthRoutesCompat {
    const val Login = AuthRoutes.Login
    const val Home = AuthRoutes.Home
}

// Direct class exports for backward compatibility - preserved original patterns
val AuthRepository = com.foxy.player.authentication.network.AuthRepository::class
val AuthenticatedApiClient = com.foxy.player.authentication.network.AuthenticatedApiClient::class
val AuthScreen = com.foxy.player.authentication.ui.AuthScreen::class
val LoginScreen = com.foxy.player.authentication.ui.LoginScreen::class
val AuthViewModel = com.foxy.player.authentication.ui.AuthViewModel::class
val SecureTokenStorage = com.foxy.player.authentication.utils.SecureTokenStorage::class
