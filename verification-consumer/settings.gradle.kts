import org.gradle.api.initialization.resolve.RepositoriesMode

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
    val remoteOnly = providers.gradleProperty("remoteOnly").isPresent
    val koinRepository = providers.gradleProperty("koinMavenRepo").orElse("https://jitpack.io").get()
    val statelyRepository = providers.gradleProperty("statelyMavenRepo").orElse("https://jitpack.io").get()
    if (remoteOnly) {
        require(koinRepository == "https://jitpack.io" && statelyRepository == "https://jitpack.io") {
            "remoteOnly requires both component repositories to be https://jitpack.io"
        }
        require(!providers.gradleProperty("verifyLocalSource").isPresent) {
            "remoteOnly cannot use local source substitution"
        }
        repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    }
    repositories {
        if (remoteOnly) {
            exclusiveContent {
                forRepository { maven("https://jitpack.io") }
                filter {
                    includeGroup("com.github.gycrosskit.koin-ohos")
                    includeGroup("com.github.gycrosskit.stately-ohos")
                }
            }
        } else {
            maven(koinRepository)
            maven(statelyRepository)
        }
        maven("https://maven.eazytec-cloud.com/nexus/repository/maven-public/")
        maven("https://mirrors.tencent.com/nexus/repository/maven-public/")
        google()
        mavenCentral()
    }
}
rootProject.name = "koin-multiplatform-consumer"
