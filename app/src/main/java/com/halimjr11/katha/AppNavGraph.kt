package com.halimjr11.katha

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.halimjr11.details.MovieDetailScreen
import com.halimjr11.genres.GenreListScreen
import com.halimjr11.katha.data.GenresKey
import com.halimjr11.katha.data.MovieDetailKey
import com.halimjr11.katha.data.MovieListKey
import com.halimjr11.movies.MovieListScreen
import kotlinx.serialization.Serializable

@Composable
fun AppNavGraph() {
    val backStack = rememberNavBackStack(GenresKey)
    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<GenresKey> {
                GenreListScreen(onGenreClick = { id, name -> backStack.add(MovieListKey(id, name)) })
            }
            entry<MovieListKey> { key ->
                MovieListScreen(
                    genreId = key.genreId,
                    genreName = key.genreName,
                    onBack = { backStack.removeLastOrNull() },
                    onMovieClick = { backStack.add(MovieDetailKey(it)) }
                )
            }
            entry<MovieDetailKey> { key ->
                MovieDetailScreen(key.movieId, onBack = { backStack.removeLastOrNull() })
            }
        }
    )
}
