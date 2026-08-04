# 한국교원대학교 교수업적평가시스템 — 공통기능 1차

이 저장소는 시스템 관리 영역의 1차 완료 목표 9개 기능을 구현한다.

## 기술 스택

- Backend: Java 17, Spring Boot 3.3.x, Maven, MyBatis, Flyway, PostgreSQL 16
- Frontend: React 18, TypeScript, Vite 5, nginx `/api/*` reverse proxy
- Infra: Docker Compose (`infra/docker-compose.yml`)

## 실행

```bash
docker compose -f infra/docker-compose.yml up -d --build --wait
```

앱 서비스 포트:

- Frontend: http://localhost:3000
- Backend API: 프론트엔드 nginx reverse proxy를 통해 `/api/...` 상대경로로 호출

시드 관리자 계정:

- 아이디: `admin`
- 비밀번호: `admin`
- 역할: `R09` 시스템관리자

## 빠른 API 검증

```bash
BASE_URL=http://localhost:3000
curl -i "$BASE_URL/api/health"
curl -i -c cookies.txt -X POST "$BASE_URL/api/auth/login" \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"admin"}'
curl -i -b cookies.txt "$BASE_URL/api/auth/me"
curl -i -b cookies.txt "$BASE_URL/api/users"
curl -i -b cookies.txt "$BASE_URL/api/organizations"
curl -i -b cookies.txt "$BASE_URL/api/roles"
curl -i -b cookies.txt "$BASE_URL/api/user-roles"
curl -i -b cookies.txt "$BASE_URL/api/menu-permissions"
curl -i -b cookies.txt "$BASE_URL/api/menus/tree"
curl -i -b cookies.txt "$BASE_URL/api/menus"
curl -i -b cookies.txt "$BASE_URL/api/code-groups"
curl -i -b cookies.txt "$BASE_URL/api/code-groups/USE_YN/codes"
```

각 응답은 `success`, `data` 또는 `error`, `timestamp`를 포함하는 envelope 형태다.

## 화면 범위

로그인 후 다음 9개 관리 화면을 제공한다.

- `/system/users` 사용자 관리
- `/system/organizations` 조직 관리
- `/system/roles` 역할 관리
- `/system/user-roles` 사용자 역할 관리
- `/system/menu-permissions` 메뉴 권한 관리
- `/system/menu-structure` 메뉴 구조 관리
- `/system/menu-info` 메뉴 정보 관리
- `/system/code-groups` 코드그룹 관리
- `/system/detail-codes` 상세코드 관리

## 테스트 작성 범위

- Backend: `backend/src/test/java/kr/ac/knue/fpe/contract/CommonApiContractTest.java`
- Frontend: `frontend/tests/relativeApiClient.test.ts`, `frontend/tests/AppRoutes.test.tsx`

로컬 검증은 Docker Compose 기동 검증과 각 디렉터리의 테스트 스크립트를 기준으로 수행한다. Frontend 런타임은 nginx가 정적 파일을 제공하고 `/api/` 요청만 backend 서비스로 프록시한다.
