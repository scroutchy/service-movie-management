package com.scr.project.smm.domains.movie.component

import com.scr.project.smm.AbstractIntegrationTest
import com.scr.project.smm.domains.movie.dao.MovieDao
import com.scr.project.smm.domains.movie.repository.MovieRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.Pageable
import reactor.kotlin.test.test
import java.time.LocalDate

@SpringBootTest
internal class MovieRepositoryTest(
    @Autowired private val repository: MovieRepository,
    @Autowired private val movieDao: MovieDao
) :
    AbstractIntegrationTest() {

    @BeforeEach
    fun setUp() {
        movieDao.initTestData()
    }

    @Test
    fun `findAllBetween should succeed and return all movies and no dates provided`() {
        repository.findAllMoviesBetweenDates(Pageable.ofSize(10))
            .collectList()
            .test()
            .expectSubscription()
            .consumeNextWith {
                assertThat(it).hasSize(movieDao.count().toInt())
                assertThat(movieDao.findAll()).containsAll(it)
            }
            .verifyComplete()
    }

    @Test
    fun `findAllBetween should succeed and return empty list when no movies are found and both limit dates are provided`() {
        repository.findAllMoviesBetweenDates(Pageable.ofSize(10), LocalDate.of(1500, 1, 1), LocalDate.of(1600, 1, 1))
            .collectList()
            .test()
            .expectSubscription()
            .consumeNextWith {
                assertThat(it).isEmpty()
            }
            .verifyComplete()
    }

    @Test
    fun `findAllBetween should succeed and list a subset of movies matching when the end date is provided`() {
        val endDate = LocalDate.of(2000, 1, 1)
        repository.findAllMoviesBetweenDates(Pageable.ofSize(10), null, endDate)
            .collectList()
            .test()
            .expectSubscription()
            .consumeNextWith {
                assertThat(it).hasSize(movieDao.findAllBy { m -> m.releaseDate <= endDate }.size)
                assertThat(movieDao.findAllBy { m -> m.releaseDate <= endDate }).containsAll(it)
            }
            .verifyComplete()
    }

    @Test
    fun `findAllBetween should succeed and list a subset of movies matching when the start date is provided`() {
        val startDate = LocalDate.of(2000, 1, 1)
        repository.findAllMoviesBetweenDates(Pageable.ofSize(10), startDate, null)
            .collectList()
            .test()
            .expectSubscription()
            .consumeNextWith {
                assertThat(it).hasSize(movieDao.findAllBy { m -> m.releaseDate >= startDate }.size)
                assertThat(movieDao.findAllBy { m -> m.releaseDate >= startDate }).containsAll(it)
            }
            .verifyComplete()
    }
}