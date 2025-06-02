package com.scr.project.smm.entrypoint.integration.resource

import com.scr.project.commons.cinema.test.awaitUntil
import com.scr.project.smm.AbstractIntegrationTest
import com.scr.project.smm.RewardedKafkaTestConsumer
import com.scr.project.smm.TestKafkaConfig
import com.scr.project.smm.domains.movie.dao.MovieDao
import com.scr.project.smm.domains.movie.model.entity.MovieType.Comedy
import com.scr.project.smm.entrypoint.model.api.MovieApiDto
import com.scr.project.smm.entrypoint.resource.ApiConstants.MOVIE_PATH
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.test.web.reactive.server.WebTestClient
import java.time.LocalDate

@SpringBootTest(webEnvironment = RANDOM_PORT, properties = ["spring.kafka.enabled=false"])
@AutoConfigureWebTestClient
@AutoConfigureRestDocs
@Import(TestKafkaConfig::class)
class MovieResourceNoKafkaIntegrationTest(
    @Autowired private val webTestClient: WebTestClient,
    @Autowired private val movieDao: MovieDao,
    @Autowired private val kafkaRewardedConsumer: RewardedKafkaTestConsumer,
) : AbstractIntegrationTest() {

    @LocalServerPort
    private var port: Int = 0

    @BeforeEach
    fun setUp() {
        movieDao.initTestData()
        kafkaRewardedConsumer.clearTopic()
    }

    @Test
    fun `create should succeed and create a movie in database and not send kafka notification`() {
        val movieRequest = MovieApiDto("The Mask", LocalDate.of(1994, 7, 29), Comedy)
        val initialCount = movieDao.count()
        webTestClient.mutate().baseUrl("http://localhost:$port")
            .build()
            .post()
            .uri(MOVIE_PATH)
            .header(AUTHORIZATION, "Bearer ${testJwtUtil.writeToken}")
            .bodyValue(movieRequest)
            .exchange()
            .expectStatus().isCreated
            .expectBody(MovieApiDto::class.java)
            .consumeWith {
                val body = it.responseBody as MovieApiDto
                with(body) {
                    assertThat(title).isEqualTo(movieRequest.title)
                    assertThat(releaseDate).isEqualTo(movieRequest.releaseDate)
                    assertThat(type).isEqualTo(movieRequest.type)
                    assertThat(synopsis).isEqualTo("\"The Dark Knight\" explores themes of chaos, morality, and the limits of heroism as Batman confronts the Joker, a nihilistic criminal who tests the ethical boundaries of Gotham City.")
                    assertThat(id).isNotNull
                }
                assertThat(movieDao.count()).isEqualTo(initialCount + 1)
                val movie = movieDao.findById(ObjectId(body.id!!))
                with(movie!!) {
                    assertThat(id).isEqualTo(ObjectId(body.id))
                    assertThat(title).isEqualTo(body.title)
                    assertThat(releaseDate).isEqualTo(body.releaseDate)
                    assertThat(type).isEqualTo(body.type)
                }
                awaitUntil {
                    val messages = kafkaRewardedConsumer.poll()
                    assertThat(messages).hasSize(0)
                }
            }
    }
}