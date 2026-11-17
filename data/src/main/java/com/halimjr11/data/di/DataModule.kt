package com.halimjr11.data.di

import com.halimjr11.common.DefaultDispatcherProvider
import com.halimjr11.common.DispatcherProvider
import com.halimjr11.data.repository.MovieRepositoryImpl
import com.halimjr11.domain.repository.MovieRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

// TmdbApiService comes from networkModule (core:network)
val dataModule = module {
    singleOf(::DefaultDispatcherProvider) bind DispatcherProvider::class
    singleOf(::MovieRepositoryImpl) bind MovieRepository::class
}
