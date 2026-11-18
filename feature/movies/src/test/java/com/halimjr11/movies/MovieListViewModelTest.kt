package com.halimjr11.movies

import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import com.halimjr11.domain.model.Movie
import com.halimjr11.domain.usecase.DiscoverMoviesUseCase
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
class MovieListViewModelTest {
    private val useCase = mockk<DiscoverMoviesUseCase>()

    // runTest must share this dispatcher/scheduler, or paging work on Main never advances
    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeEach fun setUp() = Dispatchers.setMain(testDispatcher)
    @AfterEach fun tearDown() = Dispatchers.resetMain()

    // asSnapshot waits for idle load states, which PagingData.from(list) alone never reports
    private val idle = androidx.paging.LoadStates(
        androidx.paging.LoadState.NotLoading(false),
        androidx.paging.LoadState.NotLoading(true),
        androidx.paging.LoadState.NotLoading(true)
    )

    private fun movie(id: Int) = Movie(id, "M$id", "", "2024", "7.0")

    @Test fun exposesPagedMoviesForGenre() = runTest(testDispatcher) {
        every { useCase(28) } returns flowOf(PagingData.from(listOf(movie(1), movie(2)), idle))
        val vm = MovieListViewModel(28, useCase)
        assertEquals(listOf(1, 2), vm.movies.asSnapshot().map { it.id })
        verify(exactly = 1) { useCase(28) }
    }

    @Test fun withoutGenreItShowsAllMovies() = runTest(testDispatcher) {
        every { useCase(null) } returns flowOf(PagingData.from(listOf(movie(5)), idle))
        val vm = MovieListViewModel(null, useCase)
        assertEquals(listOf(5), vm.movies.asSnapshot().map { it.id })
        verify(exactly = 1) { useCase(null) }
    }

    @Test fun emptyGenreEmitsNoItems() = runTest(testDispatcher) {
        every { useCase(99) } returns flowOf(PagingData.empty(idle))
        assertEquals(emptyList<Movie>(), MovieListViewModel(99, useCase).movies.asSnapshot())
    }
}
