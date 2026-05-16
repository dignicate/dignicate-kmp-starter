package com.dignicate.kmpstarter.core

enum class AppEnvironment(val rawName: String) {
    PRODUCTION("prod"),
    STAGING("stg"),
    DEVELOPMENT("dev"),
    UNKNOWN("UNKNOWN");

    val isProduction: Boolean get() = this == PRODUCTION

    companion object {
        fun fromName(name: String): AppEnvironment = when (name) {
            "prod" -> PRODUCTION
            "stg" -> STAGING
            "dev" -> DEVELOPMENT
            else -> UNKNOWN
        }
    }
}
