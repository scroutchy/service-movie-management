package com.scr.project.smm.domains.movie.model.entity

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDate

@Document
data class Movie(
    val title: String,
    val releaseDate: LocalDate,
    val type: MovieType,
    @field:Id @BsonId var id: ObjectId? = null,
)