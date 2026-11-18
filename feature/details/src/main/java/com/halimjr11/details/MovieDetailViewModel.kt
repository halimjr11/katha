package com.halimjr11.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.halimjr11.common.NetworkResult
import com.halimjr11.domain.model.MovieDetail
import com.halimjr11.domain.model.Review
import com.halimjr11.domain.model.VideoTrailer
import com.halimjr11.domain.usecase.GetMovieDetailUseCase
import com.halimjr11.domain.usecase.GetMovieReviewsUseCase
import com.halimjr11.domain.usecase.GetMovieTrailerUseCase
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MovieDetailUiState(
    val isLoading: Boolean = true,
    val detail: MovieDetail? = null,
    val trailer: VideoTrailer? = null,
    val error: String? = null
)

class MovieDetailViewModel(
    private val movieId: Int,
    private val getMovieDetail: GetMovieDetailUseCase,
    private val getMovieTrailer: GetMovieTrailerUseCase,
    getMovieReviews: GetMovieReviewsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MovieDetailUiState())
    val uiState: StateFlow<MovieDetailUiState> = _uiState.asStateFlow()

    val reviews: Flow<PagingData<Review>> = getMovieReviews(movieId).cachedIn(viewModelScope)

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = MovieDetailUiState(isLoading = true)
            // detail + trailer in parallel
            val (detail, trailer) = coroutineScope {
                val d = async { getMovieDetail(movieId) }
                val t = async { getMovieTrailer(movieId) }
                d.await() to t.await()
            }
            _uiState.value = when (detail) {
                is NetworkResult.Success -> MovieDetailUiState(
                    isLoading = false,
                    detail = detail.data,
                    // trailer failure only hides the CTA ("Trailer Unavailable"), not the whole screen
                    trailer = (trailer as? NetworkResult.Success)?.data
                )

                is NetworkResult.Error -> MovieDetailUiState(isLoading = false, error = ERROR_MESSAGE)
            }
        }
    }

    companion object {
        const val ERROR_MESSAGE = "Failed to load movie. Check your connection and try again."
    }
}
