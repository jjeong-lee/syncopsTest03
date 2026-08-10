#!/usr/bin/env python3
"""Fail when generated application code contains a Phase 12 excluded capability."""

from __future__ import annotations

import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parents[1]
SOURCE_ROOTS = (ROOT / "backend", ROOT / "frontend", ROOT / "infra")
EXCLUDED_PATTERNS = {
    "attachment capability": r"(?i)(attachment|첨부파일)",
    "Excel capability": r"(?i)(excel|xlsx|spreadsheet)",
    "personal-information capability": r"(?i)(personal[-_ ]?information|개인정보)",
    "access-log capability": r"(?i)(access[-_ ]?log|접속기록)",
    "batch capability": r"(?i)(batch[-_ ]?(job|task|work)|배치작업)",
    "evaluation-domain capability": r"(?i)(evaluation[-_ ]?(rule|period|target|score)|업적입력|평가규칙|평가대상자|점수산출)",
    "external KORUS integration": r"(?i)(https?://[^\s\"']*korus|WebClient|RestTemplate|FeignClient)",
}
ALLOWED_FILES = {ROOT / "infra" / "verify-scope.py"}
SOURCE_SUFFIXES = {".java", ".ts", ".tsx", ".xml", ".yml", ".yaml", ".conf", ".sh"}


def source_files() -> list[pathlib.Path]:
    return [
        path
        for source_root in SOURCE_ROOTS
        for path in source_root.rglob("*")
        if path.is_file() and path.suffix in SOURCE_SUFFIXES and path not in ALLOWED_FILES
    ]


def main() -> int:
    violations: list[str] = []
    for path in source_files():
        content = path.read_text(encoding="utf-8")
        for label, pattern in EXCLUDED_PATTERNS.items():
            if re.search(pattern, content):
                violations.append(f"{path.relative_to(ROOT)}: {label}")
    if violations:
        print("Excluded-scope capability detected:", file=sys.stderr)
        print("\n".join(violations), file=sys.stderr)
        return 1
    print("Scope verification passed: no excluded capability was found in backend/, frontend/, or infra/.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
