package com.halimjr11.data.mapper

import com.halimjr11.network.dto.VideoDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class VideoMapperTest {
    private fun v(key: String, type: String, official: Boolean = false, site: String = "YouTube") =
        VideoDto(key = key, type = type, official = official, site = site)

    @Test
    fun officialTrailerBeatsTrailerBeatsTeaser() {
        val list = listOf(v("teaser", "Teaser"), v("plain", "Trailer"), v("off", "Trailer", true), v("vim", "Trailer", true, "Vimeo"))
        assertEquals("off", list.toTrailer()?.key)
        assertEquals("plain", list.filter { it.official != true }.toTrailer()?.key)
        assertEquals("teaser", listOf(v("teaser", "Teaser")).toTrailer()?.key)
    }

    @Test
    fun noYoutubeReturnsNull() {
        assertNull(listOf(v("x", "Trailer", site = "Vimeo"), v("", "Trailer")).toTrailer())
    }
}
