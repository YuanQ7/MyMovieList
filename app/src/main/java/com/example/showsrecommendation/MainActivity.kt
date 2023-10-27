package com.example.showsrecommendation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.showsrecommendation.screens.MovieListScreen

import com.example.showsrecommendation.ui.theme.ShowsRecommendationTheme
import com.example.showsrecommendation.models.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ShowsRecommendationTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = "movie_list_screen"
                ) {
                    composable("movie_list_screen") {
                        MovieListScreen(
                            navController = navController,
                            viewModel = mainViewModel
                        )
                    }
                }
            }
        }
    }
}