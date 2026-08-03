# 교수업적평가 공통기능 1차 구축

한국교원대학교 교수업적평가시스템의 시스템 관리 영역 중 9개 공통기능을 구현한 Spring Boot/MyBatis/PostgreSQL + React/Vite 애플리케이션입니다.

## 실행

```bash
docker compose -f infra/docker-compose.yml up -d --build
```

- Frontend: http://localhost:3000
- Backend health: http://localhost:8080/api/health
- OpenAPI docs: http://localhost:8080/swagger-ui.html

## 시드 계정

- 아이디: `admin`
- 비밀번호: `admin`
- 역할: `R09` 시스템관리자

비밀번호는 DB에 SHA-256 hash로 저장되며 원문 비밀번호를 저장하지 않습니다.

## 주요 화면

- `/login` 로그인
- `/system/users` 사용자 관리
- `/system/organizations` 조직 관리
- `/system/roles` 역할 관리
- `/system/user-roles` 사용자 역할 관리
- `/system/menu-permissions` 메뉴 권한 관리
- `/system/menu-structure` 메뉴 구조 관리
- `/system/menu-info` 메뉴 정보 관리
- `/system/code-groups` 코드그룹 관리
- `/system/code-groups/EVAL_AREA/codes` 상세코드 관리

## API smoke 예시

```bash
curl -i http://localhost:8080/api/health
curl -i -c cookies.txt -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"admin"}'
curl -i -b cookies.txt http://localhost:8080/api/auth/me
curl -i -b cookies.txt http://localhost:8080/api/admin/users
curl -i -b cookies.txt http://localhost:8080/api/admin/roles
```

## 범위 제한

1차 범위는 사용자·조직, 역할·권한, 메뉴, 공통코드의 9개 기능으로 제한합니다. 파일, Excel, 개인정보 관리, 접속기록, 감사로그 조회 화면, 배치 운영, 교수업적평가 업무 데이터 기능은 생성하지 않았습니다.
