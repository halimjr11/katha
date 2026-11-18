package com.halimjr11.movies

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.halimjr11.domain.model.Movie
import com.halimjr11.domain.usecase.DiscoverMoviesUseCase
import kotlinx.coroutines.flow.Flow

// Paging's LoadState is the screen state here, so no separate UiState class.
// genreId null = the home feed (all movies); otherwise movies of that genre.
class MovieListViewModel(
    genreId: Int?,
    discoverMovies: DiscoverMoviesUseCase
) : ViewModel() {
    val movies: Flow<PagingData<Movie>> = discoverMovies(genreId).cachedIn(viewModelScope)
}
