package com.foxy.player.music

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

class NavControllerNavigator(private val navController: NavHostController) : Navigator {
    override fun navigate(route: String) {
        navController.navigate(route)
    }
}

@Composable
fun MusicNavHost(navController: NavHostController) {
    val authRepo = com.foxy.player.authentication.AuthRepository()
    val api = com.foxy.player.authentication.AuthenticatedApiClient(authRepo)
    val discovery = MusicDiscoveryService(api)
    val heuristic = HeuristicMusicDiscovery(discovery)

    NavHost(navController = navController, startDestination = Routes.Home) {
        composable(Routes.Home) {
            // Provide VM for hub
            val vm = androidx.lifecycle.viewmodel.compose.viewModel<MusicHubViewModel>(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return MusicHubViewModel(authRepo, api, discovery, heuristic) as T
                }
            })
            MusicLibraryHubRender(viewModel = vm, navigator = NavControllerNavigator(navController))
        }
        composable(Routes.SongsList) { SongsListScreen(discovery) }
        composable(Routes.ArtistsList) { ArtistsListScreen(discovery) }
        composable(Routes.AlbumsList) { AlbumsListScreen(discovery) }
        composable(Routes.FoldersView) { FoldersViewScreen(discovery) }
    }
}
