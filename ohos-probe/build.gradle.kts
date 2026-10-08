plugins {
    id("org.jetbrains.kotlin.multiplatform") version "2.2.21-1.0.0" apply false
}

subprojects {
    // Native KLIB names must match the upstream Core required by official Koin Compose.
    group = "io.insert-koin"
    version = providers.environmentVariable("VERSION").orElse("4.1.1-ohos-2.2.21-7").get()
    plugins.apply("maven-publish")
    extensions.configure<org.gradle.api.publish.PublishingExtension> {
        publications.withType<org.gradle.api.publish.maven.MavenPublication>().configureEach {
            groupId = "com.github.gycrosskit.koin-ohos"
            pom {
                name.set(project.name)
                description.set("OpenHarmony-compatible upstream library")
                url.set("https://github.com/gycrosskit/koin-ohos")
                licenses {
                    license {
                        name.set("Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                        distribution.set("repo")
                    }
                }
            }
        }
        repositories.maven {
            name = "gycrosskit"
            url = uri(providers.gradleProperty("gycrosskitMavenRepo").orElse(rootProject.file("../build/release-maven").absolutePath).get())
        }
    }
}
