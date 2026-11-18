package com.halimjr11.data.paging

import androidx.paging.PagingSource.LoadParams
import androidx.paging.PagingSource.LoadResult
import com.halimjr11.network.api.TmdbApiService
import com.halimjr11.network.dto.DiscoverMoviesResponseDto
import com.halimjr11.network.dto.MovieDto
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.MethodSource
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.util.stream.Stream

class DiscoverMoviesPagingSourceTest {
    private val api = mockk<TmdbApiService>()
    private val source = DiscoverMoviesPagingSource(api, genreId = 28)
    private fun refresh(key: Int?) = LoadParams.Refresh(key, 20, false)

    @Test fun firstPageMapsItemsAndDropsOnesWithoutId() = runTest {
        coEvery { api.discoverMovies(28, 1) } returns DiscoverMoviesResponseDto(
            results = listOf(MovieDto(id = 1, title = "A"), MovieDto(id = null)),
            totalPages = 2
        )
        val page = source.load(refresh(null)) as LoadResult.Page
        assertEquals(listOf(1), page.data.map { it.id })
    }

    // page, totalPages, items on the page -> prevKey, nextKey. NULL = no key
    @ParameterizedTest(name = "loading page {0} of {1} ({2} items) -> prevKey={3}, nextKey={4}")
    @CsvSource(
        value = [
            "1, 2, 1, NULL, 2",
            "2, 2, 1, 1, NULL",
            "3, 10, 20, 2, 4",
            "2, 9, 0, 1, NULL", // empty page ends paging
            "1, 0, 0, NULL, NULL", // no pages at all
            "500, 1000, 1, 499, NULL", // TMDB caps at 500
        ],
        nullValues = ["NULL"]
    )
    fun pageKeys(page: Int, totalPages: Int, items: Int, prev: Int?, next: Int?) = runTest {
        coEvery { api.discoverMovies(28, page) } returns DiscoverMoviesResponseDto(
            results = List(items) { MovieDto(id = it + 1) }, totalPages = totalPages
        )
        val result = source.load(refresh(page)) as LoadResult.Page
        assertEquals(prev, result.prevKey)
        assertEquals(next, result.nextKey)
    }

    @Test fun nullBodyFieldsDoNotCrash() = runTest {
        coEvery { api.discoverMovies(28, 1) } returns DiscoverMoviesResponseDto()
        val page = source.load(refresh(null)) as LoadResult.Page
        assertTrue(page.data.isEmpty())
        assertNull(page.nextKey)
    }

    @ParameterizedTest(name = "{argumentSetName}")
    @MethodSource("failures")
    fun failuresBecomeLoadError(failure: Throwable) = runTest {
        coEvery { api.discoverMovies(28, 2) } throws failure
        assertTrue(source.load(refresh(2)) is LoadResult.Error)
    }

    @Test fun cancellationIsRethrownNotWrapped() = runTest {
        coEvery { api.discoverMovies(28, 1) } throws CancellationException("cancelled")
        val thrown = runCatching { source.load(refresh(null)) }.exceptionOrNull()
        assertTrue(thrown is CancellationException, "expected CancellationException but was $thrown")
    }

    companion object {
        @JvmStatic fun failures(): Stream<Arguments> = Stream.of(
            Arguments.argumentSet("IOException(offline) -> LoadResult.Error", IOException("offline")),
            Arguments.argumentSet("HttpException(500) -> LoadResult.Error", HttpException(Response.error<Any>(500, "".toResponseBody()))),
            Arguments.argumentSet("RuntimeException(malformed JSON) -> LoadResult.Error", RuntimeException("Expected BEGIN_OBJECT")),
        )
    }
}
