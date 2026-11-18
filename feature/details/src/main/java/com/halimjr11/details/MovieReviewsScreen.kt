package com.halimjr11.details

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.halimjr11.ui.components.FullScreenError
import com.halimjr11.ui.components.FullScreenLoading
import com.halimjr11.ui.components.PagingFooter
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

/** All reviews of a movie, loaded page by page as the user scrolls. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieReviewsScreen(
    movieId: Int,
    onBack: () -> Unit,
    viewModel: MovieReviewsViewModel = koinViewModel(key = "reviews_$movieId", parameters = { parametersOf(movieId) })
) {
    val reviews = viewModel.reviews.collectAsLazyPagingItems()
    val refresh = reviews.loadState.refresh
    val append = reviews.loadState.append

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reviews") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                refresh is LoadState.Loading && reviews.itemCount == 0 -> FullScreenLoading()
                refresh is LoadState.Error && reviews.itemCount == 0 ->
                    FullScreenError("Couldn't load reviews. Check your connection and try again.", reviews::retry)
                refresh is LoadState.NotLoading && reviews.itemCount == 0 ->
                    Text("No user reviews yet.", Modifier.align(Alignment.Center))
                else -> LazyColumn(Modifier.fillMaxSize()) {
                    items(reviews.itemCount) { index -> reviews[index]?.let { ReviewCard(it) } }
                    item { PagingFooter(append is LoadState.Loading, append is LoadState.Error, reviews::retry) }
                }
            }
        }
    }
}
