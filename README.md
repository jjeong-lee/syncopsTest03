# 교수업적평가 시스템 공통기능 1차

## 로컬 실행

Docker Compose v2가 설치된 환경에서 다음 명령으로 backend, frontend, PostgreSQL을 함께 기동합니다.

```bash
docker compose -f infra/docker-compose.yml up --build
```

- Frontend: http://localhost:3000
- Backend health: http://localhost:8080/api/health

## 시드 관리자

로컬 실행 직후 다음 계정으로 로그인할 수 있습니다.

- 사용자 ID: `admin`
- 비밀번호: `admin`

이 계정에는 R09 시스템관리자 역할과 시스템 관리의 9개 목표 메뉴 접근 권한이 시드됩니다.
