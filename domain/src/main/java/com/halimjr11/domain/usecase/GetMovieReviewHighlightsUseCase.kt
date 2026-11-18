package com.halimjr11.domain.usecase

import com.halimjr11.common.NetworkResult
import com.halimjr11.domain.model.Review
import com.halimjr11.domain.repository.MovieRepository

/** The first few reviews shown on the detail screen; "see all" uses [GetMovieReviewsUseCase]. */
class GetMovieReviewHighlightsUseCase(private val repository: MovieRepository) {
    suspend operator fun invoke(movieId: Int, limit: Int = DEFAULT_LIMIT): NetworkResult<List<Review>> =
        repository.getReviewHighlights(movieId, limit)

    companion object {
        const val DEFAULT_LIMIT = 3
    }
}
