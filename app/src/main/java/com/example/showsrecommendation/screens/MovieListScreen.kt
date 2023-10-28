package com.example.showsrecommendation.screens

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Button
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

@Composable
fun MovieListScreen(
    navController: NavController,
    viewModel: MainViewModel
) {
    val mainState = viewModel.mainState.collectAsState()
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column {
            Text(text = "Trending Movies")
            MovieList(
                navController,
                viewModel,
                "fantasy",
                modifier = Modifier.fillMaxHeight(0.25f)
            )
            Text(text = "Horror Movies")
            MovieList(
                navController,
                viewModel,
                "horror",
                modifier = Modifier.fillMaxHeight(0.25f)
            )
//            Spacer(Modifier.height(16.dp))
//            MovieList(navController = navController, viewModel = viewModel, genre = "horror")
        }
        val isLoading = viewModel.isMovieLoading.collectAsState()
        val loadingErrorMessage = viewModel.loadingErrorMessage.collectAsState()

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // TODO: having this box and this if condition fixes pagination??
            if (isLoading.value) {
                CircularProgressIndicator(
                    modifier = Modifier.fillMaxSize(0.5f)
                )
            }
            if (loadingErrorMessage.value.isNotBlank()) {
                Log.d("TESTING", "Box error not blank")
                RetrySection(viewModel = viewModel) {
                    viewModel.loadMoviesPaginated("fantasy")
                }
            }
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
    val mainState = viewModel.mainState.collectAsState()
    val isLoading = viewModel.isMovieLoading.collectAsState()
    val endReached = viewModel.endReached.collectAsState()
    val loadingErrorMessage = viewModel.loadingErrorMessage.collectAsState()
    val itemCount = mainState.value.getMovieList(genre).count()

//    Log.d("TESTING", "lazyrow recomp")
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(5.dp)
    ) {
        items(count = itemCount) {
            if (it >= itemCount - 1 && !endReached.value[genre]!! && !isLoading.value) {
                Log.w("TESTING", "$genre uiRowCount: $itemCount")
                viewModel.loadMoviesPaginated(genre)
            }
            MovieCard(
                it,
                genre,
                navController,
                viewModel,
                modifier = Modifier.border(BorderStroke(3.dp, Color.Black))
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

    // get state to display
    val mainState = viewModel.mainState.collectAsState()
    val imageUrlState = mainState.value.getMovieList(genre)[index].posterImageUrl
    val textState = mainState.value.getMovieList(genre)[index].videoUrl
//    Log.d("TESTING", "cardview recomp")

    Box(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val isLoading = viewModel.isMovieLoading.collectAsState()
            // todo: add back
//            if (isLoading.value) {
//                CircularProgressIndicator(modifier = Modifier.fillMaxSize(0.5f))
//            }
            AsyncImage(
                modifier = Modifier.fillMaxSize(),
                model = imageUrlState,
//                        model = ImageRequest.Builder(this)
//                                .data("http://image.tmdb.org/t/p/w500/mXLOHHc1Zeuwsl4xYKjKh2280oL.jpg")
//                                .crossfade(true)
//                                .build(),
                contentDescription = "Some caption"
            )
//            Text(
//                modifier = Modifier.padding(5.dp),
//                text = textState,
//                color = Color.Black
//            )
        }
    }
}

@Composable
fun RetrySection(
    viewModel: MainViewModel,
    onRetry: () -> Unit
) {
    val loadingErrorMessage = viewModel.loadingErrorMessage.collectAsState()
    Column {
        Text(
            text = loadingErrorMessage.value
        )
        Button(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            onClick = { onRetry() }
        ) {
            Text(text = "Retry")
        }

    }
}