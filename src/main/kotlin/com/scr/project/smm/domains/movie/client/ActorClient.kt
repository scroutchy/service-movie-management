package com.scr.project.smm.domains.movie.client

import com.scr.project.smm.entrypoint.model.api.retrofit.ActorClientApiDto
import com.scr.project.smm.entrypoint.resource.ApiConstants.ACTOR_PATH
import com.scr.project.smm.entrypoint.resource.ApiConstants.ID
import com.scr.project.smm.entrypoint.resource.ApiConstants.ID_PATH
import org.bson.types.ObjectId
import org.springframework.http.HttpHeaders.AUTHORIZATION
import reactor.core.publisher.Mono
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

fun interface ActorClient {

    @GET(ACTOR_PATH + ID_PATH)
    fun findById(@Path(ID) id: ObjectId, @Header(AUTHORIZATION) token: String): Mono<Response<ActorClientApiDto>>
}