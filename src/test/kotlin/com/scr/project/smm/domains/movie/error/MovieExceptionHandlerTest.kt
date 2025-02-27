package com.scr.project.smm.domains.movie.error

import com.mongodb.ErrorCategory.DUPLICATE_KEY
import com.scr.project.smm.domains.movie.error.MovieExceptionHandler.ErrorResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.dao.DuplicateKeyException
import org.springframework.http.HttpStatus.CONFLICT
import org.springframework.http.ResponseEntity

class MovieExceptionHandlerTest {

    private val handler = MovieExceptionHandler()

    @Test
    fun `handle DuplicateKeyException returns correct response`() {
        val response: ResponseEntity<ErrorResponse> = handler.handleDuplicateKeyException(DuplicateKeyException("message"))
        assertThat(response.statusCode).isEqualTo(CONFLICT)
        assertThat(response.body?.errorCode).isEqualTo(DUPLICATE_KEY.name)
        assertThat(response.body?.errorReason).isEqualTo("Already existing key")
        assertThat(response.body?.message).isEqualTo("The input request defines a movie that already exists.")
    }
}