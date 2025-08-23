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
    NavHost(navController = navController, startDestination = Routes.Home) {
        composable(Routes.Home) { Text("Home", style = MaterialTheme.typography.titleLarge) }
        composable(Routes.SongsList) { Text("SongsList", style = MaterialTheme.typography.titleLarge) }
        composable(Routes.ArtistsList) { Text("ArtistsList", style = MaterialTheme.typography.titleLarge) }
        composable(Routes.AlbumsList) { Text("AlbumsList", style = MaterialTheme.typography.titleLarge) }
        composable(Routes.FoldersView) { Text("FoldersView", style = MaterialTheme.typography.titleLarge) }
    }
}
