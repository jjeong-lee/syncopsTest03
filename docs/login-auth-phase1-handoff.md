# 로그인 화면 변경 — Phase 1 구현 handoff

## 범위와 기준선

이 기록은 로그인 화면 변경의 Phase 1(T001~T004) 조사 결과다. 이후 Phase 2~5 구현은 아래의 실제 경로와 기존 계약을 사용해야 하며, 새 인증 endpoint, schema, migration, session/token 정책, 재시도 정책, runtime 또는 인증 provider를 추가하지 않는다.

## T001 — 기존 구현과 실행 기준선

| 대상 | 실제 경로 | 확인 내용 |
|---|---|---|
| 프런트엔드 manifest 및 test runner | `frontend/package.json` | React 18, Vite 5, Vitest. 단일 대상/관련 frontend test 명령은 `cd frontend && npm test -- --run <test-file>`이고 전체 frontend suite 명령은 `cd frontend && npm test -- --run`이다. |
| 프런트엔드 앱 shell 및 인증 상태 | `frontend/src/App.tsx` | `SystemShell`이 `/api/health`, `/api/auth/me`를 호출하고 `authorizedMenus`, `menuState`를 관리한다. |
| 보호 route | `frontend/src/AppRouter.tsx` | `ProtectedRoute`가 허용 메뉴에 없는 route에서 `PermissionNotice`를 렌더링한다. |
| API client | `frontend/src/shared/api/client.ts` | `apiRequest`는 상대경로 요청과 `credentials: "include"`를 사용하며 실패 응답을 `ApiRequestError(status, field, message)`로 변환한다. |
| 현재 sidebar | `frontend/src/App.tsx` | `SystemShell`의 `system-sidebar`가 `/api/auth/me`의 `data.menus`로 메뉴를 렌더링한다. |
| 현재 디자인 CSS | `frontend/src/styles.css` | `#4369e3` brand blue, `#222` 본문색, white/gray surface, `content-rail`, header와 `primary-action`이 기존 UI 템플릿·컬러 기준이다. |
| 프런트엔드 인증 회귀 테스트 | `frontend/src/App.test.tsx` | Vitest + Testing Library fetch stub으로 `/api/health`, `/api/auth/me`, 메뉴 노출, 보호 route denied 상태를 검증한다. |
| 백엔드 manifest 및 test runner | `backend/pom.xml` | Java 17, Spring Boot 3.3.12, Maven, MyBatis, Flyway, Spring Boot Test. 단일 대상/전체 backend suite 명령은 각각 `cd backend && mvn test -Dtest=<test-class>` 및 `cd backend && mvn test`다. |
| 백엔드 인증 HTTP 계약 테스트 | `backend/src/test/java/kr/ac/knue/facultyassessment/auth/AuthenticationApiContractTest.java` | Spring Boot + MockMvc로 login, me, logout, invalid credential, unauthenticated access와 `user_session` side effect를 검증한다. |
| 전체 기준선 스크립트 | `infra/verify-phase12.sh` | 순서: scope verification → `mvn test` → `npm test -- --run` → Compose smoke test. |

이번 Phase 1은 조사와 handoff 기록만 수행한다. 기준선 테스트와 package manager, Maven, Compose 명령은 이 단계의 지시대로 실행하지 않았다.

## T002 — 기존 인증 API 계약 대조

- `POST /api/auth/login`
  - backend route: `backend/src/main/java/kr/ac/knue/facultyassessment/auth/AuthenticationController.java`
  - request DTO: `backend/src/main/java/kr/ac/knue/facultyassessment/auth/LoginRequest.java`
  - request body는 `userId`, `password`다.
  - 성공 응답은 `ApiResponse.success(CurrentUserResponse)`이며 `data.menus`를 포함한다.
  - `CurrentUserResponse`는 `userId`, `roleCodes`, `menus`를 제공한다.
  - `frontend/src/App.tsx`는 이미 `/api/auth/me` 성공 응답의 `data.menus`를 `authorizedMenus`에 저장하고 sidebar를 렌더링한다. Phase 2는 login 성공 응답에서도 같은 메뉴 state를 갱신해야 한다.
