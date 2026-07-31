#!/usr/bin/env sh
set -eu
SCAN_PATHS="frontend/src"
if [ -d frontend/dist ]; then
  SCAN_PATHS="$SCAN_PATHS frontend/dist"
fi
if grep -R --exclude='*.test.ts' --exclude='*.test.tsx' -E "https?://localhost|backend:|database:" $SCAN_PATHS; then
  echo "Forbidden absolute API endpoint literal found" >&2
  exit 1
fi
grep -R --exclude='*.test.ts' --exclude='*.test.tsx' "'/api'\|\"/api\"" frontend/src >/dev/null
echo "relative API path verification passed"
