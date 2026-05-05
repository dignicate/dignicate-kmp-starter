package com.dignicate.kmpstarter.domain

class GetGreetingUseCase(
    private val greetingRepository: GreetingRepository
) {
    operator fun invoke(): String = greetingRepository.getGreeting()
}
