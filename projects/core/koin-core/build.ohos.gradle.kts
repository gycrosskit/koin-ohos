plugins {
    id("org.jetbrains.kotlin.multiplatform")
}

kotlin {
    ohosArm64()
    sourceSets {
        commonMain.dependencies {
            implementation("co.touchlab:stately-concurrency:2.1.0-ohos-2.2.21-1")
            implementation("co.touchlab:stately-concurrent-collections:2.1.0-ohos-2.2.21-1")
        }
    }
}
