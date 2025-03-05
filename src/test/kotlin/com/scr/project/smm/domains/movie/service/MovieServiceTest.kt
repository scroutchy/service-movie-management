package com.scr.project.smm.domains.movie.service

import com.scr.project.smm.domains.movie.error.MovieErrors.OnMovieNotFound
import com.scr.project.smm.domains.movie.model.business.Actor
import com.scr.project.smm.domains.movie.model.entity.Movie
import com.scr.project.smm.domains.movie.model.entity.MovieType.Fantasy
import com.scr.project.smm.domains.movie.repository.MovieRepository
import io.mockk.clearMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import reactor.kotlin.test.test
import reactor.kotlin.test.verifyError
import java.time.LocalDate

class MovieServiceTest {

    private val movieWithoutId = Movie("title", LocalDate.now(), Fantasy)
    private val movie = movieWithoutId.copy(id = ObjectId.get())
    private val movieRepository = mockk<MovieRepository>()
    private val actorService = mockk<ActorService>()
    private val movieService = MovieService(movieRepository, actorService)

    @BeforeEach
    internal fun setUp() {
        clearMocks(movieRepository)
        every { movieRepository.insert(movieWithoutId) } answers { movieWithoutId.copy(id = ObjectId.get()).toMono() }
    }

    @Test
    fun `create should succeed`() {
        movieService.create(movieWithoutId)
            .test()
            .expectSubscription()
            .consumeNextWith {
                assertThat(it.id).isNotNull()
                assertThat(it.title).isEqualTo(movieWithoutId.title)
                assertThat(it.releaseDate).isEqualTo(movieWithoutId.releaseDate)
                assertThat(it.type).isEqualTo(movieWithoutId.type)
            }.verifyComplete()
        verify(exactly = 1) { movieRepository.insert(movieWithoutId) }
        confirmVerified(movieRepository)
    }

    @Test
    fun `findById should succeed when movie exists`() {
        every { movieRepository.findById(movie.id!!.toHexString()) } answers { movie.toMono() }
        movieService.findById(movie.id!!)
            .test()
            .expectSubscription()
            .consumeNextWith {
                assertThat(it.movie.id).isEqualTo(movie.id)
                assertThat(it.movie.releaseDate).isEqualTo(movie.releaseDate)
                assertThat(it.movie.title).isEqualTo(movie.title)
                assertThat(it.movie.type).isEqualTo(movie.type)
                assertThat(it.actors).isEmpty()
            }.verifyComplete()
        verify(exactly = 1) { movieRepository.findById(movie.id!!.toHexString()) }
        confirmVerified(movieRepository)
    }

    @Test
    fun `findById should succeed and retrieve actors when provided in movie`() {
        val actorId = ObjectId.get().toHexString()
        every { movieRepository.findById(movie.id!!.toHexString()) } answers { movie.copy(actors = listOf(actorId)).toMono() }
        every { actorService.findById(actorId) } answers { Actor(actorId, "Brad Pitt").toMono() }
        movieService.findById(movie.id!!)
            .test()
            .expectSubscription()
            .consumeNextWith {
                assertThat(it.movie.id).isEqualTo(movie.id)
                assertThat(it.movie.releaseDate).isEqualTo(movie.releaseDate)
                assertThat(it.movie.title).isEqualTo(movie.title)
                assertThat(it.movie.type).isEqualTo(movie.type)
                assertThat(it.actors).hasSize(1)
                assertThat(it.actors.single()).isEqualTo(Actor(actorId, "Brad Pitt"))
            }.verifyComplete()
        verify(exactly = 1) { movieRepository.findById(movie.id!!.toHexString()) }
        verify(exactly = 1) { actorService.findById(any()) }
        confirmVerified(movieRepository, actorService)
    }

    @Test
    fun `findById should return exception when movie does not exist`() {
        every { movieRepository.findById(movie.id!!.toHexString()) } answers { Mono.empty() }
        movieService.findById(movie.id!!)
            .test()
            .expectSubscription()
            .consumeSubscriptionWith {
                verify(exactly = 1) { movieRepository.findById(movie.id!!.toHexString()) }
                confirmVerified(movieRepository)
            }.verifyError(OnMovieNotFound::class)
    }
}