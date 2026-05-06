package com.dignicate.kmpstarter.data

import com.dignicate.kmpstarter.domain.TimeInfo
import com.dignicate.kmpstarter.domain.TimeRepository
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class TimeRepositoryImpl(private val apiClient: TimeApiClient) : TimeRepository {

    override fun getCurrentTime(): Flow<Result<TimeInfo>> = flow {
        val result = try {
            Result.success(apiClient.getTime().toDomainObject())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }

        emit(result)
    }
}

private fun TimeDto.toDomainObject(): TimeInfo = TimeInfo(
    utc = utc,
    millis = millis,
    unixSeconds = unixSeconds,
    iso8601 = iso8601,
)
