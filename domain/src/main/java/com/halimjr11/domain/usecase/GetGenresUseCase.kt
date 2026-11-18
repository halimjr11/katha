package com.halimjr11.domain.usecase

import com.halimjr11.common.NetworkResult
import com.halimjr11.domain.model.Genre
import com.halimjr11.domain.repository.MovieRepository

class GetGenresUseCase(private val repository: MovieRepository) {
    suspend operator fun invoke(): NetworkResult<List<Genre>> = repository.getGenres()
}