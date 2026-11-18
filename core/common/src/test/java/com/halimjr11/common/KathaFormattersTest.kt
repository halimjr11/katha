package com.halimjr11.common

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

// Each test function runs once per @CsvSource row. NULL = null, '' = empty string, ' ' = blank.
class KathaFormattersTest {

    @ParameterizedTest(name = "formatRuntime({0}) = {1}")
    @CsvSource(
        value = ["142, 2h 22m", "128, 2h 8m", "120, 2h", "45, 45m", "1, 1m", "0, N/A", "-5, N/A", "NULL, N/A"],
        nullValues = ["NULL"]
    )
    fun runtime(minutes: Int?, expected: String) =
        assertEquals(expected, KathaFormatters.formatRuntime(minutes))

    @ParameterizedTest(name = "formatReleaseDate({0}) = {1}")
    @CsvSource(
        value = [
            "2024-05-15, 15 May 2024",
            "1999-12-01, 01 Dec 1999",
            "2024-02-29, 29 Feb 2024", // leap year
            "2023-02-29, 2023-02-29", // not a leap year: stays raw
            "2024-02-31, 2024-02-31",
            "2024-13-01, 2024-13-01",
            "2024-5-1, 2024-5-1", // not zero-padded
            "not-a-date, not-a-date",
            "NULL, Unknown Release Date",
            "' ', Unknown Release Date",
        ],
        nullValues = ["NULL"]
    )
    fun releaseDate(input: String?, expected: String) =
        assertEquals(expected, KathaFormatters.formatReleaseDate(input))

    @ParameterizedTest(name = "formatReleaseYear({0}) = {1}")
    @CsvSource(value = ["2024-05-15, 2024", "NULL, N/A", "'', N/A"], nullValues = ["NULL"])
    fun releaseYear(input: String?, expected: String) =
        assertEquals(expected, KathaFormatters.formatReleaseYear(input))

    @ParameterizedTest(name = "formatIsoReviewDate({0}) = {1}")
    @CsvSource(
        value = [
            "2023-07-21T14:22:10.000Z, 21 Jul 2023",
            "2023-07-21T23:59:59.000-05:00, 21 Jul 2023", // offset not applied: the written date wins
            "garbage, garbage",
            "NULL, ''",
        ],
        nullValues = ["NULL"]
    )
    fun isoReviewDate(input: String?, expected: String) =
        assertEquals(expected, KathaFormatters.formatIsoReviewDate(input))

    @ParameterizedTest(name = "formatVoteAverage({0}) = {1}")
    @CsvSource(value = ["7.845, 7.8", "8.0, 8.0", "0.0, NR", "NULL, NR"], nullValues = ["NULL"])
    fun voteAverage(input: Double?, expected: String) =
        assertEquals(expected, KathaFormatters.formatVoteAverage(input))

    @ParameterizedTest(name = "buildPosterUrl({0}) = {1}")
    @CsvSource(
        value = ["/a.jpg, https://image.tmdb.org/t/p/w500/a.jpg", "NULL, NULL", "'', NULL"],
        nullValues = ["NULL"]
    )
    fun posterUrl(path: String?, expected: String?) =
        assertEquals(expected, KathaFormatters.buildPosterUrl(path))

    @ParameterizedTest(name = "buildBackdropUrl({0}) = {1}")
    @CsvSource(
        value = ["/b.jpg, https://image.tmdb.org/t/p/w780/b.jpg", "' ', NULL", "NULL, NULL"],
        nullValues = ["NULL"]
    )
    fun backdropUrl(path: String?, expected: String?) =
        assertEquals(expected, KathaFormatters.buildBackdropUrl(path))

    @ParameterizedTest(name = "buildPosterUrl({0}, {1}) = {2}")
    @CsvSource("/a.jpg, original, https://image.tmdb.org/t/p/original/a.jpg", "/a.jpg, w185, https://image.tmdb.org/t/p/w185/a.jpg")
    fun posterUrlWithCustomSize(path: String, size: String, expected: String) =
        assertEquals(expected, KathaFormatters.buildPosterUrl(path, size))

    @ParameterizedTest(name = "buildAvatarUrl({0}) = {1}")
    @CsvSource(
        value = [
            "/abc.jpg, https://image.tmdb.org/t/p/w185/abc.jpg",
            // TMDB sometimes returns a Gravatar URL prefixed with "/"
            "/https://secure.gravatar.com/avatar/x, https://secure.gravatar.com/avatar/x",
            "https://x.test/a.png, https://x.test/a.png",
            "NULL, NULL",
            "'', NULL",
        ],
        nullValues = ["NULL"]
    )
    fun avatarUrl(path: String?, expected: String?) =
        assertEquals(expected, KathaFormatters.buildAvatarUrl(path))
}
