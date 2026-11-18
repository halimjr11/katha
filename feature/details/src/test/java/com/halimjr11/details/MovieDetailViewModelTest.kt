package com.halimjr11.details

import app.cash.turbine.test
import com.halimjr11.common.NetworkResult
import com.halimjr11.domain.model.MovieDetail
import com.halimjr11.domain.model.Review
import com.halimjr11.domain.model.VideoTrailer
import com.halimjr11.domain.usecase.GetMovieDetailUseCase
import com.halimjr11.domain.usecase.GetMovieReviewHighlightsUseCase
import com.halimjr11.domain.usecase.GetMovieTrailerUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
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
    private val highlightsUc = mockk<GetMovieReviewHighlightsUseCase>()

    @BeforeEach fun setUp() = Dispatchers.setMain(dispatcher)
    @AfterEach fun tearDown() = Dispatchers.resetMain()

    private fun vm() = MovieDetailViewModel(1, detailUc, trailerUc, highlightsUc)

    // Each scenario must emit Loading first, then its final state
    @ParameterizedTest(name = "{argumentSetName}")
    @MethodSource("scenarios")
    fun emitsLoadingThenFinalState(
        detailResult: NetworkResult<MovieDetail>,
        trailerResult: NetworkResult<VideoTrailer?>,
        reviewsResult: NetworkResult<List<Review>>,
        expected: MovieDetailUiState
    ) = runTest(dispatcher) {
        coEvery { detailUc(1) } returns detailResult
        coEvery { trailerUc(1) } returns trailerResult
        coEvery { highlightsUc(1) } returns reviewsResult
        vm().uiState.test {
            assertTrue(awaitItem().isLoading, "first state must be Loading")
            assertEquals(expected, awaitItem())
            expectNoEvents()
        }
    }

    @Test fun staysLoadingWhileRequestsAreInFlight() = runTest(dispatcher) {
        coEvery { detailUc(1) } coAnswers { awaitCancellation() }
        coEvery { trailerUc(1) } coAnswers { awaitCancellation() }
        coEvery { highlightsUc(1) } coAnswers { awaitCancellation() }
        vm().uiState.test {
            assertTrue(awaitItem().isLoading)
            expectNoEvents()
        }
    }

    @Test fun retryAfterDetailFailureGoesLoadingThenSuccess() = runTest(dispatcher) {
        coEvery { detailUc(1) } returns NetworkResult.Error("404", 404)
        coEvery { trailerUc(1) } returns NetworkResult.Success(null)
        coEvery { highlightsUc(1) } returns NetworkResult.Success(emptyList())
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

    @Test fun retryReviewsReloadsOnlyTheReviewsSection() = runTest(dispatcher) {
        coEvery { detailUc(1) } returns NetworkResult.Success(detail)
        coEvery { trailerUc(1) } returns NetworkResult.Success(trailer)
        coEvery { highlightsUc(1) } returns NetworkResult.Error("offline")
        val vm = vm()
        vm.uiState.test {
            assertTrue(awaitItem().isLoading)
            assertTrue(awaitItem().reviewsFailed)

            coEvery { highlightsUc(1) } returns NetworkResult.Success(listOf(review("a")))
            vm.retryReviews()
            val recovered = awaitItem()
            assertEquals(listOf(review("a")), recovered.highlights)
            assertEquals(false, recovered.reviewsFailed)
            assertEquals(detail, recovered.detail) // detail and trailer untouched
            cancelAndIgnoreRemainingEvents()
        }
        coVerify(exactly = 1) { detailUc(1) }
    }

    companion object {
        private val detail = MovieDetail(id = 1, title = "T", overview = "o", rating = "7.8")
        private val trailer = VideoTrailer("abc", "Trailer", "https://www.youtube.com/watch?v=abc")
        private fun review(id: String) = Review(id = id, author = "Ann", content = "Great")
        private val three = listOf(review("a"), review("b"), review("c"))

        @JvmStatic fun scenarios(): Stream<Arguments> = Stream.of(
            Arguments.argumentSet("everything ok -> Loading, then detail, trailer and 3 highlights",
                NetworkResult.Success(detail), NetworkResult.Success<VideoTrailer?>(trailer), NetworkResult.Success(three),
                MovieDetailUiState(isLoading = false, detail = detail, trailer = trailer, highlights = three)),
            Arguments.argumentSet("no trailer, no reviews -> Loading, then detail only",
                NetworkResult.Success(detail), NetworkResult.Success<VideoTrailer?>(null), NetworkResult.Success(emptyList<Review>()),
                MovieDetailUiState(isLoading = false, detail = detail)),
            Arguments.argumentSet("trailer fails -> Loading, then detail kept, trailer hidden",
                NetworkResult.Success(detail), NetworkResult.Error("boom"), NetworkResult.Success(three),
                MovieDetailUiState(isLoading = false, detail = detail, highlights = three)),
            Arguments.argumentSet("reviews fail -> Loading, then detail kept, reviews retry shown",
                NetworkResult.Success(detail), NetworkResult.Success<VideoTrailer?>(trailer), NetworkResult.Error("offline"),
                MovieDetailUiState(isLoading = false, detail = detail, trailer = trailer, reviewsFailed = true)),
            Arguments.argumentSet("detail fails (404) -> Loading, then error message",
                NetworkResult.Error("404", 404), NetworkResult.Success<VideoTrailer?>(trailer), NetworkResult.Success(three),
                MovieDetailUiState(isLoading = false, error = MovieDetailViewModel.ERROR_MESSAGE)),
        )
    }
}
