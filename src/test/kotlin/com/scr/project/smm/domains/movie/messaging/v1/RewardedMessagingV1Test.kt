package com.scr.project.smm.domains.movie.messaging.v1

import com.scr.project.commons.cinema.outbox.model.entity.Outbox
import com.scr.project.commons.cinema.outbox.service.OutboxService
import com.scr.project.smm.config.TopicProperties
import com.scr.project.smm.domains.movie.model.entity.Movie
import com.scr.project.smm.domains.movie.model.entity.MovieType.Thriller
import io.mockk.clearMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import reactor.kotlin.core.publisher.toMono
import reactor.kotlin.test.test
import java.time.LocalDate

class RewardedMessagingV1Test {

    private val producer = mockk<OutboxService>()
    private val topicProperties = TopicProperties()
    private val rewardedMessagingV1 = RewardedMessagingV1(producer, topicProperties)

    @BeforeEach
    fun setUp() {
        clearMocks(producer)
    }

    @Test
    fun `notify should succeed`() {
        val movie = Movie("title", LocalDate.now(), Thriller, "Synopsis", id = ObjectId.get())
        every { producer.send(any<Outbox>()) } answers { firstArg<Outbox>().toMono() }
        rewardedMessagingV1.notify(movie)
            .test()
            .expectSubscription()
            .consumeNextWith {
                assertThat(it).isEqualTo(movie)
            }
            .verifyComplete()
        verify(exactly = 1) { producer.send(any()) }
        confirmVerified(producer)
    }
}