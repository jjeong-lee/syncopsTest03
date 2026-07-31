CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS app_user (
  user_id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  login_id varchar(64) NOT NULL UNIQUE,
  password_hash varchar(255) NOT NULL,
  staff_no varchar(32),
  system_use_yn char(1) NOT NULL DEFAULT 'Y' CHECK (system_use_yn IN ('Y','N')),
  account_status varchar(20) NOT NULL DEFAULT 'ACTIVE' CHECK (account_status IN ('ACTIVE','INACTIVE','LOCKED')),
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now()
);
COMMENT ON TABLE app_user IS '교수업적평가시스템 내부 로그인 계정과 시스템 사용 여부를 관리한다.';
COMMENT ON COLUMN app_user.account_status IS 'ACTIVE:활성|INACTIVE:비활성|LOCKED:잠금';
COMMENT ON COLUMN app_user.staff_no IS 'korus_staff_snapshot.staff_no 참조 의도 (FK 미선언)';

CREATE TABLE IF NOT EXISTS organization (
  organization_id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  organization_code varchar(32) NOT NULL UNIQUE,
  organization_name varchar(200) NOT NULL,
  organization_type varchar(30) NOT NULL CHECK (organization_type IN ('UNIVERSITY','GRADUATE_SCHOOL','COLLEGE','DEPARTMENT','ADMIN_DEPARTMENT')),
  use_yn char(1) NOT NULL DEFAULT 'Y' CHECK (use_yn IN ('Y','N')),
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now()
);
COMMENT ON TABLE organization IS '대학·대학원·단과대학·학과·부서 기준정보를 조직코드 기준으로 보존한다.';
COMMENT ON COLUMN organization.organization_type IS 'UNIVERSITY:대학교|GRADUATE_SCHOOL:대학원|COLLEGE:단과대학|DEPARTMENT:학과|ADMIN_DEPARTMENT:행정부서';

CREATE TABLE IF NOT EXISTS korus_staff_snapshot (
  staff_no varchar(32) PRIMARY KEY,
  staff_name varchar(100) NOT NULL,
  organization_code varchar(32) NOT NULL REFERENCES organization(organization_code),
  position_name varchar(100),
  rank_name varchar(100),
  employment_status varchar(20) NOT NULL CHECK (employment_status IN ('ACTIVE','RETIRED','LEAVE')),
  retired_at date,
  last_synced_at timestamptz NOT NULL DEFAULT now(),
  status varchar(20) NOT NULL DEFAULT 'SNAPSHOT_ACTIVE' CHECK (status IN ('SNAPSHOT_ACTIVE','SNAPSHOT_EXPIRED')),
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now()
);
COMMENT ON TABLE korus_staff_snapshot IS 'KORUS 원천 인사 정보를 로컬 조회 전용 Mock snapshot으로 제공한다.';
COMMENT ON COLUMN korus_staff_snapshot.status IS 'SNAPSHOT_ACTIVE:활성스냅샷|SNAPSHOT_EXPIRED:만료스냅샷';
COMMENT ON COLUMN korus_staff_snapshot.employment_status IS 'ACTIVE:재직|RETIRED:퇴직|LEAVE:휴직';

