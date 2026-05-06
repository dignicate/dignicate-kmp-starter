package com.dignicate.kmpstarter.domain

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent

@OptIn(ExperimentalCoroutinesApi::class)
class TimeUseCaseTest {

    @Test
    fun `fetch emits time result after debounce`() = runTest {
        val expected = TimeInfo(
            utc = "2026-05-06T00:00:00Z",
            millis = 1_000L,
            unixSeconds = 1L,
            iso8601 = "2026-05-06T00:00:00Z",
        )
        val repository = FakeTimeRepository(Result.success(expected))
        val useCase = TimeUseCase(repository, scope = backgroundScope)

        useCase.data.launchIn(backgroundScope)

        runCurrent()
        assertEquals(Resource.Initialized, useCase.data.value)

        useCase.fetch()
        runCurrent()

        assertEquals(0, repository.calls)
        assertEquals(Resource.Initialized, useCase.data.value)

        advanceTimeBy(299)
        runCurrent()

        assertEquals(0, repository.calls)
        assertEquals(Resource.Initialized, useCase.data.value)

        advanceTimeBy(1)
        runCurrent()

        assertEquals(1, repository.calls)
        assertEquals(Resource.Success(expected), useCase.data.value)
    }

    @Test
    fun `fetch maps repository failure into resource failure`() = runTest {
        val error = IllegalStateException("boom")
        val repository = FakeTimeRepository(Result.failure(error))
        val useCase = TimeUseCase(repository, scope = backgroundScope)

        useCase.data.launchIn(backgroundScope)

        runCurrent()
        assertEquals(Resource.Initialized, useCase.data.value)

        useCase.fetch()
        runCurrent()
        advanceTimeBy(300)
        runCurrent()

        assertEquals(1, repository.calls)
        assertEquals(Resource.Failure(Error.Unknown(error)), useCase.data.value)
    }

    private class FakeTimeRepository(
        private val result: Result<TimeInfo>,
    ) : TimeRepository {
        var calls: Int = 0

        override fun getCurrentTime(): Flow<Result<TimeInfo>> {
            calls += 1
            return flowOf(result)
        }
    }
}
