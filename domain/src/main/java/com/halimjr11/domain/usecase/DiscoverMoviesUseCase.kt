package com.halimjr11.domain.usecase

import androidx.paging.PagingData
import com.halimjr11.domain.model.Movie
import com.halimjr11.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow

/** Movies feed; pass a genre id to filter, or null for all movies. */
class DiscoverMoviesUseCase(private val repository: MovieRepository) {
    operator fun invoke(genreId: Int? = null): Flow<PagingData<Movie>> =
        repository.discoverMovies(genreId)
}