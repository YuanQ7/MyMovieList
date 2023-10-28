package com.example.showsrecommendation.models

import android.util.Log
import com.example.showsrecommendation.network.MovieApiObject
import com.example.showsrecommendation.util.Constants.Companion.genreList
import dagger.hilt.android.scopes.ViewModelScoped

private val defaultListObj = MovieApiObject(
    "", false, "", "", List(1) { 0 },
    0, "", "", "", 0.0,
    0, 0.0)

@ViewModelScoped
data class MainUiState(
    var movieLists: HashMap<String, List<MovieApiObject>> = hashMapOf()
) {
    init {
         for (genre in genreList) {
             movieLists[genre] = listOf(defaultListObj,defaultListObj,defaultListObj
             ,defaultListObj,defaultListObj,defaultListObj,defaultListObj,defaultListObj
                 ,defaultListObj,defaultListObj
                 ,defaultListObj,defaultListObj,defaultListObj,defaultListObj)
         }
    }

    fun getMovieList(genre: String) : List<MovieApiObject> {
        return movieLists.getOrDefault(genre, movieLists["popular"]!!)
    }

    fun addToMovieList(genre: String, list: List<MovieApiObject>) {
        if (genre in movieLists) {
//            Log.w("TESTING", "$genre in movieLists")
            if (movieLists[genre]!![0] === defaultListObj) {
                // clear the dummy item
                movieLists[genre] = list.toList()
            } else {
                // otherwise add to list
               movieLists[genre] = movieLists[genre]!! + list
            }
            Log.w("TESTING", "$genre listcount: ${movieLists[genre]!!.count()}")
        }
    }
    fun setMovieList(genre: String, list: List<MovieApiObject>) {
        if (genre in movieLists) {
            movieLists[genre] = list
        }
    }
}