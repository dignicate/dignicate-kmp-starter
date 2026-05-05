package com.dignicate.kmpstarter.data

import kotlinx.serialization.Serializable

@Serializable
data class TimeDto(
    val utc: String,
    val millis: Long,
    val unixSeconds: Long,
    val iso8601: String,
)
