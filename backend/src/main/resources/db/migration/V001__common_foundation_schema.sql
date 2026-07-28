CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS organization (
  organization_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  organization_code VARCHAR(30) NOT NULL UNIQUE,
  organization_name VARCHAR(150) NOT NULL,
  organization_type VARCHAR(30) NOT NULL CHECK (organization_type IN ('UNIVERSITY','GRADUATE_SCHOOL','COLLEGE','DEPARTMENT','OFFICE')),
  parent_organization_code VARCHAR(30) REFERENCES organization(organization_code),
  effective_start_date DATE NOT NULL,
  effective_end_date DATE,
  use_yn CHAR(1) NOT NULL CHECK (use_yn IN ('Y','N')),
  created_at TIMESTAMP NOT NULL DEFAULT now(),
  updated_at TIMESTAMP NOT NULL DEFAULT now()
);
COMMENT ON TABLE organization IS '대학·대학원·단과대학·학과·부서의 조직코드와 계층/적용기간을 관리한다.';
COMMENT ON COLUMN organization.organization_type IS 'UNIVERSITY:대학교|GRADUATE_SCHOOL:대학원|COLLEGE:단과대학|DEPARTMENT:학과|OFFICE:부서';
COMMENT ON COLUMN organization.use_yn IS 'Y:사용|N:미사용';

CREATE TABLE IF NOT EXISTS korus_personnel_snapshot (
  employee_no VARCHAR(30) PRIMARY KEY,
  person_name VARCHAR(100) NOT NULL,
  organization_code VARCHAR(30) NOT NULL REFERENCES organization(organization_code),
  position_name VARCHAR(100),
  job_grade VARCHAR(80),
  employment_status VARCHAR(20) NOT NULL CHECK (employment_status IN ('ACTIVE','RETIRED','LEAVE')),
  retirement_date DATE,
  last_synced_at TIMESTAMP NOT NULL
);
COMMENT ON TABLE korus_personnel_snapshot IS 'KORUS 원천 교직원 정보를 로컬 검증용 조회 전용 Mock snapshot으로 보존한다.';
COMMENT ON COLUMN korus_personnel_snapshot.employment_status IS 'ACTIVE:재직|RETIRED:퇴직|LEAVE:휴직';

CREATE TABLE IF NOT EXISTS user_account (
  user_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  employee_no VARCHAR(30) NOT NULL UNIQUE REFERENCES korus_personnel_snapshot(employee_no),
  login_id VARCHAR(80) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  display_name VARCHAR(100) NOT NULL,
  system_use_yn CHAR(1) NOT NULL CHECK (system_use_yn IN ('Y','N')),
  account_status VARCHAR(20) NOT NULL CHECK (account_status IN ('ACTIVE','DISABLED','LOCKED')),
  created_at TIMESTAMP NOT NULL DEFAULT now(),
  updated_at TIMESTAMP NOT NULL DEFAULT now()
);
COMMENT ON TABLE user_account IS '내부 로그인과 시스템 사용여부 및 역할 연결 기준이 되는 로컬 사용자 계정이다.';
COMMENT ON COLUMN user_account.system_use_yn IS 'Y:사용|N:미사용';
COMMENT ON COLUMN user_account.account_status IS 'ACTIVE:활성|DISABLED:비활성|LOCKED:잠김';
COMMENT ON COLUMN user_account.password_hash IS 'AuthenticationService SHA-256 credential hash로 생성/비교되며 원문 비밀번호는 저장하지 않는다.';

CREATE TABLE IF NOT EXISTS role (
  role_code VARCHAR(20) PRIMARY KEY,
  role_name VARCHAR(100) NOT NULL,
  role_purpose TEXT NOT NULL,
  assignment_criteria TEXT,
  default_data_scope VARCHAR(40) NOT NULL CHECK (default_data_scope IN ('SELF','DEPARTMENT','COLLEGE','UNIVERSITY','ALL')),
  use_yn CHAR(1) NOT NULL CHECK (use_yn IN ('Y','N')),
  created_at TIMESTAMP NOT NULL DEFAULT now(),
  updated_at TIMESTAMP NOT NULL DEFAULT now()
);
COMMENT ON TABLE role IS 'R01~R09 역할코드와 역할 목적, 부여 기준, 기본 데이터 범위를 관리한다.';
COMMENT ON COLUMN role.default_data_scope IS 'SELF:본인|DEPARTMENT:학과|COLLEGE:단과대학|UNIVERSITY:대학|ALL:전체';
COMMENT ON COLUMN role.use_yn IS 'Y:사용|N:미사용';

