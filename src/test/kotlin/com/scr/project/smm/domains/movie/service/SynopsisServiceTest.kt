package com.scr.project.smm.domains.movie.service

import com.scr.project.smm.domains.movie.client.MistralClient
import com.scr.project.smm.domains.movie.config.MovieConstants.NO_SUMMARY_FOUND
import com.scr.project.smm.domains.movie.error.MovieErrors.OnSummaryNotFound
import com.scr.project.smm.entrypoint.model.api.retrofit.mistral.input.MistralInputApiDto
import com.scr.project.smm.entrypoint.model.api.retrofit.mistral.output.ChoiceApiDto
import com.scr.project.smm.entrypoint.model.api.retrofit.mistral.output.MessageOutputApiDto
import com.scr.project.smm.entrypoint.model.api.retrofit.mistral.output.MistralOutputApiDto
import io.mockk.clearMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.IOException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import reactor.kotlin.test.test
import retrofit2.Response
import java.time.LocalDate

class SynopsisServiceTest {

    private val synopsisClient = mockk<MistralClient>()
    private val synopsisService = SynopsisService(synopsisClient)
    private val synopsis = "This is the AI-generated scenario."

    @BeforeEach
    internal fun setUp() {
        clearMocks(synopsisClient)
    }

    @Test
    fun `requestSynopsis should return a synopsis`() {
        every { synopsisClient.request(any<MistralInputApiDto>()) } answers {
            Response.success(MistralOutputApiDto(listOf(ChoiceApiDto(MessageOutputApiDto(synopsis))))).toMono()
        }
        synopsisService.requestSynopsis("title", LocalDate.of(2023, 1, 1))
            .test()
            .expectSubscription()
            .consumeNextWith {
                assertThat(it).isNotNull
                assertThat(it).isEqualTo(synopsis)
            }.verifyComplete()
        verify(exactly = 1) { synopsisClient.request(any<MistralInputApiDto>()) }
        confirmVerified(synopsisClient)
    }

    @Test
    fun `requestSynopsis should handle client error`() {
        every { synopsisClient.request(any<MistralInputApiDto>()) } answers {
            Response.error<MistralOutputApiDto>(400, "Bad Request".toResponseBody()).toMono()
        }
        synopsisService.requestSynopsis("title", LocalDate.of(2023, 1, 1))
            .test()
            .expectSubscription()
            .expectErrorMatches { it is RuntimeException }
            .verify()
        verify(exactly = 1) { synopsisClient.request(any<MistralInputApiDto>()) }
        confirmVerified(synopsisClient)
    }

    @Test
    fun `requestSynopsis should handle network error`() {
        every { synopsisClient.request(any<MistralInputApiDto>()) } answers {
            Mono.error(IOException("Network issue"))
        }
        synopsisService.requestSynopsis("title", LocalDate.of(2023, 1, 1))
            .test()
            .expectSubscription()
            .expectErrorMatches { it is RuntimeException && it.cause is IOException }
            .verify()
        verify(exactly = 1) { synopsisClient.request(any<MistralInputApiDto>()) }
        confirmVerified(synopsisClient)
    }

    @Test
    fun `requestSynopsis should handle empty response`() {
        every { synopsisClient.request(any<MistralInputApiDto>()) } answers {
            Response.success(MistralOutputApiDto(emptyList())).toMono()
        }
        synopsisService.requestSynopsis("title", LocalDate.of(2023, 1, 1))
            .test()
            .expectSubscription()
            .expectErrorMatches { it is RuntimeException }
            .verify()
        verify(exactly = 1) { synopsisClient.request(any<MistralInputApiDto>()) }
        confirmVerified(synopsisClient)
    }

    @Test
    fun `requestSynopsis should handle summary not found`() {
        every { synopsisClient.request(any<MistralInputApiDto>()) } answers {
            Response.success(MistralOutputApiDto(listOf(ChoiceApiDto(MessageOutputApiDto(NO_SUMMARY_FOUND))))).toMono()
        }
        synopsisService.requestSynopsis("title", LocalDate.of(2023, 1, 1))
            .test()
            .expectSubscription()
            .expectErrorMatches { it is RuntimeException && it.cause is OnSummaryNotFound }
            .verify()
        verify(exactly = 1) { synopsisClient.request(any<MistralInputApiDto>()) }
        confirmVerified(synopsisClient)
    }
}
