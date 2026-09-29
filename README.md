# Koin Core OpenHarmony 适配

本分支基于上游 Koin 4.1.1，使用 Kotlin `2.2.21-1.0.0`，为 Android、iOS、OpenHarmony 的 KMP 共享模块提供 Koin Core。详细实现、构建命令和验证边界见 [OHOS_PORT.md](OHOS_PORT.md)；上游英文介绍保存在 [README_EN.md](README_EN.md)。

## 引入依赖

在 `settings.gradle.kts` 的 `dependencyResolutionManagement.repositories` 中加入 JitPack：

```kotlin
maven { url = uri("https://jitpack.io") }
```

在共享模块中引入：

```kotlin
commonMain.dependencies {
    implementation("com.github.gycrosskit.koin-ohos:koin-core:4.1.1-ohos-2.2.21-5")
}
```

此版本提供 JVM（供 Android 使用）、`iosArm64`、`iosSimulatorArm64`、`iosX64` 和 `ohosArm64` 变体。`stately-ohos` 是传递依赖；Gradle 根据发布的 KMP 元数据选择平台产物。iOS KLIB 在 macOS 上构建，版本化归档由 GitHub Release 保存，再经 JitPack 提供 Maven 依赖。

## 范围与验证

本分支只发布 `koin-core`，不包含 `koin-compose`、`koin-compose-viewmodel` 或 Kuikly ViewModel 生命周期接入。新版在 JitPack Maven 坐标下保留上游 `io.insert-koin` KLIB 身份；传递的 Stately 版本也保留 `co.touchlab` KLIB 身份。与官方 Koin Compose 4.1.1 同用时，在消费模块替换官方 Core 与 Stately Maven 依赖，确保每个 Native KLIB 只有一份：

```kotlin
configurations.configureEach {
    resolutionStrategy.dependencySubstitution {
        substitute(module("io.insert-koin:koin-core"))
            .using(module("com.github.gycrosskit.koin-ohos:koin-core:4.1.1-ohos-2.2.21-5"))
        listOf("stately-strict", "stately-concurrency", "stately-concurrent-collections").forEach { name ->
            substitute(module("co.touchlab:$name"))
                .using(module("com.github.gycrosskit.stately-ohos:$name:2.1.0-ohos-2.2.21-9"))
        }
    }
}
```

已验证 Android 编译、iOS KLIB 编译与模拟器 Framework 链接、OHOS 动态库链接及 JVM 注入检查；iOS/OHOS 设备运行尚未验证。验证命令见 [OHOS_PORT.md](OHOS_PORT.md)。

许可证见 [LICENSE](LICENSE)。
