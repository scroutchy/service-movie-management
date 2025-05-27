package com.scr.project.smm.config

import com.scr.project.smm.config.TopicProperties.Companion.PREFIX
import jakarta.validation.constraints.NotBlank
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

@Validated
@ConfigurationProperties(PREFIX)
data class TopicProperties(
    @field:NotBlank
    val movieCreationNotification: String = "srm-rewarded-entity-creation-events"
) {

    companion object {

        const val PREFIX = "messaging.topics"
    }
}