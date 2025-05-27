package com.scr.project.smm.domains.movie.mapper

import com.scr.project.smm.domains.movie.model.entity.Movie
import com.scr.project.smm.domains.movie.model.entity.MovieType.Thriller
import com.scr.project.srm.RewardedEntityTypeKafkaDto.MOVIE
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import java.time.LocalDate

class MovieKafkaMappingsTest {

    @Test
    fun `toRewardedKafkaDto should succeed`() {
        val movie = Movie("title", LocalDate.now(), Thriller, "LocalDate.now()", id = ObjectId.get())
        val rewarded = movie.toRewardedKafkaDto()
        assertThat(rewarded.id).isEqualTo(movie.id.toHexString())
        assertThat(rewarded.type).isEqualTo(MOVIE)
    }
}