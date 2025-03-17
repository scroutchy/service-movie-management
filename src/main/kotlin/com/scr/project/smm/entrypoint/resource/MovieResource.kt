package com.scr.project.smm.entrypoint.resource

import com.fasterxml.jackson.annotation.JsonView
import com.scr.project.commons.cinema.utils.RangedResponse
import com.scr.project.commons.cinema.utils.toRangedResponse
import com.scr.project.smm.domains.movie.service.MovieService
import com.scr.project.smm.entrypoint.mapper.toApiDto
import com.scr.project.smm.entrypoint.mapper.toEntity
import com.scr.project.smm.entrypoint.model.api.MovieApiDto
import com.scr.project.smm.entrypoint.model.api.Views.MovieListView
import com.scr.project.smm.entrypoint.resource.ApiConstants.DEFAULT_PAGE_SIZE
import com.scr.project.smm.entrypoint.resource.ApiConstants.ID_PATH
import com.scr.project.smm.entrypoint.resource.ApiConstants.MOVIE_PATH
import jakarta.validation.Valid
import org.bson.types.ObjectId
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort.Direction.ASC
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import java.time.LocalDate

@RestController
@RequestMapping(MOVIE_PATH)
class MovieResource(private val movieService: MovieService) {

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

    @GetMapping
    @JsonView(MovieListView::class)
    fun list(
        @PageableDefault(size = DEFAULT_PAGE_SIZE, sort = ["title"], direction = ASC) pageable: Pageable,
        @RequestParam startDate: LocalDate? = null,
        @RequestParam endDate: LocalDate? = null,
    ): RangedResponse<MovieApiDto> {
        return movieService.findAllBetween(pageable, startDate, endDate)
            .map { it.toApiDto() }
            .toRangedResponse(MovieApiDto::class.java, pageable)
            .doOnSubscribe { logger.debug("List request received") }
            .doOnSuccess { logger.debug("Listing movies request successfully handled") }
    }
}