package com.scr.project.smm.domains.movie.repository

import com.scr.project.smm.domains.movie.model.entity.Movie
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import java.time.LocalDate

@Repository
class MovieRepositoryImpl(private val mongoTemplate: ReactiveMongoTemplate) : MovieRepository {

    override fun findAllMoviesBetweenDates(pageable: Pageable, startDate: LocalDate?, endDate: LocalDate?): Flux<Movie> {
        val criteria = mutableListOf<Criteria>().apply {
            startDate?.let { add(Criteria.where(Movie::releaseDate.name).gte(it)) }
            endDate?.let { add(Criteria.where(Movie::releaseDate.name).lte(it)) }
        }
        val query = criteria.takeIf { it.isNotEmpty() }?.let { Query().addCriteria(Criteria().andOperator(*it.toTypedArray())) } ?: Query()
        query.with(pageable)
        return mongoTemplate.find(query, Movie::class.java)
    }
}