package com.example.showsrecommendation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.annotation.ExperimentalCoilApi
import coil.compose.AsyncImage

import com.example.showsrecommendation.ui.theme.ShowsRecommendationTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private lateinit var gridViewModel : GridViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ShowsRecommendationTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    gridViewModel = viewModel()

                    val gridUiState = gridViewModel.gridState.collectAsState()
                    MainScreenLayout(gridUiState=gridUiState)
//                    SubcomposeAsyncImage(
//                        modifier = Modifier.fillMaxSize(),
//                        model = "https://image.tmdb.org/t/p/w500/mXLOHHc1Zeuwsl4xYKjKh2280oL.jpg",
////                        model = ImageRequest.Builder(this)
////                                .data("http://image.tmdb.org/t/p/w500/mXLOHHc1Zeuwsl4xYKjKh2280oL.jpg")
////                                .crossfade(true)
////                                .build(),
//                        contentDescription = "Some caption",
//                        loading = {
//                            CircularProgressIndicator(color = Color.Black)
//                        }
//                    )
                }
            }
        }
    }

    @Composable
    fun MainScreenLayout(
        modifier: Modifier = Modifier,
        gridUiState: State<GridUiState>
    ) {
        LazyVerticalGrid(
            modifier = modifier,
            columns = GridCells.Fixed(count = 2)
//            columns = GridCells.Adaptive(minSize = 128.dp)
        ) {
            items(count = 100) {
                MovieCard(
                    imageUrl=gridUiState.value.grid[it].posterPath,
                    text=gridUiState.value.grid[it].title
                )
            }
        }
    }

    @Composable
    fun MovieCard(
        modifier: Modifier = Modifier,
        imageUrl: String,
        text: String = ""
    ) {
        Card(
            modifier = modifier
                .fillMaxSize()
                .padding(5.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AsyncImage(
                    modifier = Modifier.fillMaxSize(),
                    model = imageUrl,
//                        model = ImageRequest.Builder(this)
//                                .data("http://image.tmdb.org/t/p/w500/mXLOHHc1Zeuwsl4xYKjKh2280oL.jpg")
//                                .crossfade(true)
//                                .build(),
                    contentDescription = "Some caption"
                )
                Text(
                    modifier = Modifier.padding(5.dp), 
                    text = text,
                    color = Color.Black
                )
            }
        }
    }

    @Composable
    fun Greeting(name: String, modifier: Modifier = Modifier) {
        Text(
            text = "Hello $name!",
            modifier = modifier
        )
    }

    @Preview(showBackground = true)
    @Composable
    fun GreetingPreview() {
        ShowsRecommendationTheme {
            Greeting("Android")
        }
    }
}