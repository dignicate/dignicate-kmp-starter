package com.dignicate.kmpstarter.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class TimeApiClientImpl(private val httpClient: HttpClient) : TimeApiClient {

    override suspend fun getTime(): TimeDto =
        httpClient.get("https://freeapi.dignicate.com/time/v1/current").body()
}
