package com.foxy.player

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.foxy.player.music.MusicLibraryHubRender
import com.foxy.player.ui.theme.FoxyPlayerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FoxyPlayerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) {
                    MusicLibraryHubRender()
                }
            }
        }
    }
}
