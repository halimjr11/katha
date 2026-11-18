package com.halimjr11.katha

import com.halimjr11.katha.di.appModules
import org.junit.jupiter.api.Test
import org.koin.dsl.module
import org.koin.test.verify.verify

class KoinModulesTest {
    @OptIn(org.koin.core.annotation.KoinExperimentalAPI::class)
    @Test
    fun allModulesResolve() {
        // Int = runtime movieId/genreId from parametersOf; String = TMDB token passed to AuthInterceptor
        module { includes(appModules) }.verify(extraTypes = listOf(Int::class, String::class))
    }
}
