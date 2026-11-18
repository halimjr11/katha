package com.halimjr11.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.halimjr11.common.NetworkResult
import com.halimjr11.domain.model.MovieDetail
import com.halimjr11.domain.model.Review
import com.halimjr11.domain.model.VideoTrailer
import com.halimjr11.domain.usecase.GetMovieDetailUseCase
import com.halimjr11.domain.usecase.GetMovieReviewHighlightsUseCase
import com.halimjr11.domain.usecase.GetMovieTrailerUseCase
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MovieDetailUiState(
    val isLoading: Boolean = true,
    val detail: MovieDetail? = null,
    val trailer: VideoTrailer? = null,
    val highlights: List<Review> = emptyList(),
    val reviewsFailed: Boolean = false,
    val error: String? = null
)

class MovieDetailViewModel(
    private val movieId: Int,
    private val getMovieDetail: GetMovieDetailUseCase,
    private val getMovieTrailer: GetMovieTrailerUseCase,
    private val getReviewHighlights: GetMovieReviewHighlightsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MovieDetailUiState())
    val uiState: StateFlow<MovieDetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = MovieDetailUiState(isLoading = true)
            // detail, trailer and review highlights in parallel
            val (detail, trailer, reviews) = coroutineScope {
                val d = async { getMovieDetail(movieId) }
                val t = async { getMovieTrailer(movieId) }
                val r = async { getReviewHighlights(movieId) }
                Triple(d.await(), t.await(), r.await())
            }
            _uiState.value = when (detail) {
                is NetworkResult.Success -> MovieDetailUiState(
                    isLoading = false,
                    detail = detail.data,
                    // trailer / review failures only affect their own section, not the whole screen
                    trailer = (trailer as? NetworkResult.Success)?.data,
                    highlights = (reviews as? NetworkResult.Success)?.data.orEmpty(),
                    reviewsFailed = reviews is NetworkResult.Error
                )

                is NetworkResult.Error -> MovieDetailUiState(isLoading = false, error = ERROR_MESSAGE)
            }
        }
    }

    /** Retry only the reviews section. */
    fun retryReviews() {
        viewModelScope.launch {
            when (val result = getReviewHighlights(movieId)) {
                is NetworkResult.Success -> _uiState.update { it.copy(highlights = result.data, reviewsFailed = false) }
                is NetworkResult.Error -> _uiState.update { it.copy(reviewsFailed = true) }
            }
        }
    }

    companion object {
        const val ERROR_MESSAGE = "Failed to load movie. Check your connection and try again."
    }
}
