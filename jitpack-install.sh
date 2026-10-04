#!/usr/bin/env bash
set -euo pipefail

archive=koin-maven.tar.gz
curl -fL --retry 3 -o "$archive" "https://github.com/gycrosskit/koin-ohos/releases/download/${VERSION}/${archive}"
echo "d6a9382d99a96df5c345b54270ae628e11041e8d31d8a12d559bc35480af95fc  $archive" | sha256sum -c -
mkdir -p "$HOME/.m2/repository" build/release-maven
tar -xzf "$archive" -C "$HOME/.m2/repository"
tar -xzf "$archive" -C build/release-maven

# 在 macOS 归档前正规化 metadata；安装时只解包已校验的相同字节。
