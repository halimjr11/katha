package com.halimjr11.data.repository

import androidx.paging.testing.asSnapshot
import com.halimjr11.common.DispatcherProvider
import com.halimjr11.common.NetworkResult
import com.halimjr11.network.api.TmdbApiService
import com.halimjr11.network.dto.DiscoverMoviesResponseDto
import com.halimjr11.network.dto.GenreDto
import com.halimjr11.network.dto.GenreResponseDto
import com.halimjr11.network.dto.MovieDetailDto
import com.halimjr11.network.dto.MovieDto
import com.halimjr11.network.dto.ReviewDto
import com.halimjr11.network.dto.ReviewResponseDto
import com.halimjr11.network.dto.VideoDto
import com.halimjr11.network.dto.VideoResponseDto
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class MovieRepositoryImplTest {
    private val api = mockk<TmdbApiService>()
    private val dispatchers = object : DispatcherProvider {
        override val io: CoroutineDispatcher = Dispatchers.Unconfined
        override val main: CoroutineDispatcher = Dispatchers.Unconfined
        override val default: CoroutineDispatcher = Dispatchers.Unconfined
    }
    private val repo = MovieRepositoryImpl(api, dispatchers)

    @Test fun genresSuccessFiltersInvalid() = runTest {
        coEvery { api.getGenres() } returns GenreResponseDto(listOf(GenreDto(28, "Action"), GenreDto(1, "")))
        val result = repo.getGenres() as NetworkResult.Success
        assertEquals(listOf("Action"), result.data.map { it.name })
    }

    @Test fun genresNullBodyBecomesEmptySuccess() = runTest {
        coEvery { api.getGenres() } returns GenreResponseDto(null)
        assertEquals(NetworkResult.Success(emptyList<Any>()), repo.getGenres())
    }

    // every failure maps to NetworkResult.Error, keeping the HTTP status code when there is one
    @ParameterizedTest(name = "{argumentSetName}")
    @MethodSource("failures")
    fun failuresBecomeErrorWithStatusCode(failure: Throwable, code: Int?) = runTest {
        coEvery { api.getGenres() } throws failure
        val error = repo.getGenres() as NetworkResult.Error
        assertEquals(code, error.code)
    }

    @Test fun cancellationIsRethrownNotWrapped() = runTest {
        coEvery { api.getGenres() } throws kotlinx.coroutines.CancellationException("cancelled")
        val thrown = runCatching { repo.getGenres() }.exceptionOrNull()
        assertTrue(thrown is kotlinx.coroutines.CancellationException, "expected CancellationException but was $thrown")
    }

    @Test fun detailSuccess() = runTest {
        coEvery { api.getMovieDetail(1) } returns MovieDetailDto(id = 1, title = "T", runtime = 90)
        val detail = (repo.getMovieDetail(1) as NetworkResult.Success).data
        assertEquals("T", detail.title)
        assertEquals("1h 30m", detail.runtime)
    }

    @Test fun detailWithoutIdBecomesError() = runTest {
        coEvery { api.getMovieDetail(1) } returns MovieDetailDto()
        assertTrue(repo.getMovieDetail(1) is NetworkResult.Error)
    }

    @Test fun trailerPicksYoutubeTrailer() = runTest {
        coEvery { api.getMovieVideos(1) } returns VideoResponseDto(
            results = listOf(VideoDto(key = "abc", site = "YouTube", type = "Trailer", official = true))
        )
        val trailer = (repo.getMovieTrailer(1) as NetworkResult.Success).data
        assertEquals("https://www.youtube.com/watch?v=abc", trailer?.youtubeUrl)
    }

    @Test fun trailerAbsentIsSuccessWithNull() = runTest {
        coEvery { api.getMovieVideos(1) } returns VideoResponseDto(results = null)
        assertEquals(NetworkResult.Success(null), repo.getMovieTrailer(1))
    }

    @Test fun discoverFlowEmitsMappedMovies() = runTest {
        coEvery { api.discoverMovies(28, 1) } returns DiscoverMoviesResponseDto(
            results = listOf(MovieDto(id = 1, title = "A"), MovieDto(id = 2, title = "B")), totalPages = 1
        )
        val items = repo.discoverMovies(28).asSnapshot()
        assertEquals(listOf("A", "B"), items.map { it.title })
    }

    @Test fun discoverWithoutGenreLoadsAllMovies() = runTest {
        coEvery { api.discoverMovies(null, 1) } returns DiscoverMoviesResponseDto(
            results = listOf(MovieDto(id = 7, title = "Popular")), totalPages = 1
        )
        val items = repo.discoverMovies(null).asSnapshot()
        assertEquals(listOf("Popular"), items.map { it.title })
    }

    @Test fun reviewsFlowEmitsMappedReviews() = runTest {
        coEvery { api.getMovieReviews(7, 1) } returns ReviewResponseDto(
            results = listOf(ReviewDto(id = "r1", author = "Ann")), totalPages = 1
        )
        val items = repo.getMovieReviews(7).asSnapshot()
        assertEquals(listOf("Ann"), items.map { it.author })
    }

    companion object {
        private fun http(code: Int) = HttpException(Response.error<Any>(code, "".toResponseBody()))

        @JvmStatic fun failures(): Stream<Arguments> = Stream.of(
            Arguments.argumentSet("IOException(offline) -> Error(code=null)", IOException("offline"), null),
            Arguments.argumentSet("HttpException(401 Unauthorized) -> Error(code=401)", http(401), 401),
            Arguments.argumentSet("HttpException(404 Not Found) -> Error(code=404)", http(404), 404),
            Arguments.argumentSet("HttpException(429 Too Many Requests) -> Error(code=429)", http(429), 429),
            Arguments.argumentSet("HttpException(500 Server Error) -> Error(code=500)", http(500), 500),
            Arguments.argumentSet("RuntimeException(malformed JSON) -> Error(code=null)", RuntimeException("Expected BEGIN_OBJECT"), null),
        )
    }
}
