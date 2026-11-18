package com.halimjr11.katha.data

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** All reviews of one movie (opened from the detail screen). */
@Serializable
data class MovieReviewsKey(val movieId: Int) : NavKey
