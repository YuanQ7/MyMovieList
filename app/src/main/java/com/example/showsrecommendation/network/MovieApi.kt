package com.example.showsrecommendation.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

//private const val BASE_URL = "https://www.omdbapi.com"
private const val API_KEY = "4e20a54133b1ee1e56497bdfcac62b74"

//4e20a54133b1ee1e56497bdfcac62b74
interface MovieApi {
//    @GET("?apikey=7e05f529&")
    @GET("3/movie/{category}")
    suspend fun getMovies(
        @Path("category") category: String,
        @Query("api_key") key: String,
        @Query("language") language: String,
        @Query("page") page: Int) : MovieApiResult
}
