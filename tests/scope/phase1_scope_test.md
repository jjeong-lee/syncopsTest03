# Phase 1 Scope Guard

검증 대상은 시스템 관리 9개 기능으로 제한한다. 생성 금지 범위:

- 교수업적평가·학술지원금 업무 데이터 종속 기능
- 파일/Excel/개인정보 관리
- 접속기록/감사로그 product screen
- 배치 운영 화면/API

확인 방법:

```bash
find backend frontend -type f \
  ! -path '*/node_modules/*' ! -path '*/build/*' ! -path '*/dist/*' \
  -print | xargs grep -En "attachment|excel|audit-log|batch|performance-score|grant-application"
```

위 검색 결과가 나오면 1차 범위 밖 구현을 제거한다. 변경 이력 저장 테이블 `change_histories`는 product 감사로그 화면이 아니라 mutation trace persistence 구조이므로 허용한다.
