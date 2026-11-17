package com.halimjr11.data.mapper

import com.halimjr11.common.KathaFormatters
import com.halimjr11.domain.model.Review
import com.halimjr11.network.dto.ReviewDto

fun ReviewDto.toDomain(): Review? {
    val id = id ?: return null
    val details = authorDetails
    return Review(
        id = id,
        author = author?.takeIf { it.isNotBlank() }
            ?: details?.name?.takeIf { it.isNotBlank() }
            ?: details?.username?.takeIf { it.isNotBlank() }
            ?: "Anonymous",
        avatarUrl = KathaFormatters.buildAvatarUrl(details?.avatarPath).orEmpty(),
        rating = details?.rating?.let { KathaFormatters.formatVoteAverage(it) }.orEmpty(),
        date = KathaFormatters.formatIsoReviewDate(createdAt),
        content = content.orEmpty()
    )
}
