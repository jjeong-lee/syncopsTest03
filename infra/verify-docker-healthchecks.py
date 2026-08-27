#!/usr/bin/env python3
"""Validate that application image Dockerfiles define executable health checks."""

from __future__ import annotations

import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parents[1]
EXPECTED_HEALTHCHECKS = {
    ROOT / "backend" / "Dockerfile": r"(?im)^HEALTHCHECK\s+.*\bCMD\s+wget\b.*http://localhost:8080/api/health",
    ROOT / "frontend" / "Dockerfile": r"(?im)^HEALTHCHECK\s+.*\bCMD\s+wget\b.*http://localhost/",
}


def main() -> int:
    missing = [
        str(path.relative_to(ROOT))
        for path, pattern in EXPECTED_HEALTHCHECKS.items()
        if not re.search(pattern, path.read_text(encoding="utf-8"))
    ]
    if missing:
        print("Missing required Dockerfile HEALTHCHECK instruction:", file=sys.stderr)
        print("\n".join(missing), file=sys.stderr)
        return 1
    print("Dockerfile health check verification passed.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
