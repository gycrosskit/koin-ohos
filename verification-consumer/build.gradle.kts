plugins {
    id("org.jetbrains.kotlin.multiplatform") version "2.2.21-1.0.0"
    id("com.android.library") version "8.10.1"
}

kotlin {
    jvm()
    androidTarget()
    iosArm64 { binaries.framework() }
    iosSimulatorArm64 { binaries.framework() }
    iosX64()
    ohosArm64 { binaries.sharedLib() }
    sourceSets.commonMain.dependencies {
        implementation("com.github.gycrosskit.koin-ohos:koin-core:4.1.1-ohos-2.2.21-3")
    }
}
android {
    namespace = "org.koin.verification"
    compileSdk = 36
    defaultConfig.minSdk = 24
}

tasks.register<JavaExec>("verifyKoin") {
    val main = kotlin.targets.getByName("jvm").compilations.getByName("main")
    classpath(main.output.allOutputs, main.runtimeDependencyFiles)
    mainClass.set("ProbeKt")
}
