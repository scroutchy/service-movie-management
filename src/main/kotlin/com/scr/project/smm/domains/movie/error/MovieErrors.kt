package com.scr.project.smm.domains.movie.error

import com.scr.project.smm.domains.movie.error.MovieErrorReasonCode.ACTOR_NOT_FOUND
import com.scr.project.smm.domains.movie.error.MovieErrorReasonCode.MOVIE_NOT_FOUND
import com.scr.project.smm.domains.movie.error.MovieErrorReasonCode.SUMMARY_NOT_FOUND
import org.bson.types.ObjectId

sealed class MovieErrors : RuntimeException() {

    class OnMovieNotFound(val id: ObjectId) : MovieErrors() {

        override val message = MOVIE_NOT_FOUND.wording
    }

    class OnActorNotFound(val id: String) : MovieErrors() {

        override val message = ACTOR_NOT_FOUND.wording
    }

    class OnSummaryNotFound(val title: String, val year: Int) : MovieErrors() {

        override val message = SUMMARY_NOT_FOUND.wording
    }
}
