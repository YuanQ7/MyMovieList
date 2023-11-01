package com.example.showsrecommendation.screens

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.showsrecommendation.models.MainViewModel
import com.example.showsrecommendation.util.Utils.Companion.genreList
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MovieListScreen(
    navController: NavController,
    viewModel: MainViewModel
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        val isLoading = viewModel.isLoading.collectAsState()
        val loadingErrorMessage = viewModel.loadingErrorMessage.collectAsState()

        // TODO: how to snap to very top of column, instead of middle?
        // likewise for each movie list row
        val columnState = rememberLazyListState()
        val snapBehavior = rememberSnapFlingBehavior(lazyListState = columnState)

        Column {
            YouTubeVideoPlayer(
                genreList[0],
                viewModel,
                LocalLifecycleOwner.current,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            MainMovieDetails(
                viewModel,
                modifier = Modifier.weight(0.1f)
            )
            Spacer(Modifier.height(8.dp))
            LazyColumn(
                modifier = Modifier.weight(0.3f),
                contentPadding = PaddingValues(8.dp),
                state = columnState,
                flingBehavior = snapBehavior
            ) {
                val countItems = genreList.count()

                // TODO: to make infinite scrolling more pleasing, add refresh option when user
                // TODO: reaches end of movie lists, and scroll with animation to top

                items(count = countItems) {
                    // disabling scrolling using this causes a lot of recompositions
//                    val isFocused = if (columnState.firstVisibleItemIndex == 0) {
//                        it == 0
//                    } else {
//                        it == columnState.firstVisibleItemIndex + 1
//                    }

                    val genre = genreList[it]
                    Text(
                        text = "$genre Movies",
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    MovieList(
                        navController,
                        viewModel,
                        genre,
//                        isFocused,
                        modifier = Modifier
                    )
                }
            }


        }


        // NEED THIS SECTION! can't even refactor into method, else movie list row size won't change

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // TODO: having this box and this if condition fixes pagination??
            if (isLoading.value) {
                CircularProgressIndicator(
                    modifier = Modifier.fillMaxSize(0.3f)
                )
            }
            if (loadingErrorMessage.value.isNotBlank()) {
                Log.d("TESTING", "Box error not blank")
                RetrySection(loadingErrorMessage) {
                    viewModel.loadMoviesPaginated("fantasy")
                }
            }
        }
    }
}

@Composable
fun MainMovieDetails(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val currMovieItem = viewModel.currMovieItem.collectAsState()

    Box(modifier = modifier) {
        Column {
            Row (
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = currMovieItem.value.title,
                    modifier = Modifier
                        .padding(16.dp)
                        .weight(0.6f),
                )
                Text(
                    text = currMovieItem.value.releaseDate,
                    modifier = Modifier
                        .padding(8.dp)
                        .weight(0.4f)
                )
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MovieList(
    navController: NavController,
    viewModel: MainViewModel,
    genre: String,
//    isFocusedState: Boolean,
    modifier: Modifier = Modifier,
) {
    val mainState = viewModel.mainState.collectAsState()
    val isMovieLoading = viewModel.isMovieLoading.collectAsState()
    val endReached = viewModel.endReached.collectAsState()
//    val loadingErrorMessage = viewModel.loadingErrorMessage.collectAsState()
    val itemCount = mainState.value.getMovieList(genre).count()

    val lazyRowState = rememberLazyListState()
    val snapBehavior = rememberSnapFlingBehavior(lazyListState = lazyRowState)

    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(5.dp),
        state = lazyRowState,
        flingBehavior = snapBehavior,
//        userScrollEnabled = isFocusedState
    ) {
        items(count = itemCount) {
            if (it >= itemCount - 1 && !endReached.value[genre]!! && !isMovieLoading.value[genre]!!) {
                Log.w("TESTING", "$genre uiRowCount: $itemCount")
                viewModel.loadMoviesPaginated(genre)
            }
            MovieCard(
                genre,
                it,
                viewModel,
                navController,
                modifier = Modifier
                    .border(BorderStroke(3.dp, Color.Black))
                    .height(200.dp)
                    .aspectRatio(1 / 1.5f)
            )
        }
    }
}

@Composable
fun MovieCard(
    genre: String,
    index: Int,
    viewModel: MainViewModel,
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    val mainState = viewModel.mainState.collectAsState()
    val isMovieLoading = viewModel.isMovieLoading.collectAsState()
    val isGenreLoading = isMovieLoading.value[genre]!!
    val imageUrl = mainState.value.getMovieList(genre)[index].posterImageUrl

    Box(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isGenreLoading) {
                CircularProgressIndicator(modifier = Modifier.fillMaxSize(0.5f))
            }

            AsyncImage(
                modifier = Modifier.fillMaxSize(),
                model = imageUrl,
                contentDescription = null
            )
        }
    }
}

@Composable
fun YouTubeVideoPlayer(
    genre: String,
    viewModel: MainViewModel,
    lifecycleOwner: LifecycleOwner,
    modifier: Modifier = Modifier
) {
    val mainState = viewModel.mainState.collectAsState()
    val videoKey = mainState.value.getMovieList(genre)[0].ytVideoKey
    Log.d("TESTING", "key: $videoKey")

    AndroidView(
        modifier = modifier,
        factory = { context ->
            val player = YouTubePlayerView(context)
            lifecycleOwner.lifecycle.addObserver(player)

            val listener: YouTubePlayerListener = object : AbstractYouTubePlayerListener() {
                override fun onReady(youTubePlayer: YouTubePlayer) {
                    // maybe unnecessary null check
                    if (viewModel.ytPlayer == null) {
                        viewModel.ytPlayer = youTubePlayer
                    }
                    // no default UI views
//                    val playerView = DefaultPlayerUiController(player, youTubePlayer)
//                    player.setCustomPlayerUi(playerView.rootView)

                    // mute video at start of app launch
                    youTubePlayer.setVolume(0)
                    // todo: load video another time, when we're sure lits have been loaded
                    youTubePlayer.loadVideo(videoKey, 0f)
                }
            }
            val options = IFramePlayerOptions.Builder().controls(0).build()
            player.enableAutomaticInitialization = false
            player.initialize(listener, options)
            player
        }
    )
}


@Composable
fun RetrySection(
    loadingErrorMessage: State<String>,
    onRetry: () -> Unit
) {
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