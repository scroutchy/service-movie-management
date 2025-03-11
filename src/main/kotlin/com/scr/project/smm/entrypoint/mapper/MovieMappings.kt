package com.scr.project.smm.entrypoint.mapper

import com.scr.project.smm.domains.movie.model.entity.Movie
import com.scr.project.smm.domains.movie.ports.MovieWithActors
import com.scr.project.smm.entrypoint.model.api.MovieApiDto

fun MovieApiDto.toEntity() = Movie(title, releaseDate, type, "", actorIds)

fun Movie.toApiDto() = MovieApiDto(title, releaseDate, type, actors, listOf(), synopsis, id?.toHexString())

fun MovieWithActors.toApiDto() = MovieApiDto(
    movie.title,
    movie.releaseDate,
    movie.type,
    listOf(),
    actors.map { it.toApiDto() },
    movie.synopsis,
    movie.id?.toHexString()
)