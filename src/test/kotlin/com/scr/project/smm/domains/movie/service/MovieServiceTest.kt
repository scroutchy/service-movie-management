package com.scr.project.smm.domains.movie.service

import com.scr.project.smm.domains.movie.error.MovieErrors.OnMovieNotFound
import com.scr.project.smm.domains.movie.messaging.v1.RewardedMessagingV1
import com.scr.project.smm.domains.movie.model.business.Actor
import com.scr.project.smm.domains.movie.model.entity.Movie
import com.scr.project.smm.domains.movie.model.entity.MovieType.Fantasy
import com.scr.project.smm.domains.movie.repository.MovieRepository
import com.scr.project.smm.domains.movie.repository.SimpleMovieRepository
import com.scr.project.smm.domains.security.service.KeycloakService
import io.mockk.clearMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.domain.Pageable
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono
import reactor.kotlin.test.test
import reactor.kotlin.test.verifyError
import java.time.LocalDate

class MovieServiceTest {

    private val movieWithoutId = Movie("title", LocalDate.now(), Fantasy, "This is the synopsis")
    private val movie = movieWithoutId.copy(id = ObjectId.get())
    private val anotherMovie = Movie("another title", LocalDate.now().minusYears(5), Fantasy, "This is the synopsis", id = ObjectId.get())
    private val simpleMovieRepository = mockk<SimpleMovieRepository>()
    private val movieRepository = mockk<MovieRepository>()
    private val actorService = mockk<ActorService>()
    private val synopsisService = mockk<SynopsisService>()
    private val keycloakService = mockk<KeycloakService>()
    private val movieMessagingV1 = mockk<RewardedMessagingV1>()
    private val movieService =
        MovieService(simpleMovieRepository, movieRepository, actorService, synopsisService, keycloakService, movieMessagingV1)

    @BeforeEach
    internal fun setUp() {
        clearMocks(simpleMovieRepository, movieRepository, actorService, synopsisService, movieMessagingV1)
    }

    @Test
    fun `create should succeed`() {
        every { simpleMovieRepository.insert(any<Movie>()) } answers { firstArg<Movie>().copy(id = ObjectId.get()).toMono() }
        every { synopsisService.requestSynopsis(any<String>(), any<LocalDate>()) } answers { "This is the AI-generated synopsis".toMono() }
        every { movieMessagingV1.notify(any<Movie>()) } answers { firstArg<Movie>().toMono() }
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
        verify(exactly = 1) { simpleMovieRepository.insert(any<Movie>()) }
        verify(exactly = 1) { synopsisService.requestSynopsis(movieWithoutId.title, movieWithoutId.releaseDate) }
        verify(exactly = 1) { movieMessagingV1.notify(any<Movie>()) }
        confirmVerified(simpleMovieRepository, synopsisService, movieMessagingV1)
    }

    @Test
    fun `findById should succeed when movie exists`() {
        every { simpleMovieRepository.findById(movie.id!!.toHexString()) } answers { movie.toMono() }
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
        verify(exactly = 1) { simpleMovieRepository.findById(movie.id!!.toHexString()) }
        confirmVerified(simpleMovieRepository)
    }

    @Test
    fun `findById should succeed and retrieve actors when provided in movie`() {
        val actorId = ObjectId.get().toHexString()
        every { simpleMovieRepository.findById(movie.id!!.toHexString()) } answers { movie.copy(actors = listOf(actorId)).toMono() }
        every { actorService.findById(actorId, any()) } answers { Actor(actorId, "Brad Pitt").toMono() }
        every { keycloakService.getToken() } answers { "token".toMono() }
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
        verify(exactly = 1) { simpleMovieRepository.findById(movie.id!!.toHexString()) }
        verify(exactly = 1) { actorService.findById(any(), "Bearer token") }
        confirmVerified(simpleMovieRepository, actorService)
    }

    @Test
    fun `findById should return exception when movie does not exist`() {
        every { simpleMovieRepository.findById(movie.id!!.toHexString()) } answers { Mono.empty() }
        movieService.findById(movie.id!!)
            .test()
            .expectSubscription()
            .consumeSubscriptionWith {
                verify(exactly = 1) { simpleMovieRepository.findById(movie.id!!.toHexString()) }
                confirmVerified(simpleMovieRepository)
            }.verifyError(OnMovieNotFound::class)
    }

    @Test
    fun `findAllBetween should return all movies between provided dates`() {
        val movies = listOf(movie, anotherMovie)
        every { movieRepository.findAllMoviesBetweenDates(any(), any(), any()) } answers { movies.toFlux() }
        movieService.findAllBetween(pageable = Pageable.ofSize(10))
            .collectList()
            .test()
            .expectSubscription()
            .consumeNextWith {
                assertThat(it).hasSize(movies.size)
                assertThat(it).containsAll(movies)
            }.verifyComplete()
        verify(exactly = 1) { movieRepository.findAllMoviesBetweenDates(Pageable.ofSize(10)) }
        confirmVerified(movieRepository)
    }

    @Test
    fun `findAllBetween should return empty list when no movies between provided dates`() {
        val startDate = LocalDate.of(1500, 1, 1)
        val endDate = LocalDate.of(1600, 1, 1)
        every { movieRepository.findAllMoviesBetweenDates(any(), any(), any()) } answers { emptyList<Movie>().toFlux() }
        movieService.findAllBetween(Pageable.ofSize(10), startDate, endDate)
            .collectList()
            .test()
            .expectSubscription()
            .consumeNextWith {
                assertThat(it).isEmpty()
            }.verifyComplete()
        verify(exactly = 1) { movieRepository.findAllMoviesBetweenDates(Pageable.ofSize(10), startDate, endDate) }
        confirmVerified(movieRepository)
    }
}
