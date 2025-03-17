package com.scr.project.smm.domains.movie.repository

import com.scr.project.smm.domains.movie.model.entity.Movie
import org.springframework.data.domain.Pageable
import reactor.core.publisher.Flux
import java.time.LocalDate

interface MovieRepository {

    fun findAllMoviesBetweenDates(pageable: Pageable, startDate: LocalDate? = null, endDate: LocalDate? = null): Flux<Movie>
}