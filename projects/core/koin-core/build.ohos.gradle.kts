plugins {
    id("org.jetbrains.kotlin.multiplatform")
}

tasks.withType<JavaCompile>().configureEach {
    sourceCompatibility = "1.8"
    targetCompatibility = "1.8"
}

kotlin {
    jvm { compilerOptions.jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_1_8) }
    iosArm64()
    iosSimulatorArm64()
    iosX64()
    ohosArm64()
    sourceSets {
        commonMain.dependencies {
            implementation("com.github.gycrosskit.stately-ohos:stately-concurrency:2.1.0-ohos-2.2.21-11")
            implementation("com.github.gycrosskit.stately-ohos:stately-concurrent-collections:2.1.0-ohos-2.2.21-11")
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
        }
        jvmTest.dependencies {
            implementation("junit:junit:4.13.2")
        }
    }
}
