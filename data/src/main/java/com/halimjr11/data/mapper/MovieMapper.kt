package com.halimjr11.data.mapper

import com.halimjr11.common.KathaFormatters
import com.halimjr11.domain.model.Movie
import com.halimjr11.domain.model.MovieDetail
import com.halimjr11.network.dto.MovieDetailDto
import com.halimjr11.network.dto.MovieDto

fun MovieDto.toDomain(): Movie? {
    val id = id ?: return null
    return Movie(
        id = id,
        title = title.orEmpty(),
        posterUrl = KathaFormatters.buildPosterUrl(posterPath).orEmpty(),
        releaseYear = KathaFormatters.formatReleaseYear(releaseDate),
        rating = KathaFormatters.formatVoteAverage(voteAverage)
    )
}

fun MovieDetailDto.toDomain(): MovieDetail? {
    val id = id ?: return null
    return MovieDetail(
        id = id,
        title = title.orEmpty(),
        tagline = tagline.orEmpty(),
        overview = overview?.takeIf { it.isNotBlank() } ?: "No synopsis available.",
        posterUrl = KathaFormatters.buildPosterUrl(posterPath).orEmpty(),
        backdropUrl = KathaFormatters.buildBackdropUrl(backdropPath).orEmpty(),
        releaseDate = KathaFormatters.formatReleaseDate(releaseDate),
        runtime = KathaFormatters.formatRuntime(runtime),
        rating = KathaFormatters.formatVoteAverage(voteAverage),
        voteCount = voteCount ?: 0,
        status = status.orEmpty(),
        genres = genres.toDomain()
    )
}
