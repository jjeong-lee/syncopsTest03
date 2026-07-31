#!/usr/bin/env sh
set -eu
if grep -R --exclude='*.test.java' --exclude='*.test.ts' --exclude='*.test.tsx' -E "/api/(achievements|evaluation-rules|scores|reports|applications)" backend/src/main frontend/src 2>/dev/null; then
  echo "Out-of-scope business API or route found" >&2
  exit 1
fi
echo "business-dependent scope guard passed"
