CREATE TABLE IF NOT EXISTS app_user (
  user_id varchar(64) PRIMARY KEY,
  login_id varchar(80) NOT NULL UNIQUE,
  password_hash varchar(128) NOT NULL,
  staff_no varchar(40),
  display_name varchar(100) NOT NULL,
  org_code varchar(40),
  position_name varchar(100),
  employment_status varchar(20) NOT NULL DEFAULT 'ACTIVE' CHECK (employment_status IN ('ACTIVE','LEAVE','RETIRED')),
  system_enabled char(1) NOT NULL DEFAULT 'Y' CHECK (system_enabled IN ('Y','N')),
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now()
);
COMMENT ON TABLE app_user IS '내부 로그인 계정과 시스템 사용여부를 관리하는 사용자 계정 테이블.';
COMMENT ON COLUMN app_user.staff_no IS 'korus_staff_snapshot.staff_no 참조 의도 (FK 미선언)';
COMMENT ON COLUMN app_user.org_code IS 'organization.org_code 참조 의도 (FK 미선언)';
COMMENT ON COLUMN app_user.employment_status IS 'ACTIVE:재직|LEAVE:휴직|RETIRED:퇴직';
COMMENT ON COLUMN app_user.system_enabled IS 'Y:사용|N:미사용';

CREATE TABLE IF NOT EXISTS korus_staff_snapshot (
  staff_no varchar(40) PRIMARY KEY,
  staff_name varchar(100) NOT NULL,
  org_code varchar(40) NOT NULL,
  rank_name varchar(100) NOT NULL,
  employment_status varchar(20) NOT NULL CHECK (employment_status IN ('ACTIVE','LEAVE','RETIRED')),
  retirement_date date,
  last_synced_at timestamptz NOT NULL,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now()
);
COMMENT ON TABLE korus_staff_snapshot IS 'KORUS 교직원 원천정보를 로컬에서 조회 전용 Mock snapshot으로 제공한다.';
COMMENT ON COLUMN korus_staff_snapshot.org_code IS 'korus_org_snapshot.org_code 참조 의도 (FK 미선언)';
COMMENT ON COLUMN korus_staff_snapshot.employment_status IS 'ACTIVE:재직|LEAVE:휴직|RETIRED:퇴직';

CREATE TABLE IF NOT EXISTS korus_org_snapshot (
  org_code varchar(40) PRIMARY KEY,
  org_name varchar(120) NOT NULL,
  parent_org_code varchar(40),
  org_type varchar(40) NOT NULL CHECK (org_type IN ('UNIVERSITY','GRADUATE_SCHOOL','COLLEGE','DEPARTMENT','OFFICE')),
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now()
);
COMMENT ON TABLE korus_org_snapshot IS 'KORUS 조직 원천정보를 로컬에서 조회 전용 Mock snapshot으로 제공한다.';
COMMENT ON COLUMN korus_org_snapshot.parent_org_code IS 'korus_org_snapshot.org_code 참조 의도 (FK 미선언)';
COMMENT ON COLUMN korus_org_snapshot.org_type IS 'UNIVERSITY:대학|GRADUATE_SCHOOL:대학원|COLLEGE:단과대학|DEPARTMENT:학과|OFFICE:부서';

CREATE TABLE IF NOT EXISTS organization (
  org_code varchar(40) PRIMARY KEY,
  org_name varchar(120) NOT NULL,
  parent_org_code varchar(40) REFERENCES organization(org_code),
  org_type varchar(40) NOT NULL CHECK (org_type IN ('UNIVERSITY','GRADUATE_SCHOOL','COLLEGE','DEPARTMENT','OFFICE')),
  effective_start_date date NOT NULL,
  effective_end_date date,
  is_active char(1) NOT NULL DEFAULT 'Y' CHECK (is_active IN ('Y','N')),
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now(),
  CONSTRAINT organization_period_ck CHECK (effective_end_date IS NULL OR effective_end_date >= effective_start_date)
);
COMMENT ON TABLE organization IS '로컬 조직 계층과 적용기간을 관리하며 조직 개편 이력을 변경 이력에 남긴다.';
COMMENT ON COLUMN organization.org_type IS 'UNIVERSITY:대학|GRADUATE_SCHOOL:대학원|COLLEGE:단과대학|DEPARTMENT:학과|OFFICE:부서';
COMMENT ON COLUMN organization.is_active IS 'Y:사용|N:미사용';

CREATE TABLE IF NOT EXISTS user_organization_assignment (
  assignment_id varchar(64) PRIMARY KEY,
  user_id varchar(64) NOT NULL REFERENCES app_user(user_id),
  org_code varchar(40) NOT NULL REFERENCES organization(org_code),
  position_name varchar(100) NOT NULL,
  effective_start_date date NOT NULL,
  effective_end_date date,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now(),
  CONSTRAINT user_org_assignment_period_ck CHECK (effective_end_date IS NULL OR effective_end_date >= effective_start_date)
);
COMMENT ON TABLE user_organization_assignment IS '사용자의 조직·보직 매핑과 적용기간을 보존한다.';

