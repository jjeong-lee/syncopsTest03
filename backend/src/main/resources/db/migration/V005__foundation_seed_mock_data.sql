INSERT INTO korus_org_snapshot(org_code,org_name,parent_org_code,org_type) VALUES
('KNUE','한국교원대학교',NULL,'UNIVERSITY'),
('EDU','교육대학','KNUE','COLLEGE'),
('COMPEDU','컴퓨터교육과','EDU','DEPARTMENT'),
('SUPPORT','교수지원과','KNUE','OFFICE')
ON CONFLICT (org_code) DO UPDATE SET org_name=excluded.org_name, parent_org_code=excluded.parent_org_code, org_type=excluded.org_type, updated_at=now();

INSERT INTO organization(org_code,org_name,parent_org_code,org_type,effective_start_date,effective_end_date,is_active) VALUES
('KNUE','한국교원대학교',NULL,'UNIVERSITY','2026-01-01',NULL,'Y'),
('EDU','교육대학','KNUE','COLLEGE','2026-01-01',NULL,'Y'),
('COMPEDU','컴퓨터교육과','EDU','DEPARTMENT','2026-01-01',NULL,'Y'),
('SUPPORT','교수지원과','KNUE','OFFICE','2026-01-01',NULL,'Y')
ON CONFLICT (org_code) DO UPDATE SET org_name=excluded.org_name, parent_org_code=excluded.parent_org_code, org_type=excluded.org_type, effective_start_date=excluded.effective_start_date, effective_end_date=excluded.effective_end_date, is_active='Y', updated_at=now();

INSERT INTO korus_staff_snapshot(staff_no,staff_name,org_code,rank_name,employment_status,retirement_date,last_synced_at) VALUES
('P2026001','김교수','COMPEDU','교수','ACTIVE',NULL,now()),
('P2026002','이학과장','COMPEDU','부교수','ACTIVE',NULL,now()),
('S2026001','박담당','SUPPORT','행정주사','ACTIVE',NULL,now())
ON CONFLICT (staff_no) DO UPDATE SET staff_name=excluded.staff_name, org_code=excluded.org_code, rank_name=excluded.rank_name, employment_status=excluded.employment_status, retirement_date=excluded.retirement_date, last_synced_at=now(), updated_at=now();

INSERT INTO app_user(user_id,login_id,password_hash,staff_no,display_name,org_code,position_name,employment_status,system_enabled) VALUES
('admin','admin','8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918',NULL,'시스템관리자','SUPPORT','시스템관리자','ACTIVE','Y'),
('professor-001','prof01','8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918','P2026001','김교수','COMPEDU','교수','ACTIVE','Y'),
('support-001','support01','8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918','S2026001','박담당','SUPPORT','담당자','ACTIVE','Y'),
('auditor-001','audit01','8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918',NULL,'감사자','SUPPORT','감사자','ACTIVE','Y')
ON CONFLICT (user_id) DO UPDATE SET display_name=excluded.display_name, org_code=excluded.org_code, position_name=excluded.position_name, employment_status=excluded.employment_status, system_enabled=excluded.system_enabled, updated_at=now();

INSERT INTO user_organization_assignment(assignment_id,user_id,org_code,position_name,effective_start_date,effective_end_date) VALUES
('UOA-admin','admin','SUPPORT','시스템관리자','2026-01-01',NULL),
('UOA-professor-001','professor-001','COMPEDU','교수','2026-01-01',NULL)
ON CONFLICT (assignment_id) DO UPDATE SET org_code=excluded.org_code, position_name=excluded.position_name, effective_start_date=excluded.effective_start_date, effective_end_date=excluded.effective_end_date, updated_at=now();

INSERT INTO user_role_assignment(assignment_id,user_id,role_code,assignment_type,approved_by_user_id,effective_start_date,effective_end_date,status) VALUES
('URA-admin-R09','admin','R09','MANUAL','admin','2026-01-01',NULL,'ACTIVE'),
('URA-professor-R01','professor-001','R01','POSITION','admin','2026-01-01',NULL,'ACTIVE'),
('URA-support-R04','support-001','R04','MANUAL','admin','2026-01-01',NULL,'ACTIVE'),
('URA-auditor-R08','auditor-001','R08','MANUAL','admin','2026-01-01',NULL,'ACTIVE')
ON CONFLICT (assignment_id) DO UPDATE SET status=excluded.status, effective_end_date=excluded.effective_end_date, updated_at=now();

INSERT INTO code_group(group_id,group_name,description,managing_department,is_active) VALUES
('EVAL_AREA','평가영역','교수업적평가 영역 공통코드','교수지원과','Y'),
('PROCESS_STATUS','처리상태','공통 처리 상태 코드','교수지원과','Y'),
('AUTH_TYPE','인증구분','로그인 및 인증 구분 코드','정보전산원','Y')
ON CONFLICT (group_id) DO UPDATE SET group_name=excluded.group_name, description=excluded.description, managing_department=excluded.managing_department, is_active='Y', updated_at=now();

INSERT INTO code_detail(code_id,group_id,code_value,code_name,parent_code_id,display_order,extra_attributes,effective_start_date,effective_end_date,is_active) VALUES
('CD-EVAL-EDU','EVAL_AREA','EDU','교육',NULL,1,'{}'::jsonb,'2026-01-01',NULL,'Y'),
('CD-EVAL-RESEARCH','EVAL_AREA','RESEARCH','연구',NULL,2,'{}'::jsonb,'2026-01-01',NULL,'Y'),
('CD-STATUS-ACTIVE','PROCESS_STATUS','ACTIVE','활성',NULL,1,'{}'::jsonb,'2026-01-01',NULL,'Y'),
('CD-AUTH-LOCAL','AUTH_TYPE','LOCAL','내부계정',NULL,1,'{}'::jsonb,'2026-01-01',NULL,'Y')
ON CONFLICT (code_id) DO UPDATE SET code_name=excluded.code_name, display_order=excluded.display_order, extra_attributes=excluded.extra_attributes, is_active='Y', updated_at=now();
