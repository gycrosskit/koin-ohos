# Koin Core OHOS 适配

此分支基于上游 `4.1.1`，使用 Kotlin `2.2.21-1.0.0`。独立 `ohos-probe` 构建发布 JVM（Android 使用）、`iosArm64`、`iosSimulatorArm64`、`iosX64` 和 `ohosArm64` 变体，复用上游 JVM/Native 源码。三端共用请使用 `-5`。

```kotlin
// settings.gradle.kts 的 dependencyResolutionManagement.repositories
maven { url = uri("https://jitpack.io") }

// 三端共享模块的 commonMain.dependencies
implementation("com.github.gycrosskit.koin-ohos:koin-core:4.1.1-ohos-2.2.21-5")
```

`Stately 2.1.0-ohos-2.2.21-9` 是传递依赖，Gradle 根据 `.module` 元数据选择各端变体。iOS KLIB 在 macOS 构建，GitHub Release 保存构建包，由 JitPack 提供 Maven 依赖。构建时使用上游 `io.insert-koin` 生成 KLIB 身份，Maven Publication 使用 JitPack 组名；Stately 同理保留 `co.touchlab` KLIB 身份。与官方 Koin Compose 同用时，还需按 [README](README.md#范围与验证) 替换官方 Core 与 Stately 的 Maven 依赖，避免重复 KLIB。

## 构建与验证

```bash
bash projects/gradlew -p ohos-probe :koin-core:publishAllPublicationsToGycrosskitRepository
python3 prepare-jitpack-maven.py build/release-maven
COPYFILE_DISABLE=1 tar --no-xattrs -czf koin-maven.tar.gz -C build/release-maven .

# 独立消费者仅通过 Maven 坐标解析，不引用本仓库源码。
# 需要 Android SDK（ANDROID_HOME）、Xcode 及 OHOS Native 工具链。
bash projects/gradlew -p verification-consumer \
  compileDebugKotlinAndroid compileKotlinIosArm64 compileKotlinIosSimulatorArm64 \
  compileKotlinIosX64 linkDebugFrameworkIosSimulatorArm64 linkDebugSharedOhosArm64 verifyKoin

# 官方 Koin Compose 4.1.1 与 fork Core、Stately 在默认 Native 缓存下共同链接。
bash projects/gradlew -p verification-compose-consumer linkDebugFrameworkIosSimulatorArm64
```

发布前可分别通过 `-PkoinMavenRepo=/path/to/koin-staging` 和 `-PstatelyMavenRepo=/path/to/stately-staging` 验证本地待发布产物。发布构建可通过 `-PstatelyMavenRepo=...` 使用待发布 Stately。

验证范围是实际 Android Gradle Plugin `androidTarget()` 解析和编译、iOS KLIB 编译与模拟器 Framework 链接、OHOS 动态库链接，以及 JVM 运行单例/工厂注入检查。新增的 Compose 消费工程专门覆盖 [Issue #4](https://github.com/gycrosskit/koin-ohos/issues/4) 的 iOS 默认缓存链接。设备运行尚未验证。此分支只发布 Koin Core，不包含 `koin-compose`、`koin-compose-viewmodel` 或 Kuikly ViewModel 生命周期接入。

本地未发布修复补齐根 `metadataSourcesElements` 正规化，并在归档前重算已有 `.module` 校验和；JitPack 安装器不再改写归档字节。已发布版本的 API/Native 变体可用，但来源变体 URL 被改写为 API JAR；不能把编译通过视为所有变体正确。新版本须重新构建归档、核对引用/校验值并更新安装器 SHA，不覆盖旧标签或归档。回归入口：`python3 scripts/test-jitpack-metadata.py`。
