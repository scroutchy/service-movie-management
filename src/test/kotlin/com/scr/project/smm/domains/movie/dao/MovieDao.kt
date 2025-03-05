package com.scr.project.smm.domains.movie.dao

import com.scr.project.commons.cinema.test.dao.GenericDao
import com.scr.project.smm.domains.movie.model.entity.Movie

class MovieDao(mongoUri: String) : GenericDao<Movie>(mongoUri, Movie::class.java, "movie") {

    override fun defaultEntities() = listOf(pulpFiction(), theDarkKnight())
}