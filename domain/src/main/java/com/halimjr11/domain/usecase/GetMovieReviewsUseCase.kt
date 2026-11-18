package com.halimjr11.domain.usecase

import androidx.paging.PagingData
import com.halimjr11.domain.model.Review
import com.halimjr11.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow

class GetMovieReviewsUseCase(private val repository: MovieRepository) {
    operator fun invoke(movieId: Int): Flow<PagingData<Review>> =
        repository.getMovieReviews(movieId)
}