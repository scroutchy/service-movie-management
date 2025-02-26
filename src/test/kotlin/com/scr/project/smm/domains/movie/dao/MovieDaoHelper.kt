package com.scr.project.smm.domains.movie.dao

import com.scr.project.smm.domains.movie.model.entity.Movie
import java.time.LocalDate

fun pulpFiction() = Movie("Pulp Fiction", LocalDate.of(1994, 10, 14), "Theatrical")