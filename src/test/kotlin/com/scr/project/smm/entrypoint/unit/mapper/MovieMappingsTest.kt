package com.scr.project.smm.entrypoint.unit.mapper

import com.scr.project.smm.domains.movie.model.entity.Movie
import com.scr.project.smm.domains.movie.model.entity.MovieType.ScienceFiction
import com.scr.project.smm.domains.movie.model.entity.MovieType.Thriller
import com.scr.project.smm.entrypoint.mapper.toApiDto
import com.scr.project.smm.entrypoint.mapper.toEntity
import com.scr.project.smm.entrypoint.model.api.MovieApiDto
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import java.time.LocalDate

class MovieMappingsTest {

    @Test
    fun `toEntity should succeed`() {
        val movieApiDto =
            MovieApiDto("title", LocalDate.now(), Thriller, listOf(ObjectId.get().toHexString(), ObjectId.get().toHexString()))
        val movie = movieApiDto.toEntity()
        assertThat(movie).isNotNull
        assertThat(movie.title).isEqualTo(movieApiDto.title)
        assertThat(movie.releaseDate).isEqualTo(movieApiDto.releaseDate)
        assertThat(movie.type).isEqualTo(movieApiDto.type)
        assertThat(movie.synopsis).isBlank()
        assertThat(movie.actors).hasSize(movieApiDto.actorIds.size)
        movie.actors.forEach { movieApiDto.actorIds.contains(it) }
    }

    @Test
    fun `toApiDto should succeed`() {
        val movie = Movie(
            "title",
            LocalDate.now(),
            ScienceFiction,
            "This is the synopsis",
            listOf(ObjectId.get().toHexString(), ObjectId.get().toHexString()),
            ObjectId.get()
        )
        val movieApiDto = movie.toApiDto()
        assertThat(movieApiDto).isNotNull
        assertThat(movieApiDto.title).isEqualTo(movie.title)
        assertThat(movieApiDto.releaseDate).isEqualTo(movie.releaseDate)
        assertThat(movieApiDto.type).isEqualTo(movie.type)
        assertThat(movieApiDto.synopsis).isEqualTo(movie.synopsis)
        assertThat(movieApiDto.actorIds).hasSize(movie.actors.size)
        movieApiDto.actorIds.forEach { movie.actors.contains(it) }
        assertThat(movieApiDto.id).isEqualTo(movie.id?.toHexString())
    }
}