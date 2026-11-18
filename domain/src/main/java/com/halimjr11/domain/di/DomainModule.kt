package com.halimjr11.domain.di

import com.halimjr11.domain.usecase.DiscoverMoviesUseCase
import com.halimjr11.domain.usecase.GetGenresUseCase
import com.halimjr11.domain.usecase.GetMovieDetailUseCase
import com.halimjr11.domain.usecase.GetMovieReviewsUseCase
import com.halimjr11.domain.usecase.GetMovieTrailerUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

// koin-core is pure Kotlin/JVM (exposed via :core:common), so :domain stays free of Android.
val domainModule = module {
    factoryOf(::GetGenresUseCase)
    factoryOf(::DiscoverMoviesUseCase)
    factoryOf(::GetMovieDetailUseCase)
    factoryOf(::GetMovieTrailerUseCase)
    factoryOf(::GetMovieReviewsUseCase)
}
