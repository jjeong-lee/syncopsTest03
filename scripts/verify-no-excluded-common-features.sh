#!/usr/bin/env sh
set -eu
if grep -R --exclude='*.test.java' --exclude='*.test.ts' --exclude='*.test.tsx' -E "/api/(files|excel|personal-info|access-logs|audit-logs|batches)" backend/src/main frontend/src 2>/dev/null; then
  echo "Excluded common feature API or route found" >&2
  exit 1
fi
echo "excluded common feature guard passed"
