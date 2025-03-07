package com.scr.project.smm.domains.movie.client

import com.scr.project.smm.entrypoint.model.api.retrofit.mistral.input.MistralInputApiDto
import com.scr.project.smm.entrypoint.model.api.retrofit.mistral.output.MistralOutputApiDto
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import reactor.core.publisher.Mono
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

fun interface MistralClient {

    companion object {

        const val COMPLETION_PATH = "chat/completions"
    }

    @Headers("$CONTENT_TYPE: application/json")
    @POST(COMPLETION_PATH)
    fun request(@Body input: MistralInputApiDto): Mono<Response<MistralOutputApiDto>>
}