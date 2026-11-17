package com.halimjr11.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VideoResponseDto(
    @SerialName("id") val id: Int? = null,
    @SerialName("results") val results: List<VideoDto>? = null
)

@Serializable
data class VideoDto(
    @SerialName("id") val id: String? = null,
    @SerialName("key") val key: String? = null,
    @SerialName("name") val name: String? = null,
    @SerialName("site") val site: String? = null,
    @SerialName("size") val size: Int? = null,
    @SerialName("type") val type: String? = null,
    @SerialName("official") val official: Boolean? = null
)
