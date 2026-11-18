package com.halimjr11.domain.repository

import androidx.paging.PagingData
import com.halimjr11.common.NetworkResult
import com.halimjr11.domain.model.Genre
import com.halimjr11.domain.model.Movie
import com.halimjr11.domain.model.MovieDetail
import com.halimjr11.domain.model.Review
import com.halimjr11.domain.model.VideoTrailer
import kotlinx.coroutines.flow.Flow

interface MovieRepository {
    suspend fun getGenres(): NetworkResult<List<Genre>>
    fun discoverMovies(genreId: Int?): Flow<PagingData<Movie>>
    suspend fun getMovieDetail(movieId: Int): NetworkResult<MovieDetail>
    suspend fun getMovieTrailer(movieId: Int): NetworkResult<VideoTrailer?>
    fun getMovieReviews(movieId: Int): Flow<PagingData<Review>>
    /** First [limit] reviews, for the detail screen. The full list is [getMovieReviews]. */
    suspend fun getReviewHighlights(movieId: Int, limit: Int): NetworkResult<List<Review>>
}
