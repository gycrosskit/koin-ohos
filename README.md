# Koin：GY CrossKit 分支说明

这是 [InsertKoinIO/koin](https://github.com/InsertKoinIO/koin) 的分支仓库。默认 `main` 跟随上游代码，**不包含** GY CrossKit 的 OpenHarmony 适配；上游 Koin 的完整英文介绍保存在 [README_EN.md](README_EN.md)。

## OpenHarmony 适配

适配代码与发布配置位于 [`codex/ohos-4.1.1` 分支](https://github.com/gycrosskit/koin-ohos/tree/codex/ohos-4.1.1)。请以该分支的 [OHOS_PORT.md](https://github.com/gycrosskit/koin-ohos/blob/codex/ohos-4.1.1/OHOS_PORT.md) 查看 JitPack 坐标、支持目标和验证范围；不要使用默认 `main` 的构建结果判断鸿蒙版本是否可用。

该适配基于 Koin Core 4.1.1，供 Android、iOS、OpenHarmony 的 KMP 共享模块使用，不包含 Koin Compose。普通 Koin 使用与上游版本更新请参考 [官方文档](https://insert-koin.io/docs/setup/koin)。

许可证见 [LICENSE](LICENSE)。
