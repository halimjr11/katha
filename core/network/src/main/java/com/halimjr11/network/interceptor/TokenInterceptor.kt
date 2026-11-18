package com.halimjr11.network.interceptor

import com.halimjr11.network.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response

class TokenInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val apiKey = BuildConfig.TMDB_BEARER_TOKEN
        val bearer = "Bearer"

        val request = chain.request().newBuilder()
            .addHeader("Accept", "application/json")
            .addHeader("Authorization", listOf(bearer, apiKey).joinToString(" "))
            .build()
        return chain.proceed(request)
    }
}