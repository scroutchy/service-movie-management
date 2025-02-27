package com.scr.project.smm.entrypoint.resource

import com.scr.project.smm.domains.movie.service.MovieService
import com.scr.project.smm.entrypoint.mapper.toApiDto
import com.scr.project.smm.entrypoint.mapper.toEntity
import com.scr.project.smm.entrypoint.model.api.MovieApiDto
import com.scr.project.smm.entrypoint.resource.ApiConstants.ID_PATH
import com.scr.project.smm.entrypoint.resource.ApiConstants.MOVIE_PATH
import jakarta.validation.Valid
import org.bson.types.ObjectId
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping(MOVIE_PATH)
class MovieResource(val movieService: MovieService) {

    private val logger: Logger = LoggerFactory.getLogger(MovieResource::class.java)

    @PostMapping
    fun create(@RequestBody @Valid request: MovieApiDto): Mono<ResponseEntity<MovieApiDto>> {
        return movieService.create(request.toEntity())
            .map { it.toApiDto() }
            .map { ResponseEntity(it, CREATED) }
            .doOnSubscribe { logger.debug("Creation request received") }
            .doOnSuccess { logger.info("Creation request successfully handled") }
    }

    @GetMapping(ID_PATH)
    fun find(@PathVariable id: ObjectId): Mono<MovieApiDto> {
        return movieService.findById(id)
            .map { it.toApiDto() }
            .doOnSubscribe { logger.debug("Find request received") }
            .doOnSuccess { logger.debug("Finding movie request with id {${it.id}} successfully handled") }
    }
}