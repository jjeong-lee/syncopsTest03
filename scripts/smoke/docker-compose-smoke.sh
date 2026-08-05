#!/usr/bin/env bash
set -euo pipefail
docker compose -f infra/docker-compose.yml up --build -d
trap 'docker compose -f infra/docker-compose.yml down' EXIT
curl -fsS http://localhost:8080/api/health | grep -q 'UP'
cookie=$(mktemp)
curl -fsS -c "$cookie" -H 'Content-Type: application/json' -d '{"loginId":"admin","password":"admin"}' http://localhost:8080/api/auth/login | grep -q 'R09'
for path in /api/users /api/organizations /api/organizations/tree /api/roles /api/user-roles /api/menu-permissions /api/menus/tree /api/menus /api/code-groups /api/code-details; do
  curl -fsS -b "$cookie" "http://localhost:8080$path" | grep -q '"success":true'
done
