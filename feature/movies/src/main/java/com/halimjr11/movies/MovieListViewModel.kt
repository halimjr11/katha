package com.halimjr11.movies

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.halimjr11.domain.model.Movie
import com.halimjr11.domain.usecase.DiscoverMoviesByGenreUseCase
import kotlinx.coroutines.flow.Flow

// Paging's LoadState is the screen state here, so no separate UiState class.
class MovieListViewModel(
    genreId: Int,
    discoverMovies: DiscoverMoviesByGenreUseCase
) : ViewModel() {
    val movies: Flow<PagingData<Movie>> = discoverMovies(genreId).cachedIn(viewModelScope)
}
