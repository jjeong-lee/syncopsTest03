#!/usr/bin/env bash
set -euo pipefail
login() { local user=$1 cookie=$2; curl -fsS -c "$cookie" -H 'Content-Type: application/json' -d "{"loginId":"$user","password":"admin"}" http://localhost:8080/api/auth/login >/dev/null; }
admin_cookie=$(mktemp); support_cookie=$(mktemp); professor_cookie=$(mktemp)
login admin "$admin_cookie"; login support01 "$support_cookie"; login prof01 "$professor_cookie"
curl -fsS -b "$admin_cookie" -X PATCH -H 'Content-Type: application/json' -d '{"systemEnabled":"Y","reason":"smoke"}' http://localhost:8080/api/users/professor-001/usage | grep -q '"success":true'
curl -fsS -b "$support_cookie" http://localhost:8080/api/users | grep -q '"success":true'
readonly_status=$(curl -s -o /dev/null -w '%{http_code}' -b "$support_cookie" -X PATCH -H 'Content-Type: application/json' -d '{"systemEnabled":"N","reason":"smoke"}' http://localhost:8080/api/users/professor-001/usage)
test "$readonly_status" = "403"
blocked_status=$(curl -s -o /dev/null -w '%{http_code}' -b "$professor_cookie" http://localhost:8080/api/users)
test "$blocked_status" = "403"
