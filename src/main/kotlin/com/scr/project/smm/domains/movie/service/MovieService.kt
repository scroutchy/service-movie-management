package com.scr.project.smm.domains.movie.service

import com.scr.project.smm.domains.movie.model.entity.Movie
import com.scr.project.smm.domains.movie.ports.MoviePort
import com.scr.project.smm.domains.movie.repository.MovieRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class MovieService(val movieRepository: MovieRepository) : MoviePort {

    private val logger: Logger = LoggerFactory.getLogger(MovieService::class.java)

    override fun create(movie: Movie): Mono<Movie> {
        return movieRepository.insert(movie)
            .doOnSubscribe { logger.debug("Creating movie") }
            .doOnSuccess { logger.info("Creation of movie with id ${it.id} was successful.") }
            .doOnError { logger.warn("Creation of movie failed.") }
    }
}