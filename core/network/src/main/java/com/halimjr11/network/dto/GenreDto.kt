package com.halimjr11.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GenreResponseDto(
    @SerialName("genres") val genres: List<GenreDto>? = null
)

@Serializable
data class GenreDto(
    @SerialName("id") val id: Int? = null,
    @SerialName("name") val name: String? = null
)
