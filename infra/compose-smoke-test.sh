#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
COMPOSE_FILE="$ROOT_DIR/infra/docker-compose.yml"
PROJECT_NAME="faculty-assessment-smoke-${RANDOM}${RANDOM}"
BACKEND_URL="http://localhost:8080"
FRONTEND_URL="http://localhost:3000"
COOKIE_JAR="$(mktemp)"

cleanup() {
  rm -f "$COOKIE_JAR"
  docker compose --project-name "$PROJECT_NAME" -f "$COMPOSE_FILE" down --volumes --remove-orphans
}
trap cleanup EXIT

wait_for_healthy_service() {
  local service="$1"
  local attempts=0
  while (( attempts < 30 )); do
    local container_id
    container_id="$(docker compose --project-name "$PROJECT_NAME" -f "$COMPOSE_FILE" ps -q "$service")"
    if [[ -n "$container_id" ]] && [[ "$(docker inspect --format '{{.State.Health.Status}}' "$container_id")" == "healthy" ]]; then
      return 0
    fi
    attempts=$((attempts + 1))
    sleep 2
  done
  echo "서비스가 healthy 상태가 되지 않았습니다: $service" >&2
  return 1
}

assert_status() {
  local expected_status="$1"
  shift
  local actual_status
  actual_status="$(curl --silent --show-error --output /dev/null --write-out '%{http_code}' "$@")"
  if [[ "$actual_status" != "$expected_status" ]]; then
    echo "예상 HTTP $expected_status, 실제 HTTP $actual_status: $*" >&2
    return 1
  fi
}

docker compose --project-name "$PROJECT_NAME" -f "$COMPOSE_FILE" up --build --detach
wait_for_healthy_service database
wait_for_healthy_service backend
wait_for_healthy_service frontend

curl --fail --silent --show-error "$BACKEND_URL/api/health" | grep -q '"success":true'
curl --fail --silent --show-error --cookie-jar "$COOKIE_JAR" \
  --header 'Content-Type: application/json' \
  --data '{"userId":"admin","password":"admin"}' \
  "$BACKEND_URL/api/auth/login" | grep -q '"success":true'

for route in \
  /system/user-organization/users \
  /system/user-organization/organizations \
  /system/roles-permissions/roles \
  /system/roles-permissions/user-roles \
  /system/roles-permissions/menu-permissions \
  /system/menus/structure \
  /system/menus/information \
  /system/common-codes/groups \
  '/system/common-codes/detail-codes?groupId=CG-EMPLOYMENT-STATUS'; do
  assert_status 200 "$FRONTEND_URL$route"
done

for api_path in \
  /api/users \
  /api/organizations \
  /api/roles \
  /api/users/member/roles \
  /api/menu-permissions \
  /api/menus \
  /api/code-groups \
  /api/code-groups/CG-EMPLOYMENT-STATUS/detail-codes; do
  assert_status 200 --cookie "$COOKIE_JAR" "$BACKEND_URL$api_path"
done

assert_status 401 "$BACKEND_URL/api/users"
echo "Compose smoke test passed: health, admin login, 9 UI routes, 9 target read flows, unauthenticated protection"
