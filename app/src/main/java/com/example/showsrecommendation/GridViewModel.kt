package com.example.showsrecommendation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.showsrecommendation.network.ApiStatus
import com.example.showsrecommendation.network.MovieApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GridViewModel @Inject constructor (
    private val movieApi: MovieApi
) : ViewModel() {

    private val _gridState = MutableStateFlow(GridUiState())
    val gridState: StateFlow<GridUiState> = _gridState.asStateFlow()

    private val _genreState = MutableStateFlow("")
    val genreState: StateFlow<String> = _genreState

    // Todo: whether api has finished retrieving movie data
    private val _apiStatus = MutableStateFlow(ApiStatus.LOADING)
    val apiStatus = _apiStatus

    init {
        getMovieData()
    }

    // http://image.tmdb.org/t/p/w500/your_poster_path
    private fun getMovieData() {
        viewModelScope.launch {
            val imageBaseUrl = "https://image.tmdb.org/t/p/w500"
            val apiKey = "4e20a54133b1ee1e56497bdfcac62b74"
            val result = movieApi.getMovies(
                "popular", apiKey, "en-US", 1)

//            _status.value = result.movieApiObjects[0].title + " " + result.totalResults
            Log.w("HELLO", "" +  result.movieApiObjects.count())

            val uiGrid = gridState.value.grid.clone()
            for (i in 0 until uiGrid.count().coerceAtMost(result.movieApiObjects.count())) {
                // after finish getting movies, assign them to our GridStateUi state,
                // so UI changes can reflect in MainActivity
                uiGrid[i] = result.movieApiObjects[i]

                // make paths the url to the images
                uiGrid[i].posterPath = imageBaseUrl + uiGrid[i].posterPath
                uiGrid[i].backdropPath = imageBaseUrl + uiGrid[i].backdropPath

                Log.d("TEST", uiGrid[i].posterPath + "\n" + uiGrid[i].backdropPath)
            }
            // notify MainActivity
            gridState.value.grid = uiGrid
        }
    }

}