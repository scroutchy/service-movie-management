package com.scr.project.smm.entrypoint.unit.resource

import com.scr.project.smm.domains.movie.error.MovieErrors.OnMovieNotFound
import com.scr.project.smm.domains.movie.model.entity.Movie
import com.scr.project.smm.domains.movie.model.entity.MovieType.Western
import com.scr.project.smm.domains.movie.service.MovieService
import com.scr.project.smm.entrypoint.mapper.toEntity
import com.scr.project.smm.entrypoint.model.api.MovieApiDto
import com.scr.project.smm.entrypoint.resource.MovieResource
import io.mockk.clearMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import reactor.kotlin.core.publisher.toMono
import reactor.kotlin.test.test
import reactor.kotlin.test.verifyError
import java.time.LocalDate

class MovieResourceTest {

    private val movieRequest = MovieApiDto("title", LocalDate.now(), Western)
    private val movieService = mockk<MovieService>()
    private val movieResource = MovieResource(movieService)

    @BeforeEach
    internal fun setUp() {
        clearMocks(movieService)
        every { movieService.create(any<Movie>()) } answers { movieRequest.toEntity().copy(id = ObjectId()).toMono() }
        every { movieService.findById(any<ObjectId>()) } answers { movieRequest.toEntity().copy(id = firstArg()).toMono() }
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

    @Test
    fun `find should succeed and return a movie response when id exits`() {
        val id = ObjectId.get()
        movieResource.find(id)
            .test()
            .expectSubscription()
            .consumeNextWith {
                assertThat(it).isNotNull()
                assertThat(it.id).isEqualTo(id.toHexString())
                assertThat(it.title).isEqualTo(movieRequest.title)
                assertThat(it.releaseDate).isEqualTo(movieRequest.releaseDate)
                assertThat(it.type).isEqualTo(movieRequest.type)
            }
            .verifyComplete()
        verify(exactly = 1) { movieService.findById(id) }
        confirmVerified(movieService)
    }

    @Test
    fun `find should return an exception when movie id is not exist`() {
        val id = ObjectId.get()
        every { movieService.findById(id) } answers { OnMovieNotFound(id).toMono() }
        movieResource.find(id)
            .test()
            .expectSubscription()
            .consumeSubscriptionWith {
                verify(exactly = 1) { movieService.findById(id) }
                confirmVerified(movieService)
            }
            .verifyError(OnMovieNotFound::class)
    }
}