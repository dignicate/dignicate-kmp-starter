package com.dignicate.kmpstarter.data

import com.dignicate.kmpstarter.domain.TimeInfo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class TimeRepositoryImplTest {

    @Test
    fun `getCurrentTime can be collected repeatedly without closing api client`() = runBlocking {
        val apiClient = FakeTimeApiClient()
        val repository = TimeRepositoryImpl(apiClient)
        val expected = TimeInfo(
            utc = "2026-05-06T00:00:00Z",
            millis = 1_000L,
            unixSeconds = 1L,
            iso8601 = "2026-05-06T00:00:00Z",
        )

        val firstResult = repository.getCurrentTime().first()
        val secondResult = repository.getCurrentTime().first()

        assertEquals(Result.success(expected), firstResult)
        assertEquals(Result.success(expected), secondResult)
        assertEquals(0, apiClient.closeCalls)
        assertEquals(2, apiClient.getTimeCalls)
    }

    private class FakeTimeApiClient : TimeApiClient {
        var getTimeCalls: Int = 0
        var closeCalls: Int = 0
        val response = TimeDto(
            utc = "2026-05-06T00:00:00Z",
            millis = 1_000L,
            unixSeconds = 1L,
            iso8601 = "2026-05-06T00:00:00Z",
        )

        override suspend fun getTime(): TimeDto {
            getTimeCalls += 1
            return response
        }
    }
}
