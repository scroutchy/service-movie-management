package com.scr.project.smm.domains.movie.repository

import com.scr.project.smm.domains.movie.model.entity.Movie
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository

@Repository
interface MovieRepository : ReactiveMongoRepository<Movie, String>