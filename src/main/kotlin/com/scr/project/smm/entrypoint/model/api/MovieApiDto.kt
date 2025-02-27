package com.scr.project.smm.entrypoint.model.api

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
    var id: String? = null
)