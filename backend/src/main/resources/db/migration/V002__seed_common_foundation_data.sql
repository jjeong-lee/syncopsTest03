INSERT INTO organization(organization_code, organization_name, organization_type, parent_organization_code, effective_start_date, effective_end_date, use_yn)
VALUES ('KNUE','한국교원대학교','UNIVERSITY',NULL,'2026-01-01',NULL,'Y')
ON CONFLICT (organization_code) DO NOTHING;
INSERT INTO organization(organization_code, organization_name, organization_type, parent_organization_code, effective_start_date, effective_end_date, use_yn)
VALUES ('EDU','교육학과','DEPARTMENT','KNUE','2026-01-01',NULL,'Y')
ON CONFLICT (organization_code) DO NOTHING;

INSERT INTO korus_personnel_snapshot(employee_no, person_name, organization_code, position_name, job_grade, employment_status, retirement_date, last_synced_at)
VALUES ('A0001','시스템관리자','EDU','시스템 관리자','관리자','ACTIVE',NULL,now())
ON CONFLICT (employee_no) DO NOTHING;

INSERT INTO user_account(user_id, employee_no, login_id, password_hash, display_name, system_use_yn, account_status)
VALUES ('00000000-0000-0000-0000-000000000001','A0001','admin','8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918','시스템관리자','Y','ACTIVE')
ON CONFLICT (login_id) DO NOTHING;

INSERT INTO role(role_code, role_name, role_purpose, assignment_criteria, default_data_scope, use_yn) VALUES
('R01','교원','본인 관련 업무 수행','교원 재직자','SELF','Y'),
('R02','학과장','소속 학과 교원 확인','학과장 보직','DEPARTMENT','Y'),
('R03','단과대학(원) 행정실','단과대학 또는 대학원 행정 처리','행정실 배정','COLLEGE','Y'),
('R04','교수지원과','기준정보와 평가 관련 행정 관리','교수지원과 담당자','UNIVERSITY','Y'),
('R05','산학협력단','연구비·간접비·지식재산 자료 관리','산학협력단 담당자','UNIVERSITY','Y'),
('R06','입학인재관리과','입학·취업률 자료 관리','입학인재관리과 담당자','UNIVERSITY','Y'),
('R07','실적부서','담당 실적 자료 관리','실적부서 담당자','DEPARTMENT','Y'),
('R08','점수산출 감사자','산출 과정과 근거 조회','감사자 지정','ALL','Y'),
('R09','시스템관리자','사용자·조직·메뉴·권한·코드 관리','시스템 관리자 지정','ALL','Y')
ON CONFLICT (role_code) DO NOTHING;

INSERT INTO user_role(user_role_id, user_id, role_code, role_source, valid_from, valid_to, approved_by_user_id, assignment_status)
VALUES ('00000000-0000-0000-0000-000000000101','00000000-0000-0000-0000-000000000001','R09','MANUAL','2026-01-01',NULL,'00000000-0000-0000-0000-000000000001','ACTIVE')
ON CONFLICT (user_role_id) DO NOTHING;

INSERT INTO organization_user_mapping(employee_no, organization_code, position_role_code, effective_start_date, effective_end_date, mapping_status)
VALUES ('A0001','EDU','R09','2026-01-01',NULL,'ACTIVE')
ON CONFLICT DO NOTHING;

INSERT INTO menu(menu_id,parent_menu_id,menu_level,display_order,menu_name,screen_id,url_path,icon_name,business_category,description,use_yn) VALUES
('10000000-0000-0000-0000-000000000000',NULL,'TOP',1,'시스템 관리',NULL,NULL,'settings','시스템','시스템 관리 대메뉴','Y'),
('11000000-0000-0000-0000-000000000000','10000000-0000-0000-0000-000000000000','MIDDLE',1,'사용자·조직 관리',NULL,NULL,'users','시스템','사용자와 조직 기준정보','Y'),
('12000000-0000-0000-0000-000000000000','10000000-0000-0000-0000-000000000000','MIDDLE',2,'역할·권한 관리',NULL,NULL,'shield','시스템','역할과 메뉴 권한','Y'),
('13000000-0000-0000-0000-000000000000','10000000-0000-0000-0000-000000000000','MIDDLE',3,'메뉴 관리',NULL,NULL,'menu','시스템','메뉴 구조와 정보','Y'),
('14000000-0000-0000-0000-000000000000','10000000-0000-0000-0000-000000000000','MIDDLE',4,'공통코드 관리',NULL,NULL,'code','시스템','공통코드 기준정보','Y'),
('11100000-0000-0000-0000-000000000000','11000000-0000-0000-0000-000000000000','LEAF',1,'사용자 관리','USR-001','/system/users','user','시스템','사용자 관리','Y'),
('11200000-0000-0000-0000-000000000000','11000000-0000-0000-0000-000000000000','LEAF',2,'조직 관리','ORG-001','/system/organizations','building','시스템','조직 관리','Y'),
('12100000-0000-0000-0000-000000000000','12000000-0000-0000-0000-000000000000','LEAF',1,'역할 관리','ROLE-001','/system/roles','key','시스템','역할 관리','Y'),
('12200000-0000-0000-0000-000000000000','12000000-0000-0000-0000-000000000000','LEAF',2,'사용자 역할 관리','UROLE-001','/system/user-roles','badge','시스템','사용자 역할 관리','Y'),
('12300000-0000-0000-0000-000000000000','12000000-0000-0000-0000-000000000000','LEAF',3,'메뉴 권한 관리','MPERM-001','/system/menu-permissions','lock','시스템','메뉴 권한 관리','Y'),
('13100000-0000-0000-0000-000000000000','13000000-0000-0000-0000-000000000000','LEAF',1,'메뉴 구조 관리','MSTRUCT-001','/system/menu-structure','tree','시스템','메뉴 구조 관리','Y'),
('13200000-0000-0000-0000-000000000000','13000000-0000-0000-0000-000000000000','LEAF',2,'메뉴 정보 관리','MINFO-001','/system/menu-info','panel','시스템','메뉴 정보 관리','Y'),
('14100000-0000-0000-0000-000000000000','14000000-0000-0000-0000-000000000000','LEAF',1,'코드그룹 관리','CGRP-001','/system/code-groups','folder','시스템','코드그룹 관리','Y'),
('14200000-0000-0000-0000-000000000000','14000000-0000-0000-0000-000000000000','LEAF',2,'상세코드 관리','DCODE-001','/system/codes','list','시스템','상세코드 관리','Y')
ON CONFLICT (menu_id) DO NOTHING;

INSERT INTO menu_permission(target_type,target_id,menu_id,access_allowed_yn)
SELECT 'ROLE','R09',menu_id,'Y' FROM menu
ON CONFLICT (target_type,target_id,menu_id) DO NOTHING;

INSERT INTO code_group(group_id, group_name, description, managing_department, use_yn)
VALUES ('ROLE_SOURCE','역할 부여 출처','수동 또는 보직 기반 역할 구분','교수지원과','Y'), ('USE_YN','사용여부','공통 사용여부 값','교수지원과','Y')
ON CONFLICT (group_id) DO NOTHING;

INSERT INTO detail_code(group_id, code_value, code_name, sort_order, use_yn, valid_from)
VALUES ('ROLE_SOURCE','MANUAL','수동',1,'Y','2026-01-01'), ('ROLE_SOURCE','POSITION_BASED','보직 기반',2,'Y','2026-01-01'), ('USE_YN','Y','사용',1,'Y','2026-01-01'), ('USE_YN','N','미사용',2,'Y','2026-01-01')
ON CONFLICT (group_id, code_value) DO NOTHING;
