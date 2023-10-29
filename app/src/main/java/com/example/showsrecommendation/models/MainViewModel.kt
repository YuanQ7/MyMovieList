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
import com.example.showsrecommendation.util.Constants.Companion.defaultListItem
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
    private val mainUiState: MainUiState,
    private val savedStateHandle: SavedStateHandle,
//    val videoPlayer: Player
) : ViewModel() {

//    private val videoUri = savedStateHandle.getStateFlow(
//        PLAYER_URI_SAVE_KEY, MediaItem.fromUri())
//    )
    var ytPlayer: YouTubePlayer? = null

    private val _mainState = MutableStateFlow(mainUiState)
    val mainState: StateFlow<MainUiState> = _mainState.asStateFlow()

    private val _currMovieItem = MutableStateFlow(defaultListItem)
    val currMovieItem = _currMovieItem.asStateFlow()

    // to avoid any possible unnecessary recompositions, use key-value pair instead?
//    private val _horrorList = MutableStateFlow(mainUiState.movieLists.entries)

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
//        videoPlayer.prepare()
        // initialize for every genre
        for (genre in genreList) {
            endReached.value[genre] = false
            currPage[genre] = 1
        }
//        loadMoviesPaginated("popular")
    }

    // Exoplayer methods (unused)
//    fun addMediaUri(uri: String) {
//        if (videoPlayer.mediaItemCount < 5) {
//            videoPlayer.setMediaItem(MediaItem.fromUri(uri))
////            playVideo()
//        } else if (videoPlayer.mediaItemCount == 5) {
//
//        }
////        Log.d("TESTING", "videoPlayer media count: ${videoPlayer.mediaItemCount}")
////        playVideo()
//    }
//
//    fun playVideo() {
//        videoPlayer.setMediaItem(
//            if (videoPlayer.mediaItemCount > 0) {
////                Log.d("TESTING", "first video: ${videoPlayer.getMediaItemAt(0)}")
//                videoPlayer.getMediaItemAt(0)
//            } else {
//                return
//            }
//        )
//    }

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
                _mainState.value.addToMovieList(genre,
                    result.movieApiObjects.map {
                        MovieListItem(
                            it.title,
                            it.overview,
                            convertDate(it.releaseDate),
                            it.adult,
                            it.genreIds,
                            it.voteCount,
                            it.voteAverage,
                            it.posterImageUrl,
                            it.backdropImageUrl,
                            it.ytVideoKey
                        )
                    }
                )
//                movieLists.value.movieLists = HashMap(movieLists.value.movieLists)
                _loadingErrorMessage.value = ""
//                _mainState.value = mainState.value.copy()
            }
        }
        _isMovieLoading.value = false
        _currMovieItem.value = mainState.value.getMovieList(genre)[0]
    }


    private fun processVideoData(response: Resource<MovieApiVideosResult>, currMovieObj: MovieApiObject) {
        when (response) {
            is Resource.Error -> {
                loadingErrorMessage.value = response.message!!
                Log.w("TESTING", response.message)
            }
            is Resource.Success -> {
                val trailers = response.data!!.movieApiVideos

                // if no video data, skip
                if (trailers.isEmpty()) {
                    return
                }

                // in case there are no trailers, use anything else
                var cachedKey = ""
                var foundTeaser = false
                var foundTrailer = false

                for (trailer in trailers) {
                    if (trailer.site == "YouTube") {
                        if (trailer.type.contains("Trailer")) {
//                            currMovieObj.videoUrl = YOUTUBE_BASE_URL + trailers[0].key
                            currMovieObj.ytVideoKey = trailer.key
                            foundTrailer = true
                            break
                        } else if (trailer.type.contains("Teaser")) {
                            currMovieObj.ytVideoKey = trailer.key
                            foundTeaser = true
                        } else if (!foundTeaser) {
                            cachedKey = trailer.key
                        }
                    }
                }

                // if currMovieObj.ytVideoKey is blank, use any video (if any) as trailer
                if (!foundTeaser and !foundTrailer) {
                    currMovieObj.ytVideoKey = cachedKey
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

    private fun convertDate(date: String) : String {
        val dates = date.split("-")
        val year = dates[0]

        var month = dates[1]

        if (dates.count() > 1) {
            // maybe convert to when statement
            if (month == "1") {
                month = "January"
            } else if (month == "2") {
                month = "February"
            } else if (month == "3") {
                month = "March"
            } else if (month == "4") {
                month = "April"
            } else if (month == "5") {
                month = "May"
            } else if (month == "6") {
                month = "June"
            } else if (month == "7") {
                month = "July"
            } else if (month == "8") {
                month = "August"
            } else if (month == "9") {
                month = "September"
            } else if (month == "10") {
                month = "October"
            } else if (month == "11") {
                month = "November"
            } else if (month == "12") {
                month = "December"
            } else {
                month = ""
            }
        }
        return "$month $year"

    }

//    override fun onCleared() {
//        super.onCleared()
//        videoPlayer.release()
//    }
}



