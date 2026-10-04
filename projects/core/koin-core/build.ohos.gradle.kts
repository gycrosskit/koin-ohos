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
            implementation("com.github.gycrosskit.stately-ohos:stately-concurrency:2.1.0-ohos-2.2.21-10")
            implementation("com.github.gycrosskit.stately-ohos:stately-concurrent-collections:2.1.0-ohos-2.2.21-10")
        }
    }
}
