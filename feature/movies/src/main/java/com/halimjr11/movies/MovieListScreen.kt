package com.halimjr11.movies

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.halimjr11.domain.model.Movie
import com.halimjr11.ui.components.FullScreenError
import com.halimjr11.ui.components.FullScreenLoading
import com.halimjr11.ui.components.PagingFooter
import com.halimjr11.ui.components.RemoteImage
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

/** Home: all movies. The search icon opens the genre search. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoviesHomeScreen(
    onSearchGenres: () -> Unit,
    onMovieClick: (movieId: Int) -> Unit,
    viewModel: MovieListViewModel = koinViewModel(key = "movies_all", parameters = { parametersOf() })
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Katha") },
                actions = {
                    IconButton(onClick = onSearchGenres) {
                        Icon(Icons.Filled.Search, contentDescription = "Search genres")
                    }
                }
            )
        }
    ) { padding ->
        MovieGrid(viewModel, onMovieClick, Modifier.padding(padding))
    }
}

/** Movies of one genre, reached from the genre search. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenreMoviesScreen(
    genreId: Int,
    genreName: String,
    onBack: () -> Unit,
    onMovieClick: (movieId: Int) -> Unit,
    // key: one ViewModel per genre, even if the back stack holds several
    viewModel: MovieListViewModel = koinViewModel(key = "movies_genre_$genreId", parameters = { parametersOf(genreId) })
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$genreName Movies") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        MovieGrid(viewModel, onMovieClick, Modifier.padding(padding))
    }
}

/** Shared by both screens: paged grid with loading, error, empty and append-error states. */
@Composable
private fun MovieGrid(
    viewModel: MovieListViewModel,
    onMovieClick: (movieId: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val movies = viewModel.movies.collectAsLazyPagingItems()
    val refresh = movies.loadState.refresh
    val append = movies.loadState.append

    Box(modifier.fillMaxSize()) {
        when {
            refresh is LoadState.Loading && movies.itemCount == 0 -> FullScreenLoading()
            refresh is LoadState.Error && movies.itemCount == 0 ->
                FullScreenError("Couldn't load movies. Check your connection and try again.", movies::retry)
            refresh is LoadState.NotLoading && movies.itemCount == 0 ->
                Text("No movies found.", Modifier.align(Alignment.Center))
            else -> LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // No item key: TMDB discover can repeat ids across pages, and a duplicate key crashes Lazy layouts.
                items(movies.itemCount) { index ->
                    movies[index]?.let { MovieCard(it, onClick = { onMovieClick(it.id) }) }
                }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    PagingFooter(
                        isLoading = append is LoadState.Loading,
                        isError = append is LoadState.Error,
                        onRetry = movies::retry
                    )
                }
            }
        }
    }
}

@Composable
private fun MovieCard(movie: Movie, onClick: () -> Unit) {
    ElevatedCard(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        RemoteImage(
            url = movie.posterUrl,
            contentDescription = movie.title,
            modifier = Modifier.fillMaxWidth().aspectRatio(2f / 3f),
            contentScale = ContentScale.Crop
        )
        Column(Modifier.padding(10.dp)) {
            Text(
                movie.title,
                minLines = 2, // reserve 2 lines so cards in a row keep the same height
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                "${movie.releaseYear}  ${if (movie.rating == "NR") "NR" else "★ ${movie.rating}"}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
