package com.halimjr11.movies

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

// genreId is supplied at call site via parametersOf
val movieListModule = module {
    viewModelOf(::MovieListViewModel)
}
