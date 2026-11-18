package com.halimjr11.domain.usecase

import androidx.paging.PagingData
import app.cash.turbine.test
import com.halimjr11.domain.model.Review
import com.halimjr11.domain.repository.MovieRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import java.io.IOException
import java.util.stream.Stream

class GetMovieReviewsUseCaseTest {
    private val repo = mockk<MovieRepository>()
    private val useCase = GetMovieReviewsUseCase(repo)

    // The use case must forward every upstream event untouched: items in order, then complete or error
    @ParameterizedTest(name = "upstream: {argumentSetName}")
    @MethodSource("upstreams")
    fun forwardsEveryUpstreamEvent(emitted: List<PagingData<Review>>, failure: Throwable?) = runTest {
        every { repo.getMovieReviews(1) } returns flow {
            emitted.forEach { emit(it) }
            failure?.let { throw it }
        }
        useCase(1).test {
            emitted.forEach { assertSame(it, awaitItem()) }
            if (failure == null) awaitComplete() else assertSame(failure, awaitError())
        }
    }

    @ParameterizedTest(name = "passes movieId={0} to the repository")
    @ValueSource(ints = [7, 8, 9])
    fun passesTheGivenMovieId(movieId: Int) {
        val flow = flowOf(PagingData.empty<Review>())
        every { repo.getMovieReviews(movieId) } returns flow
        assertSame(flow, useCase(movieId))
        verify(exactly = 1) { repo.getMovieReviews(movieId) }
    }

    companion object {
        private val first = PagingData.empty<Review>()
        private val second = PagingData.empty<Review>()

        @JvmStatic fun upstreams(): Stream<Arguments> = Stream.of(
            Arguments.argumentSet("2 pages, then completes", listOf(first, second), null),
            Arguments.argumentSet("no pages, completes immediately", emptyList<PagingData<Review>>(), null),
            Arguments.argumentSet("1 page, then fails with IOException(offline)", listOf(first), IOException("offline")),
        )
    }
}
