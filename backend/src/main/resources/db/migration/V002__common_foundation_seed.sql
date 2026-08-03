INSERT INTO user_accounts(user_id, username, password_hash, system_enabled) VALUES
('admin','admin','8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918',true),
('STAFF-001','professor01','8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918',true)
ON CONFLICT (user_id) DO NOTHING;

INSERT INTO organizations(organization_code, organization_name, organization_type, parent_organization_code, effective_start_date, is_active) VALUES
('KNUE','한국교원대학교','UNIVERSITY',NULL,'2026-01-01',true),
('EDU-COLLEGE','교육과학대학','COLLEGE','KNUE','2026-01-01',true),
('COMP-EDU','컴퓨터교육과','DEPARTMENT','EDU-COLLEGE','2026-01-01',true)
ON CONFLICT (organization_code) DO NOTHING;

INSERT INTO korus_staff_snapshots(staff_no, staff_name, organization_code, position_name, employment_status, retirement_date, last_synced_at) VALUES
('admin','시스템 관리자','KNUE','시스템관리자','ACTIVE',NULL,now()),
('STAFF-001','김교원','COMP-EDU','교수','ACTIVE',NULL,now())
ON CONFLICT (staff_no) DO NOTHING;

INSERT INTO organization_user_mappings(user_id, organization_code, position_name, effective_start_date, is_active) VALUES
('admin','KNUE','시스템관리자','2026-01-01',true),
('STAFF-001','COMP-EDU','교수','2026-01-01',true)
ON CONFLICT DO NOTHING;

INSERT INTO roles(role_code, role_name, purpose, grant_criteria, default_data_scope) VALUES
('R01','교원','본인 관련 업무를 수행하는 일반 사용자 역할','교원 재직자','SELF'),
('R02','학과장','소속 학과 교원 관련 업무를 확인하는 역할','학과장 보직','DEPARTMENT'),
('R03','단과대학(원) 행정실','단과대학 또는 대학원 행정 처리 역할','단과대학/대학원 행정 보직','COLLEGE'),
('R04','교수지원과','기준정보와 평가 관련 행정 관리 역할','교수지원과 담당자','GLOBAL'),
('R05','산학협력단','연구비·간접비·지식재산 관련 자료 관리 역할','산학협력단 담당자','RESEARCH'),
('R06','입학인재관리과','입학·취업률 관련 자료 관리 역할','입학인재관리과 담당자','ADMISSION'),
('R07','실적부서','담당 실적 자료 관리 역할','실적 담당 부서','ASSIGNED'),
('R08','점수산출 감사자','산출 과정과 근거를 조회하는 감사 역할','감사자','AUDIT_READ'),
('R09','시스템관리자','사용자·조직·메뉴·권한·코드 관리를 수행하는 관리자 역할','시스템 관리자','SYSTEM')
ON CONFLICT (role_code) DO NOTHING;

INSERT INTO user_roles(user_id, role_code, assignment_source, effective_start_date, approved_by, status) VALUES
('admin','R09','MANUAL','2026-01-01','admin','ACTIVE'),
('STAFF-001','R01','POSITION_BASED','2026-01-01','admin','ACTIVE')
ON CONFLICT DO NOTHING;

WITH ins AS (
  INSERT INTO menus(menu_id,parent_menu_id,menu_level,menu_name,screen_id,url,icon,business_category,description,display_order,is_active) VALUES
  (1,NULL,'TOP','시스템 관리',NULL,NULL,'settings','COMMON','시스템 관리 대메뉴',1,true),
  (2,1,'MIDDLE','사용자·조직 관리',NULL,NULL,'users','COMMON','사용자와 조직 기준정보',1,true),
  (3,1,'MIDDLE','역할·권한 관리',NULL,NULL,'shield','COMMON','역할과 메뉴 권한',2,true),
  (4,1,'MIDDLE','메뉴 관리',NULL,NULL,'menu','COMMON','메뉴 구조와 실행정보',3,true),
  (5,1,'MIDDLE','공통코드 관리',NULL,NULL,'codes','COMMON','공통코드 관리',4,true),
  (10,2,'SCREEN','사용자 관리','USER_MANAGEMENT','/system/users','user','COMMON','사용자 검색과 역할 관리',1,true),
  (11,2,'SCREEN','조직 관리','ORGANIZATION_MANAGEMENT','/system/organizations','org','COMMON','조직 계층 관리',2,true),
  (12,3,'SCREEN','역할 관리','ROLE_MANAGEMENT','/system/roles','role','COMMON','역할 정의 관리',1,true),
  (13,3,'SCREEN','사용자 역할 관리','USER_ROLE_MANAGEMENT','/system/user-roles','assignment','COMMON','사용자 역할 부여와 회수',2,true),
  (14,3,'SCREEN','메뉴 권한 관리','MENU_PERMISSION_MANAGEMENT','/system/menu-permissions','permission','COMMON','메뉴 접근 권한',3,true),
  (15,4,'SCREEN','메뉴 구조 관리','MENU_STRUCTURE_MANAGEMENT','/system/menu-structure','tree','COMMON','메뉴 계층 관리',1,true),
  (16,4,'SCREEN','메뉴 정보 관리','MENU_INFO_MANAGEMENT','/system/menu-info','info','COMMON','메뉴 실행정보 관리',2,true),
  (17,5,'SCREEN','코드그룹 관리','CODE_GROUP_MANAGEMENT','/system/code-groups','group','COMMON','코드그룹 관리',1,true),
  (18,5,'SCREEN','상세코드 관리','DETAIL_CODE_MANAGEMENT','/system/code-groups/EVAL_AREA/codes','code','COMMON','상세코드 관리',2,true)
  ON CONFLICT (menu_id) DO NOTHING RETURNING menu_id
) SELECT 1;

SELECT setval(pg_get_serial_sequence('menus', 'menu_id'), (SELECT COALESCE(MAX(menu_id), 1) FROM menus));

INSERT INTO menu_permissions(principal_type, principal_id, menu_id, permission_effect, is_active)
SELECT 'ROLE','R09',menu_id,'ALLOW',true FROM menus WHERE menu_level='SCREEN'
ON CONFLICT DO NOTHING;

INSERT INTO code_groups(group_id, group_name, description, managing_department, is_active) VALUES
('EVAL_AREA','평가영역','평가영역 선택값','교수지원과',true)
ON CONFLICT (group_id) DO NOTHING;

INSERT INTO detail_codes(group_id, code_value, code_name, sort_order, additional_attributes, is_active) VALUES
('EVAL_AREA','TEACH','교육',1,'{}',true),
('EVAL_AREA','RESEARCH','연구',2,'{}',true)
ON CONFLICT (group_id, code_value) DO NOTHING;
