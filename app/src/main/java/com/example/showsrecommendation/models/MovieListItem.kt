package com.example.showsrecommendation.models

// represents an movie item in the lists to display in UI
data class MovieListItem (
    val title: String,
    val overview: String,
    val releaseDate: String,
    val adult: Boolean,
    val genreIds: List<Int>,
    val voteCount: Int,
    val voteAverage: Double,
    val posterImageUrl: String,
    val backdropImageUrl: String,
    val ytVideoKey: String
)