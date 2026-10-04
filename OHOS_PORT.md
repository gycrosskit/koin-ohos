# Koin Core OHOS 适配

此分支基于上游 `4.1.1`，使用 Kotlin `2.2.21-1.0.0`。独立 `ohos-probe` 构建发布 JVM（Android 使用）、`iosArm64`、`iosSimulatorArm64`、`iosX64` 和 `ohosArm64` 变体，复用上游 JVM/Native 源码。三端共用请使用 `-6`。

消费者的仓库、依赖和官方 Compose 混用配置见 [README](README.md#安装) 与[接入指南](docs/接入指南.md)。Stately `2.1.0-ohos-2.2.21-10` 为传递依赖。发布构建保留上游 `io.insert-koin` / `co.touchlab` KLIB 身份，Maven Publication 则使用 JitPack 组名，避免官方 Native 依赖无法匹配。

## 构建与验证

```bash
bash projects/gradlew -p ohos-probe :koin-core:publishAllPublicationsToGycrosskitRepository
python3 prepare-jitpack-maven.py build/release-maven
COPYFILE_DISABLE=1 tar --no-xattrs -czf koin-ohos-maven.tar.gz -C build/release-maven .

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

`4.1.1-ohos-2.2.21-6` 与配套 Stately `2.1.0-ohos-2.2.21-10` 已从真实 JitPack 坐标通过上述消费者验证，包括 iOS arm64/模拟器 Framework、OHOS 动态库、JVM 注入断言及默认 Native 缓存的官方 Compose 混用链接。

归档发布前规范化根 `metadataSourcesElements` 并重算校验和，安装器只安装校验后的相同字节。6 个真实远程 publication 的 POM、全部变体文件、大小、四种声明哈希、ZIP CRC、available-at 和内部依赖均通过；Release 重下载 SHA-256 一致。JitPack 额外生成的 root identity redirect 与高阶 sidecar 的 404 单列为渠道边界，公开 MD5/SHA-1 及必要变体引用正常。回归入口：`python3 scripts/test-jitpack-metadata.py`。旧标签和归档未覆盖。
