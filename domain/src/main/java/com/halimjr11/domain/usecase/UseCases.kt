package com.halimjr11.domain.usecase

import androidx.paging.PagingData
import com.halimjr11.common.NetworkResult
import com.halimjr11.domain.model.Genre
import com.halimjr11.domain.model.Movie
import com.halimjr11.domain.model.MovieDetail
import com.halimjr11.domain.model.Review
import com.halimjr11.domain.model.VideoTrailer
import com.halimjr11.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow

class GetGenresUseCase(private val repository: MovieRepository) {
    suspend operator fun invoke(): NetworkResult<List<Genre>> = repository.getGenres()
}

class DiscoverMoviesByGenreUseCase(private val repository: MovieRepository) {
    operator fun invoke(genreId: Int): Flow<PagingData<Movie>> =
        repository.discoverMoviesByGenre(genreId)
}

class GetMovieDetailUseCase(private val repository: MovieRepository) {
    suspend operator fun invoke(movieId: Int): NetworkResult<MovieDetail> =
        repository.getMovieDetail(movieId)
}

class GetMovieTrailerUseCase(private val repository: MovieRepository) {
    suspend operator fun invoke(movieId: Int): NetworkResult<VideoTrailer?> =
        repository.getMovieTrailer(movieId)
}

class GetMovieReviewsUseCase(private val repository: MovieRepository) {
    operator fun invoke(movieId: Int): Flow<PagingData<Review>> =
        repository.getMovieReviews(movieId)
}
