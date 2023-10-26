package com.example.showsrecommendation.models

import com.example.showsrecommendation.network.MovieApiObject
import dagger.hilt.android.scopes.ViewModelScoped

private val defaultMovieApiObject = MovieApiObject(
    "", false, "", "", List(1) { 0 },
    0, "", "", "", "", 0.0,
    0, 0.0)

@ViewModelScoped
data class GridUiState(
    var grid: Array<MovieApiObject> = Array(100) {
        defaultMovieApiObject
    }
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GridUiState

        if (!grid.contentEquals(other.grid)) return false

        return true
    }

    override fun hashCode(): Int {
        return grid.contentHashCode()
    }
}