# Koin Core OHOS 适配

当前宿主沿用已验收版本 `4.1.1-ohos-2.2.21-8-rc.1`，发布状态、冻结归档及该版本验收以精确 [Release](https://github.com/gycrosskit/koin-ohos/releases/tag/4.1.1-ohos-2.2.21-8-rc.1) 为准。[测试源码、执行平台与各轮验证](docs/功能与平台差异.md#验证结果与测试入口)区分本地删除验证、历史 CI 与消费结果。

本轮只合并源码清理，不发布新制品。当前源码已移除上游 Compose 模块、示例及专属接线，Core 公共 API 与 KLIB 身份保持不变；既有 RC 归档不包含这次物理删除。下文 `4.1.1-ohos-2.2.21-7` 及更早 tag 的验收保留为历史事实，不代表本次源码清理的验收。

此分支基于上游 `4.1.1`，使用 Kotlin `2.2.21-1.0.0`。独立 `ohos-probe` 构建发布 JVM（Android 使用）、`iosArm64`、`iosSimulatorArm64`、`iosX64` 和 `ohosArm64` 变体，复用上游 JVM/Native 源码。三端共用版本与坐标见 [README](README.md#安装)。

当前消费者的仓库、依赖和容器生命周期见 [README](README.md#安装) 与[接入指南](docs/接入指南.md)。Stately `2.1.0-ohos-2.2.21-11` 为传递依赖。发布构建保留上游 `io.insert-koin` / `co.touchlab` KLIB 身份，Maven Publication 则使用 JitPack 组名，避免官方 Native 依赖无法匹配。

## 构建与验证

```bash
# 后续独立发布：先将 NEW_RC_VERSION 设置为新的不可变 RC，不重建或覆盖旧版本。
VERSION="$NEW_RC_VERSION" bash scripts/package-maven.sh

# 独立消费者仅通过 Maven 坐标解析，不引用本仓库源码。
# 需要 Android SDK（ANDROID_HOME）、Xcode 及 OHOS Native 工具链。
VERSION=4.1.1-ohos-2.2.21-8-rc.1
bash projects/gradlew -p verification-consumer -PremoteOnly "-PkoinCoreVersion=$VERSION" \
  compileDebugKotlinAndroid compileKotlinIosArm64 compileKotlinIosSimulatorArm64 \
  compileKotlinIosX64 linkDebugFrameworkIosSimulatorArm64 linkDebugSharedOhosArm64 verifyKoin
```

打包输出在 `build/release/<版本>/`，包括归档、`SHA256SUMS` 与 `source-receipt.json`。核对 producer 的精确提交、tree 与归档 SHA-256 后，将真实哈希追加到 `release-checksums.txt`；不使用占位哈希，不覆盖旧版本。创建同版本不可变 tag 与 prerelease 并上传相同字节后，通过 [Fork release validation](.github/workflows/release-validation.yml) 验证公开文件及三端消费者。

发布前本地 staging 检查可分别传入 `-PkoinMavenRepo=/path/to/koin-staging` 和 `-PstatelyMavenRepo=/path/to/stately-staging`，但不能同时使用 `-PremoteOnly`。这些检查不是公开坐标消费证据；当前 Stately 仍为 `2.1.0-ohos-2.2.21-11`。

验证范围是实际 Android Gradle Plugin `androidTarget()` 解析和编译、iOS KLIB 编译与模拟器 Framework 链接、OHOS 动态库链接，以及 JVM 运行单例/工厂注入检查。当前退出独立 CMP 验证；[Issue #4](https://github.com/gycrosskit/koin-ohos/issues/4) 的旧默认缓存混用工程保留在 [4.1.1-ohos-2.2.21-7](https://github.com/gycrosskit/koin-ohos/tree/4.1.1-ohos-2.2.21-7/verification-compose-consumer)，复验时使用该 tag 的原脚本。设备运行尚未验证。此分支只发布 Koin Core，不包含 `koin-compose`、`koin-compose-viewmodel` 或 Kuikly ViewModel 生命周期接入。

`4.1.1-ohos-2.2.21-6` 与配套 Stately `2.1.0-ohos-2.2.21-10` 已从真实 JitPack 坐标通过当时的消费者验证，包括 iOS arm64/模拟器 Framework、OHOS 动态库、JVM 注入断言及默认 Native 缓存的官方 Compose 混用链接。

归档发布前规范化根 `metadataSourcesElements` 并重算校验和，安装器只安装校验后的相同字节。6 个真实远程 publication 的 POM、全部变体文件、大小、四种声明哈希、ZIP CRC、available-at 和内部依赖均通过；Release 重下载 SHA-256 一致。JitPack 额外生成的 root identity redirect 与高阶 sidecar 的 404 单列为渠道边界，公开 MD5/SHA-1 及必要变体引用正常。回归入口：`python3 scripts/test-jitpack-metadata.py`。旧标签和归档未覆盖。
