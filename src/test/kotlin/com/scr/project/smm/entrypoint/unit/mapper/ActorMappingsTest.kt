package com.scr.project.smm.entrypoint.unit.mapper

import com.scr.project.smm.domains.movie.model.business.Actor
import com.scr.project.smm.entrypoint.mapper.toActor
import com.scr.project.smm.entrypoint.mapper.toApiDto
import com.scr.project.smm.entrypoint.model.api.retrofit.ActorClientApiDto
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test

class ActorMappingsTest {

    @Test
    fun `toActor should succeed`() {
        val actorClientApiDto = ActorClientApiDto(ObjectId.get().toHexString(), "surname", "name")
        val actor = actorClientApiDto.toActor()
        assertThat(actor.id).isEqualTo(actorClientApiDto.id)
        assertThat(actor.fullName).isEqualTo(actorClientApiDto.name + " " + actorClientApiDto.surname)
    }

    @Test
    fun `toApiDto should succeed`() {
        val actor = Actor("id", "fullName")
        val actorApiDto = actor.toApiDto()
        assertThat(actorApiDto.id).isEqualTo(actor.id)
        assertThat(actorApiDto.fullName).isEqualTo(actorApiDto.fullName)
    }
}