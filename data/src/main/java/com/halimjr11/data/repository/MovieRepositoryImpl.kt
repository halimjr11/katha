package com.halimjr11.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.halimjr11.common.DispatcherProvider
import com.halimjr11.common.NetworkResult
import com.halimjr11.data.mapper.toDomain
import com.halimjr11.data.mapper.toTrailer
import com.halimjr11.data.paging.DiscoverMoviesPagingSource
import com.halimjr11.data.paging.MovieReviewsPagingSource
import com.halimjr11.domain.model.Genre
import com.halimjr11.domain.model.Movie
import com.halimjr11.domain.model.MovieDetail
import com.halimjr11.domain.model.Review
import com.halimjr11.domain.model.VideoTrailer
import com.halimjr11.domain.repository.MovieRepository
import com.halimjr11.network.api.TmdbApiService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class MovieRepositoryImpl(
    private val api: TmdbApiService,
    private val dispatcherProvider: DispatcherProvider
) : MovieRepository {

    private val pagingConfig = PagingConfig(pageSize = 20, enablePlaceholders = false)

    override suspend fun getGenres(): NetworkResult<List<Genre>> =
        safeCall { api.getGenres().genres.toDomain() }

    override fun discoverMoviesByGenre(genreId: Int): Flow<PagingData<Movie>> =
        Pager(pagingConfig) { DiscoverMoviesPagingSource(api, genreId) }.flow

    override suspend fun getMovieDetail(movieId: Int): NetworkResult<MovieDetail> =
        safeCall { requireNotNull(api.getMovieDetail(movieId).toDomain()) { "Invalid movie detail" } }

    override suspend fun getMovieTrailer(movieId: Int): NetworkResult<VideoTrailer?> =
        safeCall { api.getMovieVideos(movieId).results.orEmpty().toTrailer() }

    override fun getMovieReviews(movieId: Int): Flow<PagingData<Review>> =
        Pager(pagingConfig) { MovieReviewsPagingSource(api, movieId) }.flow

    private suspend fun <T> safeCall(block: suspend () -> T): NetworkResult<T> =
        withContext(dispatcherProvider.io) {
            try {
                NetworkResult.Success(block())
            } catch (e: CancellationException) {
                throw e
            } catch (e: HttpException) {
                NetworkResult.Error(e.message(), e.code(), e)
            } catch (e: IOException) {
                NetworkResult.Error(e.message ?: "Network error", null, e)
            } catch (e: Exception) {
                NetworkResult.Error(e.message ?: "Invalid response", null, e)
            }
        }
}
