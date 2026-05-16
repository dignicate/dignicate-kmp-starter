package com.dignicate.kmpstarter.core

actual fun getAppVersion(): String = BuildConfig.VERSION_NAME

actual fun getPackageName(): String = BuildConfig.PACKAGE_NAME

actual fun isDebugBuild(): Boolean = BuildConfig.DEBUG
