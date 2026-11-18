package com.halimjr11.domain.usecase

import com.halimjr11.common.NetworkResult
import com.halimjr11.domain.model.Review
import com.halimjr11.domain.repository.MovieRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class GetMovieReviewHighlightsUseCaseTest {
    private val repo = mockk<MovieRepository>()
    private val useCase = GetMovieReviewHighlightsUseCase(repo)

    @Test fun defaultLimitIsThree() = runTest {
        coEvery { repo.getReviewHighlights(7, 3) } returns NetworkResult.Success(emptyList())
        useCase(7)
        coVerify(exactly = 1) { repo.getReviewHighlights(7, 3) }
        assertEquals(3, GetMovieReviewHighlightsUseCase.DEFAULT_LIMIT)
    }

    @ParameterizedTest(name = "repository returns {argumentSetName}")
    @MethodSource("results")
    fun returnsRepositoryResultUnchanged(result: NetworkResult<List<Review>>) = runTest {
        coEvery { repo.getReviewHighlights(7, 2) } returns result
        assertSame(result, useCase(7, limit = 2))
    }

    companion object {
        @JvmStatic fun results(): Stream<Arguments> = Stream.of(
            Arguments.argumentSet("Success(2 reviews)", NetworkResult.Success(listOf(Review(id = "a"), Review(id = "b")))),
            Arguments.argumentSet("Success(no reviews)", NetworkResult.Success(emptyList<Review>())),
            Arguments.argumentSet("Error(offline, no status code)", NetworkResult.Error("offline")),
            Arguments.argumentSet("Error(404 Not Found)", NetworkResult.Error("Not Found", 404)),
        )
    }
}
