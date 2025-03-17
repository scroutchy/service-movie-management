package com.scr.project.smm.entrypoint.unit.resource

import com.scr.project.smm.domains.movie.error.MovieErrors.OnMovieNotFound
import com.scr.project.smm.domains.movie.model.business.Actor
import com.scr.project.smm.domains.movie.model.entity.Movie
import com.scr.project.smm.domains.movie.model.entity.MovieType.Fantasy
import com.scr.project.smm.domains.movie.model.entity.MovieType.Western
import com.scr.project.smm.domains.movie.ports.MovieWithActors
import com.scr.project.smm.domains.movie.service.MovieService
import com.scr.project.smm.entrypoint.mapper.toEntity
import com.scr.project.smm.entrypoint.model.api.ActorApiDto
import com.scr.project.smm.entrypoint.model.api.MovieApiDto
import com.scr.project.smm.entrypoint.resource.MovieResource
import io.mockk.clearMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.domain.Pageable
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono
import reactor.kotlin.test.test
import reactor.kotlin.test.verifyError
import java.time.LocalDate

class MovieResourceTest {

    private val movieRequest = MovieApiDto("title", LocalDate.now(), Western)
    private val movieService = mockk<MovieService>()
    private val movieResource = MovieResource(movieService)

    @BeforeEach
    internal fun setUp() {
        clearMocks(movieService)
        every { movieService.create(any<Movie>()) } answers { movieRequest.toEntity().copy(id = ObjectId()).toMono() }
    }

    @Test
    fun `create should succeed`() {
        movieResource.create(movieRequest)
            .test()
            .expectSubscription()
            .consumeNextWith {
                with(it.body!!) {
                    assertThat(id).isNotNull()
                    assertThat(title).isEqualTo(movieRequest.title)
                    assertThat(releaseDate).isEqualTo(movieRequest.releaseDate)
                    assertThat(type).isEqualTo(movieRequest.type)
                }
            }
    }

    @Test
    fun `find should succeed and return a movie response when id exits`() {
        val id = ObjectId.get()
        every { movieService.findById(any<ObjectId>()) } answers {
            MovieWithActors(movieRequest.toEntity().copy(id = firstArg()), listOf()).toMono()
        }
        movieResource.find(id)
            .test()
            .expectSubscription()
            .consumeNextWith {
                assertThat(it).isNotNull()
                assertThat(it.id).isEqualTo(id.toHexString())
                assertThat(it.title).isEqualTo(movieRequest.title)
                assertThat(it.releaseDate).isEqualTo(movieRequest.releaseDate)
                assertThat(it.type).isEqualTo(movieRequest.type)
                assertThat(it.actorIds).isEmpty()
                assertThat(it.actors).isEmpty()
            }
            .verifyComplete()
        verify(exactly = 1) { movieService.findById(id) }
        confirmVerified(movieService)
    }

    @Test
    fun `find should succeed and return a movie response with actors when provided`() {
        val id = ObjectId.get()
        val actorId = ObjectId.get().toHexString()
        every { movieService.findById(any<ObjectId>()) } answers {
            MovieWithActors(movieRequest.toEntity().copy(id = firstArg()), listOf(Actor(actorId, "Brad Pitt"))).toMono()
        }
        movieResource.find(id)
            .test()
            .expectSubscription()
            .consumeNextWith {
                assertThat(it).isNotNull()
                assertThat(it.id).isEqualTo(id.toHexString())
                assertThat(it.title).isEqualTo(movieRequest.title)
                assertThat(it.releaseDate).isEqualTo(movieRequest.releaseDate)
                assertThat(it.type).isEqualTo(movieRequest.type)
                assertThat(it.actorIds).isEmpty()
                assertThat(it.actors).isEqualTo(listOf(ActorApiDto(actorId, "Brad Pitt")))
            }
            .verifyComplete()
        verify(exactly = 1) { movieService.findById(id) }
        confirmVerified(movieService)
    }

