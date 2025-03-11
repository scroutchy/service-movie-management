package com.scr.project.smm.domains.movie.service

import com.scr.project.smm.domains.movie.client.MistralClient
import com.scr.project.smm.entrypoint.model.api.retrofit.mistral.input.MessageInputApiDto
import com.scr.project.smm.entrypoint.model.api.retrofit.mistral.input.MistralInputApiDto
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import retrofit2.HttpException
import java.io.IOException

@Service
class SynopsisService(private val mistralClient: MistralClient) {

    companion object {

        private const val CONTEXT = "Answer in an academic way and no formatting in answer"
        private const val REQUEST = "Provide me with a short summary in one sentence of synopsis of %s"
    }

    private val logger: Logger = LoggerFactory.getLogger(SynopsisService::class.java)

    fun requestSynopsis(title: String): Mono<String> {
        val input = MistralInputApiDto(
            listOf(
                MessageInputApiDto("system", CONTEXT),
                MessageInputApiDto("user", REQUEST.format(title))
            ),
        )
        return mistralClient.request(input)
            .flatMap { r ->
                if (r.isSuccessful) {
                    r.body()?.choices?.firstOrNull()?.message?.content?.toMono()
                        ?: Mono.error(RuntimeException("Synopsis content is null"))
                } else {
                    val errorBody = r.errorBody()?.string()
                    val errorMessage = "HTTP error: ${r.code()} - ${r.message()} - Body : $errorBody"
                    logger.warn(errorMessage)
                    Mono.error(RuntimeException(errorMessage))
                }
            }
            .doOnError { error -> logger.warn("Error while getting synopsis of $title", error) }
            .doOnSuccess { logger.info("Successfully got synopsis of $title") }
            .onErrorResume { throwable ->
                when (throwable) {
                    is IOException -> Mono.error(RuntimeException("Network error occurred", throwable))
                    is HttpException -> Mono.error(RuntimeException("HTTP error occurred", throwable))
                    else -> Mono.error(RuntimeException("Unexpected error occurred", throwable))
                }
            }
    }
}