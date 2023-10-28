package com.example.showsrecommendation.repository

import com.example.showsrecommendation.network.MovieApi
import com.example.showsrecommendation.network.MovieApiResult
import com.example.showsrecommendation.network.MovieApiVideosResult
import com.example.showsrecommendation.util.Constants.Companion.API_KEY
import com.example.showsrecommendation.util.Resource
import java.lang.Exception
import javax.inject.Inject
import javax.inject.Singleton

// sample url: http://image.tmdb.org/t/p/w500/your_poster_path

// sample search: https://api.themoviedb.org/3/movie/550?api_key=4e20a54133b1ee1e56497bdfcac62b74

private val genreStringToInt = hashMapOf(
    "horror" to 27,
    "fantasy" to 14
)

@Singleton
class MovieRepository @Inject constructor(
    private val movieApi: MovieApi
) {
    suspend fun getMovieList(
        category: String,
        language: String,
        page: Int,
        isMovie: Boolean
    ) : Resource<MovieApiResult> {
        val type = if (isMovie) "movie" else "tv"
        val response = try {
            movieApi.getMovieList(type, category, API_KEY, language, page)
        } catch (e: Exception) {
            return Resource.Error("${e.message}")
        }
//        Log.w("TEST", response)
        return Resource.Success(response)
    }

    suspend fun getMovieGenreList(
        language: String,
        page: Int,
        isMovie: Boolean,
        genre: String
    ) : Resource<MovieApiResult> {
        val type = if (isMovie) "movie" else "tv"
        val response = try {
            movieApi.getMovieGenreList(
                type,
                API_KEY,
                language,
                page,
                genreStringToInt.getOrDefault(genre, 27))
        } catch (e: Exception) {
            return Resource.Error("Error retrieving shows")
        }
//        Log.w("TEST", response)
        return Resource.Success(response)
    }

    suspend fun getMovieVideos(
        id: Int,
        language: String,
        isMovie: Boolean
    ): Resource<MovieApiVideosResult> {
        val type = if (isMovie) "movie" else "tv"
        val response = try {
            movieApi.getMovieVideos(type, id, API_KEY, language)
        } catch (e: Exception) {
            return Resource.Error("Error retrieving videos")
        }
        return Resource.Success(response)
    }

}