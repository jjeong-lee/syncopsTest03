#!/usr/bin/env sh
set -eu
BASE="${BASE:-http://localhost:8080}"
COOKIE="${COOKIE:-/tmp/fpe-cookie.txt}"
curl -fsS "$BASE/api/health" >/dev/null
curl -fsS -c "$COOKIE" -H 'Content-Type: application/json' -d '{"loginId":"admin","password":"admin"}' "$BASE/api/auth/login" >/dev/null
for path in /api/users /api/organizations /api/roles /api/user-roles /api/menu-permissions /api/menus /api/menus/tree /api/code-groups /api/code-groups/STATUS/codes; do
  curl -fsS -b "$COOKIE" "$BASE$path" >/dev/null
done
curl -fsS -b "$COOKIE" -H 'Content-Type: application/json' -X PATCH -d '{"systemUseYn":"Y","reason":"quickstart smoke"}' "$BASE/api/users/00000000-0000-0000-0000-000000000902/usage" >/dev/null
echo "quickstart smoke passed"
