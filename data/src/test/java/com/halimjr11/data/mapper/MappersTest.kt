package com.halimjr11.data.mapper

import com.halimjr11.network.dto.AuthorDetailsDto
import com.halimjr11.network.dto.GenreDto
import com.halimjr11.network.dto.MovieDetailDto
import com.halimjr11.network.dto.MovieDto
import com.halimjr11.network.dto.ReviewDto
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class MappersTest {

    // --- Genre ---
    @Test fun genresDropNullIdAndBlankName() {
        val list = listOf(GenreDto(1, "Action"), GenreDto(null, "NoId"), GenreDto(2, " "), GenreDto(3, null))
        assertEquals(listOf(1), list.toDomain().map { it.id })
        assertEquals(emptyList<Any>(), (null as List<GenreDto>?).toDomain())
    }

    // --- Movie ---
    @Test fun movieMapsFormattedFields() {
        val movie = MovieDto(
            id = 5, title = "Deadpool", posterPath = "/p.jpg",
            releaseDate = "2024-07-24", voteAverage = 7.712
        ).toDomain()!!
        assertEquals("https://image.tmdb.org/t/p/w500/p.jpg", movie.posterUrl)
        assertEquals("2024", movie.releaseYear)
        assertEquals("7.7", movie.rating)
    }

    @Test fun movieWithoutIdIsDropped() = assertNull(MovieDto(title = "x").toDomain())

    @Test fun movieWithMissingFieldsGetsSafeDefaults() {
        val movie = MovieDto(id = 1).toDomain()!!
        assertEquals("", movie.title)
        assertEquals("", movie.posterUrl)
        assertEquals("N/A", movie.releaseYear)
        assertEquals("NR", movie.rating)
    }

    // --- Movie detail ---
    @Test fun detailMapsAllFields() {
        val detail = MovieDetailDto(
            id = 1, title = "T", tagline = "Tag", overview = "Story",
            posterPath = "/p.jpg", backdropPath = "/b.jpg", releaseDate = "2024-07-24",
            runtime = 128, voteAverage = 7.712, voteCount = 5420, status = "Released",
            genres = listOf(GenreDto(28, "Action"), GenreDto(35, "Comedy"))
        ).toDomain()!!
        assertEquals("24 Jul 2024", detail.releaseDate)
        assertEquals("2h 8m", detail.runtime)
        assertEquals("7.7", detail.rating)
        assertEquals("https://image.tmdb.org/t/p/w780/b.jpg", detail.backdropUrl)
        assertEquals(5420, detail.voteCount)
        assertEquals(listOf("Action", "Comedy"), detail.genres.map { it.name })
    }

    @Test fun detailFallbacksForMissingData() {
        val detail = MovieDetailDto(id = 1, overview = "  ", runtime = 0).toDomain()!!
        assertEquals("No synopsis available.", detail.overview)
        assertEquals("N/A", detail.runtime)
        assertEquals("", detail.tagline)
        assertEquals("", detail.backdropUrl)
        assertEquals(0, detail.voteCount)
        assertEquals(emptyList<Any>(), detail.genres)
    }

    @Test fun detailWithoutIdIsNull() = assertNull(MovieDetailDto().toDomain())

    // --- Review ---
    @Test fun reviewMapsAuthorAvatarRatingDate() {
        val review = ReviewDto(
            id = "r1", author = "JohnDoe", content = "Great",
            createdAt = "2024-07-25T10:15:30.123Z",
            authorDetails = AuthorDetailsDto(avatarPath = "/a.jpg", rating = 8.0)
        ).toDomain()!!
        assertEquals("JohnDoe", review.author)
        assertEquals("https://image.tmdb.org/t/p/w185/a.jpg", review.avatarUrl)
        assertEquals("8.0", review.rating)
        assertEquals("25 Jul 2024", review.date)
    }

    @Test fun reviewGravatarPathLosesLeadingSlash() {
        val review = ReviewDto(id = "r", authorDetails = AuthorDetailsDto(avatarPath = "/https://secure.gravatar.com/x")).toDomain()!!
        assertEquals("https://secure.gravatar.com/x", review.avatarUrl)
    }

    @Test fun reviewWithoutRatingHasBlankRating() {
        val review = ReviewDto(id = "r", authorDetails = AuthorDetailsDto(rating = null)).toDomain()!!
        assertEquals("", review.rating)
        assertEquals("", review.avatarUrl)
    }

    // author, details.name, details.username -> expected display name. NULL = null, ' ' = blank
    @ParameterizedTest(name = "[{index}] author=\"{0}\", details.name=\"{1}\", details.username=\"{2}\" -> shows \"{3}\"")
    @CsvSource(
        value = ["A, N, U, A", "NULL, N, U, N", "' ', ' ', U, U", "NULL, NULL, NULL, Anonymous"],
        nullValues = ["NULL"]
    )
    fun reviewAuthorFallbackOrder(author: String?, name: String?, username: String?, expected: String) {
        val review = ReviewDto(id = "r", author = author, authorDetails = AuthorDetailsDto(name = name, username = username)).toDomain()!!
        assertEquals(expected, review.author)
    }

    @Test fun reviewWithoutIdIsDropped() = assertNull(ReviewDto(author = "A").toDomain())
}
