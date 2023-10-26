package com.example.showsrecommendation.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.showsrecommendation.models.GridUiState
import com.example.showsrecommendation.models.GridViewModel

@Composable
fun MovieListScreen(
    navController: NavController,
    gridViewModel: GridViewModel
) {
    val gridUiState = gridViewModel.gridState.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column {
            Spacer(modifier = Modifier.height(10.dp))
            MovieGrid(gridUiState = gridUiState.value)
        }
    }
}

@Composable
fun MovieGrid(
    modifier: Modifier = Modifier,
    gridUiState: GridUiState
) {
    LazyVerticalGrid(
        modifier = modifier,
        columns = GridCells.Fixed(count = 2)
    ) {
        items(count = 100) {
            MovieCard(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(5.dp),
                imageUrlState=gridUiState.grid[it].posterImageUrl,
                textState=gridUiState.grid[it].videoUrl
            )
        }
    }
}

@Composable
fun MovieCard(
    modifier: Modifier = Modifier,
    imageUrlState: String,
    textState: String = ""
) {
    Card(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                modifier = Modifier.fillMaxSize(),
                model = imageUrlState,
//                        model = ImageRequest.Builder(this)
//                                .data("http://image.tmdb.org/t/p/w500/mXLOHHc1Zeuwsl4xYKjKh2280oL.jpg")
//                                .crossfade(true)
//                                .build(),
                contentDescription = "Some caption"
            )
            Text(
                modifier = Modifier.padding(5.dp),
                text = textState,
                color = Color.Black
            )
        }
    }
}