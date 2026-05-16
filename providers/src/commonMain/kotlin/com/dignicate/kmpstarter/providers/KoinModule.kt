package com.dignicate.kmpstarter.providers

import com.dignicate.kmpstarter.core.AppConfig
import com.dignicate.kmpstarter.data.TimeApiClient
import com.dignicate.kmpstarter.data.TimeApiClientImpl
import com.dignicate.kmpstarter.data.TimeRepositoryImpl
import com.dignicate.kmpstarter.domain.TimeRepository
import com.dignicate.kmpstarter.domain.TimeUseCase
import com.dignicate.kmpstarter.viewmodel.feature.debug.DebugMenuViewModel
import com.dignicate.kmpstarter.viewmodel.feature.home.HomeViewModel
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.koin.mp.KoinPlatform

private fun coreModule(appConfig: AppConfig) = module {
    single { appConfig }
}

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
}

private val domainModule = module {
    factory { TimeUseCase(get()) }
}

private val viewModelModule = module {
    viewModel { HomeViewModel(get()) }
    viewModel { DebugMenuViewModel(get()) }
}

fun initKoin(appConfig: AppConfig) {
    if (KoinPlatform.getKoinOrNull() != null) {
        return
    }

    startKoin {
        modules(
            coreModule(appConfig),
            dataModule,
            domainModule,
            viewModelModule,
        )
    }
}
