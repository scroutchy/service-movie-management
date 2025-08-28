package com.scr.project.smm.domains.movie.service

import com.scr.project.smm.domains.movie.client.ActorClient
import com.scr.project.smm.domains.movie.error.MovieErrors.OnActorNotFound
import com.scr.project.smm.domains.movie.error.MovieErrors.OnActorServiceUnavailable
import com.scr.project.smm.entrypoint.model.api.retrofit.ActorClientApiDto
import io.github.resilience4j.circuitbreaker.CallNotPermittedException
import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.mockk.clearMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker
import org.springframework.http.HttpHeaders.EMPTY
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import reactor.kotlin.test.test

class ActorServiceTest {

    private val actorClient = mockk<ActorClient>()
    private val circuitBreaker = mockk<ReactiveCircuitBreaker>()
    private val actorService = ActorService(actorClient, circuitBreaker)
    private val actor = ActorClientApiDto(ObjectId.get().toHexString(), "surname", "name")
    private val token = "dummyToken"

    @BeforeEach
    fun setUp() {
        clearMocks(actorClient, circuitBreaker)
        val monoSlot = slot<Mono<ActorClientApiDto>>()
        every { circuitBreaker.run(capture(monoSlot), any()) } answers { monoSlot.captured }
    }

    @Test
    fun `findById should succeed`() {
        every { actorClient.findById(ObjectId(actor.id), token) } answers { actor.toMono() }
        actorService.findById(actor.id, token)
            .test()
            .expectSubscription()
            .consumeNextWith {
                assertThat(it.id).isEqualTo(actor.id)
                assertThat(it.fullName).isEqualTo("${actor.name} ${actor.surname}")
            }.verifyComplete()
        verify(exactly = 1) { actorClient.findById(ObjectId(actor.id), token) }
        confirmVerified(actorClient)
    }

    @Test
    fun `findById should correctly handle exception when retrofit client returns 404`() {
        val actorId = ObjectId.get().toHexString()
        every { actorClient.findById(ObjectId(actorId), token) } answers {
            WebClientResponseException.create(404, "Actor is not registered", EMPTY, ByteArray(0), null).toMono()
        }
        actorService.findById(actorId, token)
            .test()
            .expectSubscription()
            .expectErrorSatisfies {
                assertThat(it).isInstanceOf(OnActorNotFound::class.java)
            }
            .verify()
        verify(exactly = 1) { actorClient.findById(ObjectId(actorId), token) }
        confirmVerified(actorClient)
    }

    @Test
    fun `findById should correctly handle exception when retrofit client returns 500`() {
        val actorId = ObjectId.get().toHexString()
        every { actorClient.findById(ObjectId(actorId), token) } answers {
            WebClientResponseException.create(500, "Internal Server Error", EMPTY, ByteArray(0), null).toMono()
        }
        actorService.findById(actorId, token)
            .test()
            .expectSubscription()
            .expectErrorSatisfies {
                assertThat(it).isInstanceOf(WebClientResponseException::class.java)
            }
            .verify()
        verify(exactly = 1) { actorClient.findById(ObjectId(actorId), token) }
        confirmVerified(actorClient)
    }

    @Test
    fun `findById should not call actorClient when circuit breaker is open`() {
        val fallback = slot<java.util.function.Function<Throwable, Mono<ActorClientApiDto>>>()
        val exception = CallNotPermittedException.createCallNotPermittedException(CircuitBreaker.ofDefaults("actorService"))
        every { circuitBreaker.run(any<Mono<ActorClientApiDto>>(), capture(fallback)) } answers {
            fallback.captured.apply(exception)
        }
        actorService.findById(actor.id, token)
            .test()
            .expectSubscription()
            .expectErrorMatches {
                it is OnActorServiceUnavailable
            }
            .verify()
        verify(inverse = true) { actorClient.findById(any(), any()) }
        confirmVerified(actorClient)
    }
}