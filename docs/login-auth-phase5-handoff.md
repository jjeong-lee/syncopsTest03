# 로그인 화면 변경 — Phase 5 통합 검증 handoff

## 범위

이 기록은 로그인 화면 변경의 최종 통합 검증(T019~T021) 대상과 재현 명령을 남긴다. 이번 phase에서는 기존 인증 API, DB schema/migration, 세션·토큰 정책, Docker/Compose 설정을 변경하지 않았다.

## T019 — 미인증 진입 → 로그인 → 허용 메뉴

통합 회귀 검증은 `frontend/src/App.test.tsx`의 `인증 전환 통합 회귀` suite에 추가했다.

- 보호 route `/system/roles-permissions/menu-permissions` 진입 시 `/api/auth/me`의 `401`을 받아 `auth-login-screen`을 표시한다.
- denied 안내 `접근 권한이 있는 메뉴를 선택하세요.`가 로그인 화면에 남지 않는 것을 확인한다.
- `admin` 자격 증명 제출이 `POST /api/auth/login`의 `{userId, password}` body로 전송되는 것을 확인한다.
- 성공 응답 `data.menus`에 포함된 `메뉴 권한 관리`만 sidebar에 표시하고, 응답에 없는 `사용자 관리`는 표시하지 않는 것을 확인한다.

실행 명령:

```bash
cd frontend && npm test -- --run src/App.test.tsx -t "인증 전환 통합 회귀"
```

## T020 — 로그인 상태 로그아웃

`frontend/src/App.test.tsx`의 `로그아웃` suite가 로그인 후 기존 `POST /api/auth/logout` 요청과 로그인 화면 복귀를 검증한다.

- `shell-logout-button`을 누른 뒤 `auth-login-screen`이 표시된다.
- `{userId: "admin", password: ""}` body로 `/api/auth/login`에 POST하지 않는다.
- 기존 `/api/auth/logout` 요청은 로그인에 사용한 자격 증명 body를 유지한다. 이는 기존 서버 `LoginRequest` 계약을 따르며, 새 logout API를 추가하지 않는다.

실행 명령:

```bash
cd frontend && npm test -- --run src/App.test.tsx -t "로그아웃"
```

## T021 — 실행 순서와 결과

Phase 1에서 확인한 실제 runner 순서는 단일 frontend target → 인증 관련 frontend suite → backend 인증 contract suite → 전체 기준선 script다.

| 순서 | 명령 | 대상 | 결과 |
|---|---|---|---|
| 1 | `cd frontend && npm test -- --run src/App.test.tsx -t "인증 전환 통합 회귀"` | T019 통합 흐름 | ad-hoc verification으로 실행 시도했으나 `vitest: not found`로 차단됨 — `frontend/node_modules`에 의존성이 materialize되지 않았고 codegen 지시상 설치를 수행하지 않음 |
| 2 | `cd frontend && npm test -- --run src/App.test.tsx -t "로그아웃"` | T020 로그아웃 흐름 | 미실행 — 동일 사유 |
| 3 | `cd frontend && npm test -- --run src/App.test.tsx` | 인증 관련 frontend suite | 미실행 — 동일 사유 |
| 4 | `cd backend && mvn test -Dtest=AuthenticationApiContractTest` | 기존 login/me/logout MockMvc contract 및 session persistence | 미실행 — codegen 지시가 Maven 실행을 금지함 |
| 5 | `./infra/verify-phase12.sh` | scope verification → backend 전체 suite → frontend 전체 suite → Compose smoke | 미실행 — codegen 지시가 package manager, Maven, Compose 실행을 금지함 |

정적 확인 결과:

- `git diff --check`가 통과하여 이번 phase에서 추가한 테스트에 trailing whitespace 오류가 없다.
- `frontend/src/App.test.tsx`에는 T019의 단일 end-to-end UI 상태 전환과 T020의 네트워크 요청 부재 검증이 있다.
- T019은 OS-safe temporary script `/tmp/hermes-verify-mb92mauz.sh`로 ad-hoc 실행을 시도했고, 실행 직후 해당 script를 제거했다. 의존성 미설치로 `vitest: not found`가 반환되어 suite green 근거는 아니다.
- 후속 OS-safe temporary script `/tmp/hermes-verify-ze73dovn.py`는 T019의 401 로그인 전환, login request, 허용 메뉴/비허용 메뉴 assertion과 `App.tsx`의 login·unauthenticated 경계를 정적으로 검사해 통과했고, 실행 직후 제거했다. 이는 source-level ad-hoc 확인이며 browser 또는 Vitest 실행 통과를 대체하지 않는다.
- 실제 실행 결과는 위 명령을 허용된 환경에서 실행한 뒤 이 handoff의 결과 열에 기록해야 한다. 실행 실패 시 해당 Story의 RED/GREEN cycle로 되돌아가며 새 API, schema, infrastructure로 우회하지 않는다.

## 변경 파일

- `frontend/src/App.test.tsx` — T019 통합 회귀 테스트 추가
- `docs/login-auth-phase5-handoff.md` — T019~T021 재현 명령 및 실행 제한 기록
