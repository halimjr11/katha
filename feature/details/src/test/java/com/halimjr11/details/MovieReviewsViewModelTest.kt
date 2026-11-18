package com.halimjr11.details

import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import com.halimjr11.domain.model.Review
import com.halimjr11.domain.usecase.GetMovieReviewsUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MovieReviewsViewModelTest {
    private val useCase = mockk<GetMovieReviewsUseCase>()

    // runTest must share this dispatcher/scheduler, or paging work on Main never advances
    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeEach fun setUp() = Dispatchers.setMain(testDispatcher)
    @AfterEach fun tearDown() = Dispatchers.resetMain()

    // asSnapshot waits for idle load states, which PagingData.from(list) alone never reports
    private val idle = LoadStates(LoadState.NotLoading(false), LoadState.NotLoading(true), LoadState.NotLoading(true))

    private fun review(id: String) = Review(id = id, author = "Ann", content = "Great")

    @Test fun exposesPagedReviewsOfTheMovie() = runTest(testDispatcher) {
        every { useCase(7) } returns flowOf(PagingData.from(listOf(review("a"), review("b")), idle))
        val vm = MovieReviewsViewModel(7, useCase)
        assertEquals(listOf("a", "b"), vm.reviews.asSnapshot().map { it.id })
        verify(exactly = 1) { useCase(7) }
    }

    @Test fun movieWithoutReviewsEmitsNoItems() = runTest(testDispatcher) {
        every { useCase(8) } returns flowOf(PagingData.empty(idle))
        assertEquals(emptyList<Review>(), MovieReviewsViewModel(8, useCase).reviews.asSnapshot())
    }
}
