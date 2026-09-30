# Koin Core OHOS 适配

此分支基于上游 `4.1.1`，使用 Kotlin `2.2.21-1.0.0`。独立 `ohos-probe` 构建发布 JVM（Android 使用）、`iosArm64`、`iosSimulatorArm64`、`iosX64` 和 `ohosArm64` 变体，复用上游 JVM/Native 源码。三端共用请使用 `-5`。

消费者的仓库、依赖和官方 Compose 混用配置见 [README](README.md#安装) 与[接入指南](docs/接入指南.md)。Stately `2.1.0-ohos-2.2.21-9` 为传递依赖。发布构建保留上游 `io.insert-koin` / `co.touchlab` KLIB 身份，Maven Publication 则使用 JitPack 组名，避免官方 Native 依赖无法匹配。

## 构建与验证

```bash
bash projects/gradlew -p ohos-probe :koin-core:publishAllPublicationsToGycrosskitRepository
COPYFILE_DISABLE=1 tar -czf koin-maven.tar.gz -C build/release-maven .

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