    @Test
    fun `find should return an exception when movie id does not exist`() {
        val id = ObjectId.get()
        every { movieService.findById(id) } answers { OnMovieNotFound(id).toMono() }
        movieResource.find(id)
            .test()
            .expectSubscription()
            .consumeSubscriptionWith {
                verify(exactly = 1) { movieService.findById(id) }
                confirmVerified(movieService)
            }
            .verifyError(OnMovieNotFound::class)
    }

    @Test
    fun `list should succeed and return a list of movies`() {
        val movie1 = generateMovie("Movie 1")
        val movie2 = generateMovie("Movie 2")
        val movies = listOf(movie1, movie2)
        every { movieService.findAllBetween(Pageable.ofSize(10)) } answers { movies.toFlux() }
        movieResource.list(Pageable.ofSize(10))
            .test()
            .expectSubscription()
            .consumeNextWith {
                assertThat(it.body).hasSize(movies.size)
            }
            .verifyComplete()
        verify(exactly = 1) { movieService.findAllBetween(Pageable.ofSize(10)) }
        confirmVerified(movieService)
    }

    @Test
    fun `list should succeed and return a sublist of movies when filtering by release date before`() {
        val movie1 = generateMovie("Movie 1")
        val movie2 = generateMovie("Movie 2").copy(releaseDate = LocalDate.now().minusDays(5))
        val movies = listOf(movie1, movie2)
        every { movieService.findAllBetween(Pageable.ofSize(10), null, LocalDate.now().minusDays(7)) } answers {
            movies.filter { it.releaseDate <= LocalDate.now().minusDays(7) }.toFlux()
        }
        movieResource.list(Pageable.ofSize(10), null, LocalDate.now().minusDays(7))
            .test()
            .expectSubscription()
            .consumeNextWith {
                assertThat(it.body).hasSize(movies.filter { it.releaseDate <= LocalDate.now().minusDays(7) }.size)
            }
            .verifyComplete()
        verify(exactly = 1) { movieService.findAllBetween(Pageable.ofSize(10), null, LocalDate.now().minusDays(7)) }
        confirmVerified(movieService)
    }

    @Test
    fun `list should succeed and return a sublist of movies when filtering by release date after`() {
        val movie1 = generateMovie("Movie 1")
        val movie2 = generateMovie("Movie 2").copy(releaseDate = LocalDate.now().minusDays(5))
        val movies = listOf(movie1, movie2)
        every { movieService.findAllBetween(Pageable.ofSize(10), LocalDate.now().minusDays(7)) } answers {
            movies.filter { it.releaseDate >= LocalDate.now().minusDays(7) }.toFlux()
        }
        movieResource.list(Pageable.ofSize(10), LocalDate.now().minusDays(7))
            .test()
            .expectSubscription()
            .consumeNextWith {
                assertThat(it.body).hasSize(movies.filter { it.releaseDate >= LocalDate.now().minusDays(7) }.size)
            }
            .verifyComplete()
        verify(exactly = 1) { movieService.findAllBetween(Pageable.ofSize(10), LocalDate.now().minusDays(7)) }
        confirmVerified(movieService)
    }

    @Test
    fun `list should succeed and return a empty list when both limit dates are provided and no movie matches`() {
        every { movieService.findAllBetween(Pageable.ofSize(10), any(), any()) } answers {
            emptyList<Movie>().toFlux()
        }
        movieResource.list(Pageable.ofSize(10), LocalDate.now().minusDays(3), LocalDate.now())
            .test()
            .expectSubscription()
            .consumeNextWith {
                assertThat(it.body).isEmpty()
            }
            .verifyComplete()
        verify(exactly = 1) { movieService.findAllBetween(Pageable.ofSize(10), LocalDate.now().minusDays(3), LocalDate.now()) }
        confirmVerified(movieService)
    }

    private fun generateMovie(title: String) = Movie(title, LocalDate.now().minusDays(10), Fantasy, "This is the synopsis of $title")
}