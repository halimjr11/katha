package com.halimjr11.movies

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

// genreId is optional: parametersOf(id) filters, no parameter means all movies
val movieListModule = module {
    viewModel { params -> MovieListViewModel(genreId = params.getOrNull(), discoverMovies = get()) }
}
