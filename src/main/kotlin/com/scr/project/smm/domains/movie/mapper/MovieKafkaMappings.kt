package com.scr.project.smm.domains.movie.mapper

import com.scr.project.smm.domains.movie.model.entity.Movie
import com.scr.project.srm.RewardedEntityTypeKafkaDto.MOVIE
import com.scr.project.srm.RewardedKafkaDto

fun Movie.toRewardedKafkaDto() = RewardedKafkaDto(id.toHexString(), MOVIE)