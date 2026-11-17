package com.halimjr11.common

import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object KathaFormatters {

    private val inputDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US)
    private val outputDateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.US)

    /**
     * Converts "2024-05-15" -> "15 May 2024"
     */
    fun formatReleaseDate(dateStr: String?): String {
        if (dateStr.isNullOrBlank()) return "Unknown Release Date"
        return runCatching {
            LocalDate.parse(dateStr, inputDateFormatter).format(outputDateFormatter)
        }.getOrDefault(dateStr)
    }

    /**
     * Extracts Year "2024-05-15" -> "2024"
     */
    fun formatReleaseYear(dateStr: String?): String {
        if (dateStr.isNullOrBlank()) return "N/A"
        return dateStr.take(4)
    }

    /**
     * Converts minutes (142) -> "2h 22m"
     */
    fun formatRuntime(minutes: Int?): String {
        if (minutes == null || minutes <= 0) return "N/A"
        val hours = minutes / 60
        val remainingMinutes = minutes % 60
        return when {
            hours > 0 && remainingMinutes > 0 -> "${hours}h ${remainingMinutes}m"
            hours > 0 -> "${hours}h"
            else -> "${remainingMinutes}m"
        }
    }

    /**
     * Formats ISO-8601 review date "2023-07-21T14:22:10.000Z" -> "21 Jul 2023"
     */
    fun formatIsoReviewDate(isoDate: String?): String {
        if (isoDate.isNullOrBlank()) return ""
        return runCatching {
            OffsetDateTime.parse(isoDate).format(outputDateFormatter)
        }.getOrDefault(isoDate.take(10))
    }

    /**
     * Formats rating to 1 decimal place (e.g. 7.845 -> "7.8")
     */
    fun formatVoteAverage(voteAverage: Double?): String {
        if (voteAverage == null || voteAverage <= 0.0) return "NR"
        return String.format(Locale.US, "%.1f", voteAverage)
    }

    fun buildPosterUrl(posterPath: String?, size: String = "w500"): String? {
        if (posterPath.isNullOrBlank()) return null
        return "https://image.tmdb.org/t/p/$size$posterPath"
    }

    fun buildBackdropUrl(backdropPath: String?, size: String = "w780"): String? {
        if (backdropPath.isNullOrBlank()) return null
        return "https://image.tmdb.org/t/p/$size$backdropPath"
    }

    fun buildAvatarUrl(avatarPath: String?): String? {
        if (avatarPath.isNullOrBlank()) return null
        return when {
            avatarPath.startsWith("/http") -> avatarPath.removePrefix("/")
            avatarPath.startsWith("http") -> avatarPath
            else -> "https://image.tmdb.org/t/p/w185$avatarPath"
        }
    }
}