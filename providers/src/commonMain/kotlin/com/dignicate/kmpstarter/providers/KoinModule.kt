package com.dignicate.kmpstarter.providers

import com.dignicate.kmpstarter.data.GreetingRepositoryImpl
import com.dignicate.kmpstarter.domain.GetGreetingUseCase
import com.dignicate.kmpstarter.domain.GreetingRepository
import com.dignicate.kmpstarter.viewmodel.GreetingViewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.mp.KoinPlatform

private val dataModule = module {
    single<GreetingRepository> { GreetingRepositoryImpl() }
}

private val domainModule = module {
    factory { GetGreetingUseCase(get()) }
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
