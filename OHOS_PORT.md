# Koin Core OHOS 适配

此分支基于上游 `4.1.1`，使用 Kotlin `2.2.21-1.0.0`。独立 `ohos-probe` 构建发布 JVM（Android 使用）、`iosArm64`、`iosSimulatorArm64`、`iosX64` 和 `ohosArm64` 变体，复用上游 JVM/Native 源码。三端共用请使用 `-4`。

```kotlin
// settings.gradle.kts 的 dependencyResolutionManagement.repositories
maven { url = uri("https://jitpack.io") }

// 三端共享模块的 commonMain.dependencies
implementation("com.github.gycrosskit.koin-ohos:koin-core:4.1.1-ohos-2.2.21-4")
```

`Stately 2.1.0-ohos-2.2.21-7` 是传递依赖，Gradle 根据 `.module` 元数据选择各端变体。iOS KLIB 在 macOS 构建，GitHub Release 保存构建包，由 JitPack 提供 Maven 依赖。此坐标仅提供 Koin Core；若同时使用官方 Koin Compose，需确保依赖图中没有官方 `io.insert-koin:koin-core` 的重复类。

## 构建与验证

```bash
bash projects/gradlew -p ohos-probe :koin-core:publishAllPublicationsToGycrosskitRepository

# 独立消费者仅通过 Maven 坐标解析，不引用本仓库源码。
# 需要 Android SDK（ANDROID_HOME）、Xcode 及 OHOS Native 工具链。
bash projects/gradlew -p verification-consumer \
  compileDebugKotlinAndroid compileKotlinIosArm64 compileKotlinIosSimulatorArm64 \
  compileKotlinIosX64 linkDebugFrameworkIosSimulatorArm64 linkDebugSharedOhosArm64 verifyKoin
```

发布前可分别通过 `-PkoinMavenRepo=/path/to/koin-staging` 和 `-PstatelyMavenRepo=/path/to/stately-staging` 验证本地待发布产物。发布构建可通过 `-PstatelyMavenRepo=...` 使用待发布 Stately。

验证范围是实际 Android Gradle Plugin `androidTarget()` 解析和编译、iOS KLIB 编译与模拟器 Framework 链接、OHOS 动态库链接，以及 JVM 运行单例/工厂注入检查。设备运行尚未验证。此分支只发布 Koin Core，不包含 `koin-compose`、`koin-compose-viewmodel` 或 Kuikly ViewModel 生命周期接入。
