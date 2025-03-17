package com.scr.project.smm.domains.movie.ports

import com.scr.project.smm.domains.movie.model.business.Actor
import com.scr.project.smm.domains.movie.model.entity.Movie
import org.bson.types.ObjectId
import org.springframework.data.domain.Pageable
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDate

interface MoviePort {

    fun create(movie: Movie): Mono<Movie>

    fun findById(id: ObjectId): Mono<MovieWithActors>

    fun findAllBetween(pageable: Pageable, startDate: LocalDate? = null, endDate: LocalDate? = null): Flux<Movie>
}

data class MovieWithActors(val movie: Movie, val actors: List<Actor> = listOf())