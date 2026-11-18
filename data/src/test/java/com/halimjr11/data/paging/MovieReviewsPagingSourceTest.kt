package com.halimjr11.data.paging

import androidx.paging.PagingSource.LoadParams
import androidx.paging.PagingSource.LoadResult
import com.halimjr11.network.api.TmdbApiService
import com.halimjr11.network.dto.ReviewDto
import com.halimjr11.network.dto.ReviewResponseDto
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.jupiter.api.Assertions.assertEquals
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

class MovieReviewsPagingSourceTest {
    private val api = mockk<TmdbApiService>()
    private val source = MovieReviewsPagingSource(api, movieId = 9)
    private fun refresh(key: Int?) = LoadParams.Refresh(key, 20, false)

    @Test fun firstPageMapsItemsAndDropsOnesWithoutId() = runTest {
        coEvery { api.getMovieReviews(9, 1) } returns ReviewResponseDto(
            results = listOf(ReviewDto(id = "a"), ReviewDto(id = null)), totalPages = 3
        )
        val page = source.load(refresh(null)) as LoadResult.Page
        assertEquals(listOf("a"), page.data.map { it.id })
    }

    // page, totalPages, items on the page -> prevKey, nextKey. NULL = no key
    @ParameterizedTest(name = "loading page {0} of {1} ({2} items) -> prevKey={3}, nextKey={4}")
    @CsvSource(
        value = [
            "1, 3, 2, NULL, 2",
            "3, 3, 1, 2, NULL",
            "2, 3, 20, 1, 3",
            "2, 9, 0, 1, NULL", // empty page ends paging
            "1, 0, 0, NULL, NULL", // no reviews
        ],
        nullValues = ["NULL"]
    )
    fun pageKeys(page: Int, totalPages: Int, items: Int, prev: Int?, next: Int?) = runTest {
        coEvery { api.getMovieReviews(9, page) } returns ReviewResponseDto(
            results = List(items) { ReviewDto(id = "r$it") }, totalPages = totalPages
        )
        val result = source.load(refresh(page)) as LoadResult.Page
        assertEquals(prev, result.prevKey)
        assertEquals(next, result.nextKey)
    }

    @ParameterizedTest(name = "{argumentSetName}")
    @MethodSource("failures")
    fun failuresBecomeLoadError(failure: Throwable) = runTest {
        coEvery { api.getMovieReviews(9, 2) } throws failure
        assertTrue(source.load(refresh(2)) is LoadResult.Error)
    }

    @Test fun cancellationIsRethrownNotWrapped() = runTest {
        coEvery { api.getMovieReviews(9, 1) } throws CancellationException("cancelled")
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