CREATE TABLE IF NOT EXISTS organization_user_mapping (
  mapping_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  employee_no VARCHAR(30) NOT NULL REFERENCES korus_personnel_snapshot(employee_no),
  organization_code VARCHAR(30) NOT NULL REFERENCES organization(organization_code),
  position_role_code VARCHAR(20) REFERENCES role(role_code),
  effective_start_date DATE NOT NULL,
  effective_end_date DATE,
  mapping_status VARCHAR(20) NOT NULL CHECK (mapping_status IN ('ACTIVE','ENDED')),
  created_at TIMESTAMP NOT NULL DEFAULT now(),
  updated_at TIMESTAMP NOT NULL DEFAULT now()
);
COMMENT ON TABLE organization_user_mapping IS '교직원의 조직 소속과 보직 기반 역할 후보를 적용기간 이력으로 관리한다.';
COMMENT ON COLUMN organization_user_mapping.mapping_status IS 'ACTIVE:활성|ENDED:종료';

CREATE TABLE IF NOT EXISTS user_role (
  user_role_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES user_account(user_id),
  role_code VARCHAR(20) NOT NULL REFERENCES role(role_code),
  role_source VARCHAR(20) NOT NULL CHECK (role_source IN ('MANUAL','POSITION_BASED')),
  valid_from DATE NOT NULL,
  valid_to DATE,
  approved_by_user_id UUID REFERENCES user_account(user_id),
  assignment_status VARCHAR(20) NOT NULL CHECK (assignment_status IN ('ACTIVE','REVOKED','EXPIRED')),
  created_at TIMESTAMP NOT NULL DEFAULT now(),
  updated_at TIMESTAMP NOT NULL DEFAULT now()
);
COMMENT ON TABLE user_role IS '사용자별 수동 또는 보직 기반 역할 부여·회수 이력과 유효기간을 보존한다.';
COMMENT ON COLUMN user_role.role_source IS 'MANUAL:수동|POSITION_BASED:보직기반';
COMMENT ON COLUMN user_role.assignment_status IS 'ACTIVE:활성|REVOKED:회수|EXPIRED:만료';

CREATE TABLE IF NOT EXISTS menu (
  menu_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  parent_menu_id UUID REFERENCES menu(menu_id),
  menu_level VARCHAR(20) NOT NULL CHECK (menu_level IN ('TOP','MIDDLE','LEAF')),
  display_order INTEGER NOT NULL,
  menu_name VARCHAR(100) NOT NULL,
  screen_id VARCHAR(60),
  url_path VARCHAR(200),
  icon_name VARCHAR(80),
  business_category VARCHAR(80),
  description TEXT,
  use_yn CHAR(1) NOT NULL CHECK (use_yn IN ('Y','N')),
  created_at TIMESTAMP NOT NULL DEFAULT now(),
  updated_at TIMESTAMP NOT NULL DEFAULT now()
);
COMMENT ON TABLE menu IS '시스템 관리 대/중/소 메뉴 계층과 실행 화면 연결 정보를 관리한다.';
COMMENT ON COLUMN menu.menu_level IS 'TOP:대메뉴|MIDDLE:중메뉴|LEAF:소메뉴';
COMMENT ON COLUMN menu.use_yn IS 'Y:사용|N:미사용';

CREATE TABLE IF NOT EXISTS menu_permission (
  menu_permission_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  target_type VARCHAR(20) NOT NULL CHECK (target_type IN ('ROLE','ORGANIZATION','USER')),
  target_id VARCHAR(80) NOT NULL,
  menu_id UUID NOT NULL REFERENCES menu(menu_id),
  access_allowed_yn CHAR(1) NOT NULL CHECK (access_allowed_yn IN ('Y','N')),
  created_at TIMESTAMP NOT NULL DEFAULT now(),
  updated_at TIMESTAMP NOT NULL DEFAULT now(),
  UNIQUE(target_type, target_id, menu_id)
);
COMMENT ON TABLE menu_permission IS '역할·조직·사용자 단위 메뉴 접근 허용 여부를 UI 노출과 서버 차단 기준으로 저장한다.';
COMMENT ON COLUMN menu_permission.target_type IS 'ROLE:역할|ORGANIZATION:조직|USER:사용자';
COMMENT ON COLUMN menu_permission.target_id IS 'role.role_code, organization.organization_code, user_account.user_id 참조 의도 (다형 FK 미선언)';
COMMENT ON COLUMN menu_permission.access_allowed_yn IS 'Y:허용|N:차단';

