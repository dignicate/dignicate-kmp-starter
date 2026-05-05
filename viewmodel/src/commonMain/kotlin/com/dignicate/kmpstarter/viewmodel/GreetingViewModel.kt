package com.dignicate.kmpstarter.viewmodel

import com.dignicate.kmpstarter.domain.GetGreetingUseCase

class GreetingViewModel(
    private val getGreetingUseCase: GetGreetingUseCase
) {
    fun greeting(): String = getGreetingUseCase()
}
