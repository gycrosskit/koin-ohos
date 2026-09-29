# Koin Core OHOS 适配

此分支基于上游 `4.1.1`，使用 Kotlin `2.2.21-1.0.0`。独立 `ohos-probe` 构建发布 JVM（Android 使用）、`iosArm64`、`iosSimulatorArm64`、`iosX64` 和 `ohosArm64` 变体，复用上游 JVM/Native 源码。`-1` 是历史 OHOS 单目标版本；三端共用请使用 `-2`。

```kotlin
// settings.gradle.kts 的 dependencyResolutionManagement.repositories
maven { url = uri("https://gycrosskit.github.io/koin-ohos/maven") }
maven { url = uri("https://gycrosskit.github.io/stately-ohos/maven") }

// 三端共享模块的 commonMain.dependencies
implementation("io.insert-koin:koin-core:4.1.1-ohos-2.2.21-2") {
    // Koin Compose 等依赖仍声明官方 4.1.1，固定 Core 防止选回无 OHOS 的版本。
    version { strictly("4.1.1-ohos-2.2.21-2") }
}
```

`Stately 2.1.0-ohos-2.2.21-2` 是传递依赖，Gradle 根据 `.module` 元数据选择各端变体。公开 Maven 无需凭据；产物保存在各自仓库 `docs/maven`，由 GitHub Pages 提供。

## 构建与验证

```bash
bash projects/gradlew -p ohos-probe :koin-core:publishAllPublicationsToGycrosskitRepository

# 独立消费者仅通过 Maven 坐标解析，不引用本仓库源码。
# 需要 Android SDK（ANDROID_HOME）、Xcode 及 OHOS Native 工具链。
bash projects/gradlew -p verification-consumer \
  compileDebugKotlinAndroid compileKotlinIosArm64 compileKotlinIosSimulatorArm64 \
  compileKotlinIosX64 linkDebugFrameworkIosSimulatorArm64 linkDebugSharedOhosArm64 verifyKoin
```

发布前可分别通过 `-PkoinMavenRepo=/path/to/koin-ohos/docs/maven` 和 `-PstatelyMavenRepo=/path/to/stately-ohos/docs/maven` 验证本地待发布产物。发布构建可通过 `-PstatelyMavenRepo=...` 使用待发布 Stately。

验证范围是实际 Android Gradle Plugin `androidTarget()` 解析和编译、iOS KLIB 编译与模拟器 Framework 链接、OHOS 动态库链接，以及 JVM 运行单例/工厂注入检查。设备运行尚未验证。此分支只发布 Koin Core，不包含 `koin-compose`、`koin-compose-viewmodel` 或 Kuikly ViewModel 生命周期接入。
