package com.halimjr11.domain.usecase

import com.halimjr11.common.NetworkResult
import com.halimjr11.domain.model.Genre
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

class GetGenresUseCaseTest {
    private val repo = mockk<MovieRepository>()
    private val useCase = GetGenresUseCase(repo)

    @ParameterizedTest(name = "repository returns {argumentSetName}")
    @MethodSource("results")
    fun returnsRepositoryResultUnchanged(result: NetworkResult<List<Genre>>) = runTest {
        coEvery { repo.getGenres() } returns result
        assertSame(result, useCase())
        coVerify(exactly = 1) { repo.getGenres() }
    }

    companion object {
        @JvmStatic fun results(): Stream<Arguments> = Stream.of(
            Arguments.argumentSet("Success(2 genres: Action, Drama)", NetworkResult.Success(listOf(Genre(1, "Action"), Genre(2, "Drama")))),
            Arguments.argumentSet("Success(empty list)", NetworkResult.Success(emptyList<Genre>())),
            Arguments.argumentSet("Error(offline, no status code)", NetworkResult.Error("offline")),
            Arguments.argumentSet("Error(401 Unauthorized)", NetworkResult.Error("Unauthorized", 401)),
            Arguments.argumentSet("Error(500 server error)", NetworkResult.Error("boom", 500)),
        )
    }
}
