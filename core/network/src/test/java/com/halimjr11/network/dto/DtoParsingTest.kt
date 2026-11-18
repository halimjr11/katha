package com.halimjr11.network.dto

import com.google.gson.Gson
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

// Same Gson setup as networkModule
class DtoParsingTest {
    private val gson = Gson()
    private inline fun <reified T> parse(json: String): T = gson.fromJson(json, T::class.java)

    @Test fun emptyObjectsParseToNulls() {
        assertNull(parse<GenreResponseDto>("{}").genres)
        val discover = parse<DiscoverMoviesResponseDto>("{}")
        assertNull(discover.results)
        assertNull(discover.totalPages)
        assertNull(parse<VideoResponseDto>("{}").results)
        assertNull(parse<ReviewResponseDto>("{}").results)
        assertNull(parse<MovieDetailDto>("{}").id)
    }

    @Test fun movieDetailMapsSnakeCaseAndIgnoresUnknown() {
        val dto = parse<MovieDetailDto>(
            """{"id":1,"poster_path":"/p.jpg","backdrop_path":null,"release_date":"2024-07-24",
               "vote_average":7.7,"vote_count":10,"runtime":128,"extra":"x","genres":[{"id":28,"name":"Action"}]}"""
        )
        assertEquals("/p.jpg", dto.posterPath)
        assertNull(dto.backdropPath)
        assertEquals("2024-07-24", dto.releaseDate)
        assertEquals(128, dto.runtime)
        assertEquals("Action", dto.genres?.single()?.name)
    }

    @Test fun reviewParsesAuthorDetailsWithNullRating() {
        val dto = parse<ReviewResponseDto>(
            """{"id":1,"page":1,"total_pages":3,"results":[{"id":"r1","author":"A",
               "author_details":{"avatar_path":"/a.jpg","rating":null},"created_at":"2024-07-25T10:15:30.123Z"}]}"""
        )
        val review = dto.results!!.single()
        assertEquals("/a.jpg", review.authorDetails?.avatarPath)
        assertNull(review.authorDetails?.rating)
        assertEquals(3, dto.totalPages)
    }

    @Test fun videoParsesOfficialFlag() {
        val dto = parse<VideoResponseDto>(
            """{"results":[{"key":"k","site":"YouTube","type":"Trailer","official":true}]}"""
        )
        assertEquals(true, dto.results!!.single().official)
    }
}
