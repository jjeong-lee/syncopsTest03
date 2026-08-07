# 교수업적평가 공통기능 25개

Java 17 + Spring Boot 3.3.x + MyBatis + PostgreSQL 16 백엔드와 React 18 + TypeScript + Vite 5 프론트엔드로 구성된 공통기능 관리 애플리케이션입니다.

## 로컬 실행

```bash
docker compose -f infra/docker-compose.yml up --build
```

- Frontend: http://localhost:3000
- Backend health: http://localhost:8080/api/health
- Swagger UI: http://localhost:3000/swagger-ui/index.html

## 시드 관리자 계정

- 아이디: `admin`
- 비밀번호: `admin` (`APP_LOCAL_ADMIN_PASSWORD`로 변경 가능)
- 역할: `R09` 시스템관리자

## 구현 범위

25개 관리 화면은 `/admin/...` 경로에 구성되어 있으며 모든 브라우저 API 호출은 상대경로 `/api/...`를 사용합니다. 첨부파일 메타데이터와 관리 데이터는 PostgreSQL에 저장하고, 파일 저장소는 Docker named volume `attachment-files`를 사용하도록 분리했습니다.
