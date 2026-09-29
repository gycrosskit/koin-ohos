pluginManagement {
    repositories {
        maven("https://maven.eazytec-cloud.com/nexus/repository/maven-public/")
        maven("https://mirrors.tencent.com/nexus/repository/maven-public/")
        mavenCentral()
        google()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        maven(providers.gradleProperty("koinMavenRepo").orElse("https://jitpack.io").get())
        maven(providers.gradleProperty("statelyMavenRepo").orElse("https://jitpack.io").get())
        google { content { includeGroupByRegex("androidx\\..*") } }
        mavenCentral()
        maven("https://maven.eazytec-cloud.com/nexus/repository/maven-public/")
    }
}

rootProject.name = "koin-compose-consumer"
