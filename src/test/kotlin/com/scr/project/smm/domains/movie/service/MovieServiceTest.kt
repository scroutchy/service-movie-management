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

    private val movieWithoutId = Movie("title", LocalDate.now(), Fantasy, "This is the synopsis")
    private val movie = movieWithoutId.copy(id = ObjectId.get())
    private val movieRepository = mockk<MovieRepository>()
    private val actorService = mockk<ActorService>()
    private val synopsisService = mockk<SynopsisService>()
    private val movieService = MovieService(movieRepository, actorService, synopsisService)

    @BeforeEach
    internal fun setUp() {
        clearMocks(movieRepository, actorService, synopsisService)
    }

    @Test
    fun `create should succeed`() {
        every { movieRepository.insert(any<Movie>()) } answers { firstArg<Movie>().copy(id = ObjectId.get()).toMono() }
        every { synopsisService.requestSynopsis(any<String>()) } answers { "This is the AI-generated synopsis".toMono() }
        movieService.create(movieWithoutId)
            .test()
            .expectSubscription()
            .consumeNextWith {
                assertThat(it.id).isNotNull()
                assertThat(it.title).isEqualTo(movieWithoutId.title)
                assertThat(it.releaseDate).isEqualTo(movieWithoutId.releaseDate)
                assertThat(it.type).isEqualTo(movieWithoutId.type)
                assertThat(it.synopsis).isEqualTo("This is the AI-generated synopsis")
            }.verifyComplete()
        verify(exactly = 1) { movieRepository.insert(any<Movie>()) }
        verify(exactly = 1) { synopsisService.requestSynopsis(movieWithoutId.title) }
        confirmVerified(movieRepository, synopsisService)
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
                assertThat(it.movie.synopsis).isEqualTo(movie.synopsis)
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
                assertThat(it.movie.synopsis).isEqualTo(movie.synopsis)
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