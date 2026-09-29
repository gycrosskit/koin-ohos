plugins {
    id("org.jetbrains.kotlin.multiplatform")
}

kotlin {
    jvm { compilerOptions.jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_1_8) }
    iosArm64()
    iosSimulatorArm64()
    iosX64()
    ohosArm64()
    sourceSets {
        commonMain.dependencies {
            implementation("co.touchlab:stately-concurrency:2.1.0-ohos-2.2.21-2")
            implementation("co.touchlab:stately-concurrent-collections:2.1.0-ohos-2.2.21-2")
        }
    }
}
