package com.halimjr11.network.dto

import com.google.gson.annotations.SerializedName

data class ReviewResponseDto(
    @SerializedName("id")
    val id: Int? = null,
    @SerializedName("page")
    val page: Int? = null,
    @SerializedName("results")
    val results: List<ReviewDto>? = null,
    @SerializedName("total_pages")
    val totalPages: Int? = null,
    @SerializedName("total_results")
    val totalResults: Int? = null
)

data class ReviewDto(
    @SerializedName("id")
    val id: String? = null,
    @SerializedName("author")
    val author: String? = null,
    @SerializedName("author_details")
    val authorDetails: AuthorDetailsDto? = null,
    @SerializedName("content")
    val content: String? = null,
    @SerializedName("created_at")
    val createdAt: String? = null
)

data class AuthorDetailsDto(
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("username")
    val username: String? = null,
    @SerializedName("avatar_path")
    val avatarPath: String? = null,
    @SerializedName("rating")
    val rating: Double? = null
)
