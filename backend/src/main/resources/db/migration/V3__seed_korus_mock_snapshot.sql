INSERT INTO organization(organization_code, organization_name, organization_type, parent_organization_code, effective_start_date, relation_change_reason) VALUES
('KNUE','한국교원대학교','UNIVERSITY',NULL,'2026-01-01','초기 시드'),
('COL-EDU','교육대학','COLLEGE','KNUE','2026-01-01','초기 시드'),
('DEP-COMMON','공통교육학과','DEPARTMENT','COL-EDU','2026-01-01','초기 시드'),
('OFF-SUPPORT','교수지원과','OFFICE','KNUE','2026-01-01','초기 시드')
ON CONFLICT (organization_code) DO UPDATE SET organization_name=EXCLUDED.organization_name, organization_type=EXCLUDED.organization_type, parent_organization_code=EXCLUDED.parent_organization_code, updated_at=CURRENT_TIMESTAMP;

INSERT INTO korus_staff_snapshot(staff_no, staff_name, organization_code, position_name, employment_status, duty_name, retirement_date, last_synced_at) VALUES
('100001','관리자','OFF-SUPPORT','행정사무관','ACTIVE','시스템관리자',NULL,CURRENT_TIMESTAMP),
('200001','김교원','DEP-COMMON','교수','ACTIVE','학과교수',NULL,CURRENT_TIMESTAMP),
('200002','이학과장','DEP-COMMON','교수','ACTIVE','학과장',NULL,CURRENT_TIMESTAMP)
ON CONFLICT (staff_no) DO UPDATE SET staff_name=EXCLUDED.staff_name, organization_code=EXCLUDED.organization_code, position_name=EXCLUDED.position_name, employment_status=EXCLUDED.employment_status, duty_name=EXCLUDED.duty_name, last_synced_at=CURRENT_TIMESTAMP;

INSERT INTO user_account(user_id, staff_no, username, password_hash, system_use_yn) VALUES
('admin','100001','admin','5353fa8f83d8f81c028c2dfd654264b33e2ce349bb9c5d295f325e7cb0f1c763','Y'),
('faculty01','200001','faculty01','5353fa8f83d8f81c028c2dfd654264b33e2ce349bb9c5d295f325e7cb0f1c763','Y')
ON CONFLICT (user_id) DO UPDATE SET staff_no=EXCLUDED.staff_no, username=EXCLUDED.username, password_hash=EXCLUDED.password_hash, system_use_yn=EXCLUDED.system_use_yn, updated_at=CURRENT_TIMESTAMP;

INSERT INTO organization_user_assignment(staff_no, organization_code, duty_name, effective_start_date)
VALUES ('100001','OFF-SUPPORT','시스템관리자','2026-01-01'), ('200001','DEP-COMMON','학과교수','2026-01-01'), ('200002','DEP-COMMON','학과장','2026-01-01')
ON CONFLICT DO NOTHING;

INSERT INTO user_role(user_id, role_code, assignment_type, valid_from, approved_by, change_reason) VALUES
('admin','R09','MANUAL','2026-01-01','admin','초기 관리자 시드'),
('faculty01','R01','POSITION_BASED','2026-01-01','admin','초기 교원 시드')
ON CONFLICT DO NOTHING;

INSERT INTO code_group(group_id, group_name, description, management_department) VALUES
('EMPLOYMENT_STATUS','재직상태','교직원 재직 상태 코드','교수지원과'),
('USE_YN','사용여부','공통 사용 여부 코드','시스템관리')
ON CONFLICT (group_id) DO UPDATE SET group_name=EXCLUDED.group_name, description=EXCLUDED.description, management_department=EXCLUDED.management_department, updated_at=CURRENT_TIMESTAMP;

INSERT INTO detail_code(group_id, code_value, code_name, sort_order, use_yn, valid_from) VALUES
('EMPLOYMENT_STATUS','ACTIVE','재직',1,'Y','2026-01-01'),
('EMPLOYMENT_STATUS','LEAVE','휴직',2,'Y','2026-01-01'),
('EMPLOYMENT_STATUS','RETIRED','퇴직',3,'Y','2026-01-01'),
('USE_YN','Y','사용',1,'Y','2026-01-01'),
('USE_YN','N','미사용',2,'Y','2026-01-01')
ON CONFLICT (group_id, code_value) DO UPDATE SET code_name=EXCLUDED.code_name, sort_order=EXCLUDED.sort_order, use_yn=EXCLUDED.use_yn, updated_at=CURRENT_TIMESTAMP;
