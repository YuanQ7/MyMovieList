package com.example.showsrecommendation.models

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.showsrecommendation.network.MovieApiObject
import com.example.showsrecommendation.network.MovieApiResult
import com.example.showsrecommendation.network.MovieApiVideosResult
import com.example.showsrecommendation.repository.MovieRepository
import com.example.showsrecommendation.util.Constants.Companion.IMAGE_BASE_URL
import com.example.showsrecommendation.util.Constants.Companion.LANG_EN
import com.example.showsrecommendation.util.Constants.Companion.VIMEO_BASE_URL
import com.example.showsrecommendation.util.Constants.Companion.YOUTUBE_BASE_URL
import com.example.showsrecommendation.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor (
    private val repository: MovieRepository,
    mainUiState: MainUiState
) : ViewModel() {

    private val _movieLists = MutableStateFlow(mainUiState)
    val movieLists: StateFlow<MainUiState> = _movieLists.asStateFlow()

    private val _popularList = MutableStateFlow(mainUiState.movieLists["popular"]!!)
    val popularList = _popularList


    // Todo: whether api has finished retrieving movie data
    private val _isMovieLoading = MutableStateFlow(false)
    val isMovieLoading = _isMovieLoading

    private val _isVideoLoading = MutableStateFlow(false)
    val isVideoLoading = _isVideoLoading

    private val _currPage = MutableStateFlow(1)
    val currPage = _currPage

    // if we ran out of pages to get from api (highly unlikely)
    private val _endReached = MutableStateFlow(false)
    val endReached = _endReached

    private val _loadingErrorMessage = MutableStateFlow("")
    val loadingErrorMessage = _loadingErrorMessage

    init {

    }

    fun loadMoviesPaginated() {
        Log.w("TESTING", "paginating ${currPage.value} ${movieLists.value.getMovieList("popular").count()}")
        getMovieData(true) {
            processMovieData(it, "popular")
        }
    }

    private fun processMovieData(
        response: Resource<MovieApiResult>,
        genre: String
    ) {
        _isMovieLoading.value = false
        when (response) {
            is Resource.Error -> {
                loadingErrorMessage.value = response.message!!
                Log.w("TESTING", response.message)
            }
            is Resource.Success -> {
                currPage.value++
                // result is Success, data cannot be null
                val result = response.data!!

                endReached.value = currPage.value >= result.totalPages

//            _status.value = result.movieApiObjects[0].title + " " + result.totalResults
//            Log.w("HELLO", "" + result.movieApiObjects.count())

                val newUiList = movieLists.value.getMovieList(genre)
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
                    currMovieObj.backdropImageUrl = IMAGE_BASE_URL + currMovieObj.backdropPath

//                Log.d("TEST", uiGrid[i].posterPath + "\n" + uiGrid[i].backdropPath)
                }
                // notify MainActivity
                Log.w("TESTING", newUiList[0].posterPath)
                movieLists.value.addToMovieList(genre, result.movieApiObjects)
//                movieLists.value.movieLists = HashMap(movieLists.value.movieLists)
            }
        }
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
                    } else if (trailers[0].site == "Vimeo") {
                        currMovieObj.videoUrl = VIMEO_BASE_URL + trailers[0].key
                    }
                }
//                Log.w("TESTING", currMovieObj.videoUrl)
            }
        }
    }

    private fun getMovieData(isMovie: Boolean, onFinish: (Resource<MovieApiResult>) -> Unit) {
        // grab movie data response from repository
        _isMovieLoading.value = true
        viewModelScope.launch {
            onFinish(repository.getMovieList(
                "popular", LANG_EN, currPage.value, isMovie
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
}

