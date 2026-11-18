package com.halimjr11.katha.di

import com.halimjr11.data.di.dataModule
import com.halimjr11.details.movieDetailModule
import com.halimjr11.domain.di.domainModule
import com.halimjr11.genres.genresModule
import com.halimjr11.movies.movieListModule
import com.halimjr11.network.di.networkModule

val appModules = listOf(
    networkModule, dataModule, domainModule,
    genresModule, movieListModule, movieDetailModule
)
