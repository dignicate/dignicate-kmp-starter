package com.dignicate.kmpstarter.data

import com.dignicate.kmpstarter.domain.TimeInfo

fun TimeDto.toDomainObject(): TimeInfo = TimeInfo(
    utc = utc,
    millis = millis,
    unixSeconds = unixSeconds,
    iso8601 = iso8601,
)
