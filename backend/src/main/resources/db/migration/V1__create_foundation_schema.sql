CREATE SEQUENCE IF NOT EXISTS organization_user_assignment_seq START WITH 1000;
CREATE SEQUENCE IF NOT EXISTS user_role_seq START WITH 1000;
CREATE SEQUENCE IF NOT EXISTS menu_permission_seq START WITH 1000;

CREATE TABLE IF NOT EXISTS organization (
  organization_code varchar(30) PRIMARY KEY,
  organization_name varchar(200) NOT NULL,
  organization_type varchar(30) NOT NULL CHECK (organization_type IN ('UNIVERSITY','GRADUATE_SCHOOL','COLLEGE','DEPARTMENT','OFFICE')),
  parent_organization_code varchar(30) REFERENCES organization(organization_code),
  effective_start_date date NOT NULL,
  effective_end_date date,
  relation_change_reason varchar(500),
  created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  use_yn varchar(1) NOT NULL DEFAULT 'Y' CHECK (use_yn IN ('Y','N'))
);
COMMENT ON TABLE organization IS '대학·대학원·단과대학·학과·부서의 조직 기준정보와 상하위 관계를 관리한다.';
COMMENT ON COLUMN organization.organization_type IS 'UNIVERSITY:대학교|GRADUATE_SCHOOL:대학원|COLLEGE:단과대학|DEPARTMENT:학과|OFFICE:부서';

CREATE TABLE IF NOT EXISTS korus_staff_snapshot (
  staff_no varchar(30) PRIMARY KEY,
  staff_name varchar(100) NOT NULL,
  organization_code varchar(30) NOT NULL REFERENCES organization(organization_code),
  position_name varchar(100),
  employment_status varchar(30) NOT NULL CHECK (employment_status IN ('ACTIVE','LEAVE','RETIRED')),
  duty_name varchar(100),
  retirement_date date,
  last_synced_at timestamp NOT NULL,
  created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  use_yn varchar(1) NOT NULL DEFAULT 'Y' CHECK (use_yn IN ('Y','N'))
);
COMMENT ON TABLE korus_staff_snapshot IS 'KORUS 원천 교직원 정보를 로컬 검증용 조회 전용 snapshot으로 제공한다.';
COMMENT ON COLUMN korus_staff_snapshot.employment_status IS 'ACTIVE:재직|LEAVE:휴직|RETIRED:퇴직';

CREATE TABLE IF NOT EXISTS user_account (
  user_id varchar(50) PRIMARY KEY,
  staff_no varchar(30) UNIQUE REFERENCES korus_staff_snapshot(staff_no),
  username varchar(100) UNIQUE NOT NULL,
  password_hash varchar(255) NOT NULL,
  system_use_yn varchar(1) NOT NULL DEFAULT 'Y' CHECK (system_use_yn IN ('Y','N')),
  change_reason varchar(500),
  created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  use_yn varchar(1) NOT NULL DEFAULT 'Y' CHECK (use_yn IN ('Y','N'))
);
COMMENT ON TABLE user_account IS '내부 로그인 계정과 시스템 사용여부를 관리하는 로컬 계정 테이블이다.';
COMMENT ON COLUMN user_account.password_hash IS 'AuthenticationPort.hash가 생성하며 원문 비밀번호는 저장하지 않는다.';

CREATE TABLE IF NOT EXISTS organization_user_assignment (
  assignment_id bigint PRIMARY KEY DEFAULT nextval('organization_user_assignment_seq'),
  staff_no varchar(30) NOT NULL REFERENCES korus_staff_snapshot(staff_no),
  organization_code varchar(30) NOT NULL REFERENCES organization(organization_code),
  duty_name varchar(100),
  effective_start_date date NOT NULL,
  effective_end_date date,
  created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  use_yn varchar(1) NOT NULL DEFAULT 'Y' CHECK (use_yn IN ('Y','N'))
);
COMMENT ON TABLE organization_user_assignment IS '교직원의 보직 또는 조직 소속 매핑과 적용기간을 보존한다.';

