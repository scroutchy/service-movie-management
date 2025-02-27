package com.scr.project.smm.domains.movie.service

import com.scr.project.smm.domains.movie.client.ActorClient
import com.scr.project.smm.domains.movie.error.MovieErrors.OnActorNotFound
import com.scr.project.smm.entrypoint.model.api.retrofit.ActorClientApiDto
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders.EMPTY
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.kotlin.core.publisher.toMono
import reactor.kotlin.test.test

class ActorServiceTest {

    private val actorClient = mockk<ActorClient>()
    private val actorService = ActorService(actorClient)
    private val actor = ActorClientApiDto(ObjectId.get().toHexString(), "surname", "name")

    @Test
    fun `findById should succeed`() {
        every { actorClient.findById(ObjectId(actor.id)) } answers { actor.toMono() }
        actorService.findById(actor.id)
            .test()
            .expectSubscription()
            .consumeNextWith {
                assertThat(it.id).isEqualTo(actor.id)
                assertThat(it.fullName).isEqualTo("${actor.name} ${actor.surname}")
            }.verifyComplete()
        verify(exactly = 1) { actorClient.findById(ObjectId(actor.id)) }
        confirmVerified(actorClient)
    }

    @Test
    fun `findById should correctly handle exception when retrofit client returns 404`() {
        val actorId = ObjectId.get().toHexString()
        every { actorClient.findById(ObjectId(actorId)) } answers {
            WebClientResponseException.create(404, "Actor is not registered", EMPTY, ByteArray(0), null).toMono()
        }
        actorService.findById(actorId)
            .test()
            .expectSubscription()
            .expectErrorSatisfies {
                assertThat(it).isInstanceOf(OnActorNotFound::class.java)
            }
            .verify()
        verify(exactly = 1) { actorClient.findById(ObjectId(actorId)) }
        confirmVerified(actorClient)
    }

    @Test
    fun `findById should correctly handle exception when retrofit client returns 500`() {
        val actorId = ObjectId.get().toHexString()
        every { actorClient.findById(ObjectId(actorId)) } answers {
            WebClientResponseException.create(500, "Internal Server Error", EMPTY, ByteArray(0), null).toMono()
        }
        actorService.findById(actorId)
            .test()
            .expectSubscription()
            .expectErrorSatisfies {
                assertThat(it).isInstanceOf(WebClientResponseException::class.java)
            }
            .verify()
        verify(exactly = 1) { actorClient.findById(ObjectId(actorId)) }
        confirmVerified(actorClient)
    }
}