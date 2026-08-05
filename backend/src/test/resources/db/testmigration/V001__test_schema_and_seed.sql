CREATE TABLE IF NOT EXISTS app_user (
  user_id varchar(64) PRIMARY KEY,
  login_id varchar(80) NOT NULL UNIQUE,
  password_hash varchar(128) NOT NULL,
  staff_no varchar(40),
  display_name varchar(100) NOT NULL,
  org_code varchar(40),
  position_name varchar(100),
  employment_status varchar(20) NOT NULL DEFAULT 'ACTIVE',
  system_enabled char(1) NOT NULL DEFAULT 'Y',
  created_at timestamp NOT NULL DEFAULT now(),
  updated_at timestamp NOT NULL DEFAULT now()
);
CREATE TABLE IF NOT EXISTS korus_staff_snapshot (
  staff_no varchar(40) PRIMARY KEY,
  staff_name varchar(100) NOT NULL,
  org_code varchar(40) NOT NULL,
  rank_name varchar(100) NOT NULL,
  employment_status varchar(20) NOT NULL,
  retirement_date date,
  last_synced_at timestamp NOT NULL,
  created_at timestamp NOT NULL DEFAULT now(),
  updated_at timestamp NOT NULL DEFAULT now()
);
CREATE TABLE IF NOT EXISTS organization (
  org_code varchar(40) PRIMARY KEY,
  org_name varchar(120) NOT NULL,
  parent_org_code varchar(40),
  org_type varchar(40) NOT NULL,
  effective_start_date date NOT NULL,
  effective_end_date date,
  is_active char(1) NOT NULL DEFAULT 'Y',
  created_at timestamp NOT NULL DEFAULT now(),
  updated_at timestamp NOT NULL DEFAULT now()
);
CREATE TABLE IF NOT EXISTS role (
  role_code varchar(3) PRIMARY KEY,
  role_name varchar(100) NOT NULL,
  purpose text NOT NULL,
  grant_criteria text NOT NULL,
  default_data_scope varchar(10) NOT NULL,
  is_active char(1) NOT NULL DEFAULT 'Y',
  created_at timestamp NOT NULL DEFAULT now(),
  updated_at timestamp NOT NULL DEFAULT now()
);
CREATE TABLE IF NOT EXISTS user_role_assignment (
  assignment_id varchar(64) PRIMARY KEY,
  user_id varchar(64) NOT NULL,
  role_code varchar(3) NOT NULL,
  assignment_type varchar(20) NOT NULL,
  approved_by_user_id varchar(64),
  effective_start_date date NOT NULL,
  effective_end_date date,
  status varchar(20) NOT NULL,
  created_at timestamp NOT NULL DEFAULT now(),
  updated_at timestamp NOT NULL DEFAULT now()
);
CREATE TABLE IF NOT EXISTS menu (
  menu_id varchar(64) PRIMARY KEY,
  parent_menu_id varchar(64),
  menu_name varchar(120) NOT NULL,
  screen_id varchar(80),
  route_path varchar(200),
  icon_name varchar(80),
  business_category varchar(40) NOT NULL,
  description text,
  display_order integer NOT NULL,
  is_active char(1) NOT NULL DEFAULT 'Y',
  created_at timestamp NOT NULL DEFAULT now(),
  updated_at timestamp NOT NULL DEFAULT now()
);
CREATE TABLE IF NOT EXISTS menu_permission (
  permission_id varchar(64) PRIMARY KEY,
  target_type varchar(20) NOT NULL,
  target_id varchar(64) NOT NULL,
  menu_id varchar(64) NOT NULL,
  permission_level varchar(20) NOT NULL,
  is_active char(1) NOT NULL DEFAULT 'Y',
  created_at timestamp NOT NULL DEFAULT now(),
  updated_at timestamp NOT NULL DEFAULT now()
);
CREATE TABLE IF NOT EXISTS code_group (
  group_id varchar(64) PRIMARY KEY,
  group_name varchar(120) NOT NULL,
  description text,
  managing_department varchar(120),
  is_active char(1) NOT NULL DEFAULT 'Y',
  created_at timestamp NOT NULL DEFAULT now(),
  updated_at timestamp NOT NULL DEFAULT now()
);
CREATE TABLE IF NOT EXISTS code_detail (
  code_id varchar(64) PRIMARY KEY,
  group_id varchar(64) NOT NULL,
  code_value varchar(80) NOT NULL,
  code_name varchar(120) NOT NULL,
  parent_code_id varchar(64),
  display_order integer NOT NULL DEFAULT 0,
  extra_attributes text,
  effective_start_date date NOT NULL,
  effective_end_date date,
  is_active char(1) NOT NULL DEFAULT 'Y',
  created_at timestamp NOT NULL DEFAULT now(),
  updated_at timestamp NOT NULL DEFAULT now()
);
CREATE TABLE IF NOT EXISTS user_session (
  session_id varchar(64) PRIMARY KEY,
  user_id varchar(64) NOT NULL,
  issued_at timestamp NOT NULL,
  expires_at timestamp NOT NULL,
  status varchar(20) NOT NULL,
  created_at timestamp NOT NULL DEFAULT now(),
  updated_at timestamp NOT NULL DEFAULT now()
);
CREATE TABLE IF NOT EXISTS change_history (
  history_id varchar(64) PRIMARY KEY,
  entity_name varchar(80) NOT NULL,
  entity_id varchar(120) NOT NULL,
  operation_type varchar(20) NOT NULL,
  before_value text,
  after_value text,
  reason text,
  processed_by_user_id varchar(64) NOT NULL,
  processed_at timestamp NOT NULL,
  created_at timestamp NOT NULL DEFAULT now()
);

