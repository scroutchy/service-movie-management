package com.scr.project.smm.domains.movie.service

import com.scr.project.smm.domains.movie.model.entity.Movie
import com.scr.project.smm.domains.movie.model.entity.MovieType.Fantasy
import com.scr.project.smm.domains.movie.repository.MovieRepository
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

class MovieServiceTest {

    private val movieWithoutId = Movie("title", LocalDate.now(), Fantasy)
    private val movieRepository = mockk<MovieRepository>()
    private val movieService = MovieService(movieRepository)

    @BeforeEach
    internal fun setUp() {
        clearMocks(movieRepository)
        every { movieRepository.insert(movieWithoutId) } answers { movieWithoutId.copy(id = ObjectId.get()).toMono() }
    }

    @Test
    fun `create should succeed`() {
        movieService.create(movieWithoutId)
            .test()
            .expectSubscription()
            .consumeNextWith {
                assertThat(it.id).isNotNull()
                assertThat(it.title).isEqualTo(movieWithoutId.title)
                assertThat(it.releaseDate).isEqualTo(movieWithoutId.releaseDate)
                assertThat(it.type).isEqualTo(movieWithoutId.type)
            }.verifyComplete()
        verify(exactly = 1) { movieRepository.insert(movieWithoutId) }
        confirmVerified(movieRepository)
    }
}