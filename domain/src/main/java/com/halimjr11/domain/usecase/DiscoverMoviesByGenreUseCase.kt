package com.halimjr11.domain.usecase

import androidx.paging.PagingData
import com.halimjr11.domain.model.Movie
import com.halimjr11.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow

class DiscoverMoviesByGenreUseCase(private val repository: MovieRepository) {
    operator fun invoke(genreId: Int): Flow<PagingData<Movie>> =
        repository.discoverMoviesByGenre(genreId)
}