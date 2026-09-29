plugins {
    id("org.jetbrains.kotlin.multiplatform") version "2.2.21-1.0.0"
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.21-1.0.0"
}

val coreVersion = providers.gradleProperty("koinCoreVersion").orElse("4.1.1-ohos-2.2.21-5").get()
val statelyVersion = providers.gradleProperty("statelyVersion").orElse("2.1.0-ohos-2.2.21-9").get()
val forkedCore = "com.github.gycrosskit.koin-ohos:koin-core:$coreVersion"

configurations.configureEach {
    resolutionStrategy.dependencySubstitution {
        substitute(module("io.insert-koin:koin-core")).using(module(forkedCore))
        listOf("stately-strict", "stately-concurrency", "stately-concurrent-collections").forEach { name ->
            substitute(module("co.touchlab:$name"))
                .using(module("com.github.gycrosskit.stately-ohos:$name:$statelyVersion"))
        }
    }
}

kotlin {
    iosSimulatorArm64 { binaries.framework() }
    sourceSets.commonMain.dependencies {
        implementation("org.jetbrains.compose.runtime:runtime:1.10.3")
        implementation("org.jetbrains.compose.foundation:foundation:1.10.3")
        implementation("org.jetbrains.compose.ui:ui:1.10.3")
        implementation(forkedCore)
        implementation("io.insert-koin:koin-compose:4.1.1")
        implementation("io.insert-koin:koin-compose-viewmodel:4.1.1")
    }
}
