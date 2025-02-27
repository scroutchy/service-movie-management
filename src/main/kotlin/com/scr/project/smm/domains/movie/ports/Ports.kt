package com.scr.project.smm.domains.movie.ports

import com.scr.project.smm.domains.movie.model.business.Actor
import com.scr.project.smm.domains.movie.model.entity.Movie
import org.bson.types.ObjectId
import reactor.core.publisher.Mono

interface MoviePort {

    fun create(movie: Movie): Mono<Movie>

    fun findById(id: ObjectId): Mono<MovieWithActors>
}

data class MovieWithActors(val movie: Movie, val actors: List<Actor> = listOf())