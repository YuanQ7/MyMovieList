package com.example.showsrecommendation.util

import com.example.showsrecommendation.models.MovieListItem

class Utils {

    companion object {
        val genreStringToInt = mapOf(
            "Horror" to 27,
            "Fantasy" to 14,
            "Action" to 28,
            "Adventure" to 12,
            "Animation" to 16,
            "Comedy" to 35,
            "Crime" to 80,
//            "Documentary" to 99,
            "Drama" to 18,
            "Family" to 10751,
            "Mystery" to 9648,
            "Music" to 10402,
            "Romance" to 10749,
            "Science Fiction" to 878,
            "Thriller" to 53,
            "War" to 10752
        )

        val genreList = genreStringToInt.keys.toList()

        val defaultListItem = MovieListItem(
            "", "", "", false, listOf(1), 0,
            0.0, "", "", ""
        )
    }
}