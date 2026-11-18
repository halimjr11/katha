package com.halimjr11.common

import java.util.Locale

object KathaFormatters {

    private val months = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

    // Hand-parsed instead of SimpleDateFormat (not thread-safe) or java.time (needs API 26, minSdk is 24).
    // Stateless, so safe to call from any thread.
    /** "2024-05-15" -> "15 May 2024"; null when not a real calendar date. */
    private fun reformatDate(date: String): String? {
        val parts = date.split('-')
        if (parts.size != 3 || parts[0].length != 4 || parts[1].length != 2 || parts[2].length != 2) return null
        if (!parts.all { p -> p.all { it in '0'..'9' } }) return null
        val (year, month, day) = parts.map { it.toInt() }
        if (month !in 1..12 || day !in 1..daysInMonth(year, month)) return null
        return "${parts[2]} ${months[month - 1]} ${parts[0]}"
    }

    private fun daysInMonth(year: Int, month: Int): Int = when (month) {
        2 -> if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
        4, 6, 9, 11 -> 30
        else -> 31
    }

    /**
     * Converts "2024-05-15" -> "15 May 2024"
     */
    fun formatReleaseDate(dateStr: String?): String {
        if (dateStr.isNullOrBlank()) return "Unknown Release Date"
        return reformatDate(dateStr) ?: dateStr
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
        // keep the date as written (no timezone shift): only the first 10 chars matter
        val date = isoDate.take(10)
        return reformatDate(date) ?: date
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