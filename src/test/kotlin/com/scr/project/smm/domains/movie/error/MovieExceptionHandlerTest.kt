package com.scr.project.smm.domains.movie.error

import com.mongodb.ErrorCategory.DUPLICATE_KEY
import com.scr.project.smm.domains.movie.error.MovieErrorReasonCode.ACTOR_NOT_FOUND
import com.scr.project.smm.domains.movie.error.MovieErrorReasonCode.MOVIE_NOT_FOUND
import com.scr.project.smm.domains.movie.error.MovieErrors.OnActorNotFound
import com.scr.project.smm.domains.movie.error.MovieErrors.OnMovieNotFound
import com.scr.project.smm.domains.movie.error.MovieExceptionHandler.ErrorResponse
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.springframework.dao.DuplicateKeyException
import org.springframework.http.HttpStatus.CONFLICT
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.ResponseEntity

class MovieExceptionHandlerTest {

    private val handler = MovieExceptionHandler()

    @Test
    fun `handle OnMovieNotFoundException returns correct response`() {
        val objectId = ObjectId.get()
        val response: ResponseEntity<ErrorResponse> = handler.handleOnMovieNotFound(OnMovieNotFound(objectId))
        assertThat(response.statusCode).isEqualTo(NOT_FOUND)
        assertThat(response.body?.errorCode).isEqualTo(MOVIE_NOT_FOUND.name)
        assertThat(response.body?.errorReason).isEqualTo(MOVIE_NOT_FOUND.wording)
        assertThat(response.body?.message).isEqualTo("The movie with id $objectId was not found.")
    }

    @Test
    fun `handle DuplicateKeyException returns correct response`() {
        val response: ResponseEntity<ErrorResponse> = handler.handleDuplicateKeyException(DuplicateKeyException("message"))
        assertThat(response.statusCode).isEqualTo(CONFLICT)
        assertThat(response.body?.errorCode).isEqualTo(DUPLICATE_KEY.name)
        assertThat(response.body?.errorReason).isEqualTo("Already existing key")
        assertThat(response.body?.message).isEqualTo("The input request defines a movie that already exists.")
    }

    @Test
    fun `handle OnActorNotFoundException returns correct response`() {
        val objectId = ObjectId.get().toHexString()
        val response: ResponseEntity<ErrorResponse> = handler.handleOnActorNotFound(OnActorNotFound(objectId))
        assertThat(response.statusCode).isEqualTo(NOT_FOUND)
        assertThat(response.body?.errorCode).isEqualTo(ACTOR_NOT_FOUND.name)
        assertThat(response.body?.errorReason).isEqualTo(ACTOR_NOT_FOUND.wording)
        assertThat(response.body?.message).isEqualTo("The actor with id $objectId was not found in Actor component.")
    }

}