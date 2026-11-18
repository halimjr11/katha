package com.halimjr11.domain.usecase

import com.halimjr11.common.NetworkResult
import com.halimjr11.domain.model.MovieDetail
import com.halimjr11.domain.repository.MovieRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class GetMovieDetailUseCaseTest {
    private val repo = mockk<MovieRepository>()
    private val useCase = GetMovieDetailUseCase(repo)

    @ParameterizedTest(name = "movie {0} -> {argumentSetName}")
    @MethodSource("scenarios")
    fun passesMovieIdAndReturnsRepositoryResult(movieId: Int, result: NetworkResult<MovieDetail>) = runTest {
        coEvery { repo.getMovieDetail(movieId) } returns result
        assertSame(result, useCase(movieId))
        coVerify(exactly = 1) { repo.getMovieDetail(movieId) }
    }

    companion object {
        @JvmStatic fun scenarios(): Stream<Arguments> = Stream.of(
            Arguments.argumentSet("Success(detail \"T\")", 7, NetworkResult.Success(MovieDetail(id = 7, title = "T"))),
            Arguments.argumentSet("Success(detail \"U\")", 8, NetworkResult.Success(MovieDetail(id = 8, title = "U"))),
            Arguments.argumentSet("Error(404 Not Found)", 9, NetworkResult.Error("Not Found", 404)),
            Arguments.argumentSet("Error(offline, no status code)", 10, NetworkResult.Error("offline")),
        )
    }
}
