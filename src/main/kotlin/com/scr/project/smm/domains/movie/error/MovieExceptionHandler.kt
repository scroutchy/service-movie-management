package com.scr.project.smm.domains.movie.error

import org.springframework.dao.DuplicateKeyException
import org.springframework.http.HttpStatus.CONFLICT
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class MovieExceptionHandler {

    @ExceptionHandler(DuplicateKeyException::class)
    fun handleDuplicateKeyException(ex: DuplicateKeyException): ResponseEntity<ErrorResponse> {
        val body = ErrorResponse(
            com.mongodb.ErrorCategory.DUPLICATE_KEY.name,
            "Already existing key",
            "The input request defines a movie that already exists.",
        )
        return ResponseEntity(body, CONFLICT)
    }

    data class ErrorResponse(val errorCode: String, val errorReason: String, val message: String)
}