CREATE TABLE IF NOT EXISTS role (
  role_code varchar(3) PRIMARY KEY CHECK (role_code IN ('R01','R02','R03','R04','R05','R06','R07','R08','R09')),
  role_name varchar(100) NOT NULL,
  purpose text NOT NULL,
  grant_criteria text NOT NULL,
  default_data_scope varchar(10) NOT NULL CHECK (default_data_scope IN ('SELF','ORG','ALL')),
  is_active char(1) NOT NULL DEFAULT 'Y' CHECK (is_active IN ('Y','N')),
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now()
);
COMMENT ON TABLE role IS 'R01~R09 역할 정의와 부여 기준, 데이터 범위 기본값을 관리한다.';
COMMENT ON COLUMN role.role_code IS 'R01:교원|R02:학과장|R03:단과대학(원) 행정실|R04:교수지원과|R05:산학협력단|R06:입학인재관리과|R07:실적부서|R08:점수산출 감사자|R09:시스템관리자';
COMMENT ON COLUMN role.default_data_scope IS 'SELF:본인|ORG:조직|ALL:전체';
COMMENT ON COLUMN role.is_active IS 'Y:사용|N:미사용';

CREATE TABLE IF NOT EXISTS user_role_assignment (
  assignment_id varchar(64) PRIMARY KEY,
  user_id varchar(64) NOT NULL REFERENCES app_user(user_id),
  role_code varchar(3) NOT NULL REFERENCES role(role_code),
  assignment_type varchar(20) NOT NULL CHECK (assignment_type IN ('POSITION','MANUAL')),
  approved_by_user_id varchar(64) REFERENCES app_user(user_id),
  effective_start_date date NOT NULL,
  effective_end_date date,
  status varchar(20) NOT NULL CHECK (status IN ('ACTIVE','REVOKED','EXPIRED')),
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now(),
  CONSTRAINT user_role_assignment_period_ck CHECK (effective_end_date IS NULL OR effective_end_date >= effective_start_date)
);
COMMENT ON TABLE user_role_assignment IS '사용자 역할 부여·변경·회수와 승인자 및 유효기간을 기록한다.';
COMMENT ON COLUMN user_role_assignment.assignment_type IS 'POSITION:보직기반|MANUAL:수동';
COMMENT ON COLUMN user_role_assignment.status IS 'ACTIVE:활성|REVOKED:회수|EXPIRED:만료';

CREATE TABLE IF NOT EXISTS menu (
  menu_id varchar(64) PRIMARY KEY,
  parent_menu_id varchar(64) REFERENCES menu(menu_id),
  menu_name varchar(120) NOT NULL,
  screen_id varchar(80),
  route_path varchar(200),
  icon_name varchar(80),
  business_category varchar(40) NOT NULL CHECK (business_category IN ('SYSTEM','USER_ORG','ROLE_PERMISSION','MENU','COMMON_CODE')),
  description text,
  display_order integer NOT NULL,
  is_active char(1) NOT NULL DEFAULT 'Y' CHECK (is_active IN ('Y','N')),
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now()
);
COMMENT ON TABLE menu IS '시스템 관리 메뉴 계층과 실행 화면 연결 정보를 관리한다.';
COMMENT ON COLUMN menu.business_category IS 'SYSTEM:시스템|USER_ORG:사용자조직|ROLE_PERMISSION:역할권한|MENU:메뉴|COMMON_CODE:공통코드';
COMMENT ON COLUMN menu.is_active IS 'Y:사용|N:미사용';

CREATE TABLE IF NOT EXISTS menu_permission (
  permission_id varchar(64) PRIMARY KEY,
  target_type varchar(20) NOT NULL CHECK (target_type IN ('ROLE','ORGANIZATION','USER')),
  target_id varchar(64) NOT NULL,
  menu_id varchar(64) NOT NULL REFERENCES menu(menu_id),
  permission_level varchar(20) NOT NULL CHECK (permission_level IN ('NONE','READ','WRITE')),
  is_active char(1) NOT NULL DEFAULT 'Y' CHECK (is_active IN ('Y','N')),
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now(),
  UNIQUE(target_type, target_id, menu_id)
);
COMMENT ON TABLE menu_permission IS '역할·조직·사용자 단위 메뉴 접근 권한을 UI 메뉴 노출과 서버 접근통제에 공통 사용한다.';
COMMENT ON COLUMN menu_permission.target_type IS 'ROLE:역할|ORGANIZATION:조직|USER:사용자';
COMMENT ON COLUMN menu_permission.target_id IS 'target_type에 따라 role.role_code, organization.org_code, app_user.user_id 참조 의도 (FK 미선언)';
COMMENT ON COLUMN menu_permission.permission_level IS 'NONE:없음|READ:조회|WRITE:등록수정';
COMMENT ON COLUMN menu_permission.is_active IS 'Y:사용|N:미사용';

