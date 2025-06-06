package com.scr.project.smm.domains.movie.messaging.v1

import com.scr.project.commons.cinema.outbox.model.entity.Outbox
import com.scr.project.commons.cinema.outbox.service.IOutboxService
import com.scr.project.smm.config.TopicProperties
import com.scr.project.smm.domains.movie.mapper.toHexString
import com.scr.project.smm.domains.movie.mapper.toRewardedKafkaDto
import com.scr.project.smm.domains.movie.model.entity.Movie
import com.scr.project.srm.RewardedKafkaDto
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
@EnableConfigurationProperties(TopicProperties::class)
class RewardedMessagingV1(private val producer: IOutboxService, private val topicProperties: TopicProperties) {

    private val logger = LoggerFactory.getLogger(RewardedMessagingV1::class.java)

    fun notify(movie: Movie): Mono<Movie> {
        return producer.send(
            Outbox(
                RewardedKafkaDto::class.java.name,
                movie.id.toHexString(),
                movie.toRewardedKafkaDto().toString(),
                topicProperties.movieCreationNotification
            )
        ).thenReturn(movie)
            .doOnSubscribe { logger.debug("Handling process of movie creation notification for movie with id ${movie.id}") }
            .doOnSuccess { logger.info("Notification creation process if any was successfully handled for movie with id ${movie.id}") }
            .doOnError { logger.warn("Failed to handle the process of movie creation notification for movie with id ${movie.id}") }
    }
}