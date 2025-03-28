package com.scr.project.smm.domains.movie.service

import com.scr.project.smm.domains.movie.client.ActorClient
import com.scr.project.smm.domains.movie.error.MovieErrors.OnActorNotFound
import com.scr.project.smm.domains.movie.model.business.Actor
import com.scr.project.smm.entrypoint.mapper.toActor
import com.scr.project.smm.entrypoint.model.api.retrofit.ActorClientApiDto
import org.bson.types.ObjectId
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono

@Service
class ActorService(private val actorClient: ActorClient) {

    private val logger: Logger = LoggerFactory.getLogger(ActorService::class.java)

    fun findById(id: String, token: String): Mono<Actor> {
        return actorClient.findById(ObjectId(id), token)
            .doOnSubscribe { logger.debug("Retrieving actor data for actor $id") }
            .doOnNext { logger.debug("Retrieved actor data for actor $id") }
            .onErrorMap {
                if (it is WebClientResponseException && it.statusCode == NOT_FOUND) {
                    OnActorNotFound(id)
                } else {
                    logger.warn("Unknown error while retrieving actor data for actor $id", it)
                    it
                }
            }
            .map(ActorClientApiDto::toActor)
    }
}