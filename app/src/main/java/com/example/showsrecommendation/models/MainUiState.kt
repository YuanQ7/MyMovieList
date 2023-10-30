package com.example.showsrecommendation.models

import android.util.Log
import com.example.showsrecommendation.network.MovieApiObject
import com.example.showsrecommendation.util.Utils.Companion.defaultListItem
import com.example.showsrecommendation.util.Utils.Companion.genreList
import dagger.hilt.android.scopes.ViewModelScoped

@ViewModelScoped
data class MainUiState(
    val movieLists: HashMap<String, List<MovieListItem>> = hashMapOf()
) {
    init {
         for (genre in genreList) {
             movieLists[genre] = listOf(defaultListItem)
         }
    }

    fun getMovieList(genre: String) : List<MovieListItem> {
        Log.d("TESTING", "getting genre list of $genre")
        return movieLists[genre]!!
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