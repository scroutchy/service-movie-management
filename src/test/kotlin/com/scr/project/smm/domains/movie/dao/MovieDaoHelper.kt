package com.scr.project.smm.domains.movie.dao

import com.scr.project.smm.domains.movie.model.entity.Movie
import com.scr.project.smm.domains.movie.model.entity.MovieType.Comedy
import com.scr.project.smm.domains.movie.model.entity.MovieType.Drama
import org.bson.types.ObjectId
import java.time.LocalDate

fun pulpFiction() = Movie(
    "Pulp Fiction",
    LocalDate.of(1994, 10, 14),
    Comedy,
    listOf(),
    ObjectId("67c09246c908c7ee39bc5e88")
)

fun theDarkKnight() = Movie(
    "The Dark Knight",
    LocalDate.of(2008, 7, 18),
    Drama,
    listOf("67c09246c908c7ee39bc5e91", "67c09246c908c7ee39bc5e92"),
    ObjectId("67c09246c908c7ee39bc5e89")
)