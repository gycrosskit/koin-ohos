#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
: "${VERSION:?Provide an immutable Maven tag}"
[[ "$VERSION" =~ ^[0-9][0-9A-Za-z._-]*$ ]] || { echo 'Invalid version' >&2; exit 1; }
refs="$(git ls-remote https://github.com/gycrosskit/koin-ohos.git "refs/tags/$VERSION" "refs/tags/$VERSION^{}")"
commit="$(printf '%s\n' "$refs" | awk '$2 ~ /\^\{\}$/ {peeled=$1} $2 !~ /\^\{\}$/ {direct=$1} END {print peeled ? peeled : direct}')"
[[ "$commit" =~ ^[a-f0-9]{40}$ ]] || { echo 'Tag has no exact commit' >&2; exit 1; }
test "$(git rev-parse HEAD)" = "$commit" || { echo 'Checkout does not match the exact remote tag commit' >&2; exit 1; }
checksum="$(awk -v version="$VERSION" '$1 == version {print $2}' release-checksums.txt)"
[[ "$checksum" =~ ^[a-f0-9]{64}$ ]] || { echo "Missing frozen checksum for $VERSION" >&2; exit 1; }
publications=koin-core,koin-core-jvm,koin-core-iosarm64,koin-core-iossimulatorarm64,koin-core-iosx64,koin-core-ohosarm64
output="$(mktemp -d)"
trap 'rm -rf "$output"' EXIT
archive="$output/koin-ohos-maven.tar.gz"
curl -fsSL --retry 3 --connect-timeout 30 --max-time 300 -o "$archive" "https://github.com/gycrosskit/koin-ohos/releases/download/$VERSION/koin-ohos-maven.tar.gz"
printf '%s  %s\n' "$checksum" "$archive" | shasum -a 256 -c -
python3 - "$archive" "$output/maven" <<'EXTRACT'
import sys, tarfile
from pathlib import Path
root = Path(sys.argv[2]).resolve()
with tarfile.open(sys.argv[1]) as bundle:
    for member in bundle.getmembers():
        assert member.isfile() or member.isdir(), member.name
        assert (root / member.name).resolve().is_relative_to(root), member.name
    bundle.extractall(root)
EXTRACT
python3 scripts/check-maven.py "$output/maven" com.github.gycrosskit.koin-ohos "$VERSION" koin-core ios_arm64,ios_x64,ios_simulator_arm64,ohos_arm64 "$publications" --jvm-only
python3 .ci-shared/templates/check-public-maven.py --repo koin-ohos --version "$VERSION" --commit "$commit" --expected-publications "$publications" --output-dir "${CI_DIAGNOSTICS_DIR:-ci-diagnostics}/public"
