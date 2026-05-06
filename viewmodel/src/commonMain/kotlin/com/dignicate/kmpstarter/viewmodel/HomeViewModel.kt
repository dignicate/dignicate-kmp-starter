package com.dignicate.kmpstarter.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dignicate.kmpstarter.domain.Error
import com.dignicate.kmpstarter.domain.Resource
import com.dignicate.kmpstarter.domain.TimeUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val timeUseCase: TimeUseCase,
) : ViewModel() {
    data class ViewState(
        val isLoading: Boolean = false,
        val currentTime: String? = null,
        val errorMessage: String? = null,
    )

    private val _viewState = MutableStateFlow(ViewState())
    val viewState: StateFlow<ViewState> = _viewState.asStateFlow()

    init {
        viewModelScope.launch {
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
        viewModelScope.launch { timeUseCase.fetch() }
    }

    fun onRefresh() {
        viewModelScope.launch { timeUseCase.fetch() }
    }
}
