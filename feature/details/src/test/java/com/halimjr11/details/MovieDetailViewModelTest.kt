package com.halimjr11.details

import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import app.cash.turbine.test
import com.halimjr11.common.NetworkResult
import com.halimjr11.domain.model.MovieDetail
import com.halimjr11.domain.model.Review
import com.halimjr11.domain.model.VideoTrailer
import com.halimjr11.domain.usecase.GetMovieDetailUseCase
import com.halimjr11.domain.usecase.GetMovieReviewsUseCase
import com.halimjr11.domain.usecase.GetMovieTrailerUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

@OptIn(ExperimentalCoroutinesApi::class)
class MovieDetailViewModelTest {
    // runTest shares this scheduler, so the ViewModel's coroutines run while Turbine awaits
    private val dispatcher = StandardTestDispatcher()
    private val detailUc = mockk<GetMovieDetailUseCase>()
    private val trailerUc = mockk<GetMovieTrailerUseCase>()
    private val reviewsUc = mockk<GetMovieReviewsUseCase>()

    // asSnapshot waits for idle load states, which PagingData.from(list) alone never reports
    private val idle = LoadStates(LoadState.NotLoading(false), LoadState.NotLoading(true), LoadState.NotLoading(true))

    @BeforeEach fun setUp() {
        Dispatchers.setMain(dispatcher)
        every { reviewsUc(1) } returns flowOf(PagingData.empty())
    }

    @AfterEach fun tearDown() = Dispatchers.resetMain()

    private fun vm() = MovieDetailViewModel(1, detailUc, trailerUc, reviewsUc)

    // Each scenario must emit Loading first, then its final state
    @ParameterizedTest(name = "{argumentSetName}")
    @MethodSource("scenarios")
    fun emitsLoadingThenFinalState(
        detailResult: NetworkResult<MovieDetail>,
        trailerResult: NetworkResult<VideoTrailer?>,
        expected: MovieDetailUiState
    ) = runTest(dispatcher) {
        coEvery { detailUc(1) } returns detailResult
        coEvery { trailerUc(1) } returns trailerResult
        vm().uiState.test {
            assertTrue(awaitItem().isLoading, "first state must be Loading")
            assertEquals(expected, awaitItem())
            expectNoEvents()
        }
    }

    @Test fun staysLoadingWhileRequestsAreInFlight() = runTest(dispatcher) {
        coEvery { detailUc(1) } coAnswers { awaitCancellation() }
        coEvery { trailerUc(1) } coAnswers { awaitCancellation() }
        vm().uiState.test {
            assertTrue(awaitItem().isLoading)
            expectNoEvents()
        }
    }

    @Test fun retryAfterDetailFailureGoesLoadingThenSuccess() = runTest(dispatcher) {
        coEvery { detailUc(1) } returns NetworkResult.Error("404", 404)
        coEvery { trailerUc(1) } returns NetworkResult.Success(null)
        val vm = vm()
        vm.uiState.test {
            assertTrue(awaitItem().isLoading)
            assertEquals(MovieDetailViewModel.ERROR_MESSAGE, awaitItem().error)

            coEvery { detailUc(1) } returns NetworkResult.Success(detail)
            vm.load()
            assertTrue(awaitItem().isLoading)
            assertEquals(detail, awaitItem().detail)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test fun exposesReviewsFlow() = runTest(dispatcher) {
        val review = Review(id = "r1", author = "Ann", content = "Great")
        every { reviewsUc(1) } returns flowOf(PagingData.from(listOf(review), idle))
        coEvery { detailUc(1) } returns NetworkResult.Success(detail)
        coEvery { trailerUc(1) } returns NetworkResult.Success(null)
        assertEquals(listOf(review), vm().reviews.asSnapshot())
    }

    companion object {
        private val detail = MovieDetail(id = 1, title = "T", overview = "o", rating = "7.8")
        private val trailer = VideoTrailer("abc", "Trailer", "https://www.youtube.com/watch?v=abc")

        @JvmStatic fun scenarios(): Stream<Arguments> = Stream.of(
            Arguments.argumentSet("detail ok + trailer ok -> Loading, then detail and trailer", NetworkResult.Success(detail), NetworkResult.Success<VideoTrailer?>(trailer),
                MovieDetailUiState(isLoading = false, detail = detail, trailer = trailer)),
            Arguments.argumentSet("detail ok + no trailer -> Loading, then detail without trailer", NetworkResult.Success(detail), NetworkResult.Success<VideoTrailer?>(null),
                MovieDetailUiState(isLoading = false, detail = detail, trailer = null)),
            Arguments.argumentSet("detail ok + trailer fails -> Loading, then detail kept, trailer hidden", NetworkResult.Success(detail), NetworkResult.Error("boom"),
                MovieDetailUiState(isLoading = false, detail = detail, trailer = null)),
            Arguments.argumentSet("detail fails (404) -> Loading, then error message", NetworkResult.Error("404", 404), NetworkResult.Success<VideoTrailer?>(trailer),
                MovieDetailUiState(isLoading = false, error = MovieDetailViewModel.ERROR_MESSAGE)),
        )
    }
}
