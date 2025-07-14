package com.scr.project.smm.domains.movie.service

import com.scr.project.smm.domains.movie.model.business.MovieWithActors
import com.scr.project.smm.domains.movie.model.entity.Movie
import com.scr.project.smm.domains.security.service.KeycloakService
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono

@Component
class MovieEnricher(
    private val actorService: ActorService,
    private val keycloakService: KeycloakService
) {

    fun enrichWithActors(movie: Movie): Mono<MovieWithActors> {
        return movie.actors.takeIf { it.isNotEmpty() }?.let {
            keycloakService.getToken().map(::toBearerToken)
                .flatMapMany { t -> it.toFlux().flatMap { a -> actorService.findById(a, t) } }
                .collectList()
                .map { MovieWithActors(movie, it) }
        } ?: MovieWithActors(movie).toMono()
    }

    private fun toBearerToken(token: String) = "Bearer $token"
}
