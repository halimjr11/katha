package com.halimjr11.genres

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val genresModule = module {
    viewModelOf(::GenreListViewModel)
}
