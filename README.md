# 교수업적평가 시스템 공통기능 1차 범위

한국교원대학교 교수업적평가 시스템의 시스템 관리 공통기능 9개 화면을 구현한 Spring Boot/MyBatis + React/Vite 애플리케이션입니다.

## 실행

```bash
docker compose -f infra/docker-compose.yml up --build
```

- Frontend: http://localhost:3000
- Backend health: http://localhost:8080/api/health
- Seed 관리자: `admin/admin`

## 포함 범위

- 사용자 관리: `/system/users`
- 조직 관리: `/system/organizations`
- 역할 관리: `/system/roles`
- 사용자 역할 관리: `/system/user-roles`
- 메뉴 권한 관리: `/system/menu-permissions`
- 메뉴 구조 관리: `/system/menu-structure`
- 메뉴 정보 관리: `/system/menu-info`
- 코드그룹 관리: `/system/code-groups`
- 상세코드 관리: `/system/code-details`

KORUS 인사·조직 정보는 로컬 PostgreSQL seed 기반 Mock snapshot으로만 조회되며 외부 KORUS/SSO/API에 접속하지 않습니다. 브라우저 API 호출은 상대경로 `/api/...`만 사용하고 nginx가 backend 서비스로 proxy합니다.

## 검증 스크립트

```bash
scripts/smoke/docker-compose-smoke.sh
scripts/smoke/authorization-smoke.sh
```

이번 코드 생성 지시에는 package manager 실행과 장시간 명령 실행 금지가 포함되어 있어, lockfile 생성과 실제 `mvn test`/`npm test`/compose 실행은 수행하지 않았습니다.
