package com.dignicate.kmpstarter.core

import com.dignicate.kmpstarter.BuildConfig

actual fun getAppVersion(): String = BuildConfig.VERSION_NAME
