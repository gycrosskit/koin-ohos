pluginManagement {
    repositories {
        maven("https://maven.eazytec-cloud.com/nexus/repository/maven-public/")
        maven("https://mirrors.tencent.com/nexus/repository/maven-public/")
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}
dependencyResolutionManagement {
    repositories {
        maven(providers.gradleProperty("koinMavenRepo").orElse("https://gycrosskit.github.io/koin-ohos/maven").get())
        maven(providers.gradleProperty("statelyMavenRepo").orElse("https://gycrosskit.github.io/stately-ohos/maven").get())
        maven("https://maven.eazytec-cloud.com/nexus/repository/maven-public/")
        maven("https://mirrors.tencent.com/nexus/repository/maven-public/")
        google()
        mavenCentral()
    }
}
rootProject.name = "koin-multiplatform-consumer"
