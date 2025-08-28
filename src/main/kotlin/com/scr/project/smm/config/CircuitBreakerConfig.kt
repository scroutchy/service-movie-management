package com.scr.project.smm.config

import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CircuitBreakerConfig {

    @Bean
    fun actorServiceCircuitBreaker(factory: ReactiveCircuitBreakerFactory<*, *>): ReactiveCircuitBreaker = factory.create("actorService")
}