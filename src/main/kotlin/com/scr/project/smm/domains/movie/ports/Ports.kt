package com.scr.project.smm.domains.movie.ports

import com.scr.project.smm.domains.movie.model.entity.Movie
import reactor.core.publisher.Mono

fun interface MoviePort {

    fun create(movie: Movie): Mono<Movie>
}