INSERT INTO organization (organization_id, organization_code, organization_name, organization_type, use_yn)
VALUES
 ('00000000-0000-0000-0000-000000000101','KNUE','한국교원대학교','UNIVERSITY','Y'),
 ('00000000-0000-0000-0000-000000000102','EDU','교육학과','DEPARTMENT','Y'),
 ('00000000-0000-0000-0000-000000000103','ADMIN','교수지원과','ADMIN_DEPARTMENT','Y')
ON CONFLICT (organization_code) DO NOTHING;

INSERT INTO organization_relation_history (relation_id, organization_id, parent_organization_id, valid_from, status, change_reason)
VALUES
 ('00000000-0000-0000-0000-000000001102','00000000-0000-0000-0000-000000000102','00000000-0000-0000-0000-000000000101','2026-01-01','ACTIVE','초기 조직 시드'),
 ('00000000-0000-0000-0000-000000001103','00000000-0000-0000-0000-000000000103','00000000-0000-0000-0000-000000000101','2026-01-01','ACTIVE','초기 조직 시드')
ON CONFLICT (relation_id) DO NOTHING;

INSERT INTO role (role_code, role_name, purpose, grant_criteria, default_data_scope, use_yn)
VALUES
 ('R01','교원','본인 관련 업무 수행','재직 교원','SELF','Y'),
 ('R02','학과장','소속 학과 교원 관련 업무 확인','학과장 보직','DEPARTMENT','Y'),
 ('R03','단과대학(원) 행정실','단과대학 또는 대학원 행정 처리','행정실 담당자','COLLEGE','Y'),
 ('R04','교수지원과','기준정보와 평가 관련 행정 관리','교수지원과 담당자','ADMIN','Y'),
 ('R05','산학협력단','연구비·간접비·지식재산 자료 관리','산학협력단 담당자','ADMIN','Y'),
 ('R06','입학인재관리과','입학·취업률 관련 자료 관리','입학인재관리과 담당자','ADMIN','Y'),
 ('R07','실적부서','담당 실적 자료 관리','실적부서 담당자','ADMIN','Y'),
 ('R08','점수산출 감사자','산출 과정과 근거 조회','감사자','ALL','Y'),
 ('R09','시스템관리자','사용자·조직·메뉴·권한·코드 관리','시스템 관리자','ALL','Y')
ON CONFLICT (role_code) DO NOTHING;

INSERT INTO korus_staff_snapshot (staff_no, staff_name, organization_code, position_name, rank_name, employment_status, retired_at, last_synced_at, status)
VALUES
 ('1001','홍길동','EDU','학과장','교수','ACTIVE',NULL,now(),'SNAPSHOT_ACTIVE'),
 ('9001','관리자','ADMIN','시스템관리자','직원','ACTIVE',NULL,now(),'SNAPSHOT_ACTIVE')
ON CONFLICT (staff_no) DO NOTHING;

INSERT INTO app_user (user_id, login_id, password_hash, staff_no, system_use_yn, account_status)
VALUES
 ('00000000-0000-0000-0000-000000000901','admin','admin','9001','Y','ACTIVE'),
 ('00000000-0000-0000-0000-000000000902','faculty','faculty','1001','Y','ACTIVE')
ON CONFLICT (login_id) DO NOTHING;

INSERT INTO user_role_assignment (assignment_id, user_id, role_code, grant_type, valid_from, approver_user_id, status)
VALUES
 ('00000000-0000-0000-0000-000000002901','00000000-0000-0000-0000-000000000901','R09','MANUAL','2026-01-01','00000000-0000-0000-0000-000000000901','ACTIVE'),
 ('00000000-0000-0000-0000-000000002902','00000000-0000-0000-0000-000000000902','R01','POSITION_BASED','2026-01-01','00000000-0000-0000-0000-000000000901','ACTIVE')
ON CONFLICT (assignment_id) DO NOTHING;

INSERT INTO staff_assignment (assignment_id, staff_no, organization_id, assignment_type, title, valid_from, status)
VALUES ('00000000-0000-0000-0000-000000003001','1001','00000000-0000-0000-0000-000000000102','POSITION','학과장','2026-01-01','ACTIVE')
ON CONFLICT (assignment_id) DO NOTHING;

