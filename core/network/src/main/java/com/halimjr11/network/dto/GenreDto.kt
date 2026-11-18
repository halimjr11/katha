package com.halimjr11.network.dto

import com.google.gson.annotations.SerializedName

data class GenreResponseDto(
    @SerializedName("genres")
    val genres: List<GenreDto>? = null
)

data class GenreDto(
    @SerializedName("id")
    val id: Int? = null,
    @SerializedName("name")
    val name: String? = null
)
