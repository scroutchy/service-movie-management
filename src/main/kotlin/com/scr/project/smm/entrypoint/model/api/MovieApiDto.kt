package com.scr.project.smm.entrypoint.model.api

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY
import com.scr.project.smm.domains.movie.model.entity.MovieType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.PastOrPresent
import java.time.LocalDate

data class MovieApiDto(
    @field:NotBlank
    val title: String,
    @field:PastOrPresent
    val releaseDate: LocalDate,
    val type: MovieType,
    @JsonProperty(access = WRITE_ONLY)
    val actorIds: List<String> = listOf(),
    val actors: List<ActorApiDto> = listOf(),
    val synopsis: String? = null,
    var id: String? = null
)