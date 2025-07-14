package com.scr.project.smm.domains.movie.model.business

import com.scr.project.smm.domains.movie.model.entity.Movie

data class MovieWithActors(val movie: Movie, val actors: List<Actor> = listOf())