package com.halimjr11.domain.usecase

import com.halimjr11.common.NetworkResult
import com.halimjr11.domain.model.VideoTrailer
import com.halimjr11.domain.repository.MovieRepository

class GetMovieTrailerUseCase(private val repository: MovieRepository) {
    suspend operator fun invoke(movieId: Int): NetworkResult<VideoTrailer?> =
        repository.getMovieTrailer(movieId)
}