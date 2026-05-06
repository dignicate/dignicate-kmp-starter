package com.dignicate.kmpstarter.data

import com.dignicate.kmpstarter.domain.TimeInfo
import com.dignicate.kmpstarter.domain.TimeRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class TimeRepositoryImpl(private val apiClient: TimeApiClient) : TimeRepository {

    override fun getCurrentTime(): Flow<Result<TimeInfo>> = callbackFlow {
        try {
            val dto = apiClient.getTime()
            trySend(Result.success(dto.toDomainObject()))
        } catch (e: Exception) {
            trySend(Result.failure(e))
        }
        awaitClose { apiClient.close() }
    }
}

private fun TimeDto.toDomainObject(): TimeInfo = TimeInfo(
    utc = utc,
    millis = millis,
    unixSeconds = unixSeconds,
    iso8601 = iso8601,
)
