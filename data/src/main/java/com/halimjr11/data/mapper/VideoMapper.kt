package com.halimjr11.data.mapper

import com.halimjr11.domain.model.VideoTrailer
import com.halimjr11.network.dto.VideoDto

/**
 * Official Trailer > Trailer > Teaser, YouTube only. Null when none.
 **/
fun List<VideoDto>.toTrailer(): VideoTrailer? {
    val youtube = filter { it.site.equals("YouTube", ignoreCase = true) && !it.key.isNullOrBlank() }
    val pick = youtube.firstOrNull { it.type == "Trailer" && it.official == true }
        ?: youtube.firstOrNull { it.type == "Trailer" }
        ?: youtube.firstOrNull { it.type == "Teaser" }
        ?: return null
    val key = pick.key!!
    return VideoTrailer(
        key = key,
        name = pick.name.orEmpty(),
        youtubeUrl = "https://www.youtube.com/watch?v=$key"
    )
}
