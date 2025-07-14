package com.scr.project.smm.domains.movie.service

import com.scr.project.smm.domains.movie.error.MovieErrors.OnMovieNotFound
import com.scr.project.smm.domains.movie.messaging.v1.RewardedMessagingV1
import com.scr.project.smm.domains.movie.model.business.MovieWithActors
import com.scr.project.smm.domains.movie.model.entity.Movie
import com.scr.project.smm.domains.movie.ports.MoviePort
import com.scr.project.smm.domains.movie.repository.MovieRepository
import com.scr.project.smm.domains.movie.repository.SimpleMovieRepository
import org.bson.types.ObjectId
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import java.time.LocalDate

@Service
class MovieService(
    private val simpleMovieRepository: SimpleMovieRepository,
    private val movieRepository: MovieRepository,
    private val synopsisService: SynopsisService,
    private val movieMessagingV1: RewardedMessagingV1,
    private val movieEnricher: MovieEnricher
) : MoviePort {

    private val logger: Logger = LoggerFactory.getLogger(MovieService::class.java)

    @Transactional
    override fun create(movie: Movie): Mono<Movie> {
        return synopsisService.requestSynopsis(movie.title, movie.releaseDate)
            .map { movie.copy(synopsis = it) }
            .flatMap(simpleMovieRepository::insert)
            .flatMap(movieMessagingV1::notify)
            .doOnSubscribe { logger.debug("Creating movie") }
            .doOnSuccess { logger.info("Creation of movie with id ${it.id} was successful.") }
            .doOnError { logger.warn("Creation of movie failed.") }
    }

    override fun findById(id: ObjectId): Mono<MovieWithActors> {
        return simpleMovieRepository.findById(id.toHexString())
            .doOnSubscribe { logger.debug("Finding movie") }
            .switchIfEmpty { Mono.error(OnMovieNotFound(id)) }
            .flatMap { movieEnricher.enrichWithActors(it) }
            .doOnSuccess { logger.debug("Finding movie with id ${it.movie.id} was successful.") }
            .doOnError { logger.warn("Error when finding movie with id $id") }
    }

    override fun findAllBetween(pageable: Pageable, startDate: LocalDate?, endDate: LocalDate?): Flux<Movie> {
        return movieRepository.findAllMoviesBetweenDates(pageable, startDate, endDate)
            .doOnSubscribe { logger.debug("Listing movies") }
            .doOnComplete { logger.debug("Listing movies was successful.") }
            .doOnError { logger.warn("Error when listing movies") }
    }
}
