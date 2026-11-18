package com.halimjr11.details

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

// movieId is supplied at call site via parametersOf
val movieDetailModule = module {
    viewModelOf(::MovieDetailViewModel)
}
