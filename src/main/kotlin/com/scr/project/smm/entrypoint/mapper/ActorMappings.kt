package com.scr.project.smm.entrypoint.mapper

import com.scr.project.smm.domains.movie.model.business.Actor
import com.scr.project.smm.entrypoint.model.api.ActorApiDto
import com.scr.project.smm.entrypoint.model.api.retrofit.ActorClientApiDto

fun ActorClientApiDto.toActor() = Actor(id, "$name $surname")

fun Actor.toApiDto() = ActorApiDto(id, fullName)