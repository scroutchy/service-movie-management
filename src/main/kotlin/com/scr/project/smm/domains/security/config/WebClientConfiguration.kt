package com.scr.project.smm.domains.security.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebClientConfiguration {

    @Bean
    fun keycloakWebClient(): WebClient {
        return WebClient.builder().defaultHeader(CONTENT_TYPE, APPLICATION_FORM_URLENCODED_VALUE).build()
    }
}