CREATE TABLE IF NOT EXISTS code_group (
  group_id VARCHAR(60) PRIMARY KEY,
  group_name VARCHAR(120) NOT NULL,
  description TEXT,
  managing_department VARCHAR(120),
  use_yn CHAR(1) NOT NULL CHECK (use_yn IN ('Y','N')),
  created_at TIMESTAMP NOT NULL DEFAULT now(),
  updated_at TIMESTAMP NOT NULL DEFAULT now()
);
COMMENT ON TABLE code_group IS '공통코드 그룹의 ID, 명칭, 설명, 관리부서, 사용여부를 관리한다.';
COMMENT ON COLUMN code_group.use_yn IS 'Y:사용|N:미사용';
COMMENT ON COLUMN code_group.managing_department IS 'organization.organization_code 또는 부서 표시명 참조 의도 (FK 미선언)';

CREATE TABLE IF NOT EXISTS detail_code (
  detail_code_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  group_id VARCHAR(60) NOT NULL REFERENCES code_group(group_id),
  code_value VARCHAR(80) NOT NULL,
  code_name VARCHAR(120) NOT NULL,
  parent_detail_code_id UUID REFERENCES detail_code(detail_code_id),
  sort_order INTEGER NOT NULL,
  additional_attributes JSONB,
  use_yn CHAR(1) NOT NULL CHECK (use_yn IN ('Y','N')),
  valid_from DATE,
  valid_to DATE,
  created_at TIMESTAMP NOT NULL DEFAULT now(),
  updated_at TIMESTAMP NOT NULL DEFAULT now(),
  UNIQUE(group_id, code_value)
);
COMMENT ON TABLE detail_code IS '코드그룹별 상세코드 값, 표시명, 상위코드, 정렬순서, 추가속성, 사용기간을 관리한다.';
COMMENT ON COLUMN detail_code.use_yn IS 'Y:사용|N:미사용';
COMMENT ON COLUMN detail_code.additional_attributes IS 'CodeService.create/update 시 관리자가 입력한 JSON 속성으로 갱신';

CREATE TABLE IF NOT EXISTS user_session (
  session_id VARCHAR(120) PRIMARY KEY,
  user_id UUID NOT NULL REFERENCES user_account(user_id),
  issued_at TIMESTAMP NOT NULL,
  expires_at TIMESTAMP NOT NULL,
  same_site_policy VARCHAR(20) NOT NULL CHECK (same_site_policy IN ('Lax')),
  session_status VARCHAR(20) NOT NULL CHECK (session_status IN ('ACTIVE','EXPIRED','REVOKED')),
  created_at TIMESTAMP NOT NULL DEFAULT now(),
  updated_at TIMESTAMP NOT NULL DEFAULT now()
);
COMMENT ON TABLE user_session IS 'HttpOnly SameSite=Lax cookie 기반 보호 API 세션을 서버 측에서 검증하기 위한 세션 저장소다.';
COMMENT ON COLUMN user_session.same_site_policy IS 'Lax:Lax';
COMMENT ON COLUMN user_session.session_status IS 'ACTIVE:활성|EXPIRED:만료|REVOKED:회수';

CREATE INDEX IF NOT EXISTS idx_user_account_login ON user_account(login_id);
CREATE INDEX IF NOT EXISTS idx_user_search ON korus_personnel_snapshot(employee_no, person_name, organization_code, job_grade, employment_status, last_synced_at);
CREATE INDEX IF NOT EXISTS idx_user_role_user ON user_role(user_id, role_code, assignment_status, valid_from, valid_to);
CREATE INDEX IF NOT EXISTS idx_organization_hierarchy ON organization(parent_organization_code, organization_code, effective_start_date, effective_end_date);
CREATE INDEX IF NOT EXISTS idx_role_lookup ON role(role_code, use_yn);
CREATE INDEX IF NOT EXISTS idx_menu_hierarchy ON menu(parent_menu_id, display_order, use_yn);
CREATE INDEX IF NOT EXISTS idx_menu_url ON menu(url_path);
CREATE INDEX IF NOT EXISTS idx_menu_permission_target ON menu_permission(target_type, target_id, menu_id);
CREATE INDEX IF NOT EXISTS idx_code_group_lookup ON code_group(group_id, use_yn);
CREATE INDEX IF NOT EXISTS idx_detail_code_lookup ON detail_code(group_id, code_value, sort_order, use_yn);
CREATE INDEX IF NOT EXISTS idx_user_session_lookup ON user_session(user_id, expires_at, session_status);
