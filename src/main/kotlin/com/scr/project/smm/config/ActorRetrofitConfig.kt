package com.scr.project.smm.config

import com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.jakewharton.retrofit2.adapter.reactor.ReactorCallAdapterFactory
import com.scr.project.smm.domains.movie.client.ActorClient
import okhttp3.OkHttpClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory

@Configuration
class ActorRetrofitConfig {

    @Bean
    fun actorRetrofit(@Value("\${actor.service.url}") actorServiceUrl: String): Retrofit {
        val objectMapper = ObjectMapper().registerKotlinModule().configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
        val client = OkHttpClient.Builder()
            .addNetworkInterceptor(LoggingInterceptor())
            .build()

        return Retrofit.Builder()
            .baseUrl(actorServiceUrl)
            .client(client)
            .addCallAdapterFactory(ReactorCallAdapterFactory.create())
            .addConverterFactory(JacksonConverterFactory.create(objectMapper))
            .build()
    }

    @Bean
    fun actorClient(actorRetrofit: Retrofit): ActorClient {
        return actorRetrofit.create(ActorClient::class.java)
    }
}