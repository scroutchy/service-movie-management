package com.scr.project.smm.entrypoint.model.api

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY
import com.fasterxml.jackson.annotation.JsonView
import com.scr.project.smm.domains.movie.model.entity.MovieType
import com.scr.project.smm.entrypoint.model.api.Views.MovieListView
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.PastOrPresent
import java.time.LocalDate

data class MovieApiDto(
    @field:NotBlank
    @JsonView(MovieListView::class)
    val title: String,
    @field:PastOrPresent
    @JsonView(MovieListView::class)
    val releaseDate: LocalDate,
    @JsonView(MovieListView::class)
    val type: MovieType,
    @JsonProperty(access = WRITE_ONLY)
    val actorIds: List<String> = listOf(),
    val actors: List<ActorApiDto> = listOf(),
    val synopsis: String? = null,
    @JsonView(MovieListView::class)
    var id: String? = null
)