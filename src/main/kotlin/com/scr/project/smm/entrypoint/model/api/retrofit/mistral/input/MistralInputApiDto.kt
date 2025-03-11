package com.scr.project.smm.entrypoint.model.api.retrofit.mistral.input

import com.fasterxml.jackson.annotation.JsonProperty

data class MistralInputApiDto(
    val messages: List<MessageInputApiDto>,
    val model: String = "mistral-large-latest",
    @JsonProperty("max_tokens")
    val maxTokens: Int = 128
)