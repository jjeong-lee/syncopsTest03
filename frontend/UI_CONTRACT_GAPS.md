# UI Contract Gap Notes

` .aiops-spec/ui-design.md`와 현재 `frontend/`/현재 API 매핑을 느슨하게 대조한 결과, 이번 보강 범위에서 꾸며내지 않고 남긴 gap이다.

- 검색 조건은 화면별 입력 필드로 노출했지만 현재 backend 조회 API는 `keyword`/`filter` 단일 문자열만 수신한다. 따라서 frontend는 명시 필드 값을 공백으로 합쳐 `keyword`로 전달하며, field별 query parameter 계약은 backend/OpenAPI와 매핑될 때까지 보류한다.
- 권한 없는 leaf menu 숨김과 직접 route permission-denied는 현재 `GET /api/auth/me`가 R09 seed 세션만 반환하고 화면별 메뉴 권한 목록 API를 제공하지 않아 동적 권한 매트릭스로 구현하지 않았다. 직접 route의 401/403 응답은 기존 permission 상태 표시를 유지한다.
- OpenAPI에는 화면별 operationId가 분리되어 있으나 현재 backend controller는 `/api/admin/cmn/fr/**` wildcard write로 처리한다. frontend는 route/API path와 HTTP method를 보존하되, operationId별 세부 endpoint가 확인되기 전까지 임의 endpoint를 만들지 않는다.
- ASCII wireframe의 좌우 tree/matrix 위치와 세부 컬럼 순서는 기능 계약이 아닌 참고 배치로 두었고, 이번 변경에서는 사용자 기능 계약에 해당하는 필드 노출·payload 제한·읽기 전용 제약만 보강했다.
