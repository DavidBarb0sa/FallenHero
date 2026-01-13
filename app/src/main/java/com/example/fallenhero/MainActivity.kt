package com.example.fallenhero

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.fallenhero.ui.theme.FallenHeroTheme
import com.example.fallenhero.view.GameOverView
import com.example.fallenhero.view.GameScreenView
import com.example.fallenhero.view.HighScoresView
import com.example.fallenhero.view.HomeView
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseFirestore.getInstance().enableNetwork()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        setContent {
            val navController = rememberNavController()
            FallenHeroTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { _ ->
                    NavHost(
                        navController = navController,
                        startDestination = "home",
                        modifier = Modifier
                    ){
                        composable("home"){
                            HomeView(
                                modifier = Modifier,
                                navController = navController
                            )
                        }
                        composable("game"){
                            GameScreenView(modifier = Modifier, navController = navController)
                        }
                        composable("gameover/{score}") { backStackEntry ->
                            val score = backStackEntry.arguments?.getString("score")?.toInt() ?: 0
                            GameOverView(
                                score = score,
                                navController = navController,
                                context = this@MainActivity // <- adiciona isto
                            )
                        }
                        composable("highscores") {
                            HighScoresView(navController)
                        }
                    }
                }
            }
        }
    }
}
