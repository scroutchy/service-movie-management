package com.scr.project.smm.domains.movie.config

import com.scr.project.smm.domains.movie.model.entity.Movie
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.Sort.Direction.ASC
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.index.Index

@Configuration(proxyBeanMethods = false)
class MovieMongoIndexConfiguration(mongoTemplate: ReactiveMongoTemplate) {

    init {
        movieIndexes(mongoTemplate)
    }

    private fun movieIndexes(mongoTemplate: ReactiveMongoTemplate) {
        mongoTemplate.indexOps(Movie::class.java)
            .createIndex(
                Index().on(Movie::title.name, ASC)
                    .unique()
            ).subscribe()
    }
}