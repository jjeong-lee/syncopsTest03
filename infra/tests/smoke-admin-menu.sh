#!/usr/bin/env bash
set -euo pipefail
BASE_URL=${BASE_URL:-http://localhost:3000}
COOKIE_JAR=${COOKIE_JAR:-/tmp/fpe-common-cookie.txt}
curl -fsS "$BASE_URL/api/health" >/dev/null
curl -fsS -c "$COOKIE_JAR" -H 'Content-Type: application/json' -X POST "$BASE_URL/api/auth/login" -d '{"loginId":"admin","password":"admin"}' >/dev/null
for path in /api/auth/me /api/users /api/organizations /api/organizations/tree /api/roles /api/user-roles /api/menus/tree /api/menus '/api/menu-permissions?targetType=ROLE&targetId=R09' /api/code-groups /api/detail-codes; do
  curl -fsS -b "$COOKIE_JAR" "$BASE_URL$path" >/dev/null
done
printf 'OK admin menu smoke passed
'
