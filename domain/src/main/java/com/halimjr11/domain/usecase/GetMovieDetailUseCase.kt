package com.halimjr11.domain.usecase

import com.halimjr11.common.NetworkResult
import com.halimjr11.domain.model.MovieDetail
import com.halimjr11.domain.repository.MovieRepository

class GetMovieDetailUseCase(private val repository: MovieRepository) {
    suspend operator fun invoke(movieId: Int): NetworkResult<MovieDetail> =
        repository.getMovieDetail(movieId)
}