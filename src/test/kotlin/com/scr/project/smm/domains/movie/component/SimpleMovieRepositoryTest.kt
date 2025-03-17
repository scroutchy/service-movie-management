package com.scr.project.smm.domains.movie.component

import com.scr.project.smm.AbstractIntegrationTest
import com.scr.project.smm.domains.movie.dao.MovieDao
import com.scr.project.smm.domains.movie.dao.pulpFiction
import com.scr.project.smm.domains.movie.model.entity.Movie
import com.scr.project.smm.domains.movie.model.entity.MovieType.Fantasy
import com.scr.project.smm.domains.movie.repository.SimpleMovieRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import reactor.kotlin.test.test
import java.time.LocalDate

@SpringBootTest
internal class SimpleMovieRepositoryTest(
    @Autowired private val simpleMovieRepository: SimpleMovieRepository,
    @Autowired private val movieDao: MovieDao,
) : AbstractIntegrationTest() {

    @BeforeEach
    fun setUp() {
        movieDao.initTestData()
    }

    @Test
    fun `insert should succeed`() {
        val movie = Movie("The Fellowship of the Ring", LocalDate.of(2001, 12, 19), Fantasy, "This is the synopsis")
        val initialCount = movieDao.count()
        simpleMovieRepository.insert(movie)
            .test()
            .expectSubscription()
            .consumeNextWith {
                assertThat(it.id).isNotNull
                assertThat(it.title).isEqualTo(movie.title)
                assertThat(it.releaseDate).isEqualTo(movie.releaseDate)
                assertThat(it.type).isEqualTo(movie.type)
                assertThat(it.synopsis).isEqualTo(movie.synopsis)
                assertThat(movieDao.count()).isEqualTo(initialCount + 1)
            }
            .verifyComplete()
    }

    @Test
    fun `findBy should return correct movie`() {
        val movie = pulpFiction()
        simpleMovieRepository.findById(movie.id!!.toHexString())
            .test()
            .expectSubscription()
            .consumeNextWith {
                assertThat(it.id).isEqualTo(movie.id)
                assertThat(it.title).isEqualTo(movie.title)
                assertThat(it.releaseDate).isEqualTo(movie.releaseDate)
                assertThat(it.type).isEqualTo(movie.type)
                assertThat(it.actors).isEqualTo(movie.actors)
            }
            .verifyComplete()
    }

    @Test
    fun `findById should return null when id not in database`() {
        simpleMovieRepository.findById("dummyId")
            .test()
            .expectSubscription()
            .expectNextCount(0)
            .verifyComplete()
    }
}