package com.dignicate.kmpstarter.domain

data class TimeInfo(
    val utc: String,
    val millis: Long,
    val unixSeconds: Long,
    val iso8601: String,
)
