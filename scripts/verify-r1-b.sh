#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
COMPOSE_FILE="$ROOT_DIR/infra/docker-compose.yml"
PROJECT_NAME="faculty-assessment-r1-b-${RANDOM}${RANDOM}"
BACKEND_URL="http://localhost:8080"
FRONTEND_URL="http://localhost:3000"
COOKIE_JAR="$(mktemp)"

cleanup() {
  rm -f "$COOKIE_JAR"
  docker compose --project-name "$PROJECT_NAME" -f "$COMPOSE_FILE" down --volumes --remove-orphans
}
trap cleanup EXIT

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

request_json() {
  local output_file="$1"
  shift
  curl --fail --silent --show-error --output "$output_file" "$@"
}

assert_success() {
  python3 - "$1" <<'PY'
import json
import sys

with open(sys.argv[1], encoding="utf-8") as response_file:
    if json.load(response_file).get("success") is not True:
        raise SystemExit("success=true 응답이 필요합니다.")
PY
}

assert_menu_requery() {
  python3 - "$1" <<'PY'
import json
import sys

with open(sys.argv[1], encoding="utf-8") as response_file:
    response = json.load(response_file)
menus = response.get("data") or []
matches = [menu for menu in menus if menu.get("menuId") == "MENU-MENU-USAGE-MANAGEMENT"]
if response.get("success") is not True or len(matches) != 1 or matches[0].get("useYn") != "Y" or not matches[0].get("exposureStartAt"):
    raise SystemExit("메뉴 사용 관리 저장 후 재조회 값이 올바르지 않습니다.")
PY
}

assert_code_requery() {
  python3 - "$1" "$2" <<'PY'
import json
import sys

with open(sys.argv[1], encoding="utf-8") as response_file:
    response = json.load(response_file)
matches = [
    code for code in response.get("data") or []
    if code.get("codeValue") == sys.argv[2]
    and code.get("useYn") == "Y"
    and code.get("applicationStartDate") == "2026-01-01"
]
if response.get("success") is not True or len(matches) != 1:
    raise SystemExit("코드 사용 관리 저장 후 재조회 값이 올바르지 않습니다.")
PY
}

assert_settings_requery() {
  python3 - "$1" "$2" <<'PY'
import json
import sys

expected = {
    "common": {
        "sessionIdleMinutes": 30,
        "pageSize": 20,
        "defaultSearchPeriodDays": 30,
        "bulkQueryThreshold": 1000,
        "longRunningWorkNoticeSeconds": 60,
    },
    "reference": {
        "currentEvaluationYear": 2026,
        "defaultSearchYear": 2025,
        "targetYear": 2027,
        "referenceDataCopyYn": "Y",
        "initializationYn": "N",
    },
}[sys.argv[2]]
with open(sys.argv[1], encoding="utf-8") as response_file:
    response = json.load(response_file)
if response.get("success") is not True or response.get("data") != expected:
    raise SystemExit(f"{sys.argv[2]} 설정 저장 후 재조회 값이 올바르지 않습니다.")
PY
}

(
  cd "$ROOT_DIR/backend"
  mvn -Dtest=R1BRegressionTest test
  mvn -DskipTests package
)
(
  cd "$ROOT_DIR/frontend"
  npm run test -- --run
  npm run build
)
docker compose -f "$COMPOSE_FILE" config >/dev/null

docker compose --project-name "$PROJECT_NAME" -f "$COMPOSE_FILE" up --build --detach
wait_for_healthy_service database
wait_for_healthy_service backend
wait_for_healthy_service frontend

health_response="$(mktemp)"
login_response="$(mktemp)"
menus_response="$(mktemp)"
menu_save_response="$(mktemp)"
menu_requery_response="$(mktemp)"
code_group_save_response="$(mktemp)"
code_save_response="$(mktemp)"
code_requery_response="$(mktemp)"
common_save_response="$(mktemp)"
common_requery_response="$(mktemp)"
reference_save_response="$(mktemp)"
reference_requery_response="$(mktemp)"
trap 'rm -f "$health_response" "$login_response" "$menus_response" "$menu_save_response" "$menu_requery_response" "$code_group_save_response" "$code_save_response" "$code_requery_response" "$common_save_response" "$common_requery_response" "$reference_save_response" "$reference_requery_response"; cleanup' EXIT