CREATE TABLE IF NOT EXISTS role (
  role_code varchar(10) PRIMARY KEY CHECK (role_code IN ('R01','R02','R03','R04','R05','R06','R07','R08','R09')),
  role_name varchar(100) NOT NULL,
  purpose varchar(500) NOT NULL,
  grant_criteria varchar(500) NOT NULL,
  data_scope_default varchar(200) NOT NULL,
  created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  use_yn varchar(1) NOT NULL DEFAULT 'Y' CHECK (use_yn IN ('Y','N'))
);
COMMENT ON TABLE role IS 'R01~R09 업무 역할의 목적, 부여 기준, 데이터 범위 기본값을 관리한다.';
COMMENT ON COLUMN role.role_code IS 'R01:교원|R02:학과장|R03:단과대학원행정실|R04:교수지원과|R05:산학협력단|R06:입학인재관리과|R07:실적부서|R08:점수산출감사자|R09:시스템관리자';

CREATE TABLE IF NOT EXISTS user_role (
  user_role_id bigint PRIMARY KEY DEFAULT nextval('user_role_seq'),
  user_id varchar(50) NOT NULL REFERENCES user_account(user_id),
  role_code varchar(10) NOT NULL REFERENCES role(role_code),
  assignment_type varchar(20) NOT NULL CHECK (assignment_type IN ('POSITION_BASED','MANUAL')),
  valid_from date NOT NULL,
  valid_to date,
  approved_by varchar(50) NOT NULL REFERENCES user_account(user_id),
  change_reason varchar(500),
  created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  use_yn varchar(1) NOT NULL DEFAULT 'Y' CHECK (use_yn IN ('Y','N'))
);
COMMENT ON TABLE user_role IS '사용자별 역할 부여·회수 이력과 유효기간, 승인자를 보존한다.';

COMMENT ON COLUMN user_role.assignment_type IS 'POSITION_BASED:보직기반|MANUAL:수동부여';

CREATE TABLE IF NOT EXISTS menu (
  menu_id varchar(50) PRIMARY KEY,
  parent_menu_id varchar(50) REFERENCES menu(menu_id),
  menu_level integer NOT NULL CHECK (menu_level IN (1,2,3)),
  display_order integer NOT NULL,
  menu_name varchar(200) NOT NULL,
  screen_id varchar(50),
  url varchar(200),
  icon varchar(100),
  business_type varchar(100),
  description varchar(500),
  active_yn varchar(1) NOT NULL DEFAULT 'Y' CHECK (active_yn IN ('Y','N')),
  created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  use_yn varchar(1) NOT NULL DEFAULT 'Y' CHECK (use_yn IN ('Y','N'))
);
COMMENT ON TABLE menu IS '시스템 관리 대·중·소메뉴 계층과 화면 실행 정보를 관리한다.';
COMMENT ON COLUMN menu.menu_level IS '1:대메뉴|2:중메뉴|3:소메뉴';

CREATE TABLE IF NOT EXISTS menu_permission (
  permission_id bigint PRIMARY KEY DEFAULT nextval('menu_permission_seq'),
  target_type varchar(20) NOT NULL CHECK (target_type IN ('ROLE','ORGANIZATION','USER')),
  target_id varchar(50) NOT NULL,
  menu_id varchar(50) NOT NULL REFERENCES menu(menu_id),
  access_allowed_yn varchar(1) NOT NULL DEFAULT 'N' CHECK (access_allowed_yn IN ('Y','N')),
  explicit_deny_yn varchar(1) NOT NULL DEFAULT 'N' CHECK (explicit_deny_yn IN ('Y','N')),
  created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  use_yn varchar(1) NOT NULL DEFAULT 'Y' CHECK (use_yn IN ('Y','N')),
  CONSTRAINT uq_menu_permission_target UNIQUE (target_type, target_id, menu_id)
);
COMMENT ON TABLE menu_permission IS '역할·조직·사용자 단위 메뉴 접근 허용과 명시 차단 정책을 관리한다.';
COMMENT ON COLUMN menu_permission.target_id IS 'role.role_code 또는 organization.organization_code 또는 user_account.user_id 참조 의도 (다형 대상)';
COMMENT ON COLUMN menu_permission.target_type IS 'ROLE:역할|ORGANIZATION:조직|USER:사용자';

