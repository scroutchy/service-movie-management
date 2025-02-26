package com.scr.project.smm.entrypoint.model.api

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.PastOrPresent
import java.time.LocalDate

data class MovieApiDto(
    @field:NotBlank
    val title: String,
    @field:PastOrPresent
    val releaseDate: LocalDate,
    val type: String,
    var id: String? = null
)