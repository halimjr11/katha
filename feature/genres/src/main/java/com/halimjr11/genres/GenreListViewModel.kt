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
    val error: String? = null
)

class GenreListViewModel(private val getGenres: GetGenresUseCase) : ViewModel() {

    private val _uiState = MutableStateFlow(GenreListUiState())
    val uiState: StateFlow<GenreListUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { GenreListUiState(isLoading = true) }
            _uiState.value = when (val result = getGenres()) {
                is NetworkResult.Success ->
                    if (result.data.isEmpty()) GenreListUiState(isLoading = false, error = EMPTY_MESSAGE)
                    else GenreListUiState(isLoading = false, genres = result.data)

                is NetworkResult.Error -> GenreListUiState(isLoading = false, error = ERROR_MESSAGE)
            }
        }
    }

    companion object {
        const val ERROR_MESSAGE = "Couldn't load genres. Check your connection and try again."
        const val EMPTY_MESSAGE = "No genres available."
    }
}
