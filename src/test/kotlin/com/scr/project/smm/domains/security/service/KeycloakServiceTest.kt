package com.scr.project.smm.domains.security.service

import com.scr.project.smm.domains.security.error.KeycloakErrors.OnKeycloakError
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClient.RequestBodyUriSpec
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec
import reactor.core.publisher.Mono
import reactor.kotlin.test.test

class KeycloakServiceTest {

    private val webClient = mockk<WebClient>()
    private val requestBodyUriSpec = mockk<RequestBodyUriSpec>()
    private val requestHeadersSpec = mockk<RequestHeadersSpec<*>>()
    private val responseSpec = mockk<ResponseSpec>()
    private val service = KeycloakService(
        webClient,
        "http://auth-server",
        "realm",
        "clientId",
        "clientSecret"
    )

    @Test
    fun `getToken should return token when valid response`() {
        every { webClient.post() } returns requestBodyUriSpec
        every { requestBodyUriSpec.uri(any<String>()) } returns requestBodyUriSpec
        every { requestBodyUriSpec.header(AUTHORIZATION, any()) } returns requestBodyUriSpec
        every { requestBodyUriSpec.bodyValue(any()) } returns requestHeadersSpec
        every { requestHeadersSpec.retrieve() } returns responseSpec
        every { responseSpec.onStatus(any(), any()) } returns responseSpec
        every { responseSpec.bodyToMono(Map::class.java) } returns Mono.just(mapOf("access_token" to "token123"))

        service.getToken()
            .test()
            .expectSubscription()
            .consumeNextWith {
                assertThat(it).isEqualTo("token123")
            }
            .verifyComplete()

        verify(exactly = 1) { webClient.post() }
        confirmVerified(webClient)
    }

    @Test
    fun `getToken should throw exception when error`() {
        every { webClient.post() } returns requestBodyUriSpec
        every { requestBodyUriSpec.uri(any<String>()) } returns requestBodyUriSpec
        every { requestBodyUriSpec.header(AUTHORIZATION, any()) } returns requestBodyUriSpec
        every { requestBodyUriSpec.bodyValue(any()) } returns requestHeadersSpec
        every { requestHeadersSpec.retrieve() } returns responseSpec
        every { responseSpec.onStatus(any(), any()) } returns responseSpec
        every { responseSpec.bodyToMono(Map::class.java) } returns Mono.error(OnKeycloakError("Keycloak error: 401 - Unauthorized"))

        service.getToken()
            .test()
            .expectErrorMatches { it.message?.contains("Keycloak error") == true }
            .verify()

        verify(exactly = 1) { webClient.post() }
        confirmVerified(webClient)
    }

    @Test
    fun `getToken should use cache and only http call once`() {
        every { webClient.post() } returns requestBodyUriSpec
        every { requestBodyUriSpec.uri(any<String>()) } returns requestBodyUriSpec
        every { requestBodyUriSpec.header(AUTHORIZATION, any()) } returns requestBodyUriSpec
        every { requestBodyUriSpec.bodyValue(any()) } returns requestHeadersSpec
        every { requestHeadersSpec.retrieve() } returns responseSpec
        every { responseSpec.onStatus(any(), any()) } returns responseSpec
        every { responseSpec.bodyToMono(Map::class.java) } returns Mono.just(mapOf("access_token" to "token123"))
        val tokenMono = service.getToken()
        tokenMono.test().expectNext("token123").verifyComplete()
        tokenMono.test().expectNext("token123").verifyComplete()

        verify(exactly = 1) { webClient.post() }
        confirmVerified(webClient)
    }
}