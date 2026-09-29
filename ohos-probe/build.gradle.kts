plugins {
    id("org.jetbrains.kotlin.multiplatform") version "2.2.21-1.0.0" apply false
}

subprojects {
    group = "com.github.gycrosskit.koin-ohos"
    version = providers.environmentVariable("VERSION").orElse("4.1.1-ohos-2.2.21-4").get()
    plugins.apply("maven-publish")
    extensions.configure<org.gradle.api.publish.PublishingExtension> {
        repositories.maven {
            name = "gycrosskit"
            url = uri(providers.gradleProperty("gycrosskitMavenRepo").orElse(rootProject.file("../build/release-maven").absolutePath).get())
        }
    }
}
