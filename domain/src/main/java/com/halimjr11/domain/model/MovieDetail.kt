package com.halimjr11.domain.model

data class MovieDetail(
    val id: Int = 0,
    val title: String = "",
    val tagline: String = "",
    val overview: String = "",
    val posterUrl: String = "",
    val backdropUrl: String = "",
    val releaseDate: String = "",
    val runtime: String = "",
    val rating: String = "",
    val voteCount: Int = 0,
    val status: String = "",
    val genres: List<Genre> = emptyList()
)