- `GET /api/auth/me`
  - backend route: `AuthenticationController.currentUser`.
  - session filter: `backend/src/main/java/kr/ac/knue/facultyassessment/auth/SessionAuthorizationFilter.java`.
  - `SESSION` cookie가 없거나 ACTIVE session을 찾지 못하면 JSON `ApiError`와 HTTP 401 (`UNAUTHENTICATED`)을 반환한다.
  - 현재 frontend는 이 401을 포함한 모든 실패를 `menuState = "denied"`로 처리한다. 이 동작이 Phase 3에서 로그인 화면 전환 대상으로 변경된다.
- 계약 대조 결과: `{userId, password}` login 요청, 성공 `data.menus`, unauthenticated `/api/auth/me`의 401이 모두 기존 구현과 일치한다. `OQ-TECH-001` 또는 `OQ-AUTH-002`로 되돌릴 계약 불일치는 발견하지 못했다.

## T003 — DB/schema/migration 읽기 경계

- Flyway migration root: `backend/src/main/resources/db/migration/`.
- 인증 관련 schema: `backend/src/main/resources/db/migration/V1__foundation_schema.sql`.
  - `user_account`: 로그인 credential과 `use_yn`을 보관한다.
  - `user_session`: HttpOnly `SESSION` cookie가 가리키는 ACTIVE/TERMINATED session을 보관한다.
  - `menu`, `menu_permission`, `user_role`: 인증 사용자의 허용 메뉴를 결정한다.
- seed data: `backend/src/main/resources/db/migration/V2__foundation_seed_data.sql`.
- read/write adapter boundary: `backend/src/main/java/kr/ac/knue/facultyassessment/auth/AuthenticationMapper.java`와 `LocalAuthenticationAdapter.java`.
  - login은 기존 `user_account`를 조회하고 기존 `user_session`에 ACTIVE row를 생성한다.
  - current user는 기존 ACTIVE `user_session`, `user_role`, `menu_permission`, `menu`을 읽어 `menus`를 만든다.
  - logout은 기존 `user_session` row의 status만 TERMINATED로 바꾼다.
- 결론: 로그인 화면 흐름은 기존 데이터를 소비하는 frontend 변경이다. 새 table, field, enum 또는 migration은 필요하지 않으며 만들지 않는다.

## T004 — runtime/authentication configuration 경계

- backend config: `backend/src/main/resources/application.yml`과 `backend/src/main/resources/application-prod.yml`.
  - `app.auth.session.secure`는 기본 `false`, prod profile `true`이며 `AuthenticationController`의 HttpOnly/SameSite=Lax cookie 속성에만 전달된다.
  - session lifetime은 `AuthenticationController`에서 기존 8시간으로 설정되어 있다.
  - `app.foundation.enabled`는 기존 authentication component와 filter를 활성화한다.
- runtime wiring: `infra/docker-compose.yml`.
  - backend는 기존 datasource 변수만 전달받고, frontend는 기존 nginx `/api/` proxy를 통해 backend를 호출한다.
  - `backend/src/main/resources/application.yml`의 기본 datasource host도 Compose service `database`다.
- login failure retry limit:
  - `backend/src/main/java/kr/ac/knue/facultyassessment/auth/LocalAuthenticationAdapter.java`, `AuthenticationController.java`, application config, Compose config에서 별도 retry/throttle/lockout 설정이나 구현을 찾지 못했다.
  - 따라서 현재 기준선에는 별도 앱 수준 로그인 재시도 제한이 없다. 이후 구현은 retry policy를 새로 만들거나 변경하지 않고, 기존 API failure를 UI에 표시만 한다.
- 결론: session/token/cookie 정책과 failure handling 경로가 기존 source에 있으므로 새 environment variable, Docker/Compose service, authentication provider를 추가하지 않는다.

## 후속 구현 주의점

- 기존 logout endpoint는 `POST /api/auth/logout`이며 현재 `LoginRequest` body를 받지만 request 값은 사용하지 않는다. Phase 4에서 `handleLogout`은 login endpoint를 재사용하지 않아야 하며, 실제 logout endpoint가 필요한 request body contract를 그대로 확인한 후 사용한다.
- Phase 2~4에서 production code를 바꾸기 전에는 각각의 requirement에 대한 frontend RED test를 먼저 작성하고, 해당 테스트가 기능 부재 또는 기존 denied/login-reuse 동작 때문에 실패함을 확인한다.
