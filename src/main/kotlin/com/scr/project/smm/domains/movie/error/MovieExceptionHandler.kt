package com.scr.project.smm.domains.movie.error

import com.mongodb.ErrorCategory.DUPLICATE_KEY
import com.scr.project.smm.domains.movie.error.MovieErrorReasonCode.ACTOR_NOT_FOUND
import com.scr.project.smm.domains.movie.error.MovieErrorReasonCode.MOVIE_NOT_FOUND
import com.scr.project.smm.domains.movie.error.MovieErrors.OnActorNotFound
import com.scr.project.smm.domains.movie.error.MovieErrors.OnMovieNotFound
import org.springframework.dao.DuplicateKeyException
import org.springframework.http.HttpStatus.CONFLICT
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class MovieExceptionHandler {

    @ExceptionHandler(OnMovieNotFound::class)
    fun handleOnMovieNotFound(ex: OnMovieNotFound): ResponseEntity<ErrorResponse> {
        val body = ErrorResponse(MOVIE_NOT_FOUND.name, MOVIE_NOT_FOUND.wording, "The movie with id ${ex.id} was not found.")
        return ResponseEntity(body, NOT_FOUND)
    }

    @ExceptionHandler(DuplicateKeyException::class)
    fun handleDuplicateKeyException(ex: DuplicateKeyException): ResponseEntity<ErrorResponse> {
        val body = ErrorResponse(
            DUPLICATE_KEY.name,
            "Already existing key",
            "The input request defines a movie that already exists.",
        )
        return ResponseEntity(body, CONFLICT)
    }

    @ExceptionHandler(OnActorNotFound::class)
    fun handleOnActorNotFound(ex: OnActorNotFound): ResponseEntity<ErrorResponse> {
        val body =
            ErrorResponse(ACTOR_NOT_FOUND.name, ACTOR_NOT_FOUND.wording, "The actor with id ${ex.id} was not found in Actor component.")
        return ResponseEntity(body, NOT_FOUND)
    }

    data class ErrorResponse(val errorCode: String, val errorReason: String, val message: String)
}