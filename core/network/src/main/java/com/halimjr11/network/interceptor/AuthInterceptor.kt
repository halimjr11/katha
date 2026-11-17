package com.halimjr11.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val bearerToken: String
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("Accept", "application/json")
            .addHeader("Authorization", "Bearer $bearerToken")
            .build()
        return chain.proceed(request)
    }
}