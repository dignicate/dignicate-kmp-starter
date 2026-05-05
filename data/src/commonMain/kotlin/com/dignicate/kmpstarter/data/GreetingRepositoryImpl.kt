package com.dignicate.kmpstarter.data

import com.dignicate.kmpstarter.domain.GreetingRepository

class GreetingRepositoryImpl : GreetingRepository {
    override fun getGreeting(): String = "Hello from the data layer."
}
