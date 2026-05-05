rootProject.name = "dignicate-kmp-starter"

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

include(":composeApp")
include(":core")
include(":domain")
include(":data")
include(":viewmodel")
include(":providers")
include(":ui")
