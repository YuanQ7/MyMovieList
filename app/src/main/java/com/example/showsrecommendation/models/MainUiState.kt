package com.example.showsrecommendation.models

import android.util.Log
import com.example.showsrecommendation.network.MovieApiObject
import com.example.showsrecommendation.util.Constants.Companion.defaultListItem
import com.example.showsrecommendation.util.Constants.Companion.genreList
import dagger.hilt.android.scopes.ViewModelScoped
import okhttp3.internal.immutableListOf

@ViewModelScoped
data class MainUiState(
    var movieLists: HashMap<String, List<MovieListItem>> = hashMapOf()
) {
    init {
         for (genre in genreList) {
             movieLists[genre] = listOf(defaultListItem)
         }
    }

    fun getMovieList(genre: String) : List<MovieListItem> {
        return movieLists.getOrDefault(genre, movieLists["popular"]!!)
    }

    fun addToMovieList(genre: String, list: List<MovieListItem>) {
        if (genre in movieLists) {
//            Log.w("TESTING", "$genre in movieLists")
            if (movieLists[genre]!![0] === defaultListItem) {
                // clear the dummy item
                movieLists[genre] = list.toList()
            } else {
                // otherwise add to list
               movieLists[genre] = movieLists[genre]!! + list
            }
            Log.w("TESTING", "$genre listcount: ${movieLists[genre]!!.count()}")
        }
    }
}