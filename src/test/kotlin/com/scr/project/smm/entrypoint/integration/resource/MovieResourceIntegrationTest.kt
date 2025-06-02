package com.scr.project.smm.entrypoint.integration.resource

import com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName
import com.epages.restdocs.apispec.ResourceDocumentation.resource
import com.epages.restdocs.apispec.ResourceSnippetParameters
import com.epages.restdocs.apispec.WebTestClientRestDocumentationWrapper
import com.scr.project.commons.cinema.test.awaitUntil
import com.scr.project.smm.AbstractIntegrationTest
import com.scr.project.smm.RewardedKafkaTestConsumer
import com.scr.project.smm.TestKafkaConfig
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
import com.scr.project.srm.RewardedEntityTypeKafkaDto.MOVIE
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
import org.springframework.http.HttpHeaders.CONTENT_RANGE
import org.springframework.http.HttpStatus.CONFLICT
import org.springframework.http.HttpStatus.PARTIAL_CONTENT
import org.springframework.restdocs.RestDocumentationContextProvider
import org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse
import org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.documentationConfiguration
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.WebTestClient.ListBodySpec
import java.time.LocalDate

@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureWebTestClient
@AutoConfigureRestDocs
@Import(TestKafkaConfig::class)
internal class MovieResourceIntegrationTest(
    @Autowired private val webTestClient: WebTestClient,
    @Autowired private val movieDao: MovieDao,
    @Autowired private val restDocumentation: RestDocumentationContextProvider,
    @Autowired private val kafkaRewardedConsumer: RewardedKafkaTestConsumer,
) : AbstractIntegrationTest() {

    @LocalServerPort
    private var port: Int = 0

    @BeforeEach
    fun setUp() {
        movieDao.initTestData()
    }

    private companion object {

        const val MOVIE_TAG = "Movies"
        const val GET_SUMMARY = "Find movie by id"
        const val POST_SUMMARY = "Create a movie"
        const val LIST_SUMMARY = "List movies"
    }

    @Test
    fun `create should succeed and create a movie in database and send kafka notification`() {
        val movieRequest = MovieApiDto("The Mask", LocalDate.of(1994, 7, 29), Comedy)
        val initialCount = movieDao.count()
        webTestClient.mutate().baseUrl("http://localhost:$port")
            .filter(documentationConfiguration(restDocumentation))
            .build()
            .post()
            .uri(MOVIE_PATH)
            .header(AUTHORIZATION, "Bearer ${testJwtUtil.writeToken}")
            .bodyValue(movieRequest)
            .exchange()
            .expectStatus().isCreated
            .expectBody(MovieApiDto::class.java)
            .consumeWith(
                WebTestClientRestDocumentationWrapper.document(
                    "movie-create",
                    preprocessResponse(prettyPrint()),
                    resource(
                        ResourceSnippetParameters.builder()
                            .tag(MOVIE_TAG)
                            .summary(POST_SUMMARY)
                            .description("Create a new movie based on its characteristics")
                            .requestFields(
                                fieldWithPath("title").description("Title of the movie"),
                                fieldWithPath("releaseDate").description("Release date of the movie"),
                                fieldWithPath("type").description(
                                    "Type of the movie. Possible values are: Drama,\n" +
                                            "    Comedy,\n" +
                                            "    ScienceFiction,\n" +
                                            "    Fantasy,\n" +
                                            "    Horror,\n" +
                                            "    Thriller,\n" +
                                            "    Western,\n" +
                                            "    Musical,"
                                ),
                                fieldWithPath("actorIds").description("Identifiers of actors").type("ARRAY").optional(),
                                fieldWithPath("actors").description("Actors of the movie").type("ARRAY").ignored(),
                            )
                            .responseFields(
                                fieldWithPath("id").description("The unique identifier of the movie"),
                                fieldWithPath("title").description("Title of the movie"),
                                fieldWithPath("releaseDate").description("Release date of the movie"),
                                fieldWithPath("type").description(
                                    "Type of the movie. Possible values are: Drama,\n" +
                                            "    Comedy,\n" +
                                            "    ScienceFiction,\n" +
                                            "    Fantasy,\n" +
                                            "    Horror,\n" +
                                            "    Thriller,\n" +
                                            "    Western,\n" +
                                            "    Musical,"
                                ),
                                fieldWithPath("synopsis").description("Synopsis of the movie"),
                                fieldWithPath("actors").description("Actors of the movie").type("ARRAY"),
                            ).build()
                    )
                )
            )
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
                    assertThat(messages).hasSize(1)
                    with(messages.first()) {
                        assertThat(id).isEqualTo(body.id)
                        assertThat(type).isEqualTo(MOVIE)
                    }
                }
            }
    }

    @Test
    fun `create should succeed and create a movie in database with actors in it`() {
        val movieRequest = MovieApiDto("The Mask", LocalDate.of(1994, 7, 29), Comedy, listOf(ObjectId.get().toHexString()))
        val initialCount = movieDao.count()
        webTestClient.mutate().baseUrl("http://localhost:$port")
            .filter(documentationConfiguration(restDocumentation))
            .build()
            .post()
            .uri(MOVIE_PATH)
            .header(AUTHORIZATION, "Bearer ${testJwtUtil.writeToken}")
            .bodyValue(movieRequest)
            .exchange()
            .expectStatus().isCreated
            .expectBody(MovieApiDto::class.java)
            .consumeWith(
                WebTestClientRestDocumentationWrapper.document(
                    "movie-create-with-actors",
                    preprocessResponse(prettyPrint()),
                    resource(
                        ResourceSnippetParameters.builder()
                            .tag(MOVIE_TAG)
                            .summary(POST_SUMMARY)
                            .description("Create a new movie based on its characteristics")
                            .requestFields(
                                fieldWithPath("title").description("Title of the movie"),
                                fieldWithPath("releaseDate").description("Release date of the movie"),
                                fieldWithPath("type").description(
                                    "Type of the movie. Possible values are: Drama,\n" +
                                            "    Comedy,\n" +
                                            "    ScienceFiction,\n" +
                                            "    Fantasy,\n" +
                                            "    Horror,\n" +
                                            "    Thriller,\n" +
                                            "    Western,\n" +
                                            "    Musical,"
                                ),
                                fieldWithPath("actors").description("Actors of the movie").type("ARRAY").ignored(),
                            )
                            .responseFields(
                                fieldWithPath("id").description("The unique identifier of the movie"),
                                fieldWithPath("title").description("Title of the movie"),
                                fieldWithPath("releaseDate").description("Release date of the movie"),
                                fieldWithPath("type").description(
                                    "Type of the movie. Possible values are: Drama,\n" +
                                            "    Comedy,\n" +
                                            "    ScienceFiction,\n" +
                                            "    Fantasy,\n" +
                                            "    Horror,\n" +
                                            "    Thriller,\n" +
                                            "    Western,\n" +
                                            "    Musical,"
                                ),
                                fieldWithPath("synopsis").description("Synopsis of the movie"),
                                fieldWithPath("actors").description("Actors of the movie").type("ARRAY"),
                            ).build()
                    )
                )
            )
            .consumeWith {
                val body = it.responseBody as MovieApiDto
                with(body) {
                    assertThat(title).isEqualTo(movieRequest.title)
                    assertThat(releaseDate).isEqualTo(movieRequest.releaseDate)
                    assertThat(type).isEqualTo(movieRequest.type)
                    assertThat(actors).isEmpty()
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
            }
    }

    @Test
    fun `create should fail when release date is in future`() {
        val movieRequest = MovieApiDto("The Mask", LocalDate.now().plusDays(10), Comedy)
        val initialCount = movieDao.count()
        webTestClient.mutate().baseUrl("http://localhost:$port")
            .filter(documentationConfiguration(restDocumentation)).build()
            .post()
            .uri(MOVIE_PATH)
            .header(AUTHORIZATION, "Bearer ${testJwtUtil.writeToken}")
            .bodyValue(movieRequest)
            .exchange()
            .expectStatus().isBadRequest
            .expectBody(Exception::class.java)
            .consumeWith(
                WebTestClientRestDocumentationWrapper.document(
                    "create-release-date-in-future",
                    preprocessResponse(prettyPrint()),
                    resource(
                        ResourceSnippetParameters.builder()
                            .tags(MOVIE_TAG)
                            .summary(POST_SUMMARY)
                            .build()
                    )
                )
            )
        assertThat(movieDao.count()).isEqualTo(initialCount)
    }

    @Test
    fun `create should fail and return 500 when synopsis was not generated`() {
        val movieRequest = MovieApiDto("unknown", LocalDate.of(1900, 7, 29), Comedy, listOf(ObjectId.get().toHexString()))
        webTestClient.mutate().baseUrl("http://localhost:$port")
            .filter(documentationConfiguration(restDocumentation))
            .build()
            .post()
            .uri(MOVIE_PATH)
            .header(AUTHORIZATION, "Bearer ${testJwtUtil.writeToken}")
            .bodyValue(movieRequest)
            .exchange()
            .expectStatus().isEqualTo(500)
            .expectBody(Exception::class.java)
            .consumeWith(
                WebTestClientRestDocumentationWrapper.document(
                    "create-no-synopsis-generated",
                    preprocessResponse(prettyPrint()),
                    resource(
                        ResourceSnippetParameters.builder()
                            .tags(MOVIE_TAG)
                            .summary(POST_SUMMARY)
                            .build()
                    )
                )
            )
    }

    @Test
    fun `create should fail when the title of the movie already exists in database`() {
        val movieRequest = MovieApiDto("Pulp Fiction", LocalDate.of(1994, 10, 14), Drama)
        val initialCount = movieDao.count()
        webTestClient.mutate().baseUrl("http://localhost:$port")
            .filter(documentationConfiguration(restDocumentation)).build()
            .post()
            .uri(MOVIE_PATH)
            .header(AUTHORIZATION, "Bearer ${testJwtUtil.writeToken}")
            .bodyValue(movieRequest)
            .exchange()
            .expectStatus().isEqualTo(CONFLICT)
            .expectBody(Exception::class.java)
            .consumeWith(
                WebTestClientRestDocumentationWrapper.document(
                    "create-conflict",
                    preprocessResponse(prettyPrint()),
                    resource(
                        ResourceSnippetParameters.builder()
                            .tags(MOVIE_TAG)
                            .summary(POST_SUMMARY)
                            .build()
                    )
                )
            )
        assertThat(movieDao.count()).isEqualTo(initialCount)
    }

    @Test
    fun `create should return 401 when authentication fails because wrong token`() {
        val movieRequest = MovieApiDto("The Mask", LocalDate.of(1994, 7, 29), Comedy)
        webTestClient.mutate().baseUrl("http://localhost:$port").build()
            .post()
            .uri(MOVIE_PATH)
            .header(AUTHORIZATION, "Bearer dummyToken")
            .bodyValue(movieRequest)
            .exchange()
            .expectStatus().isUnauthorized
            .expectBody(ErrorResponse::class.java)
            .consumeWith(
                WebTestClientRestDocumentationWrapper.document(
                    "create-unauthorized",
                    preprocessResponse(prettyPrint()),
                    resource(
                        ResourceSnippetParameters.builder()
                            .tags(MOVIE_TAG)
                            .summary(POST_SUMMARY)
                            .build()
                    )
                )
            )
    }

    @Test
    fun `create should return 403 when authorization fails because no matching role`() {
        val movieRequest = MovieApiDto("The Mask", LocalDate.of(1994, 7, 29), Comedy)
        webTestClient.mutate().baseUrl("http://localhost:$port").build()
            .post()
            .uri(MOVIE_PATH)
            .header(AUTHORIZATION, "Bearer ${testJwtUtil.standardToken}")
            .bodyValue(movieRequest)
            .exchange()
            .expectStatus().isForbidden
            .expectBody(ErrorResponse::class.java)
            .consumeWith(
                WebTestClientRestDocumentationWrapper.document(
                    "create-forbidden",
                    preprocessResponse(prettyPrint()),
                    resource(
                        ResourceSnippetParameters.builder()
                            .tags(MOVIE_TAG)
                            .summary(POST_SUMMARY)
                            .build()
                    )
                )
            )
    }

    @Test
    fun `find should succeed and returns a movie response when id exists`() {
        val movieResponse = movieDao.findAnyBy { it.actors.isEmpty() }!!.toApiDto()
        webTestClient.mutate().baseUrl("http://localhost:$port")
            .filter(documentationConfiguration(restDocumentation))
            .build()
            .get()
            .uri("$MOVIE_PATH$ID_PATH", movieResponse.id)
            .header(AUTHORIZATION, "Bearer ${testJwtUtil.standardToken}")
            .exchange()
            .expectStatus().isOk
            .expectBody(MovieApiDto::class.java)
            .consumeWith(
                WebTestClientRestDocumentationWrapper.document(
                    "movie-find-by-id",
                    preprocessResponse(prettyPrint()),
                    resource(
                        ResourceSnippetParameters.builder()
                            .tag(MOVIE_TAG)
                            .summary(GET_SUMMARY)
                            .description("Retrieve a movie based on its unique identifier")
                            .pathParameters(
                                parameterWithName("id").description("Unique identifier of the movie")
                            )
                            .responseFields(
                                fieldWithPath("id").description("The unique identifier of the movie"),
                                fieldWithPath("title").description("Title of the movie"),
                                fieldWithPath("releaseDate").description("Release date of the movie"),
                                fieldWithPath("type").description(
                                    "Type of the movie. Possible values are: Drama,\n" +
                                            "    Comedy,\n" +
                                            "    ScienceFiction,\n" +
                                            "    Fantasy,\n" +
                                            "    Horror,\n" +
                                            "    Thriller,\n" +
                                            "    Western,\n" +
                                            "    Musical,"
                                ),
                                fieldWithPath("synopsis").description("Synopsis of the movie"),
                                fieldWithPath("actors").description("Actors of the movie").type("ARRAY"),
                            ).build()
                    )
                )
            )
            .consumeWith {
                val body = it.responseBody as MovieApiDto
                assertThat(body).isNotNull
                with(body) {
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
        webTestClient.mutate().baseUrl("http://localhost:$port")
            .filter(documentationConfiguration(restDocumentation))
            .build()
            .get()
            .uri("$MOVIE_PATH$ID_PATH", movieResponse.id)
            .header(AUTHORIZATION, "Bearer ${testJwtUtil.standardToken}")
            .exchange()
            .expectStatus().isOk
            .expectBody(MovieApiDto::class.java)
            .consumeWith(
                WebTestClientRestDocumentationWrapper.document(
                    "movie-find-by-id-with-actors",
                    preprocessResponse(prettyPrint()),
                    resource(
                        ResourceSnippetParameters.builder()
                            .tag(MOVIE_TAG)
                            .summary(GET_SUMMARY)
                            .description("Retrieve a movie based on its unique identifier")
                            .pathParameters(
                                parameterWithName("id").description("Unique identifier of the movie")
                            )
                            .responseFields(
                                fieldWithPath("id").description("The unique identifier of the movie"),
                                fieldWithPath("title").description("Title of the movie"),
                                fieldWithPath("releaseDate").description("Release date of the movie"),
                                fieldWithPath("type").description(
                                    "Type of the movie. Possible values are: Drama,\n" +
                                            "    Comedy,\n" +
                                            "    ScienceFiction,\n" +
                                            "    Fantasy,\n" +
                                            "    Horror,\n" +
                                            "    Thriller,\n" +
                                            "    Western,\n" +
                                            "    Musical,"
                                ),
                                fieldWithPath("synopsis").description("Synopsis of the movie"),
                                fieldWithPath("actors").description("Actors of the movie").type("ARRAY"),
                                fieldWithPath("actors.[].id").description("Unique identifier of the actor"),
                                fieldWithPath("actors.[].fullName").description("Full name of the actor"),
                            ).build()
                    )
                )
            )
            .consumeWith {
                val body = it.responseBody as MovieApiDto
                assertThat(body).isNotNull
                with(body) {
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
        webTestClient.mutate().baseUrl("http://localhost:$port")
            .filter(documentationConfiguration(restDocumentation))
            .build()
            .get()
            .uri("$MOVIE_PATH$ID_PATH", ObjectId.get().toHexString())
            .header(AUTHORIZATION, "Bearer ${testJwtUtil.standardToken}")
            .exchange()
            .expectStatus().isNotFound
            .expectBody(ErrorResponse::class.java)
            .consumeWith(
                WebTestClientRestDocumentationWrapper.document(
                    "find-does-no-exist",
                    preprocessResponse(prettyPrint()),
                    resource(
                        ResourceSnippetParameters.builder()
                            .tags(MOVIE_TAG)
                            .summary(GET_SUMMARY)
                            .build()
                    )
                )
            )
    }

    @Test
    fun `find should return 401 when authentication fails because no token in header`() {
        webTestClient.mutate().baseUrl("http://localhost:$port")
            .filter(documentationConfiguration(restDocumentation))
            .build()
            .get()
            .uri("$MOVIE_PATH$ID_PATH", ObjectId.get().toHexString())
            .exchange()
            .expectStatus().isUnauthorized
            .expectBody(ErrorResponse::class.java)
            .consumeWith(
                WebTestClientRestDocumentationWrapper.document(
                    "find-unauthorized",
                    preprocessResponse(prettyPrint()),
                    resource(
                        ResourceSnippetParameters.builder()
                            .tags(MOVIE_TAG)
                            .summary(GET_SUMMARY)
                            .build()
                    )
                )
            )
    }

    @Test
    fun `list should succeed and return all movies when no date is provided`() {
        webTestClient.mutate().baseUrl("http://localhost:$port")
            .filter(documentationConfiguration(restDocumentation))
            .build()
            .get()
            .uri(MOVIE_PATH)
            .header(AUTHORIZATION, "Bearer ${testJwtUtil.standardToken}")
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
            .consumeWith<ListBodySpec<MovieApiDto>>(
                WebTestClientRestDocumentationWrapper.document(
                    "list-all",
                    preprocessResponse(prettyPrint()),
                    resource(
                        ResourceSnippetParameters.builder()
                            .tags(MOVIE_TAG)
                            .summary(LIST_SUMMARY)
                            .queryParameters(
                                parameterWithName("startDate").description("To only select movies released after this date").optional(),
                                parameterWithName("endDate").description("To only select movies released before this date").optional(),
                            )
                            .responseFields(
                                fieldWithPath("[].id").description("The unique identifier of the movie"),
                                fieldWithPath("[].title").description("Title of the movie"),
                                fieldWithPath("[].releaseDate").description("Release date of the movie"),
                                fieldWithPath("[].type").description(
                                    "Type of the movie. Possible values are: Drama,\n" +
                                            "    Comedy,\n" +
                                            "    ScienceFiction,\n" +
                                            "    Fantasy,\n" +
                                            "    Horror,\n" +
                                            "    Thriller,\n" +
                                            "    Western,\n" +
                                            "    Musical,"
                                ),
                            )
                            .build()
                    )
                )
            )
    }

    @Test
    fun `list should succeed and return empty list when no movie matches the dates`() {
        val startDate = LocalDate.now().minusYears(500)
        val endDate = LocalDate.now().minusYears(400)
        webTestClient.mutate().baseUrl("http://localhost:$port").build()
            .get()
            .uri("$MOVIE_PATH?startDate=$startDate&&endDate=$endDate")
            .header(AUTHORIZATION, "Bearer ${testJwtUtil.standardToken}")
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
            .consumeWith<ListBodySpec<MovieApiDto>>(
                WebTestClientRestDocumentationWrapper.document(
                    "list-empty",
                    preprocessResponse(prettyPrint()),
                    resource(
                        ResourceSnippetParameters.builder()
                            .tags(MOVIE_TAG)
                            .summary(LIST_SUMMARY)
                            .queryParameters(
                                parameterWithName("startDate").description("To only select movies released after this date").optional(),
                                parameterWithName("endDate").description("To only select movies released before this date").optional(),
                            )
                            .build()
                    )
                )
            )
    }

    @Test
    fun `list should return a subset of movies when total result size exceeds page size`() {
        movieDao.deleteAll()
        val movies = generateListOfMovies(15)
        movieDao.insertAll(movies)
        webTestClient.mutate().baseUrl("http://localhost:$port").build()
            .get()
            .uri(MOVIE_PATH)
            .header(AUTHORIZATION, "Bearer ${testJwtUtil.standardToken}")
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
            .consumeWith<ListBodySpec<MovieApiDto>>(
                WebTestClientRestDocumentationWrapper.document(
                    "list-partial",
                    preprocessResponse(prettyPrint()),
                    resource(
                        ResourceSnippetParameters.builder()
                            .tags(MOVIE_TAG)
                            .summary(LIST_SUMMARY)
                            .queryParameters(
                                parameterWithName("startDate").description("To only select movies released after this date").optional(),
                                parameterWithName("endDate").description("To only select movies released before this date").optional(),
                            )
                            .responseFields(
                                fieldWithPath("[].id").description("The unique identifier of the movie"),
                                fieldWithPath("[].title").description("Title of the movie"),
                                fieldWithPath("[].releaseDate").description("Release date of the movie"),
                                fieldWithPath("[].type").description("Type of the movie"),
                            )
                            .build()
                    )
                )
            )
    }

    @Test
    fun `list should return 401 when authentication fails because wrong token in header`() {
        webTestClient.mutate().baseUrl("http://localhost:$port").build()
            .get()
            .uri(MOVIE_PATH)
            .header(AUTHORIZATION, "Bearer dummyToken")
            .exchange()
            .expectStatus().isUnauthorized
            .expectBody(ErrorResponse::class.java)
            .consumeWith(
                WebTestClientRestDocumentationWrapper.document(
                    "list-unauthorized",
                    preprocessResponse(prettyPrint()),
                    resource(
                        ResourceSnippetParameters.builder()
                            .tags(MOVIE_TAG)
                            .summary(GET_SUMMARY)
                            .build()
                    )
                )
            )
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