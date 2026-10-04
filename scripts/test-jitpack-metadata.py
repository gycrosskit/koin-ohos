"""Check publication normalization before the archive is installed unchanged."""
import hashlib
import json
from pathlib import Path
import subprocess
import sys
import tempfile
import unittest


class MetadataTest(unittest.TestCase):
    def test_native_api_and_checksums_survive_source_cleanup(self):
        repo = Path(__file__).resolve().parents[1]
        script = repo / "prepare-jitpack-maven.py"
        variants = [
            {"name": "metadataApiElements", "files": [{"url": "root.jar"}]},
            {"name": "metadataSourcesElements", "files": [{"url": "root-sources.jar"}]},
            {"name": "iosArm64ApiElements-published", "files": [{"url": "native.klib"}], "dependencies": [{"module": "native-dependency"}]},
            {"name": "iosArm64SourcesElements-published"},
            {"name": "iosArm64MetadataElements-published"},
        ]
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            roots = (root / ".m2/repository/com/github/gycrosskit" / repo.name, root / "build/release-maven")
            for staging in roots:
                staging.mkdir(parents=True)
                module = staging / "test.module"
                module.write_text(json.dumps({"variants": variants}))
                for algorithm in ("md5", "sha1", "sha256", "sha512"):
                    module.with_name(module.name + "." + algorithm).write_text("stale")
            result = subprocess.run([sys.executable, str(script), *map(str, roots)], text=True, capture_output=True)
            self.assertEqual(result.returncode, 0, result.stderr)
            for staging in roots:
                module = staging / "test.module"
                content = module.read_bytes()
                kept = json.loads(content)["variants"]
                self.assertEqual(kept, [variants[0], variants[2]])
                for algorithm in ("md5", "sha1", "sha256", "sha512"):
                    self.assertEqual(module.with_name(module.name + "." + algorithm).read_text(), hashlib.new(algorithm, content).hexdigest())
            before = [(staging / "test.module").read_bytes() for staging in roots]
            repeated = subprocess.run([sys.executable, str(script), *map(str, roots)], text=True, capture_output=True)
            self.assertEqual(repeated.returncode, 0, repeated.stderr)
            self.assertEqual(before, [(staging / "test.module").read_bytes() for staging in roots])


if __name__ == "__main__":
    unittest.main()
