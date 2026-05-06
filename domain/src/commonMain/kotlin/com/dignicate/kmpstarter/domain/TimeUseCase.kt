package com.dignicate.kmpstarter.domain

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlin.time.Duration.Companion.milliseconds

class TimeUseCase(
    private val repository: TimeRepository,
    scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
) {
    private val _fetchTrigger = MutableSharedFlow<Unit>()

    @OptIn(FlowPreview::class)
    val data: StateFlow<Resource<TimeInfo>> = _fetchTrigger
        .debounce(300.milliseconds)
        .mapToResource(
            scope = scope,
            then = {
                repository.getCurrentTime()
            },
        )

    suspend fun fetch() {
        _fetchTrigger.emit(Unit)
    }
}
