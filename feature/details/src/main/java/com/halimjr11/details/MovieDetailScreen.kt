package com.halimjr11.details

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.halimjr11.domain.model.MovieDetail
import com.halimjr11.domain.usecase.GetMovieReviewHighlightsUseCase
import com.halimjr11.domain.model.VideoTrailer
import com.halimjr11.ui.components.FullScreenError
import com.halimjr11.ui.components.FullScreenLoading
import com.halimjr11.ui.components.RemoteImage
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDetailScreen(
    movieId: Int,
    onSeeAllReviews: () -> Unit,
    onBack: () -> Unit,
    viewModel: MovieDetailViewModel = koinViewModel(key = "detail_$movieId", parameters = { parametersOf(movieId) })
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.detail?.title ?: "Movie Detail", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            val detail = state.detail
            when {
                state.isLoading -> FullScreenLoading()
                detail == null -> FullScreenError(
                    message = state.error ?: MovieDetailViewModel.ERROR_MESSAGE,
                    onRetry = viewModel::load
                )
                else -> LazyColumn(Modifier.fillMaxSize()) {
                    item { Header(detail) }
                    item { Info(detail, state.trailer) }
                    reviewHighlights(state, onRetry = viewModel::retryReviews, onSeeAll = onSeeAllReviews)
                }
            }
        }
    }
}

@Composable
private fun Header(detail: MovieDetail) {
    Box(Modifier.fillMaxWidth().height(300.dp)) {
        RemoteImage(detail.backdropUrl, null, Modifier.fillMaxSize())
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(listOf(Color.Transparent, MaterialTheme.colorScheme.background))
            )
        )
        Row(Modifier.align(Alignment.BottomStart).padding(16.dp), verticalAlignment = Alignment.Bottom) {
            RemoteImage(
                detail.posterUrl, detail.title,
                Modifier.width(110.dp).aspectRatio(2f / 3f).clip(RoundedCornerShape(8.dp))
            )
            Column(Modifier.padding(start = 16.dp)) {
                Text(detail.title, style = MaterialTheme.typography.headlineSmall)
                if (detail.tagline.isNotBlank()) {
                    Text(detail.tagline, style = MaterialTheme.typography.bodyMedium, fontStyle = FontStyle.Italic)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Info(detail: MovieDetail, trailer: VideoTrailer?) {
    val context = LocalContext.current
    Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        val rating = if (detail.rating == "NR") "NR" else "★ ${detail.rating} / 10"
        Text(
            listOf(detail.releaseDate, detail.runtime, rating, detail.status).filter { it.isNotBlank() }.joinToString("  •  "),
            style = MaterialTheme.typography.bodyMedium
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            detail.genres.forEach { AssistChip(onClick = {}, label = { Text(it.name) }) }
        }
        if (trailer != null) {
            Button(onClick = { context.openTrailer(trailer) }, Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.PlayArrow, null)
                Text("Watch Trailer")
            }
        } else {
            OutlinedButton(onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth()) { Text("Trailer Unavailable") }
        }
        Text("Overview", style = MaterialTheme.typography.titleMedium)
        Text(detail.overview, style = MaterialTheme.typography.bodyLarge)
        Text("User Reviews", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
    }
}

/** Up to three highlighted reviews, then a button to the full (endlessly scrolling) list. */
private fun LazyListScope.reviewHighlights(state: MovieDetailUiState, onRetry: () -> Unit, onSeeAll: () -> Unit) {
    when {
        state.reviewsFailed -> item {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Failed to load reviews")
                TextButton(onClick = onRetry) { Text("Retry") }
            }
        }
        state.highlights.isEmpty() -> item {
            Text("No user reviews yet.", Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
        }
        else -> {
            items(state.highlights, key = { it.id }) { ReviewCard(it) }
            if (state.highlights.size >= GetMovieReviewHighlightsUseCase.DEFAULT_LIMIT) {
                item {
                    OutlinedButton(
                        onClick = onSeeAll,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
                    ) { Text("See all reviews") }
                }
            }
        }
    }
}

private fun Context.openTrailer(trailer: VideoTrailer) {
    try {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:${trailer.key}")))
    } catch (_: ActivityNotFoundException) {
        // no YouTube app: open in browser
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(trailer.youtubeUrl)))
    }
}
