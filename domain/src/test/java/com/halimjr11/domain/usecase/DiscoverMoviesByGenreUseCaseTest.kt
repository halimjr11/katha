package com.halimjr11.domain.usecase

import androidx.paging.PagingData
import app.cash.turbine.test
import com.halimjr11.domain.model.Movie
import com.halimjr11.domain.repository.MovieRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.emptyFlow
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

class DiscoverMoviesByGenreUseCaseTest {
    private val repo = mockk<MovieRepository>()
    private val useCase = DiscoverMoviesByGenreUseCase(repo)

    // The use case must forward every upstream event untouched: items in order, then complete or error
    @ParameterizedTest(name = "upstream: {argumentSetName}")
    @MethodSource("upstreams")
    fun forwardsEveryUpstreamEvent(emitted: List<PagingData<Movie>>, failure: Throwable?) = runTest {
        every { repo.discoverMoviesByGenre(28) } returns flow {
            emitted.forEach { emit(it) }
            failure?.let { throw it }
        }
        useCase(28).test {
            emitted.forEach { assertSame(it, awaitItem()) }
            if (failure == null) awaitComplete() else assertSame(failure, awaitError())
        }
    }

    @ParameterizedTest(name = "passes genreId={0} to the repository")
    @ValueSource(ints = [28, 18, 35])
    fun passesTheGivenGenreId(genreId: Int) {
        val flow = flowOf(PagingData.empty<Movie>())
        every { repo.discoverMoviesByGenre(genreId) } returns flow
        assertSame(flow, useCase(genreId))
        verify(exactly = 1) { repo.discoverMoviesByGenre(genreId) }
    }

    companion object {
        private val first = PagingData.empty<Movie>()
        private val second = PagingData.empty<Movie>()

        @JvmStatic fun upstreams(): Stream<Arguments> = Stream.of(
            Arguments.argumentSet("2 pages, then completes", listOf(first, second), null),
            Arguments.argumentSet("no pages, completes immediately", emptyList<PagingData<Movie>>(), null),
            Arguments.argumentSet("1 page, then fails with IOException(offline)", listOf(first), IOException("offline")),
        )
    }
}