INSERT INTO menu (menu_id, parent_menu_id, menu_level, display_order, menu_name, screen_id, url_path, icon_name, business_area, description, use_yn)
VALUES
 ('00000000-0000-0000-0000-000000010000',NULL,1,10,'시스템 관리','SYS_ROOT','/system','grid','SYSTEM','시스템 관리 루트','Y'),
 ('00000000-0000-0000-0000-000000010100','00000000-0000-0000-0000-000000010000',2,10,'사용자·조직 관리','SYS_USER_ORG','/system/users','users','SYSTEM','사용자와 조직 관리','Y'),
 ('00000000-0000-0000-0000-000000010200','00000000-0000-0000-0000-000000010000',2,20,'역할·권한 관리','SYS_ROLE_AUTH','/system/roles','shield','SYSTEM','역할과 권한 관리','Y'),
 ('00000000-0000-0000-0000-000000010300','00000000-0000-0000-0000-000000010000',2,30,'메뉴 관리','SYS_MENU','/system/menu-structure','menu','SYSTEM','메뉴 구조 관리','Y'),
 ('00000000-0000-0000-0000-000000010400','00000000-0000-0000-0000-000000010000',2,40,'공통코드 관리','SYS_CODE','/system/code-groups','code','SYSTEM','공통코드 관리','Y'),
 ('00000000-0000-0000-0000-000000011001','00000000-0000-0000-0000-000000010100',3,10,'사용자 관리','SCR-USR-001','/system/users','user','SYSTEM','사용자 관리','Y'),
 ('00000000-0000-0000-0000-000000011002','00000000-0000-0000-0000-000000010100',3,20,'조직 관리','SCR-ORG-001','/system/organizations','org','SYSTEM','조직 관리','Y'),
 ('00000000-0000-0000-0000-000000011005','00000000-0000-0000-0000-000000010200',3,10,'역할 관리','SCR-ROLE-001','/system/roles','role','SYSTEM','역할 관리','Y'),
 ('00000000-0000-0000-0000-000000011006','00000000-0000-0000-0000-000000010200',3,20,'사용자 역할 관리','SCR-URA-001','/system/user-roles','user-role','SYSTEM','사용자 역할 관리','Y'),
 ('00000000-0000-0000-0000-000000011007','00000000-0000-0000-0000-000000010200',3,30,'메뉴 권한 관리','SCR-MP-001','/system/menu-permissions','permission','SYSTEM','메뉴 권한 관리','Y'),
 ('00000000-0000-0000-0000-000000011013','00000000-0000-0000-0000-000000010300',3,10,'메뉴 구조 관리','SCR-MENU-STRUCT-001','/system/menu-structure','tree','SYSTEM','메뉴 구조 관리','Y'),
 ('00000000-0000-0000-0000-000000011014','00000000-0000-0000-0000-000000010300',3,20,'메뉴 정보 관리','SCR-MENU-INFO-001','/system/menu-info','info','SYSTEM','메뉴 정보 관리','Y'),
 ('00000000-0000-0000-0000-000000011016','00000000-0000-0000-0000-000000010400',3,10,'코드그룹 관리','SCR-CG-001','/system/code-groups','group','SYSTEM','코드그룹 관리','Y'),
 ('00000000-0000-0000-0000-000000011017','00000000-0000-0000-0000-000000010400',3,20,'상세코드 관리','SCR-DC-001','/system/detail-codes','code-detail','SYSTEM','상세코드 관리','Y')
ON CONFLICT (menu_id) DO NOTHING;

INSERT INTO menu_permission (menu_id, subject_type, subject_id, access_allowed, function_allowed, decision_effect, use_yn)
SELECT menu_id, 'ROLE', 'R09', true, true, 'ALLOW', 'Y' FROM menu
ON CONFLICT (menu_id, subject_type, subject_id) DO NOTHING;

INSERT INTO code_group (group_id, group_name, description, managing_department, use_yn)
VALUES
 ('STATUS','처리상태','공통 처리 상태','교수지원과','Y'),
 ('EVAL_AREA','평가영역','향후 평가영역 연계 기준값','교수지원과','Y')
ON CONFLICT (group_id) DO NOTHING;

INSERT INTO detail_code (detail_code_id, group_id, code_value, code_name, sort_order, extra_attributes, valid_from, use_yn)
VALUES
 ('00000000-0000-0000-0000-000000020001','STATUS','DRAFT','초안',10,'{}','2026-01-01','Y'),
 ('00000000-0000-0000-0000-000000020002','STATUS','ACTIVE','활성',20,'{}','2026-01-01','Y'),
 ('00000000-0000-0000-0000-000000020003','STATUS','CLOSED','종료',30,'{}','2026-01-01','Y')
ON CONFLICT (group_id, code_value) DO NOTHING;