INSERT INTO role(role_code, role_name, purpose, grant_criteria, default_data_scope, is_active) VALUES
('R01','교원','본인 관련 업무','교원 기준','SELF','Y'),('R04','교수지원과','기준정보 조회','담당자 기준','ALL','Y'),('R08','점수산출 감사자','감사 조회','감사 기준','ALL','Y'),('R09','시스템관리자','전체 관리','운영자 기준','ALL','Y');
INSERT INTO organization(org_code,org_name,parent_org_code,org_type,effective_start_date,effective_end_date,is_active) VALUES
('KNUE','한국교원대학교',NULL,'UNIVERSITY','2026-01-01',NULL,'Y'),('SUPPORT','교수지원과','KNUE','OFFICE','2026-01-01',NULL,'Y'),('COMPEDU','컴퓨터교육과','KNUE','DEPARTMENT','2026-01-01',NULL,'Y');
INSERT INTO korus_staff_snapshot(staff_no,staff_name,org_code,rank_name,employment_status,retirement_date,last_synced_at) VALUES
('P2026001','김교수','COMPEDU','교수','ACTIVE',NULL,now());
INSERT INTO app_user(user_id,login_id,password_hash,staff_no,display_name,org_code,position_name,employment_status,system_enabled) VALUES
('admin','admin','8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918',NULL,'시스템관리자','SUPPORT','시스템관리자','ACTIVE','Y'),
('professor-001','prof01','8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918','P2026001','김교수','COMPEDU','교수','ACTIVE','Y'),
('support-001','support01','8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918',NULL,'박담당','SUPPORT','담당자','ACTIVE','Y');
INSERT INTO user_role_assignment(assignment_id,user_id,role_code,assignment_type,approved_by_user_id,effective_start_date,effective_end_date,status) VALUES
('URA-admin-R09','admin','R09','MANUAL','admin','2026-01-01',NULL,'ACTIVE'),
('URA-professor-R01','professor-001','R01','POSITION','admin','2026-01-01',NULL,'ACTIVE'),
('URA-support-R04','support-001','R04','MANUAL','admin','2026-01-01',NULL,'ACTIVE');
INSERT INTO menu(menu_id,parent_menu_id,menu_name,screen_id,route_path,icon_name,business_category,description,display_order,is_active) VALUES
('SCR-USER-MGMT',NULL,'사용자 관리','SCR-USER-MGMT','/system/users','user','USER_ORG','사용자 관리',1,'Y'),
('SCR-ORG-MGMT',NULL,'조직 관리','SCR-ORG-MGMT','/system/organizations','building','USER_ORG','조직 관리',2,'Y'),
('SCR-ROLE-MGMT',NULL,'역할 관리','SCR-ROLE-MGMT','/system/roles','key','ROLE_PERMISSION','역할 관리',3,'Y'),
('SCR-USER-ROLE',NULL,'사용자 역할 관리','SCR-USER-ROLE','/system/user-roles','badge','ROLE_PERMISSION','사용자 역할 관리',4,'Y'),
('SCR-MENU-PERMISSION',NULL,'메뉴 권한 관리','SCR-MENU-PERMISSION','/system/menu-permissions','lock','ROLE_PERMISSION','메뉴 권한 관리',5,'Y'),
('SCR-MENU-STRUCTURE',NULL,'메뉴 구조 관리','SCR-MENU-STRUCTURE','/system/menu-structure','tree','MENU','메뉴 구조 관리',6,'Y'),
('SCR-MENU-INFO',NULL,'메뉴 정보 관리','SCR-MENU-INFO','/system/menu-info','layout','MENU','메뉴 정보 관리',7,'Y'),
('SCR-CODE-GROUP',NULL,'코드그룹 관리','SCR-CODE-GROUP','/system/code-groups','folder','COMMON_CODE','코드그룹 관리',8,'Y'),
('SCR-CODE-DETAIL',NULL,'상세코드 관리','SCR-CODE-DETAIL','/system/code-details','list','COMMON_CODE','상세코드 관리',9,'Y');
INSERT INTO menu_permission(permission_id,target_type,target_id,menu_id,permission_level,is_active)
SELECT 'PERM-R09-' || menu_id, 'ROLE', 'R09', menu_id, 'WRITE', 'Y' FROM menu;
INSERT INTO menu_permission(permission_id,target_type,target_id,menu_id,permission_level,is_active)
SELECT 'PERM-R04-' || menu_id, 'ROLE', 'R04', menu_id, 'READ', 'Y' FROM menu;
INSERT INTO code_group(group_id,group_name,description,managing_department,is_active) VALUES ('PROCESS_STATUS','처리상태','공통 처리 상태','교수지원과','Y');
INSERT INTO code_detail(code_id,group_id,code_value,code_name,parent_code_id,display_order,extra_attributes,effective_start_date,effective_end_date,is_active) VALUES ('CD-STATUS-ACTIVE','PROCESS_STATUS','ACTIVE','활성',NULL,1,'{}','2026-01-01',NULL,'Y');
