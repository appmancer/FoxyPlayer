package com.foxy.player

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import com.foxy.player.music.MusicNavHost
import com.foxy.player.ui.theme.FoxyPlayerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            Log.d("FoxyPlayer", "🎯 MainActivity onCreate - Debug logging test")
            Log.i("FoxyPlayer", "ℹ️ INFO: MainActivity started")
            Log.w("FoxyPlayer", "⚠️ WARN: MainActivity test")
        } catch (e: Exception) {
            // Ignore logging errors in test environment
        }
        enableEdgeToEdge()
        setContent {
            FoxyPlayerTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Vertical)
                ) { innerPadding ->
                    val navController = rememberNavController()
                    // Render only the NavHost as the single source of UI
                    Surface(modifier = Modifier.padding(innerPadding)) {
                        MusicNavHost(navController)
                    }
                }
            }
        }
    }
}

@Composable
fun HelloFoxyPlayerScreen(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Hello Foxy Player",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "🦊🎵 Ready for music streaming",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HelloFoxyPlayerPreview() {
    FoxyPlayerTheme {
        HelloFoxyPlayerScreen()
    }
}
