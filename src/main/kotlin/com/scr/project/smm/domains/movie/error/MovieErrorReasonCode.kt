package com.scr.project.smm.domains.movie.error

enum class MovieErrorReasonCode(val wording: String) {
    MOVIE_NOT_FOUND("Movie is not registered"),
    ACTOR_NOT_FOUND("Actor is not registered"),
}