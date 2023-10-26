package com.example.showsrecommendation.models

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.showsrecommendation.network.ApiStatus
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
class GridViewModel @Inject constructor (
    private val repository: MovieRepository,
    gridUiState: GridUiState
) : ViewModel() {

    private val _gridState = MutableStateFlow(gridUiState)
    val gridState: StateFlow<GridUiState> = _gridState.asStateFlow()

    // Todo: whether api has finished retrieving movie data
    private val _apiStatus = MutableStateFlow(ApiStatus.LOADING)
    val apiStatus = _apiStatus

    init {
        getMovieData(true) {
            processMovieData(it)
        }
    }

    private fun processMovieData(response: Resource<MovieApiResult>) {
        if (response is Resource.Error) {
            Log.w("TESTING", response.message!!)
        }
        if (response is Resource.Success) {
            // todo check for error

            // result is Success, data cannot be null
            val result = response.data!!

//            _status.value = result.movieApiObjects[0].title + " " + result.totalResults
//            Log.w("HELLO", "" + result.movieApiObjects.count())

            val uiGrid = gridState.value.grid.clone()
            for (i in 0 until uiGrid.count().coerceAtMost(result.movieApiObjects.count())) {
                // after finish getting movies, assign them to our GridStateUi state,
                // so UI changes can reflect in MainActivity
                val currMovieObj = result.movieApiObjects[i]
                uiGrid[i] = currMovieObj

                // grab trailer url
                getMovieVideos(currMovieObj) {
                    // process show videos data (onFinish())
                    if (it is Resource.Error) {
                        Log.w("TESTING", it.message!!)
                    }
                    if (it is Resource.Success) {

                        val trailer = it.data!!.movieApiVideos[0]

                        if (trailer.site == "YouTube") {
                            currMovieObj.videoUrl = YOUTUBE_BASE_URL + trailer.key
                        } else if (trailer.site == "Vimeo") {
                            currMovieObj.videoUrl = VIMEO_BASE_URL + trailer.key
                        }
                        Log.w("TESTING", currMovieObj.videoUrl)
                    }
                }

                // make paths the url to the images
                currMovieObj.posterImageUrl = IMAGE_BASE_URL + currMovieObj.posterPath
                currMovieObj.backdropImageUrl = IMAGE_BASE_URL + currMovieObj.backdropPath

//                Log.d("TEST", uiGrid[i].posterPath + "\n" + uiGrid[i].backdropPath)
            }
            // notify MainActivity
            Log.w("TESTING", uiGrid[0].posterPath)
            gridState.value.grid = uiGrid
        }
    }
    private fun getMovieData(isMovie: Boolean, onFinish: (Resource<MovieApiResult>) -> Unit) {
        // grab movie data response from repository
        viewModelScope.launch {
            onFinish(repository.getMovieList(
                "popular", LANG_EN, 1, isMovie
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
        viewModelScope.launch {
            onFinish(repository.getMovieVideos(
                currMovieObj.id, LANG_EN, true
            ))
        }
    }

}