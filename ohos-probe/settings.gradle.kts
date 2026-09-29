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
        maven {
            url = uri(providers.gradleProperty("statelyMavenRepo").orElse("https://gycrosskit.github.io/stately-ohos/maven").get())
        }
        maven("https://maven.eazytec-cloud.com/nexus/repository/maven-public/")
        maven("https://mirrors.tencent.com/nexus/repository/maven-public/")
        mavenCentral()
    }
}

rootProject.name = "koin-ohos-probe"
include(":koin-core")
project(":koin-core").projectDir = file("../projects/core/koin-core")
project(":koin-core").buildFileName = "build.ohos.gradle.kts"
