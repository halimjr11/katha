package com.halimjr11.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReviewResponseDto(
    @SerialName("id") val id: Int? = null,
    @SerialName("page") val page: Int? = null,
    @SerialName("results") val results: List<ReviewDto>? = null,
    @SerialName("total_pages") val totalPages: Int? = null,
    @SerialName("total_results") val totalResults: Int? = null
)

@Serializable
data class ReviewDto(
    @SerialName("id") val id: String? = null,
    @SerialName("author") val author: String? = null,
    @SerialName("author_details") val authorDetails: AuthorDetailsDto? = null,
    @SerialName("content") val content: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class AuthorDetailsDto(
    @SerialName("name") val name: String? = null,
    @SerialName("username") val username: String? = null,
    @SerialName("avatar_path") val avatarPath: String? = null,
    @SerialName("rating") val rating: Double? = null
)
