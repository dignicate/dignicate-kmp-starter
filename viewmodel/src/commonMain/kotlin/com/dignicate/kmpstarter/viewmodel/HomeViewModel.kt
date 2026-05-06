package com.dignicate.kmpstarter.viewmodel

import com.dignicate.kmpstarter.domain.Error
import com.dignicate.kmpstarter.domain.Resource
import com.dignicate.kmpstarter.domain.TimeUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val timeUseCase: TimeUseCase,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob()),
) {
    data class ViewState(
        val isLoading: Boolean = false,
        val currentTime: String? = null,
        val errorMessage: String? = null,
    )

    private val _viewState = MutableStateFlow(ViewState())
    val viewState: StateFlow<ViewState> = _viewState.asStateFlow()

    init {
        scope.launch {
            timeUseCase.data.collect { resource ->
                _viewState.value = when (resource) {
                    is Resource.Initialized -> ViewState()
                    is Resource.InProgress -> ViewState(isLoading = true)
                    is Resource.Success -> ViewState(currentTime = resource.data.iso8601)
                    is Resource.Failure -> ViewState(
                        errorMessage = when (val error = resource.error) {
                            is Error.Unknown -> error.throwable.message ?: "Unknown error"
                        }
                    )
                }
            }
        }
    }

    fun onAppear() {
        scope.launch { timeUseCase.fetch() }
    }

    fun onRefresh() {
        scope.launch { timeUseCase.fetch() }
    }
}
