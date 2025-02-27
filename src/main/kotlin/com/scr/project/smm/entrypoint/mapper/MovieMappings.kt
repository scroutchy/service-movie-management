package com.scr.project.smm.entrypoint.mapper

import com.scr.project.smm.domains.movie.model.entity.Movie
import com.scr.project.smm.entrypoint.model.api.MovieApiDto

fun MovieApiDto.toEntity() = Movie(title, releaseDate, type, actors)

fun Movie.toApiDto() = MovieApiDto(title, releaseDate, type, actors, id?.toHexString())