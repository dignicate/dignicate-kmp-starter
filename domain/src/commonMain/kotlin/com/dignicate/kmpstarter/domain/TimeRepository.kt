package com.dignicate.kmpstarter.domain

import kotlinx.coroutines.flow.Flow

interface TimeRepository {
    fun getCurrentTime(): Flow<Result<TimeInfo>>
}
