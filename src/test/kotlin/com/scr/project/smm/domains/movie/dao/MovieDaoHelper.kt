package com.scr.project.smm.domains.movie.dao

import com.scr.project.smm.domains.movie.model.entity.Movie
import com.scr.project.smm.domains.movie.model.entity.MovieType.Comedy
import org.bson.types.ObjectId
import java.time.LocalDate

fun pulpFiction() = Movie(
    "Pulp Fiction",
    LocalDate.of(1994, 10, 14),
    Comedy,
    ObjectId("67c09246c908c7ee39bc5e88")
)