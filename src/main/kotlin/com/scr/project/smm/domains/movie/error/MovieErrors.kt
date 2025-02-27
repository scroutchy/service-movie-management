package com.scr.project.smm.domains.movie.error

import com.scr.project.smm.domains.movie.error.MovieErrorReasonCode.MOVIE_NOT_FOUND
import org.bson.types.ObjectId
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.web.bind.annotation.ResponseStatus

sealed class MovieErrors : RuntimeException() {

    @ResponseStatus(NOT_FOUND)
    class OnMovieNotFound(val id: ObjectId) : MovieErrors() {

        override val message = MOVIE_NOT_FOUND.wording
    }
}