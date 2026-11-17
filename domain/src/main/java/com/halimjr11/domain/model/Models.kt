package com.halimjr11.domain.model

data class Genre(val id: Int, val name: String)

data class Movie(
    val id: Int,
    val title: String,
    val posterUrl: String,
    val releaseYear: String,
    val rating: String
)

data class MovieDetail(
    val id: Int,
    val title: String,
    val tagline: String,
    val overview: String,
    val posterUrl: String,
    val backdropUrl: String,
    val releaseDate: String,
    val runtime: String,
    val rating: String,
    val voteCount: Int,
    val status: String,
    val genres: List<Genre>
)

data class Review(
    val id: String,
    val author: String,
    val avatarUrl: String,
    val rating: String,
    val date: String,
    val content: String
)

data class VideoTrailer(
    val key: String,
    val name: String,
    val youtubeUrl: String
)
