package com.scr.project.smm.entrypoint.unit.resource

import com.scr.project.smm.domains.movie.model.entity.Movie
import com.scr.project.smm.domains.movie.service.MovieService
import com.scr.project.smm.entrypoint.mapper.toEntity
import com.scr.project.smm.entrypoint.model.api.MovieApiDto
import com.scr.project.smm.entrypoint.resource.MovieResource
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import reactor.kotlin.core.publisher.toMono
import reactor.kotlin.test.test
import java.time.LocalDate

class MovieResourceTest {

    private val movieRequest = MovieApiDto("title", LocalDate.now(), "type")
    private val movieService = mockk<MovieService>()
    private val movieResource = MovieResource(movieService)

    @BeforeEach
    internal fun setUp() {
        clearMocks(movieService)
        every { movieService.create(any<Movie>()) } answers { movieRequest.toEntity().copy(id = ObjectId()).toMono() }
    }

    @Test
    fun `create should succeed`() {
        movieResource.create(movieRequest)
            .test()
            .expectSubscription()
            .consumeNextWith {
                with(it.body!!) {
                    assertThat(id).isNotNull()
                    assertThat(title).isEqualTo(movieRequest.title)
                    assertThat(releaseDate).isEqualTo(movieRequest.releaseDate)
                    assertThat(type).isEqualTo(movieRequest.type)
                }
            }
    }
}