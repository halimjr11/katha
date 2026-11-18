package com.halimjr11.common

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

class NetworkResultTest {
    @Test fun mapTransformsSuccess() {
        assertEquals(NetworkResult.Success(4), NetworkResult.Success(2).map { it * 2 })
    }

    @Test fun mapKeepsErrorUntouched() {
        val error = NetworkResult.Error("boom", 500)
        assertSame(error, (error as NetworkResult<Int>).map { it * 2 })
    }
}
