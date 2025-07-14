package com.scr.project.smm.domains.security.service

import com.scr.project.smm.domains.security.error.KeycloakErrors.OnKeycloakError
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.util.Base64

@Service
class KeycloakService(
    private val keycloakWebClient: WebClient,
    @Value("\${keycloak.auth-server-url}") private val authServerUrl: String,
    @Value("\${keycloak.realm}") private val realm: String,
    @Value("\${keycloak.client-id}") private val clientId: String,
    @Value("\${keycloak.client-secret}") private val clientSecret: String
) {

    fun getToken(): Mono<String> {
        val clientCredentials = Base64.getEncoder().encodeToString("$clientId:$clientSecret".toByteArray())
        return keycloakWebClient.post()
            .uri("$authServerUrl/realms/$realm/protocol/openid-connect/token")
            .header(AUTHORIZATION, "Basic $clientCredentials")
            .bodyValue(LinkedMultiValueMap<String, String>().apply {
                add("grant_type", "client_credentials")
            })
            .retrieve()
            .onStatus({ it.isError }) {
                it.bodyToMono(String::class.java).map { b -> OnKeycloakError("Keycloak error: ${it.statusCode()} - $b") }
            }
            .bodyToMono(Map::class.java)
            .map { response -> response["access_token"] as String }
            .cache()
    }
}