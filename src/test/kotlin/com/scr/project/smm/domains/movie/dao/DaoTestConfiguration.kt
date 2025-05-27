package com.scr.project.smm.domains.movie.dao

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
internal class DaoTestConfiguration {

    @Bean
    fun movieDao(@Value("\${spring.data.mongodb.uri}") mongoUri: String) = MovieDao(mongoUri)

    @Bean
    fun outboxDao(@Value("\${spring.data.mongodb.uri}") mongoUri: String) = OutboxDao(mongoUri)
}