package com.scr.project.smm.domains.movie.error

import com.scr.project.smm.domains.movie.error.MovieErrorReasonCode.ACTOR_NOT_FOUND
import com.scr.project.smm.domains.movie.error.MovieErrorReasonCode.MOVIE_NOT_FOUND
import org.bson.types.ObjectId
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.web.bind.annotation.ResponseStatus

sealed class MovieErrors : RuntimeException() {

    @ResponseStatus(NOT_FOUND)
    class OnMovieNotFound(val id: ObjectId) : MovieErrors() {

        override val message = MOVIE_NOT_FOUND.wording
    }

    @ResponseStatus(BAD_REQUEST)
    class OnActorNotFound(val id: String) : MovieErrors() {

        override val message = ACTOR_NOT_FOUND.wording
    }
}