request_json "$health_response" "$BACKEND_URL/api/health"
assert_success "$health_response"
request_json "$login_response" --cookie-jar "$COOKIE_JAR" --header 'Content-Type: application/json' --data '{"userId":"admin","password":"admin"}' "$BACKEND_URL/api/auth/login"
assert_success "$login_response"

for route in /system/menus/usage /system/common-codes/usage /system/settings/common /system/settings/reference-year; do
  assert_status 200 "$FRONTEND_URL$route"
done

request_json "$menus_response" --cookie "$COOKIE_JAR" "$BACKEND_URL/api/menus"
assert_menu_requery "$menus_response"
menu_payload="$(python3 - "$menus_response" <<'PY'
import json
import sys

with open(sys.argv[1], encoding="utf-8") as response_file:
    menu = next(item for item in json.load(response_file)["data"] if item["menuId"] == "MENU-MENU-USAGE-MANAGEMENT")
print(json.dumps({key: menu.get(key) for key in ("menuName", "parentMenuId", "displayOrder", "screenId", "url", "icon", "businessCategory", "description", "useYn", "exposureStartAt", "exposureEndAt")}))
PY
)"
request_json "$menu_save_response" --cookie "$COOKIE_JAR" --header 'Content-Type: application/json' --data "$menu_payload" "$BACKEND_URL/api/menus"
assert_success "$menu_save_response"
request_json "$menu_requery_response" --cookie "$COOKIE_JAR" "$BACKEND_URL/api/menus"
assert_menu_requery "$menu_requery_response"

group_id="R1B-SMOKE-${RANDOM}${RANDOM}"
code_value="R1B-SMOKE-CODE-${RANDOM}${RANDOM}"
request_json "$code_group_save_response" --cookie "$COOKIE_JAR" --header 'Content-Type: application/json' --data "{\"groupId\":\"$group_id\",\"groupName\":\"R1-B 스모크 코드그룹\"}" "$BACKEND_URL/api/code-groups"
assert_success "$code_group_save_response"
code_payload="{\"codeValue\":\"$code_value\",\"codeName\":\"R1-B 스모크 코드\",\"displayOrder\":1,\"useYn\":\"Y\",\"applicationStartDate\":\"2026-01-01\"}"
request_json "$code_save_response" --cookie "$COOKIE_JAR" --header 'Content-Type: application/json' --data "$code_payload" "$BACKEND_URL/api/code-groups/$group_id/detail-codes"
assert_success "$code_save_response"
request_json "$code_requery_response" --cookie "$COOKIE_JAR" "$BACKEND_URL/api/code-groups/$group_id/detail-codes?includeEnded=true"
assert_code_requery "$code_requery_response" "$code_value"

request_json "$common_save_response" --cookie "$COOKIE_JAR" --header 'Content-Type: application/json' --data '{"sessionIdleMinutes":30,"pageSize":20,"defaultSearchPeriodDays":30,"bulkQueryThreshold":1000,"longRunningWorkNoticeSeconds":60}' "$BACKEND_URL/api/settings/common"
assert_success "$common_save_response"
request_json "$common_requery_response" --cookie "$COOKIE_JAR" "$BACKEND_URL/api/settings/common"
assert_settings_requery "$common_requery_response" common

request_json "$reference_save_response" --cookie "$COOKIE_JAR" --header 'Content-Type: application/json' --data '{"currentEvaluationYear":2026,"defaultSearchYear":2025,"targetYear":2027,"referenceDataCopyYn":"Y","initializationYn":"N"}' "$BACKEND_URL/api/settings/reference-years"
assert_success "$reference_save_response"
request_json "$reference_requery_response" --cookie "$COOKIE_JAR" "$BACKEND_URL/api/settings/reference-years"
assert_settings_requery "$reference_requery_response" reference

assert_status 401 "$BACKEND_URL/api/settings/common"
echo "R1-B verification passed: focused regression test, backend/frontend builds, four R09 routes, and four operation save/requery flows."
