package com.scr.project.smm.domains.movie.client

import com.scr.project.smm.entrypoint.model.api.retrofit.ActorClientApiDto
import com.scr.project.smm.entrypoint.resource.ApiConstants.ACTOR_PATH
import com.scr.project.smm.entrypoint.resource.ApiConstants.ID
import com.scr.project.smm.entrypoint.resource.ApiConstants.ID_PATH
import org.bson.types.ObjectId
import reactor.core.publisher.Mono
import retrofit2.http.GET
import retrofit2.http.Path

fun interface ActorClient {

    @GET(ACTOR_PATH + ID_PATH)
    fun findById(@Path(ID) id: ObjectId): Mono<ActorClientApiDto>
}