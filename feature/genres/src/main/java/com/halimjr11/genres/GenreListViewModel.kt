package com.halimjr11.genres

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.halimjr11.common.NetworkResult
import com.halimjr11.domain.model.Genre
import com.halimjr11.domain.usecase.GetGenresUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GenreListUiState(
    val isLoading: Boolean = true,
    val genres: List<Genre> = emptyList(),
    val error: String? = null,
    val query: String = ""
) {
    /** Genres matching the search field (case-insensitive); all of them when the query is blank. */
    val visibleGenres: List<Genre>
        get() = query.trim().let { q -> if (q.isEmpty()) genres else genres.filter { it.name.contains(q, ignoreCase = true) } }
}

class GenreListViewModel(private val getGenres: GetGenresUseCase) : ViewModel() {

    private val _uiState = MutableStateFlow(GenreListUiState())
    val uiState: StateFlow<GenreListUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
    }

    fun load() {
        viewModelScope.launch {
            val query = _uiState.value.query // keep what the user typed across retries
            _uiState.update { GenreListUiState(isLoading = true, query = query) }
            _uiState.value = when (val result = getGenres()) {
                is NetworkResult.Success ->
                    if (result.data.isEmpty()) GenreListUiState(isLoading = false, error = EMPTY_MESSAGE, query = query)
                    else GenreListUiState(isLoading = false, genres = result.data, query = query)

                is NetworkResult.Error -> GenreListUiState(isLoading = false, error = ERROR_MESSAGE, query = query)
            }
        }
    }

    companion object {
        const val ERROR_MESSAGE = "Couldn't load genres. Check your connection and try again."
        const val EMPTY_MESSAGE = "No genres available."
    }
}
