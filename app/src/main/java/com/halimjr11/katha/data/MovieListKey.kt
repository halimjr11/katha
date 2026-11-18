package com.halimjr11.katha.data

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data class MovieListKey(val genreId: Int, val genreName: String) : NavKey