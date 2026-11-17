package com.halimjr11.network.api

import com.halimjr11.network.dto.DiscoverMoviesResponseDto
import com.halimjr11.network.dto.GenreResponseDto
import com.halimjr11.network.dto.MovieDetailDto
import com.halimjr11.network.dto.ReviewResponseDto
import com.halimjr11.network.dto.VideoResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// language/sort_by omitted: TMDB defaults are en-US / popularity.desc
interface TmdbApiService {

    @GET("genre/movie/list")
    suspend fun getGenres(): GenreResponseDto

    @GET("discover/movie")
    suspend fun discoverMovies(
        @Query("with_genres") genreId: Int,
        @Query("page") page: Int
    ): DiscoverMoviesResponseDto

    @GET("movie/{movie_id}")
    suspend fun getMovieDetail(@Path("movie_id") movieId: Int): MovieDetailDto

    @GET("movie/{movie_id}/videos")
    suspend fun getMovieVideos(@Path("movie_id") movieId: Int): VideoResponseDto

    @GET("movie/{movie_id}/reviews")
    suspend fun getMovieReviews(
        @Path("movie_id") movieId: Int,
        @Query("page") page: Int
    ): ReviewResponseDto
}