CREATE TABLE IF NOT EXISTS code_group (
  group_id varchar(50) PRIMARY KEY,
  group_name varchar(200) NOT NULL,
  description varchar(500),
  management_department varchar(200) NOT NULL,
  created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  use_yn varchar(1) NOT NULL DEFAULT 'Y' CHECK (use_yn IN ('Y','N'))
);
COMMENT ON TABLE code_group IS '공통코드 묶음의 그룹ID, 명칭, 설명, 관리부서를 관리한다.';

CREATE TABLE IF NOT EXISTS detail_code (
  group_id varchar(50) NOT NULL REFERENCES code_group(group_id),
  code_value varchar(50) NOT NULL,
  code_name varchar(200) NOT NULL,
  parent_code_value varchar(50),
  sort_order integer NOT NULL,
  extra_attributes jsonb,
  valid_from date,
  valid_to date,
  created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  use_yn varchar(1) NOT NULL DEFAULT 'Y' CHECK (use_yn IN ('Y','N')),
  PRIMARY KEY (group_id, code_value),
  CONSTRAINT fk_detail_code_parent FOREIGN KEY (group_id, parent_code_value) REFERENCES detail_code(group_id, code_value)
);
COMMENT ON TABLE detail_code IS '코드그룹별 상세코드와 상위코드, 정렬순서, 추가속성, 유효기간을 관리한다.';
COMMENT ON COLUMN detail_code.extra_attributes IS 'DetailCode API 저장 시 애플리케이션에서 갱신하는 연계 매핑용 JSON 속성';

CREATE TABLE IF NOT EXISTS session (
  session_id varchar(100) PRIMARY KEY,
  user_id varchar(50) NOT NULL REFERENCES user_account(user_id),
  status varchar(20) NOT NULL CHECK (status IN ('ACTIVE','EXPIRED','LOGGED_OUT')),
  created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  expires_at timestamp NOT NULL,
  last_accessed_at timestamp
);
COMMENT ON TABLE session IS '내부 계정 로그인 세션과 보호 API 인증 상태를 관리한다.';
COMMENT ON COLUMN session.status IS 'ACTIVE:활성|EXPIRED:만료|LOGGED_OUT:로그아웃';

CREATE INDEX IF NOT EXISTS idx_user_account_staff_no ON user_account(staff_no);
CREATE INDEX IF NOT EXISTS idx_korus_staff_org ON korus_staff_snapshot(organization_code);
CREATE INDEX IF NOT EXISTS idx_org_parent ON organization(parent_organization_code);
CREATE INDEX IF NOT EXISTS idx_user_role_user ON user_role(user_id);
CREATE INDEX IF NOT EXISTS idx_user_role_role_valid ON user_role(role_code, valid_from, valid_to);
CREATE INDEX IF NOT EXISTS idx_menu_parent_order ON menu(parent_menu_id, display_order);
CREATE UNIQUE INDEX IF NOT EXISTS uq_menu_sibling_order ON menu(COALESCE(parent_menu_id, ''), display_order) WHERE use_yn='Y';
CREATE INDEX IF NOT EXISTS idx_menu_permission_target ON menu_permission(target_type, target_id);
CREATE INDEX IF NOT EXISTS idx_detail_code_group_order ON detail_code(group_id, sort_order);
CREATE INDEX IF NOT EXISTS idx_detail_code_parent ON detail_code(group_id, parent_code_value);
CREATE INDEX IF NOT EXISTS idx_session_user_status ON session(user_id, status);
