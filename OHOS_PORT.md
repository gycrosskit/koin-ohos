# Koin Core OHOS 适配

此分支基于上游 `4.1.1`，以独立 `ohos-probe` 构建脚本发布 `koin-core` 的 `ohosArm64` KMP 变体，使用 Kotlin `2.2.21-1.0.0`。上游原有构建配置未改。Android/iOS 继续使用上游正式版本。

```kotlin
// settings.gradle.kts 的 dependencyResolutionManagement.repositories
maven { url = uri("https://gycrosskit.github.io/koin-ohos/maven") }
maven { url = uri("https://gycrosskit.github.io/stately-ohos/maven") }

// OHOS 目标模块的 commonMain.dependencies
implementation("io.insert-koin:koin-core:4.1.1-ohos-2.2.21-1")
```

`Stately` 是传递依赖，Gradle 会根据 `.module` 元数据选择其 OHOS KLIB。两个公开 Maven 地址均无需下载凭据；产物保存在各自仓库的 `docs/maven`，由 GitHub Pages 提供。

```bash
bash projects/gradlew -p ohos-probe :koin-core:compileKotlinOhosArm64
bash projects/gradlew -p ohos-probe :koin-core:publishAllPublicationsToGycrosskitRepository
```

当前只适配并验证 Koin Core，不包含 `koin-compose`、`koin-compose-viewmodel`。已在目标工程验证源码方式的 `koinApplication` 编译与 OHOS 链接；发布产物仍需单独验证远程解析与设备运行。
