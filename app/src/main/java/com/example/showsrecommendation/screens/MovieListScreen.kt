package com.example.showsrecommendation.screens

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.showsrecommendation.models.MainViewModel
import com.pierfrancescosoffritti.androidyoutubeplayer.core.customui.DefaultPlayerUiController
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import kotlinx.coroutines.flow.StateFlow

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
            YouTubeVideoPlayer("horror",
                1,
                viewModel,
                LocalLifecycleOwner.current
            )
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
fun YouTubeVideoPlayer(
    genre: String,
    index: Int,
    viewModel: MainViewModel,
    lifecycleOwner: LifecycleOwner
) {
//    val mainState = viewModel.mainState.collectAsState()

//    Log.d("TESTING", videoId)

    AndroidView(

        factory = { context ->
            val player = YouTubePlayerView(context = context)
            lifecycleOwner.lifecycle.addObserver(player)

            val listener: YouTubePlayerListener = object : AbstractYouTubePlayerListener() {
                override fun onReady(youTubePlayer: YouTubePlayer) {
                    val videoId = viewModel.mainState.value.getMovieList(genre)[0].ytVideoKey

                    if (viewModel.ytPlayer == null) {
                        viewModel.ytPlayer = youTubePlayer
                    }
                    // no default UI views
//                    val playerView = DefaultPlayerUiController(player, youTubePlayer)
//                    player.setCustomPlayerUi(playerView.rootView)
                    // mute video at start of app launch
                    youTubePlayer.setVolume(0)
                    youTubePlayer.loadVideo(videoId, 0f)
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
fun VideoPlayer(
    genre: String,
    viewModel: MainViewModel
) {
//    val mainUiState = viewModel.mainState.collectAsState()
//    var videoUrl = mainUiState.value.getMovieList(genre)[0].videoUrl

    // need this to make AndroidViews work
    var lifecycle by remember {
        mutableStateOf(Lifecycle.Event.ON_CREATE)
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        // observer sees lifecycle changes
        val observer = LifecycleEventObserver { _, event ->
            lifecycle = event
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    // use xml view here, no compose version of PlayerView yet
    AndroidView(
        factory = { context ->
            PlayerView(context).also {
                it.player = viewModel.videoPlayer
            }
        },
//        update = {
//            when (lifecycle) {
//                Lifecycle.Event.ON_PAUSE -> {
//                    it.onPause()
//                    it.player?.pause()
//                }
//                Lifecycle.Event.ON_RESUME -> {
//                    it.onResume()
//                }
//                else -> Unit
//            }
//        },
        modifier = Modifier.aspectRatio(16 / 9f)
    )
    viewModel.videoPlayer.setMediaItem(MediaItem.fromUri("https://www.youtube.com/watch?v=_ttcR7VDouE"))

//    viewModel.addMediaUri(Uri.parse(videoUrl))
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