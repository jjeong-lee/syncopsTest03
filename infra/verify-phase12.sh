#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

python3 "$ROOT_DIR/infra/verify-scope.py"
python3 "$ROOT_DIR/infra/verify-docker-healthchecks.py"
(
  cd "$ROOT_DIR/backend"
  mvn test
)
(
  cd "$ROOT_DIR/frontend"
  npm test -- --run
)
"$ROOT_DIR/infra/compose-smoke-test.sh"

echo "Phase 12 verification passed: backend tests, frontend tests, scope verification, and Compose smoke test."
