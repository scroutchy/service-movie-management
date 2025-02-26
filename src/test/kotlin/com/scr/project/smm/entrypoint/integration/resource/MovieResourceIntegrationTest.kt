package com.scr.project.smm.entrypoint.integration.resource

import com.scr.project.smm.AbstractIntegrationTest
import com.scr.project.smm.domains.movie.dao.MovieDao
import com.scr.project.smm.entrypoint.model.api.MovieApiDto
import com.scr.project.smm.entrypoint.resource.ApiConstants.MOVIE_PATH
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.web.reactive.server.WebTestClient
import java.time.LocalDate

@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureWebTestClient
internal class MovieResourceIntegrationTest(
    @Autowired private val webTestClient: WebTestClient,
    @Autowired private val movieDao: MovieDao,
) : AbstractIntegrationTest() {

    @LocalServerPort
    private var port: Int = 0

    @BeforeEach
    fun setUp() {
        movieDao.initTestData()
    }

    @Test
    fun `create should succeed and create a movie in database`() {
        val movieRequest = MovieApiDto("The Mask", LocalDate.of(1994, 7, 29), "Comedy")
        val initialCount = movieDao.count()
        webTestClient.mutate().baseUrl("http://localhost:$port").build()
            .post()
            .uri(MOVIE_PATH)
            .bodyValue(movieRequest)
            .exchange()
            .expectStatus().isCreated
            .expectBody(MovieApiDto::class.java)
            .consumeWith {
                val body = it.responseBody
                with(body!!) {
                    assertThat(title).isEqualTo(movieRequest.title)
                    assertThat(releaseDate).isEqualTo(movieRequest.releaseDate)
                    assertThat(type).isEqualTo(movieRequest.type)
                    assertThat(id).isNotNull
                }
                assertThat(movieDao.count()).isEqualTo(initialCount + 1)
                val actor = movieDao.findById(ObjectId(body.id!!))
                with(actor!!) {
                    assertThat(id).isEqualTo(ObjectId(body.id))
                    assertThat(title).isEqualTo(body.title)
                    assertThat(releaseDate).isEqualTo(body.releaseDate)
                    assertThat(type).isEqualTo(body.type)
                }
            }
    }
}