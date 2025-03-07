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
    "\\\"Pulp Fiction,\\\" directed by Quentin Tarantino, is a non-linear, interwoven crime film that follows several characters, including two hitmen, a gangster's wife, and a boxer, as they navigate the seedy underbelly of Los Angeles, themes of violence, redemption, and fate.",
    listOf(),
    ObjectId("67c09246c908c7ee39bc5e88")
)

fun theDarkKnight() = Movie(
    "The Dark Knight",
    LocalDate.of(2008, 7, 18),
    Drama,
    "\"The Dark Knight\" explores themes of chaos, morality, and the limits of heroism as Batman confronts the Joker, a nihilistic criminal who tests the ethical boundaries of Gotham City.",
    listOf("67c09246c908c7ee39bc5e91", "67c09246c908c7ee39bc5e92"),
    ObjectId("67c09246c908c7ee39bc5e89")
)