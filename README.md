# 교수업적평가 시스템 공통기능 1차

Spring Boot 3.3.x + MyBatis + PostgreSQL 16 backend, React 18 + Vite 5 frontend, Docker Compose runtime으로 구성된 공통기능 1차 구현입니다.

## 실행

```bash
docker compose -f infra/docker-compose.yml up --build
```

- frontend: http://localhost
- backend health: http://localhost/api/health 또는 http://localhost:8080/api/health
- seed 관리자: `admin` / `admin`

## 주요 범위

- LOGIN
- CMN-USER-MGMT 사용자 관리
- CMN-ORG-MGMT 조직 관리
- CMN-ROLE-MGMT 역할 관리
- CMN-USER-ROLE-MGMT 사용자 역할 관리
- CMN-MENU-AUTH-MGMT 메뉴 권한 관리
- CMN-MENU-STRUCT-MGMT 메뉴 구조 관리
- CMN-MENU-INFO-MGMT 메뉴 정보 관리
- CMN-CODE-GROUP-MGMT 코드그룹 관리
- CMN-DETAIL-CODE-MGMT 상세코드 관리

제외 범위인 파일, Excel, 개인정보, 접속기록, 감사로그 조회, 배치 운영, 교수업적평가 업무 데이터 종속 API와 화면은 생성하지 않았습니다.

## 검증 예시

```bash
# backend contract tests
cd backend && mvn test

# frontend unit tests
cd frontend && npm test

# health check
curl -f http://localhost/api/health
```

이번 codegen 요청의 제약에 따라 dependency install, `mvn test`, Docker build/up 같은 장시간 명령은 실행하지 않았습니다.
