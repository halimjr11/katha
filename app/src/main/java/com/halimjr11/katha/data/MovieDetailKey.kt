package com.halimjr11.katha.data

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data class MovieDetailKey(val movieId: Int) : NavKey