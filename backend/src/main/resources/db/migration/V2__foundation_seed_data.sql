INSERT INTO organization (organization_id, organization_code, organization_name, organization_type, use_yn)
VALUES ('ORG-KNUE', 'KNUE', '한국교원대학교', 'UNIVERSITY', 'Y')
ON CONFLICT (organization_id) DO NOTHING;

INSERT INTO user_account (user_id, personnel_no, password_hash, password_salt, use_yn, status)
VALUES
    ('admin', 'ADMIN-0001', 'KUdVOw5wzao7bRoBRMCApXccBVc86mFsbgU+Ep8hdKQ=', '9c96ed2eb6444e0d8d0fd6174e972df0', 'Y', 'ACTIVE'),
    ('member', 'MEMBER-0001', 'iqLXTuIthdX6QbaS0hLWFD8QHx9sVjC+Hh9w+jAtWlw=', '5b5a1fb0b16f4a98a4dbfc4f59b45d74', 'Y', 'ACTIVE')
ON CONFLICT (user_id) DO NOTHING;

INSERT INTO korus_personnel_snapshot (personnel_no, name, organization_code, position_name, employment_status, last_synced_at, use_yn)
VALUES ('MEMBER-0001', '예시 사용자', 'KNUE', '교원', '재직', current_timestamp, 'Y')
ON CONFLICT (personnel_no) DO NOTHING;

INSERT INTO role (role_code, role_name, purpose, use_yn)
VALUES
    ('R01', '교원', '본인 관련 업무를 수행하는 일반 사용자 역할', 'Y'),
    ('R02', '학과장', '소속 학과 교원 관련 업무를 확인하는 역할', 'Y'),
    ('R03', '단과대학(원) 행정실', '단과대학 또는 대학원 행정 처리 역할', 'Y'),
    ('R04', '교수지원과', '기준정보와 평가 관련 행정 관리 역할', 'Y'),
    ('R05', '산학협력단', '연구비·간접비·지식재산 관련 자료 관리 역할', 'Y'),
    ('R06', '입학인재관리과', '입학·취업률 관련 자료 관리 역할', 'Y'),
    ('R07', '실적부서', '담당 실적 자료 관리 역할', 'Y'),
    ('R08', '점수산출 감사자', '산출 과정과 근거를 조회하는 감사 역할', 'Y'),
    ('R09', '시스템관리자', '사용자·조직·메뉴·권한·코드를 관리하는 관리자 역할', 'Y')
ON CONFLICT (role_code) DO NOTHING;

INSERT INTO user_role (user_role_id, user_id, role_code, approval_user_id, effective_start_date, assignment_type, status)
VALUES
    ('USER-ROLE-ADMIN-R09', 'admin', 'R09', 'admin', current_date, 'MANUAL', 'ACTIVE'),
    ('USER-ROLE-MEMBER-R01', 'member', 'R01', 'admin', current_date, 'MANUAL', 'ACTIVE')
ON CONFLICT (user_role_id) DO NOTHING;

INSERT INTO menu (menu_id, menu_name, parent_menu_id, display_order, screen_id, url, business_category, use_yn)
VALUES
    ('MENU-SYSTEM', '시스템 관리', NULL, 1, NULL, NULL, 'SYSTEM', 'Y'),
    ('MENU-USER-ORGANIZATION', '사용자·조직 관리', 'MENU-SYSTEM', 1, NULL, NULL, 'SYSTEM', 'Y'),
    ('MENU-ROLES-PERMISSIONS', '역할·권한 관리', 'MENU-SYSTEM', 2, NULL, NULL, 'SYSTEM', 'Y'),
    ('MENU-MANAGEMENT', '메뉴 관리', 'MENU-SYSTEM', 3, NULL, NULL, 'SYSTEM', 'Y'),
    ('MENU-COMMON-CODES', '공통코드 관리', 'MENU-SYSTEM', 4, NULL, NULL, 'SYSTEM', 'Y'),
    ('MENU-USER-MANAGEMENT', '사용자 관리', 'MENU-USER-ORGANIZATION', 1, 'SCR-USER-MANAGEMENT', '/system/user-organization/users', 'SYSTEM', 'Y'),
    ('MENU-ORGANIZATION-MANAGEMENT', '조직 관리', 'MENU-USER-ORGANIZATION', 2, 'SCR-ORGANIZATION-MANAGEMENT', '/system/user-organization/organizations', 'SYSTEM', 'Y'),
    ('MENU-ROLE-MANAGEMENT', '역할 관리', 'MENU-ROLES-PERMISSIONS', 1, 'SCR-ROLE-MANAGEMENT', '/system/roles-permissions/roles', 'SYSTEM', 'Y'),
    ('MENU-USER-ROLE-MANAGEMENT', '사용자 역할 관리', 'MENU-ROLES-PERMISSIONS', 2, 'SCR-USER-ROLE-MANAGEMENT', '/system/roles-permissions/user-roles', 'SYSTEM', 'Y'),
    ('MENU-MENU-PERMISSION-MANAGEMENT', '메뉴 권한 관리', 'MENU-ROLES-PERMISSIONS', 3, 'SCR-MENU-PERMISSION-MANAGEMENT', '/system/roles-permissions/menu-permissions', 'SYSTEM', 'Y'),
    ('MENU-MENU-STRUCTURE-MANAGEMENT', '메뉴 구조 관리', 'MENU-MANAGEMENT', 1, 'SCR-MENU-STRUCTURE-MANAGEMENT', '/system/menus/structure', 'SYSTEM', 'Y'),
    ('MENU-MENU-INFORMATION-MANAGEMENT', '메뉴 정보 관리', 'MENU-MANAGEMENT', 2, 'SCR-MENU-INFORMATION-MANAGEMENT', '/system/menus/information', 'SYSTEM', 'Y'),
    ('MENU-CODE-GROUP-MANAGEMENT', '코드그룹 관리', 'MENU-COMMON-CODES', 1, 'SCR-CODE-GROUP-MANAGEMENT', '/system/common-codes/groups', 'SYSTEM', 'Y'),
    ('MENU-DETAIL-CODE-MANAGEMENT', '상세코드 관리', 'MENU-COMMON-CODES', 2, 'SCR-DETAIL-CODE-MANAGEMENT', '/system/common-codes/detail-codes', 'SYSTEM', 'Y')
ON CONFLICT (menu_id) DO NOTHING;

INSERT INTO menu_permission (menu_permission_id, subject_type, subject_id, menu_id, access_allowed, status)
SELECT 'PERMISSION-R09-' || menu_id, 'ROLE', 'R09', menu_id, 'Y', 'ACTIVE'
FROM menu
ON CONFLICT (subject_type, subject_id, menu_id) DO NOTHING;
