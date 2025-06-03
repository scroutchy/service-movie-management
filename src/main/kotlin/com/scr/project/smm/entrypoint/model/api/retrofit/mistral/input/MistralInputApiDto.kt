package com.scr.project.smm.entrypoint.model.api.retrofit.mistral.input

import com.fasterxml.jackson.annotation.JsonProperty
import com.scr.project.commons.cinema.model.api.DTO

data class MistralInputApiDto(
    val messages: List<MessageInputApiDto>,
    val model: String = "mistral-medium-latest",
    @JsonProperty("max_tokens")
    val maxTokens: Int = 128
) : DTO