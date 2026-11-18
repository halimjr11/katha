package com.halimjr11.network.interceptor

import com.halimjr11.network.BuildConfig
import io.mockk.every
import io.mockk.mockk
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TokenInterceptorTest {
    @Test fun addsBearerAndAcceptHeaders() {
        val sent = io.mockk.slot<Request>()
        val chain = mockk<Interceptor.Chain>()
        every { chain.request() } returns Request.Builder().url("https://api.test/").build()
        every { chain.proceed(capture(sent)) } returns mockk<Response>()

        TokenInterceptor().intercept(chain)

        // the token now comes from BuildConfig (local.properties), not a constructor argument
        assertEquals("Bearer ${BuildConfig.TMDB_BEARER_TOKEN}", sent.captured.header("Authorization"))
        assertEquals("application/json", sent.captured.header("Accept"))
    }
}
