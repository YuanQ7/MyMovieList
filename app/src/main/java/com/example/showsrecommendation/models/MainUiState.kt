package com.example.showsrecommendation.models

import com.example.showsrecommendation.network.MovieApiObject
import dagger.hilt.android.scopes.ViewModelScoped

private val defaultListObj = MovieApiObject(
    "", false, "", "", List(1) { 0 },
    0, "", "", "", "", 0.0,
    0, 0.0)

private val genreLists = listOf("popular", "action", "horror", "fantasy")

@ViewModelScoped
data class MainUiState(
    var movieLists: HashMap<String, List<MovieApiObject>> = hashMapOf()
) {
    init {
         for (genre in genreLists) {
             movieLists[genre] = listOf(defaultListObj, defaultListObj, defaultListObj, defaultListObj, defaultListObj,
                 defaultListObj, defaultListObj, defaultListObj, defaultListObj, defaultListObj)
         }
    }

    fun getMovieList(genre: String) : List<MovieApiObject> {
        return movieLists.getOrDefault(genre, movieLists["popular"]!!)
    }

    fun addToMovieList(genre: String, list: List<MovieApiObject>) {
        if (genre in movieLists) {
            if (movieLists[genre]!![0] == defaultListObj) {
                // clear the dummy item
                movieLists[genre] = list.toList()
            } else {
                // otherwise add to list
               movieLists[genre] = movieLists[genre]!! + list
            }
        }
    }
    fun setMovieList(genre: String, list: List<MovieApiObject>) {
        if (genre in movieLists) {
            movieLists[genre] = list
        }
    }
}