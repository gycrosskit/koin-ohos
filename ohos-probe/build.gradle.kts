plugins {
    id("org.jetbrains.kotlin.multiplatform") version "2.2.21-1.0.0" apply false
}

subprojects {
    group = "io.insert-koin"
    version = "4.1.1-ohos-2.2.21-2"
    plugins.apply("maven-publish")
    extensions.configure<org.gradle.api.publish.PublishingExtension> {
        repositories.maven {
            name = "gycrosskit"
            url = uri(providers.gradleProperty("gycrosskitMavenRepo").orElse(rootProject.file("../docs/maven").absolutePath).get())
        }
    }
}
