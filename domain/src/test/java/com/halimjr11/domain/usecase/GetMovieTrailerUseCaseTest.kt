package com.halimjr11.domain.usecase

import com.halimjr11.common.NetworkResult
import com.halimjr11.domain.model.VideoTrailer
import com.halimjr11.domain.repository.MovieRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class GetMovieTrailerUseCaseTest {
    private val repo = mockk<MovieRepository>()
    private val useCase = GetMovieTrailerUseCase(repo)

    @ParameterizedTest(name = "repository returns {argumentSetName}")
    @MethodSource("results")
    fun returnsRepositoryResultUnchanged(result: NetworkResult<VideoTrailer?>) = runTest {
        coEvery { repo.getMovieTrailer(7) } returns result
        assertSame(result, useCase(7))
    }

    companion object {
        @JvmStatic fun results(): Stream<Arguments> = Stream.of(
            Arguments.argumentSet("Success(YouTube trailer)", NetworkResult.Success<VideoTrailer?>(VideoTrailer("k", "n", "u"))),
            Arguments.argumentSet("Success(null): movie has no trailer", NetworkResult.Success<VideoTrailer?>(null)),
            Arguments.argumentSet("Error(offline, no status code)", NetworkResult.Error("offline")),
            Arguments.argumentSet("Error(404 Not Found)", NetworkResult.Error("Not Found", 404)),
        )
    }
}
