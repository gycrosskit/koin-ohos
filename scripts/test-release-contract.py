"""Lightweight release routing contracts; no Gradle or network execution."""
import os
from pathlib import Path
import re
import subprocess
import tempfile
import unittest

ROOT = Path(__file__).resolve().parents[1]


class ReleaseContractTest(unittest.TestCase):
    def test_retired_compose_has_no_modules_or_build_references(self):
        for path in ["projects/compose", "examples/sample-android-compose", "examples/sample-desktop-compose"]:
            self.assertFalse((ROOT / path).exists(), path)
        for directory in ["projects", "examples", "ohos-probe", "verification-consumer"]:
            for path in (ROOT / directory).rglob("*"):
                if not path.is_file() or {"build", ".gradle"}.intersection(path.parts):
                    continue
                if not (path.name.endswith((".gradle", ".gradle.kts")) or path.name == "libs.versions.toml"):
                    continue
                content = path.read_text()
                self.assertNotRegex(content, r"koin-(?:androidx-)?compose|:compose:|sample-(?:android|desktop)-compose", str(path))
                self.assertNotRegex(content, r"(?:org\.jetbrains|androidx)\.compose\.(?:ui|foundation|material3?|animation)(?:[\.:\"])", str(path))
        # Check the configured project graph; dormant upstream samples are outside this build.
        for directory in ["projects", "examples"]:
            base = ROOT / directory
            settings = next(base.glob("settings.gradle*"))
            content = re.sub(r"//[^\n]*", "", settings.read_text())
            included = set(re.findall(r'["\'](:[a-zA-Z0-9_:-]+)["\']', content) if directory == "projects"
                           else re.findall(r'^include ["\']([^"\']+)["\']', content, re.M))
            included = {project.lstrip(":") for project in included}
            for project in included:
                project_directory = base / project.replace(":", "/")
                self.assertTrue(project_directory.is_dir(), project)
                for build in project_directory.glob("build.gradle*"):
                    for target in re.findall(r'project\(["\'](:[^"\']+)["\']\)', build.read_text()):
                        self.assertIn(target.lstrip(":"), included, str(build))

    def test_source_and_public_downloads_have_bounded_waits(self):
        workflows = [(ROOT / ".github/workflows" / name).read_text() for name in
                     ["fork-source-regression.yml", "release-validation.yml", "package-release.yml"]]
        assignments = [line for workflow in workflows for line in workflow.splitlines()
                       if 'echo "JAVA_TOOL_OPTIONS=' in line]
        self.assertEqual(4, len(assignments))
        for line in assignments:
            self.assertIn("-Dsun.net.client.defaultConnectTimeout=30000", line)
            self.assertIn("-Dsun.net.client.defaultReadTimeout=60000", line)
        for workflow, count in zip(workflows, [3, 3, 1]):
            commands = [line for line in workflow.splitlines() if "python3 .ci-shared/templates/ci-run.py " in line]
            self.assertEqual(count, len(commands))
            self.assertTrue(all("--no-progress-seconds 300" in line for line in commands))
            self.assertIn("timeout-minutes: 90", workflow)
        source_commands = [line for line in workflows[0].splitlines() if "bash projects/gradlew " in line]
        self.assertEqual(2, len(source_commands))
        self.assertTrue(all("--rerun-tasks" in line and "--no-build-cache" in line for line in source_commands))

    def test_upstream_release_is_disabled_for_fork(self):
        workflow = (ROOT / ".github/workflows/release.yml").read_text()
        condition = re.search(r"^  release:\n    if: (.+)$", workflow, re.M).group(1)
        self.assertEqual("github.repository == 'InsertKoinIO/koin'", condition)
        for repository, allowed in [("InsertKoinIO/koin", True), ("gycrosskit/koin-ohos", False)]:
            self.assertEqual(allowed, eval(condition.replace("github.repository", "repository"),
                                           {"__builtins__": {}}, {"repository": repository}))

    def test_remote_repository_guard_rejects_overrides(self):
        settings = (ROOT / "verification-consumer/settings.gradle.kts").read_text()
        expression = re.search(r'require\((koinRepository[^\n]+)\) \{', settings).group(1)
        expression = expression.replace("&&", "and")
        for koin, stately, accepted in [
            ("https://jitpack.io", "https://jitpack.io", True),
            ("file:///tmp/staging", "https://jitpack.io", False),
            ("https://jitpack.io", "file:///tmp/staging", False),
            ("mavenLocal", "https://jitpack.io", False),
            ("https://jitpack.io", "mavenLocal", False),
            ("../source", "https://jitpack.io", False),
            ("https://jitpack.io", "../source", False),
            ("https://another.example", "https://jitpack.io", False),
        ]:
            with self.subTest(koin=koin, stately=stately):
                self.assertEqual(accepted, eval(expression, {"__builtins__": {}}, {
                    "koinRepository": koin, "statelyRepository": stately}))
        self.assertIn('require(!providers.gradleProperty("verifyLocalSource").isPresent)', settings)
        self.assertIn("repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)", settings)
        routing = re.search(r'if \(remoteOnly\) \{\n            exclusiveContent \{(.*?)\n        \} else \{', settings, re.S).group(1)
        self.assertEqual(["https://jitpack.io"], re.findall(r'maven\("([^"]+)"\)', routing))
        self.assertEqual({"com.github.gycrosskit.koin-ohos", "com.github.gycrosskit.stately-ohos"},
                         set(re.findall(r'includeGroup\("([^"]+)"\)', routing)))
        for forbidden in ["mavenLocal(", "includeBuild(", "dependencySubstitution"]:
            self.assertNotIn(forbidden, settings)
        self.assertIn("maven(koinRepository)", settings)
        self.assertIn("maven(statelyRepository)", settings)

    def test_every_public_consumer_requires_remote_only(self):
        workflow = (ROOT / ".github/workflows/release-validation.yml").read_text()
        commands = [line for line in workflow.splitlines() if "gradlew" in line and "-p verification-consumer" in line]
        self.assertEqual(2, len(commands))
        self.assertTrue(all("-PremoteOnly" in command for command in commands))
        self.assertTrue(all('"-PkoinCoreVersion=$VERSION"' in command for command in commands))

    def test_wrong_checkout_fails_before_any_download(self):
        with tempfile.TemporaryDirectory() as directory:
            directory = Path(directory)
            git = directory / "git"
            git.write_text("#!/bin/sh\ncase \"$1\" in\n  ls-remote) printf '%s\\n' '" + "a" * 40 + " refs/tags/fixture' '" + "b" * 40 + " refs/tags/fixture^{}' ;;\n  rev-parse) printf '%s\\n' '" + "c" * 40 + "' ;;\n  *) exit 99 ;;\nesac\n")
            curl = directory / "curl"
            marker = directory / "downloaded"
            curl.write_text('#!/bin/sh\ntouch "' + str(marker) + '"\nexit 99\n')
            git.chmod(0o755)
            curl.chmod(0o755)
            environment = os.environ | {"PATH": str(directory) + os.pathsep + os.environ["PATH"], "VERSION": "1.0.0-rc.1"}
            result = subprocess.run(["bash", "scripts/verify-release.sh"], cwd=ROOT,
                                    env=environment, capture_output=True, text=True)
            self.assertNotEqual(0, result.returncode)
            self.assertIn("Checkout does not match the exact remote tag commit", result.stderr)
            self.assertFalse(marker.exists())


if __name__ == "__main__":
    unittest.main()
