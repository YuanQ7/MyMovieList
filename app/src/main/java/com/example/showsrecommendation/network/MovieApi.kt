package com.example.showsrecommendation.network

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

//private const val BASE_URL = "https://www.omdbapi.com"
private const val API_KEY = "4e20a54133b1ee1e56497bdfcac62b74"

//4e20a54133b1ee1e56497bdfcac62b74
interface MovieApi {
//    @GET("?apikey=7e05f529&")
    @GET("{type}/{category}")
    suspend fun getMovieList(
        @Path("type") type: String,
        @Path("category") category: String,
        @Query("api_key") key: String,
        @Query("language") language: String,
        @Query("page") page: Int
    ) : MovieApiResult

    @GET("{type}/{category}")
    suspend fun getMovieGenreList(
        @Path("type") type: String,
        @Path("category") category: String,
        @Query("api_key") key: String,
        @Query("language") language: String,
        @Query("page") page: Int,
        @Query("with_genres") genreId: Int
    ) : MovieApiResult

    @GET("{type}/{id}/videos")
    suspend fun getMovieVideos(
        @Path("type") type: String,
        @Path("id") id: Int,
        @Query("api_key") key: String,
        @Query("language") language: String
    ) : MovieApiVideosResult
}
