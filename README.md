# 교수업적평가시스템 공통기능 1차 완료 목표

Spring Boot 3.3.x + MyBatis + PostgreSQL 16 backend, React 18 + TypeScript + Vite 5 frontend, nginx reverse proxy, Docker Compose preview 구성을 포함합니다.

## 실행

```sh
docker compose -f infra/docker-compose.yml up --build -d
curl -i http://localhost:8080/api/health
curl -i http://localhost:3000/api/health
```

Frontend: http://localhost:3000
Backend health endpoint: `/api/health`

## 시드 계정

- loginId: `admin`
- password: `admin`
- role: `R09` 시스템관리자

## 1차 목표 화면

로그인 후 다음 9개 시스템 관리 화면을 확인합니다.

- `/system/users` 사용자 관리
- `/system/organizations` 조직 관리
- `/system/roles` 역할 관리
- `/system/user-roles` 사용자 역할 관리
- `/system/menu-permissions` 메뉴 권한 관리
- `/system/menu-structure` 메뉴 구조 관리
- `/system/menu-info` 메뉴 정보 관리
- `/system/code-groups` 코드그룹 관리
- `/system/detail-codes` 상세코드 관리

## 검증 명령

사용자 요청에 따라 코드 생성 중 장기 실행 명령은 실행하지 않았습니다. 로컬에서 다음 명령으로 검증할 수 있습니다.

```sh
cd backend && ./mvnw test
cd frontend && npm test -- --run
docker compose -f infra/docker-compose.yml up --build -d
./scripts/quickstart-smoke.sh
./scripts/verify-relative-api-paths.sh
./scripts/verify-no-business-dependent-scope.sh
./scripts/verify-no-excluded-common-features.sh
```

## 구현 범위 메모

- KORUS 원천 인사·조직 정보는 `korus_staff_snapshot` 로컬 Mock snapshot으로만 조회합니다.
- 파일, Excel, 개인정보, 접속기록 조회, 감사로그 조회 화면, 배치 운영, 교수업적평가 업무 데이터 종속 API는 생성하지 않았습니다.
- 변경 이력 저장 구조(`change_history`)는 포함하지만 감사로그 조회 기능은 범위 밖입니다.
