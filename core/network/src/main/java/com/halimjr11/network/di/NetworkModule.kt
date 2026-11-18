package com.halimjr11.network.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.halimjr11.network.BuildConfig
import com.halimjr11.network.api.TmdbApiService
import com.halimjr11.network.interceptor.TokenInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val networkModule = module {
    // Gson ignores unknown JSON keys by default; DTO fields are nullable with null defaults
    single<Gson> { GsonBuilder().create() }
    single { TokenInterceptor() }
    single {
        OkHttpClient.Builder()
            .addInterceptor(get<TokenInterceptor>())
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC
                    else HttpLoggingInterceptor.Level.NONE
                }
            )
            .build()
    }
    single {
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(get())
            .addConverterFactory(GsonConverterFactory.create(get<Gson>()))
            .build()
    }
    single { get<Retrofit>().create(TmdbApiService::class.java) }
}
