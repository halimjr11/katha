package com.halimjr11.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.halimjr11.domain.model.Review
import com.halimjr11.domain.usecase.GetMovieReviewsUseCase
import kotlinx.coroutines.flow.Flow

// Paging's LoadState is the screen state here, so no separate UiState class.
class MovieReviewsViewModel(
    movieId: Int,
    getMovieReviews: GetMovieReviewsUseCase
) : ViewModel() {
    val reviews: Flow<PagingData<Review>> = getMovieReviews(movieId).cachedIn(viewModelScope)
}
