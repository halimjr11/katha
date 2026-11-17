package com.halimjr11.data.mapper

import com.halimjr11.domain.model.Genre
import com.halimjr11.network.dto.GenreDto

fun GenreDto.toDomain(): Genre? {
    val id = id ?: return null
    val name = name?.takeIf { it.isNotBlank() } ?: return null
    return Genre(id, name)
}

fun List<GenreDto>?.toDomain(): List<Genre> = orEmpty().mapNotNull { it.toDomain() }
