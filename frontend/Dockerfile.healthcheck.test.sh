#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DOCKERFILE="$ROOT_DIR/Dockerfile"

if ! grep -Eq '^FROM nginx:1\.29\.4-alpine$' "$DOCKERFILE"; then
  echo "frontend Dockerfile must use nginx:1.29.4-alpine with the Trivy-fixed Alpine packages" >&2
  exit 1
fi

if ! grep -Eq '^RUN[[:space:]]+apk upgrade --no-cache$' "$DOCKERFILE"; then
  echo "frontend Dockerfile must apply Alpine packages' available security fixes" >&2
  exit 1
fi

if ! grep -Eq '^HEALTHCHECK[[:space:]]+.*wget[[:space:]].*http://localhost/' "$DOCKERFILE"; then
  echo "frontend Dockerfile must define an HTTP HEALTHCHECK for nginx" >&2
  exit 1
fi

if ! grep -Eq '^USER[[:space:]]+nginx$' "$DOCKERFILE"; then
  echo "frontend Dockerfile must run nginx as its built-in non-root user" >&2
  exit 1
fi

if ! grep -Eq '^RUN[[:space:]]+chown -R nginx:nginx /var/cache/nginx && touch /var/run/nginx.pid && chown nginx:nginx /var/run/nginx.pid$' "$DOCKERFILE"; then
  echo "frontend Dockerfile must grant nginx ownership of its runtime cache and PID file" >&2
  exit 1
fi
