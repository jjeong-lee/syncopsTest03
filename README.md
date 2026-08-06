# 교수업적평가 시스템 공통기능

Java 17 + Spring Boot 3.3.x + MyBatis + PostgreSQL 16 백엔드와 React 18 + TypeScript + Vite 5 프론트엔드로 구성한 공통기능 25개 화면 구현체입니다.

## 실행

```bash
docker compose -f infra/docker-compose.yml up --build
```

- Frontend: http://localhost:3000
- Backend health: http://localhost:8080/api/health
- Seed admin: `admin` / `admin`

## 구현 범위

- 시스템 관리, 파일·데이터 관리, 보안·감사 관리, 시스템 운영 관리의 25개 source-backed 화면 route를 제공합니다.
- 모든 frontend API 호출은 nginx reverse proxy 기준 상대경로 `/api/...`를 사용합니다.
- PostgreSQL schema는 `backend/src/main/resources/db/migration/V1__common_foundation_schema.sql`에 있습니다.
- OpenAPI test fixture는 `backend/src/test/resources/contracts/openapi.yaml`에서 classpath resource로 로드합니다.

## 검증 참고

이번 codegen 요청은 package manager 실행, dependency install, `mvn test` 같은 장시간 명령 실행을 금지하므로 파일 생성 후 정적 검증만 수행합니다. 의존성 설치가 가능한 환경에서는 다음을 실행하세요.

```bash
cd backend && mvn test
cd frontend && npm install && npm run build && npm run test -- --run
docker compose -f infra/docker-compose.yml config
```