CREATE TABLE IF NOT EXISTS code_group (
  group_id varchar(64) PRIMARY KEY,
  group_name varchar(120) NOT NULL,
  description text,
  managing_department varchar(120),
  is_active char(1) NOT NULL DEFAULT 'Y' CHECK (is_active IN ('Y','N')),
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now()
);
COMMENT ON TABLE code_group IS '공통코드 그룹ID, 명칭, 설명, 관리부서를 관리한다.';
COMMENT ON COLUMN code_group.is_active IS 'Y:사용|N:미사용';

CREATE TABLE IF NOT EXISTS code_detail (
  code_id varchar(64) PRIMARY KEY,
  group_id varchar(64) NOT NULL REFERENCES code_group(group_id),
  code_value varchar(80) NOT NULL,
  code_name varchar(120) NOT NULL,
  parent_code_id varchar(64) REFERENCES code_detail(code_id),
  display_order integer NOT NULL DEFAULT 0,
  extra_attributes jsonb,
  effective_start_date date NOT NULL,
  effective_end_date date,
  is_active char(1) NOT NULL DEFAULT 'Y' CHECK (is_active IN ('Y','N')),
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now(),
  UNIQUE(group_id, code_value),
  CONSTRAINT code_detail_period_ck CHECK (effective_end_date IS NULL OR effective_end_date >= effective_start_date)
);
COMMENT ON TABLE code_detail IS '공통코드 상세값과 계층, 정렬순서, 추가속성, 유효기간을 관리한다.';
COMMENT ON COLUMN code_detail.extra_attributes IS 'CodeController.create/update 시 애플리케이션에서 갱신하는 연계 매핑 JSON 속성';
COMMENT ON COLUMN code_detail.is_active IS 'Y:사용|N:미사용';

CREATE TABLE IF NOT EXISTS user_session (
  session_id varchar(64) PRIMARY KEY,
  user_id varchar(64) NOT NULL REFERENCES app_user(user_id),
  issued_at timestamptz NOT NULL,
  expires_at timestamptz NOT NULL,
  status varchar(20) NOT NULL CHECK (status IN ('ACTIVE','EXPIRED','REVOKED')),
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now()
);
COMMENT ON TABLE user_session IS '내부 계정 기반 HttpOnly 세션 쿠키 인증 상태를 보존한다.';
COMMENT ON COLUMN user_session.status IS 'ACTIVE:활성|EXPIRED:만료|REVOKED:회수';

CREATE TABLE IF NOT EXISTS change_history (
  history_id varchar(64) PRIMARY KEY,
  entity_name varchar(80) NOT NULL,
  entity_id varchar(120) NOT NULL,
  operation_type varchar(20) NOT NULL CHECK (operation_type IN ('CREATE','UPDATE','DISABLE','REVOKE','REORDER')),
  before_value jsonb,
  after_value jsonb,
  reason text,
  processed_by_user_id varchar(64) NOT NULL REFERENCES app_user(user_id),
  processed_at timestamptz NOT NULL,
  created_at timestamptz NOT NULL DEFAULT now()
);
COMMENT ON TABLE change_history IS '관리 데이터 등록·수정·회수·재정렬 처리의 변경 전후 값, 처리자, 처리일시, 사유를 추적한다.';
COMMENT ON COLUMN change_history.operation_type IS 'CREATE:등록|UPDATE:수정|DISABLE:비활성화|REVOKE:회수|REORDER:재정렬';
COMMENT ON COLUMN change_history.before_value IS '각 관리 Service의 mutation 직전 값으로 애플리케이션에서 갱신하며 비밀 원문은 제외';
COMMENT ON COLUMN change_history.after_value IS '각 관리 Service의 mutation 직후 값으로 애플리케이션에서 갱신하며 비밀 원문은 제외';

CREATE INDEX IF NOT EXISTS idx_app_user_staff_no ON app_user(staff_no);
CREATE INDEX IF NOT EXISTS idx_app_user_org_code ON app_user(org_code);
CREATE INDEX IF NOT EXISTS idx_staff_snapshot_org_code ON korus_staff_snapshot(org_code);
CREATE INDEX IF NOT EXISTS idx_organization_parent ON organization(parent_org_code);
CREATE INDEX IF NOT EXISTS idx_user_role_user ON user_role_assignment(user_id, status);
CREATE INDEX IF NOT EXISTS idx_menu_parent ON menu(parent_menu_id, display_order);
CREATE INDEX IF NOT EXISTS idx_menu_permission_target ON menu_permission(target_type, target_id);
CREATE INDEX IF NOT EXISTS idx_code_detail_group ON code_detail(group_id, display_order);
CREATE INDEX IF NOT EXISTS idx_user_session_user ON user_session(user_id, status);
CREATE INDEX IF NOT EXISTS idx_change_history_entity ON change_history(entity_name, entity_id);
