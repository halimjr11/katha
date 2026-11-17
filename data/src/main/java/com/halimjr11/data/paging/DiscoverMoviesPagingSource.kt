package com.halimjr11.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.halimjr11.data.mapper.toDomain
import com.halimjr11.domain.model.Movie
import com.halimjr11.network.api.TmdbApiService
import retrofit2.HttpException
import java.io.IOException

private const val TMDB_MAX_PAGE = 500

class DiscoverMoviesPagingSource(
    private val api: TmdbApiService,
    private val genreId: Int
) : PagingSource<Int, Movie>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Movie> {
        val page = params.key ?: 1
        return try {
            val response = api.discoverMovies(genreId, page)
            val results = response.results.orEmpty()
            LoadResult.Page(
                data = results.mapNotNull { it.toDomain() },
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (page >= minOf(response.totalPages ?: 0, TMDB_MAX_PAGE) || results.isEmpty()) null else page + 1
            )
        } catch (e: IOException) {
            LoadResult.Error(e)
        } catch (e: HttpException) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Movie>): Int? =
        state.anchorPosition?.let { pos ->
            state.closestPageToPosition(pos)?.let { it.prevKey?.plus(1) ?: it.nextKey?.minus(1) }
        }
}
