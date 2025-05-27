package com.scr.project.smm.domains.movie.mapper

import org.bson.types.ObjectId

fun ObjectId?.toHexString() = this?.toHexString() ?: throw IllegalArgumentException("ObjectId is null")