CREATE TABLE IF NOT EXISTS organization_relation_history (
  relation_id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  organization_id uuid NOT NULL REFERENCES organization(organization_id),
  parent_organization_id uuid REFERENCES organization(organization_id),
  valid_from date NOT NULL,
  valid_to date,
  change_reason varchar(500),
  status varchar(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE','ENDED')),
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now()
);
COMMENT ON TABLE organization_relation_history IS '조직 상하위 관계와 적용기간 변경 이력을 누적 보존한다.';
COMMENT ON COLUMN organization_relation_history.status IS 'ACTIVE:활성|ENDED:종료';

CREATE TABLE IF NOT EXISTS staff_assignment (
  assignment_id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  staff_no varchar(32) NOT NULL REFERENCES korus_staff_snapshot(staff_no),
  organization_id uuid NOT NULL REFERENCES organization(organization_id),
  assignment_type varchar(20) NOT NULL CHECK (assignment_type IN ('POSITION','ORGANIZATION')),
  title varchar(100),
  valid_from date NOT NULL,
  valid_to date,
  status varchar(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE','ENDED')),
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now()
);
COMMENT ON TABLE staff_assignment IS '교직원 보직 또는 조직 사용자 매핑을 KORUS 후보 정보와 분리해 보존한다.';
COMMENT ON COLUMN staff_assignment.status IS 'ACTIVE:활성|ENDED:종료';
COMMENT ON COLUMN staff_assignment.assignment_type IS 'POSITION:보직|ORGANIZATION:조직';

CREATE TABLE IF NOT EXISTS role (
  role_code varchar(10) PRIMARY KEY,
  role_name varchar(100) NOT NULL,
  purpose varchar(500) NOT NULL,
  grant_criteria varchar(500),
  default_data_scope varchar(50) NOT NULL CHECK (default_data_scope IN ('SELF','DEPARTMENT','COLLEGE','ADMIN','ALL')),
  use_yn char(1) NOT NULL DEFAULT 'Y' CHECK (use_yn IN ('Y','N')),
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now()
);
COMMENT ON TABLE role IS 'R01~R09 역할 코드와 목적·부여기준·기본 데이터 범위를 관리한다.';
COMMENT ON COLUMN role.default_data_scope IS 'SELF:본인|DEPARTMENT:학과|COLLEGE:단과대학|ADMIN:행정범위|ALL:전체';

CREATE TABLE IF NOT EXISTS user_role_assignment (
  assignment_id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id uuid NOT NULL REFERENCES app_user(user_id),
  role_code varchar(10) NOT NULL REFERENCES role(role_code),
  grant_type varchar(20) NOT NULL CHECK (grant_type IN ('MANUAL','POSITION_BASED')),
  valid_from date NOT NULL,
  valid_to date,
  approver_user_id uuid REFERENCES app_user(user_id),
  status varchar(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE','REVOKED','EXPIRED')),
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now()
);
COMMENT ON TABLE user_role_assignment IS '사용자별 역할 부여·회수와 유효기간 및 승인자를 기록한다.';
COMMENT ON COLUMN user_role_assignment.status IS 'ACTIVE:활성|REVOKED:회수|EXPIRED:만료';
COMMENT ON COLUMN user_role_assignment.grant_type IS 'MANUAL:수동|POSITION_BASED:보직기반';

CREATE TABLE IF NOT EXISTS menu (
  menu_id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  parent_menu_id uuid REFERENCES menu(menu_id),
  menu_level integer NOT NULL,
  display_order integer NOT NULL,
  menu_name varchar(200) NOT NULL,
  screen_id varchar(80) UNIQUE,
  url_path varchar(255),
  icon_name varchar(80),
  business_area varchar(80),
  description varchar(500),
  use_yn char(1) NOT NULL DEFAULT 'Y' CHECK (use_yn IN ('Y','N')),
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now()
);
COMMENT ON TABLE menu IS '시스템 관리 3단계 메뉴 구조와 실행 화면 연결 정보를 관리한다.';

CREATE TABLE IF NOT EXISTS menu_permission (
  permission_id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  menu_id uuid NOT NULL REFERENCES menu(menu_id),
  subject_type varchar(20) NOT NULL CHECK (subject_type IN ('ROLE','ORGANIZATION','USER')),
  subject_id varchar(64) NOT NULL,
  access_allowed boolean NOT NULL DEFAULT false,
  function_allowed boolean NOT NULL DEFAULT false,
  decision_effect varchar(10) NOT NULL CHECK (decision_effect IN ('ALLOW','DENY')),
  use_yn char(1) NOT NULL DEFAULT 'Y' CHECK (use_yn IN ('Y','N')),
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now(),
  UNIQUE(menu_id, subject_type, subject_id)
);
COMMENT ON TABLE menu_permission IS '역할·조직·사용자 대상 메뉴 접근과 기능 권한을 저장해 화면과 서버가 동일하게 사용한다.';
COMMENT ON COLUMN menu_permission.subject_id IS 'role.role_code 또는 organization.organization_id 또는 app_user.user_id 참조 의도 (다형 FK 미선언)';
COMMENT ON COLUMN menu_permission.decision_effect IS 'ALLOW:허용|DENY:차단';

CREATE TABLE IF NOT EXISTS code_group (
  group_id varchar(50) PRIMARY KEY,
  group_name varchar(200) NOT NULL,
  description varchar(500),
  managing_department varchar(100),
  use_yn char(1) NOT NULL DEFAULT 'Y' CHECK (use_yn IN ('Y','N')),
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now()
);
COMMENT ON TABLE code_group IS '공통코드 묶음의 그룹ID·명칭·설명·관리부서를 관리한다.';

CREATE TABLE IF NOT EXISTS detail_code (
  detail_code_id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  group_id varchar(50) NOT NULL REFERENCES code_group(group_id),
  code_value varchar(50) NOT NULL,
  code_name varchar(200) NOT NULL,
  parent_detail_code_id uuid REFERENCES detail_code(detail_code_id),
  sort_order integer NOT NULL,
  extra_attributes jsonb,
  valid_from date,
  valid_to date,
  use_yn char(1) NOT NULL DEFAULT 'Y' CHECK (use_yn IN ('Y','N')),
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now(),
  UNIQUE(group_id, code_value)
);
COMMENT ON TABLE detail_code IS '코드그룹별 상세코드 값, 명칭, 계층, 정렬순서, 유효기간, 추가속성을 관리한다.';
COMMENT ON COLUMN detail_code.extra_attributes IS 'CommonCodeService.create/update 시 요청 JSON 속성으로 갱신';

CREATE TABLE IF NOT EXISTS user_session (
  session_id varchar(128) PRIMARY KEY,
  user_id uuid NOT NULL REFERENCES app_user(user_id),
  issued_at timestamptz NOT NULL DEFAULT now(),
  expires_at timestamptz NOT NULL,
  status varchar(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE','EXPIRED','REVOKED')),
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now()
);
COMMENT ON TABLE user_session IS '내부 계정 인증 세션과 현재 사용자 조회 상태를 보존한다.';
COMMENT ON COLUMN user_session.status IS 'ACTIVE:활성|EXPIRED:만료|REVOKED:철회';

CREATE TABLE IF NOT EXISTS change_history (
  history_id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  entity_name varchar(80) NOT NULL,
  entity_id varchar(128) NOT NULL,
  operation_type varchar(20) NOT NULL CHECK (operation_type IN ('CREATE','UPDATE','DELETE','DISABLE','REVOKE')),
  before_value jsonb,
  after_value jsonb,
  operator_user_id uuid NOT NULL REFERENCES app_user(user_id),
  reason varchar(500),
  created_at timestamptz NOT NULL DEFAULT now()
);
COMMENT ON TABLE change_history IS '관리 데이터 변경 전후 값, 처리자, 처리일시, 사유를 append-only로 보존한다.';
COMMENT ON COLUMN change_history.operation_type IS 'CREATE:생성|UPDATE:수정|DELETE:삭제|DISABLE:비활성화|REVOKE:회수';
COMMENT ON COLUMN change_history.before_value IS 'ChangeHistoryService.record 호출 시 변경 전 snapshot으로 갱신';
COMMENT ON COLUMN change_history.after_value IS 'ChangeHistoryService.record 호출 시 변경 후 snapshot으로 갱신';

CREATE INDEX IF NOT EXISTS idx_app_user_staff_no ON app_user(staff_no);
CREATE INDEX IF NOT EXISTS idx_korus_staff_org ON korus_staff_snapshot(organization_code);
CREATE INDEX IF NOT EXISTS idx_org_relation_child ON organization_relation_history(organization_id, status);
CREATE INDEX IF NOT EXISTS idx_user_role_user ON user_role_assignment(user_id, status);
CREATE INDEX IF NOT EXISTS idx_menu_parent_order ON menu(parent_menu_id, display_order);
CREATE INDEX IF NOT EXISTS idx_menu_permission_subject ON menu_permission(subject_type, subject_id);
CREATE INDEX IF NOT EXISTS idx_detail_code_group ON detail_code(group_id, sort_order);
CREATE INDEX IF NOT EXISTS idx_change_history_entity ON change_history(entity_name, entity_id);
