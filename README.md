# 한국교원대학교 교수업적평가시스템 공통기능 1차

Spring Boot 3.3.x + MyBatis + PostgreSQL 16 backend, React 18 + Vite 5 frontend, Docker Compose runtime으로 구성한 공통기능 1차 구현입니다.

## 실행

```bash
docker compose -f infra/docker-compose.yml config
docker compose -f infra/docker-compose.yml up --build
```

- frontend: http://localhost:3000
- backend health: http://localhost:3000/api/health
- seed login: `admin/admin`

## 1차 목표 화면

로그인 후 다음 9개 route가 렌더링됩니다.

- `/system/users` 사용자 관리
- `/system/organizations` 조직 관리
- `/system/roles` 역할 관리
- `/system/user-roles` 사용자 역할 관리
- `/system/menu-permissions` 메뉴 권한 관리
- `/system/menu-structure` 메뉴 구조 관리
- `/system/menu-info` 메뉴 정보 관리
- `/system/code-groups` 코드그룹 관리
- `/system/codes` 상세코드 관리

## 검증 명령

사용자 지시에 따라 코드 생성 중 package manager 실행, dependency install, 장시간 test/build 실행은 수행하지 않았습니다. 검토 시 아래 명령으로 확인할 수 있습니다.

```bash
cd backend && mvn test
cd frontend && npm install && npm test && npm run build
bash infra/tests/smoke-admin-menu.sh
python scripts/scope_guard.py
python scripts/execution_persistence_guard.py
```

브라우저 API client는 `/api/...` 상대경로만 사용하며, nginx가 `/api/*`를 backend service로 reverse proxy합니다.
