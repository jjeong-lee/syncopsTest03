# 구현 경로 조사 — Phase 1 / T002

## 조사 범위와 보존 규칙

이 문서는 기존 source tree와 검증 명령을 기록한다. Phase 1은 조사·증빙 단계이므로 application source, migration, seed, API operation, frontend route, role allocation, 메뉴 구조, code value/name, and runtime behavior are unchanged.

## Backend — `backend/`

- stack: Java 17, Spring Boot `3.3.12`, Maven, MyBatis, Flyway, PostgreSQL driver
- application entry point: `backend/src/main/java/kr/ac/knue/facultyassessment/FacultyAssessmentApplication.java`
- transport/service/mapper convention: feature packages contain `*Controller.java`, `*Service.java`, `*Mapper.java`, and request/search DTOs; SQL mappers are under `backend/src/main/resources/mapper/`
- persistence convention: Flyway migrations are under `backend/src/main/resources/db/migration/`; existing migrations are `V1__foundation_schema.sql` through `V4__assignment_management.sql`
- HTTP envelope and error handling: `backend/src/main/java/kr/ac/knue/facultyassessment/common/`
- session and menu authorization boundaries: `backend/src/main/java/kr/ac/knue/facultyassessment/auth/`
- existing menu and detail-code feature boundaries: `backend/src/main/java/kr/ac/knue/facultyassessment/menus/` and `backend/src/main/java/kr/ac/knue/facultyassessment/detailcodes/`
- existing MockMvc contract tests: `backend/src/test/java/kr/ac/knue/facultyassessment/**/**ApiContractTest.java`
- backend test command: `cd backend && mvn test`
- backend Docker build: `backend/Dockerfile` uses Maven build stage `build` and an executable Spring Boot JAR runtime stage
- build-context exclusions: `backend/.dockerignore`

## Frontend — `frontend/`

- stack: React `18.3.1`, TypeScript, Vite `5.4.14`, Vitest, npm
- application shell and sidebar grouping: `frontend/src/App.tsx`
- protected route convention: `frontend/src/AppRouter.tsx`
- relative API client and common error representation: `frontend/src/shared/api/client.ts`
- reusable visual source of truth: `frontend/src/styles.css`; feature-local assignment styling is `frontend/src/features/assignments/assignment-management.css`
- feature convention: each existing screen resides under `frontend/src/features/<feature>/` with a matching `.test.tsx` file
- test setup/configuration: `frontend/src/test-setup.ts`, `frontend/vitest.config.ts`
- TypeScript module resolution: `bundler` in `frontend/tsconfig.app.json`
- frontend test command: `cd frontend && npm run test -- --run`
- frontend build command: `cd frontend && npm run build`
- frontend Docker build: `frontend/Dockerfile` has a Node `build` stage and nginx runtime stage
- nginx preserves the `/api/` reverse proxy in `frontend/nginx.conf`; browser code uses only relative `/api/...` paths
- build-context exclusions: `frontend/.dockerignore`

## Infrastructure — `infra/`

- Docker Compose contract: `infra/docker-compose.yml`
- services: `database` (`postgres:16.4-alpine`), `backend`, and `frontend`
- published application ports: backend `8080:8080`; frontend `3000:80`; database has no published host port
- health contract: backend `/api/health`, with Compose health checks for database, backend, and frontend
- focused scope check: `python3 infra/verify-scope.py`
- full existing phase verifier: `infra/verify-phase12.sh` runs scope validation, Maven tests, frontend tests, and Compose smoke
- Compose smoke command: `infra/compose-smoke-test.sh` (creates an isolated Compose project, verifies health/login/routes/API reads, then cleans up)

## Later implementation map

| Future task area | Existing extension points |
|---|---|
| menu usage period | menu controller/service/mapper, `MenuManagementMapper.xml`, Flyway migrations, menu contract tests, `AppRouter.tsx`, `App.tsx` menu grouping |
| detail-code usage period | detail-code controller/service/mapper, `DetailCodeManagementMapper.xml`, Flyway migrations, detail-code contract tests, code-group/detail-code UI feature paths |
| common settings and reference years | new settings feature package following the controller → service → mapper convention, Flyway migrations, existing API envelope/auth filters, new UI feature directories plus protected routes/sidebar grouping |

No implementation command was executed in this code-generation phase. The next phase must create its required failing tests before adding production code, then use the recorded focused commands for GREEN/regression verification.
