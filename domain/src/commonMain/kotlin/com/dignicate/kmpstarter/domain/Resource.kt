package com.dignicate.kmpstarter.domain

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

sealed class Resource<out T : Any> {
    data object Initialized : Resource<Nothing>()
    data object InProgress : Resource<Nothing>()
    data class Success<out T : Any>(val data: T) : Resource<T>()
    data class Failure<out T : Any>(val error: Error) : Resource<T>()
}

sealed class Error {
    data class Unknown(val throwable: Throwable) : Error()
}

@OptIn(ExperimentalCoroutinesApi::class)
fun <S : Any, T : Any> Flow<S>.mapToResource(
    scope: CoroutineScope,
    then: (S) -> Flow<Result<T>>,
    mapError: (Throwable) -> Error = { Error.Unknown(it) },
): StateFlow<Resource<T>> = flatMapLatest { param ->
    flow {
        emit(Resource.InProgress)
        try {
            then(param).collect { result ->
                result.fold(
                    onSuccess = { emit(Resource.Success(it)) },
                    onFailure = { emit(Resource.Failure(mapError(it))) },
                )
            }
        } catch (t: Throwable) {
            emit(Resource.Failure(mapError(t)))
        }
    }
}.stateIn(
    scope = scope,
    started = SharingStarted.WhileSubscribed(5_000L),
    initialValue = Resource.Initialized,
)
