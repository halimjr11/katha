package com.halimjr11.network.dto

import com.google.gson.annotations.SerializedName

data class VideoResponseDto(
    @SerializedName("id")
    val id: Int? = null,
    @SerializedName("results")
    val results: List<VideoDto>? = null
)

data class VideoDto(
    @SerializedName("id")
    val id: String? = null,
    @SerializedName("key")
    val key: String? = null,
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("site")
    val site: String? = null,
    @SerializedName("size")
    val size: Int? = null,
    @SerializedName("type")
    val type: String? = null,
    @SerializedName("official")
    val official: Boolean? = null
)
