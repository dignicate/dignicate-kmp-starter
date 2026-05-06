package com.dignicate.kmpstarter.providers

import com.dignicate.kmpstarter.data.GreetingRepositoryImpl
import com.dignicate.kmpstarter.data.TimeApiClient
import com.dignicate.kmpstarter.data.TimeApiClientImpl
import com.dignicate.kmpstarter.data.TimeRepositoryImpl
import com.dignicate.kmpstarter.domain.GetGreetingUseCase
import com.dignicate.kmpstarter.domain.GreetingRepository
import com.dignicate.kmpstarter.domain.TimeRepository
import com.dignicate.kmpstarter.domain.TimeUseCase
import com.dignicate.kmpstarter.viewmodel.GreetingViewModel
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.mp.KoinPlatform

private val dataModule = module {
    single {
        HttpClient {
            install(ContentNegotiation) {
                json()
            }
        }
    }
    single<TimeApiClient> { TimeApiClientImpl(get()) }
    single<TimeRepository> { TimeRepositoryImpl(get()) }
    single<GreetingRepository> { GreetingRepositoryImpl() }
}

private val domainModule = module {
    factory { GetGreetingUseCase(get()) }
    factory { TimeUseCase(get()) }
}

private val viewModelModule = module {
    factory { GreetingViewModel(get()) }
}

val appModules = listOf(
    dataModule,
    domainModule,
    viewModelModule
)

fun initKoin() {
    if (KoinPlatform.getKoinOrNull() != null) {
        return
    }

    startKoin {
        modules(appModules)
    }
}
