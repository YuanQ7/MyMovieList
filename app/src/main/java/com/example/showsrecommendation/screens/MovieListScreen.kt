package com.example.showsrecommendation.screens

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
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
import com.example.showsrecommendation.models.MainViewModel
import kotlinx.coroutines.flow.asStateFlow
import java.lang.Double.max
import kotlin.math.max
import kotlin.math.min

@Composable
fun MovieListScreen(
    navController: NavController,
    viewModel: MainViewModel
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column {
            Spacer(modifier = Modifier.height(10.dp))
            MovieList(
                navController,
                viewModel,
                "popular",
                modifier = Modifier.fillMaxWidth()
                    .fillMaxHeight(0.3f)

            )
        }
    }
}


@Composable
fun MovieList(
    navController: NavController,
    viewModel: MainViewModel,
    genre: String,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp)
    ) {
        val itemCount = viewModel.movieLists.value.getMovieList(genre).count()
        val endReached = viewModel.endReached.asStateFlow().value

        items(count = itemCount) {
            if (it >= itemCount - 1 && !endReached) {
                Log.w("TESTING", "ran")
                viewModel.loadMoviesPaginated()
            }
            MovieCard(
                it,
                genre,
                navController,
                viewModel,
                modifier = Modifier
                    .fillMaxSize()
            )
        }
    }
//    LazyVerticalGrid(
//        modifier = modifier,
//        columns = GridCells.Fixed(count = 2)
//    ) {
//        items(count = 100) {
//            MovieCard(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(5.dp),
//                imageUrlState=gridUiState.grid[it].posterImageUrl,
//                textState=gridUiState.grid[it].videoUrl
//            )
//        }
//    }
}

@Composable
fun MovieCard(
    index: Int,
    genre: String,
    navController: NavController,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier,
) {
//    val listState = viewModel.popularList.collectAsState()
    val listState = viewModel.movieLists.collectAsState()
//    val imageUrlState = listState.value[index].posterImageUrl
    val imageUrlState = listState.value.getMovieList(genre)[index].posterImageUrl
    val textState = listState.value.getMovieList(genre)[index].videoUrl

    Card(
        modifier = modifier
    ) {
        val isLoading = viewModel.isMovieLoading.collectAsState()
        if (isLoading.value) {
            CircularProgressIndicator(modifier = Modifier.fillMaxSize(0.5f))
        }
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