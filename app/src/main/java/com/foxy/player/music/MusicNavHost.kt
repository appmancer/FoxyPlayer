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
    val api = com.foxy.player.authentication.AuthenticatedApiClient(
        com.foxy.player.authentication.AuthRepository()
    )
    val discovery = MusicDiscoveryService(api)
    NavHost(navController = navController, startDestination = Routes.Home) {
        composable(Routes.Home) { Text("Home", style = MaterialTheme.typography.titleLarge) }
        composable(Routes.SongsList) { SongsListScreen(discovery) }
        composable(Routes.ArtistsList) { ArtistsListScreen(discovery) }
        composable(Routes.AlbumsList) { AlbumsListScreen(discovery) }
        composable(Routes.FoldersView) { FoldersViewScreen(discovery) }
    }
}
