package com.scr.project.smm.domains.movie.service

import com.scr.project.smm.domains.movie.model.business.Actor
import com.scr.project.smm.domains.movie.model.entity.Movie
import com.scr.project.smm.domains.movie.model.entity.MovieType.Thriller
import com.scr.project.smm.domains.security.service.KeycloakService
import io.mockk.clearMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import reactor.kotlin.core.publisher.toMono
import reactor.kotlin.test.test
import java.time.LocalDate

class MovieEnricherTest {

    private val actorService = mockk<ActorService>()
    private val keycloakService = mockk<KeycloakService>()
    private val movieEnricher = MovieEnricher(actorService, keycloakService)

    @BeforeEach
    fun setUp() {
        clearMocks(actorService, keycloakService)
    }

    @Test
    fun `enrichWithActors should succeed when no actors`() {
        val movie = Movie("Nobody", LocalDate.of(2020, 1, 1), Thriller, "synopsis")
        movieEnricher.enrichWithActors(movie)
            .test()
            .expectSubscription()
            .consumeNextWith {
                assertThat(it.movie).isEqualTo(movie)
                assertThat(it.actors).isEmpty()
            }.verifyComplete()
        verify(inverse = true) { actorService.findById(any(), any()) }
        verify(inverse = true) { keycloakService.getToken() }
        confirmVerified(actorService, keycloakService)
    }

    @Test
    fun `enrichWithActors should succeed when actors are present`() {
        val actorIds = listOf("actor1", "actor2")
        val movie = Movie("Inception", LocalDate.of(2010, 7, 16), Thriller, "A mind-bending thriller", actorIds)
        every { actorService.findById(any(), any()) } answers { Actor(firstArg(), "Leonardo DiCaprio").toMono() }
        every { keycloakService.getToken() } answers { "token".toMono() }
        movieEnricher.enrichWithActors(movie)
            .test()
            .expectSubscription()
            .consumeNextWith {
                assertThat(it.movie).isEqualTo(movie)
                assertThat(it.actors).hasSize(2)
                assertThat(it.actors.map { it.id }).containsExactlyInAnyOrderElementsOf(actorIds)
            }.verifyComplete()

        verify(exactly = 2) { actorService.findById(any(), any()) }
        verify(exactly = 1) { keycloakService.getToken() }
        confirmVerified(actorService, keycloakService)
    }

    @Test
    fun `enrichWithActors should handle errors from keycloak`() {
        val actorIds = listOf("actor1", "actor2")
        val movie = Movie("Inception", LocalDate.of(2010, 7, 16), Thriller, "A mind-bending thriller", actorIds)
        every { actorService.findById(any(), any()) } answers { Actor(firstArg(), "Leonardo DiCaprio").toMono() }
        every { keycloakService.getToken() } answers { RuntimeException("Keycloak error").toMono() }
        movieEnricher.enrichWithActors(movie)
            .test()
            .expectSubscription()
            .consumeErrorWith {
                assertThat(it).isInstanceOf(RuntimeException::class.java)
                assertThat(it.message).contains("Keycloak error")
            }.verify()
        verify(exactly = 1) { keycloakService.getToken() }
        verify(inverse = true) { actorService.findById(any(), any()) }
        confirmVerified(actorService, keycloakService)
    }
}