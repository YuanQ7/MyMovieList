package com.example.showsrecommendation.models

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import com.example.showsrecommendation.network.MovieApiObject
import com.example.showsrecommendation.network.MovieApiResult
import com.example.showsrecommendation.network.MovieApiVideosResult
import com.example.showsrecommendation.repository.MovieRepository
import com.example.showsrecommendation.util.Constants.Companion.IMAGE_BASE_URL
import com.example.showsrecommendation.util.Constants.Companion.LANG_EN
import com.example.showsrecommendation.util.Constants.Companion.PLAYER_URI_SAVE_KEY
import com.example.showsrecommendation.util.Constants.Companion.VIMEO_BASE_URL
import com.example.showsrecommendation.util.Constants.Companion.YOUTUBE_BASE_URL
import com.example.showsrecommendation.util.Constants.Companion.genreList
import com.example.showsrecommendation.util.Resource
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor (
    private val repository: MovieRepository,
    private val savedStateHandle: SavedStateHandle,
    val videoPlayer: Player
) : ViewModel() {

//    private val videoUri = savedStateHandle.getStateFlow(
//        PLAYER_URI_SAVE_KEY, MediaItem.fromUri())
//    )
    var ytPlayer: YouTubePlayer? = null

    private val _mainState = MutableStateFlow(MainUiState())
    val mainState: StateFlow<MainUiState> = _mainState.asStateFlow()

    // Todo: whether api has finished retrieving movie data
    private val _isMovieLoading = MutableStateFlow(false)
    val isMovieLoading = _isMovieLoading.asStateFlow()

    private val _isVideoLoading = MutableStateFlow(false)
    val isVideoLoading = _isVideoLoading.asStateFlow()

    private val currPage = hashMapOf<String, Int>()

    // if we ran out of pages to get from api (highly unlikely)
    private val _endReached = MutableStateFlow(
        hashMapOf<String, Boolean>()
    )
    val endReached = _endReached.asStateFlow()

    private val _loadingErrorMessage = MutableStateFlow("")
    val loadingErrorMessage = _loadingErrorMessage

    init {
        videoPlayer.prepare()
        // initialize for every genre
        for (genre in genreList) {
            endReached.value[genre] = false
            currPage[genre] = 1
        }
//        loadMoviesPaginated("popular")
    }

    fun addMediaUri(uri: String) {
        if (videoPlayer.mediaItemCount < 5) {
            videoPlayer.setMediaItem(MediaItem.fromUri(uri))
//            playVideo()
        } else if (videoPlayer.mediaItemCount == 5) {

        }
//        Log.d("TESTING", "videoPlayer media count: ${videoPlayer.mediaItemCount}")
//        playVideo()
    }

    fun playVideo() {
        videoPlayer.setMediaItem(
            if (videoPlayer.mediaItemCount > 0) {
//                Log.d("TESTING", "first video: ${videoPlayer.getMediaItemAt(0)}")
                videoPlayer.getMediaItemAt(0)
            } else {
                return
            }
        )
    }

//    fun playVideo(uri: Uri) {
//        video
//    }

    fun loadMoviesPaginated(genre: String) {
        _isMovieLoading.value = true
        Log.w("TESTING", "$genre paginating ${currPage[genre]} ${_mainState.value.getMovieList(genre).count()}")
        getMovieData(genre, true) {
            processMovieData(genre, it)
        }
    }

    private fun processMovieData(
        genre: String,
        response: Resource<MovieApiResult>
    ) {
        when (response) {
            is Resource.Error -> {
                loadingErrorMessage.value = response.message!!
                Log.w("TESTING", response.message)
            }
            is Resource.Success -> {
                currPage[genre] = currPage[genre]!! + 1
                // result is Success, data cannot be null
                val result = response.data!!

                endReached.value[genre] = currPage[genre]!! >= result.totalPages

//            _status.value = result.movieApiObjects[0].title + " " + result.totalResults
//            Log.w("HELLO", "" + result.movieApiObjects.count())

                for (i in 0 until result.movieApiObjects.count()) {
                    // after finish getting movies, assign them to our GridStateUi state,
                    // so UI changes can reflect in MainActivity
                    val currMovieObj = result.movieApiObjects[i]

                    // grab trailer url
                    getMovieVideos(currMovieObj) {
                        // process show videos data (onFinish())
                        processVideoData(it, currMovieObj)
                    }

                    // make paths the url to the images
                    currMovieObj.posterImageUrl = IMAGE_BASE_URL + currMovieObj.posterPath
//                    currMovieObj.backdropImageUrl = IMAGE_BASE_URL + currMovieObj.backdropPath

//                Log.d("TEST", uiGrid[i].posterPath + "\n" + uiGrid[i].backdropPath)
                }
                // notify MainActivity
//                Log.w("TESTING", newUiList[0].posterPath)
                _mainState.value.addToMovieList(genre, result.movieApiObjects)
//                movieLists.value.movieLists = HashMap(movieLists.value.movieLists)
                _loadingErrorMessage.value = ""
//                _mainState.value = mainState.value.copy()
            }
        }
        _isMovieLoading.value = false
    }


    private fun processVideoData(response: Resource<MovieApiVideosResult>, currMovieObj: MovieApiObject) {
        when (response) {
            is Resource.Error -> {
                loadingErrorMessage.value = response.message!!
                Log.w("TESTING", response.message)
            }
            is Resource.Success -> {
                val trailers = response.data!!.movieApiVideos

                if (trailers.isNotEmpty()) {
                    if (trailers[0].site == "YouTube") {
                        currMovieObj.videoUrl = YOUTUBE_BASE_URL + trailers[0].key
                        currMovieObj.ytVideoKey = trailers[0].key
                    } else if (trailers[0].site == "Vimeo") {
                        currMovieObj.videoUrl = VIMEO_BASE_URL + trailers[0].key
                    }
                }
//                Log.d("TESTINGVIDEO", "added ${currMovieObj.videoUrl}")
//                addMediaUri(currMovieObj.videoUrl)
//                Log.w("TESTING", currMovieObj.videoUrl)
            }
        }
    }

    private fun getMovieData(genre: String, isMovie: Boolean, onFinish: (Resource<MovieApiResult>) -> Unit) {
        // grab movie data response from repository
        _isMovieLoading.value = true
        _isMovieLoading.value = true
        viewModelScope.launch {
            onFinish(repository.getMovieGenreList(
                LANG_EN, currPage[genre]!!, isMovie, genre
            ))
        }

//            Log.w("TESTING", "" + response.data!!.movieApiObjects[0].id)
//            Log.w("TESTING", "" + videos)


    }

    // we use onFinish() to make sure our api finishes running
    private fun getMovieVideos(
        currMovieObj: MovieApiObject,
        onFinish: (Resource<MovieApiVideosResult>) -> Unit
    ) {
        _isVideoLoading.value = true
        viewModelScope.launch {
            onFinish(repository.getMovieVideos(
                currMovieObj.id, LANG_EN, true
            ))
        }
    }

    override fun onCleared() {
        super.onCleared()
        videoPlayer.release()
    }
}

class youTubePlayer(
) : AbstractYouTubePlayerListener() {
    override fun onReady(youTubePlayer: YouTubePlayer) {
        super.onReady(youTubePlayer)
        youTubePlayer
    }
}

