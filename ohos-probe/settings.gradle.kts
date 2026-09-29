pluginManagement {
    repositories {
        maven("https://maven.eazytec-cloud.com/nexus/repository/maven-public/")
        maven("https://mirrors.tencent.com/nexus/repository/maven-public/")
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        maven(providers.gradleProperty("statelyMavenRepo").orElse("https://jitpack.io").get())
        maven("https://maven.eazytec-cloud.com/nexus/repository/maven-public/")
        maven("https://mirrors.tencent.com/nexus/repository/maven-public/")
        mavenCentral()
    }
}

rootProject.name = "koin-ohos-probe"
include(":koin-core")
project(":koin-core").projectDir = file("../projects/core/koin-core")
project(":koin-core").buildFileName = "build.ohos.gradle.kts"
