package com.scr.project.smm.entrypoint.integration.resource

import com.scr.project.smm.AbstractIntegrationTest
import com.scr.project.smm.domains.movie.dao.MovieDao
import com.scr.project.smm.domains.movie.dao.pulpFiction
import com.scr.project.smm.domains.movie.error.MovieExceptionHandler.ErrorResponse
import com.scr.project.smm.domains.movie.model.entity.Movie
import com.scr.project.smm.domains.movie.model.entity.MovieType.Comedy
import com.scr.project.smm.domains.movie.model.entity.MovieType.Drama
import com.scr.project.smm.entrypoint.mapper.toApiDto
import com.scr.project.smm.entrypoint.model.api.MovieApiDto
import com.scr.project.smm.entrypoint.resource.ApiConstants.DEFAULT_PAGE_SIZE
import com.scr.project.smm.entrypoint.resource.ApiConstants.ID_PATH
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
import org.springframework.http.HttpHeaders.CONTENT_RANGE
import org.springframework.http.HttpStatus.CONFLICT
import org.springframework.http.HttpStatus.PARTIAL_CONTENT
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.WebTestClient.ListBodySpec
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
        val movieRequest = MovieApiDto("The Mask", LocalDate.of(1994, 7, 29), Comedy)
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
                    assertThat(synopsis).isEqualTo("\"The Dark Knight\" explores themes of chaos, morality, and the limits of heroism as Batman confronts the Joker, a nihilistic criminal who tests the ethical boundaries of Gotham City.")
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

    @Test
    fun `create should fail when release date is in future`() {
        val movieRequest = MovieApiDto("The Mask", LocalDate.now().plusDays(10), Comedy)
        val initialCount = movieDao.count()
        webTestClient.mutate().baseUrl("http://localhost:$port").build()
            .post()
            .uri(MOVIE_PATH)
            .bodyValue(movieRequest)
            .exchange()
            .expectStatus().isBadRequest
            .expectBody(Exception::class.java)
        assertThat(movieDao.count()).isEqualTo(initialCount)
    }

    @Test
    fun `create should fail when the title of the movie already exists in database`() {
        val movieRequest = MovieApiDto("Pulp Fiction", LocalDate.of(1994, 10, 14), Drama)
        val initialCount = movieDao.count()
        webTestClient.mutate().baseUrl("http://localhost:$port").build()
            .post()
            .uri(MOVIE_PATH)
            .bodyValue(movieRequest)
            .exchange()
            .expectStatus().isEqualTo(CONFLICT)
        assertThat(movieDao.count()).isEqualTo(initialCount)
    }

    @Test
    fun `find should succeed and returns a movie response when id exists`() {
        val movieResponse = movieDao.findAnyBy { it.actors.isEmpty() }!!.toApiDto()
        webTestClient.mutate().baseUrl("http://localhost:$port").build()
            .get()
            .uri("$MOVIE_PATH$ID_PATH", movieResponse.id)
            .exchange()
            .expectStatus().isOk
            .expectBody(MovieApiDto::class.java)
            .consumeWith {
                val body = it.responseBody
                assertThat(body).isNotNull
                with(body!!) {
                    assertThat(id).isEqualTo(movieResponse.id)
                    assertThat(title).isEqualTo(movieResponse.title)
                    assertThat(releaseDate).isEqualTo(movieResponse.releaseDate)
                    assertThat(type).isEqualTo(movieResponse.type)
                    assertThat(actorIds).isEqualTo(movieResponse.actorIds)
                }
            }
    }

    @Test
    fun `find should succeed and returns a movie response when id exists and list of actors is not empty`() {
        val movieResponse = movieDao.findAnyBy { it.actors.isNotEmpty() }!!.toApiDto()
        webTestClient.mutate().baseUrl("http://localhost:$port").build()
            .get()
            .uri("$MOVIE_PATH$ID_PATH", movieResponse.id)
            .exchange()
            .expectStatus().isOk
            .expectBody(MovieApiDto::class.java)
            .consumeWith {
                val body = it.responseBody
                assertThat(body).isNotNull
                with(body!!) {
                    assertThat(id).isEqualTo(movieResponse.id)
                    assertThat(title).isEqualTo(movieResponse.title)
                    assertThat(releaseDate).isEqualTo(movieResponse.releaseDate)
                    assertThat(type).isEqualTo(movieResponse.type)
                    assertThat(actorIds).isEmpty()
                    assertThat(actors).hasSize(movieResponse.actorIds.size)
                    actors.forEach { a ->
                        assertThat(a.id).isIn(movieResponse.actorIds)
                        assertThat(a.fullName).isEqualTo("Lambert Wilson")
                    }
                }
            }
    }

    @Test
    fun `find should return 404 when id does not exist`() {
        webTestClient.mutate().baseUrl("http://localhost:$port").build()
            .get()
            .uri("$MOVIE_PATH$ID_PATH", ObjectId.get().toHexString())
            .exchange()
            .expectStatus().isNotFound
            .expectBody(ErrorResponse::class.java)
    }

    @Test
    fun `list should succeed and return all movies when no date is provided`() {
        webTestClient.mutate().baseUrl("http://localhost:$port").build()
            .get()
            .uri(MOVIE_PATH)
            .exchange()
            .expectStatus().isOk
            .expectBodyList(MovieApiDto::class.java)
            .consumeWith<ListBodySpec<MovieApiDto>> {
                val headers = it.responseHeaders
                assertThat(headers).isNotNull
                assertThat(headers.get(CONTENT_RANGE)).isEqualTo(listOf("movies 0-1/2"))
                val body = it.responseBody
                assertThat(body).isNotNull
                assertThat(body).hasSize(movieDao.count().toInt())
                body!!.forEach { movieDto ->
                    val movie = movieDao.findAnyBy { it.id!!.toHexString() == movieDto.id }
                    assertThat(movie).isNotNull
                    with(movie!!) {
                        assertThat(movieDto.id).isEqualTo(id!!.toHexString())
                        assertThat(movieDto.title).isEqualTo(title)
                        assertThat(movieDto.releaseDate).isEqualTo(releaseDate)
                        assertThat(movieDto.type).isEqualTo(type)
                        assertThat(movieDto.synopsis).isNull()
                        assertThat(movieDto.actors).isEmpty()
                        assertThat(movieDto.actorIds).isEmpty()
                    }
                }
            }
    }

    @Test
    fun `list should succeed and return empty list when no movie matches the dates`() {
        val startDate = LocalDate.now().minusYears(500)
        val endDate = LocalDate.now().minusYears(400)
        webTestClient.mutate().baseUrl("http://localhost:$port").build()
            .get()
            .uri("$MOVIE_PATH?startDate=$startDate&&endDate=$endDate")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(MovieApiDto::class.java)
            .consumeWith<ListBodySpec<MovieApiDto>> {
                val headers = it.responseHeaders
                assertThat(headers).isNotNull
                assertThat(headers.get(CONTENT_RANGE)).isEqualTo(listOf("movies */0"))
                val body = it.responseBody
                assertThat(body).isNotNull
                assertThat(body).isEmpty()
            }
    }

    @Test
    fun `list should return a subset of movies when total result size exceeds page size`() {
        movieDao.deleteAll()
        val movies = generateListOfMovies(15)
        movieDao.insertAll(movies)
        webTestClient.mutate().baseUrl("http://localhost:$port").build()
            .get()
            .uri(MOVIE_PATH)
            .exchange()
            .expectStatus().isEqualTo(PARTIAL_CONTENT)
            .expectBodyList(MovieApiDto::class.java)
            .consumeWith<ListBodySpec<MovieApiDto>> {
                val headers = it.responseHeaders
                assertThat(headers).isNotNull
                assertThat(headers.get(CONTENT_RANGE)).isEqualTo(listOf("movies 0-9/10"))
                val body = it.responseBody
                assertThat(body).isNotNull
                assertThat(body).hasSize(DEFAULT_PAGE_SIZE)
                body!!.forEach { movieDto ->
                    val movie = movieDao.findAnyBy { it.id!!.toHexString() == movieDto.id }
                    assertThat(movie).isNotNull
                    with(movie!!) {
                        assertThat(movieDto.id).isEqualTo(id!!.toHexString())
                        assertThat(movieDto.title).isEqualTo(title)
                        assertThat(movieDto.releaseDate).isEqualTo(releaseDate)
                        assertThat(movieDto.type).isEqualTo(type)
                        assertThat(movieDto.synopsis).isNull()
                        assertThat(movieDto.actors).isEmpty()
                        assertThat(movieDto.actorIds).isEmpty()
                    }
                }
            }
    }

    private fun generateListOfMovies(size: Int): List<Movie> {
        return List(size) {
            pulpFiction().copy(id = ObjectId.get(), title = generateRandomString())
        }
    }

    private fun generateRandomString(): String {
        val allowedChars = ('A'..'Z') + ('a'..'z')
        return (1..5)
            .map { allowedChars.random() }
            .joinToString("")
    }
}