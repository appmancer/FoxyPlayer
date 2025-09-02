package com.foxy.player.music

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.foxy.player.music.network.MusicDiscoveryService

class NavControllerNavigator(private val navController: NavHostController) : Navigator {
    override fun navigate(route: String) {
        navController.navigate(route)
    }
}

@Composable
fun MusicNavHost(navController: NavHostController) {
    val authRepo = com.foxy.player.authentication.network.AuthRepository()
    val api = com.foxy.player.authentication.network.AuthenticatedApiClient(authRepo)
    val discovery = MusicDiscoveryService(api)
    val heuristic = HeuristicMusicDiscovery(discovery)

    // PLY-82: Authentication Guard Integration
    val authGuard = com.foxy.player.authentication.utils.AuthGuard(authRepo)
    val startDestination = authGuard.getStartDestination()

    NavHost(navController = navController, startDestination = startDestination) {
        // PLY-82: Add login route
        composable(com.foxy.player.authentication.utils.AuthRoutes.Login) {
            // Provide AuthViewModel for login screen
            val authViewModel = viewModel<com.foxy.player.authentication.ui.AuthViewModel>(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return com.foxy.player.authentication.ui.AuthViewModel(authRepo) as T
                    }
                }
            )

            // Create login screen with authentication
            com.foxy.player.authentication.ui.LoginScreenWithNavigation(
                authViewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Routes.Home) {
                        popUpTo(com.foxy.player.authentication.utils.AuthRoutes.Login) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.Home) {
            // PLY-82: Check authentication before showing home
            if (authGuard.shouldRedirectToLogin()) {
                navController.navigate(com.foxy.player.authentication.utils.AuthRoutes.Login) {
                    popUpTo(Routes.Home) { inclusive = true }
                }
                return@composable
            }

            // Provide VM for hub
            val vm = viewModel<MusicHubViewModel>(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return MusicHubViewModel(authRepo, api, discovery, heuristic) as T
                    }
                }
            )
            MusicLibraryHubRender(viewModel = vm, navigator = NavControllerNavigator(navController))
        }
        composable(Routes.SongsList) { SongsListScreen(discovery) }
        composable(Routes.ArtistsList) { ArtistsListScreen(discovery) }
        composable(Routes.AlbumsList) { AlbumsListScreen(discovery) }
        composable(Routes.FoldersView) { FoldersViewScreen(discovery) }
    }
}
