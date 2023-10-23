package com.example.showsrecommendation.network
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// Json returned consists of page, results (list of movie api objects)
@JsonClass(generateAdapter = true)
data class MovieApiResult(
    @Json(name = "page") val page: Int,
    @Json(name = "results") val movieApiObjects: List<MovieApiObject>,
    @Json(name = "total_results") val totalResults: Int,
    @Json(name = "total_pages") val totalPages: Int
)

@JsonClass(generateAdapter = true)
data class MovieApiObject(
    @Json(name = "poster_path") var posterPath: String,
    @Json(name = "adult") val adult: Boolean,
    @Json(name = "overview") val overview: String,
    @Json(name = "release_date") val releaseDate: String,
    @Json(name = "genre_ids") val genreIds: List<Int>,
    @Json(name = "id") val id: Int,
    @Json(name = "original_title") val originalTitle: String,
    @Json(name = "original_language") val originalLanguage: String,
    @Json(name = "title") val title: String,
    @Json(name = "backdrop_path") var backdropPath: String,
    @Json(name = "popularity") val popularity: Double,
    @Json(name = "vote_count") val voteCount: Int,
    @Json(name = "video") val video: Boolean,
    @Json(name = "vote_average") val voteAverage: Double
)

enum class ApiStatus {
    LOADING, SUCCESS, STANDBY
}