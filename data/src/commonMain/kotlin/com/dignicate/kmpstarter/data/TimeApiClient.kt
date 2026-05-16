package com.dignicate.kmpstarter.data

interface TimeApiClient {
    suspend fun getTime(): TimeDto
}
