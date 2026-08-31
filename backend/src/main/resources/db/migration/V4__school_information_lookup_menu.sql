INSERT INTO menu (menu_id, menu_name, parent_menu_id, display_order, screen_id, url, business_category, description, use_yn)
VALUES
    ('MENU-EXTERNAL-INTEGRATIONS', '외부 연동', 'MENU-SYSTEM', 5, NULL, NULL, 'SYSTEM', '외부 시스템 연동 조회 메뉴', 'Y'),
    ('MENU-SCHOOL-INFORMATION-LOOKUP', '학교정보 조회', 'MENU-EXTERNAL-INTEGRATIONS', 1, 'SCR-SCHOOL-INFORMATION-LOOKUP', '/system/external-integrations/school-information', 'SYSTEM', 'NEIS 학교기본정보 조회 화면', 'Y')
ON CONFLICT (menu_id) DO NOTHING;

INSERT INTO menu_permission (menu_permission_id, subject_type, subject_id, menu_id, access_allowed, status)
VALUES
    ('PERMISSION-R09-EXTERNAL-INTEGRATIONS', 'ROLE', 'R09', 'MENU-EXTERNAL-INTEGRATIONS', 'Y', 'ACTIVE'),
    ('PERMISSION-R09-SCHOOL-INFORMATION-LOOKUP', 'ROLE', 'R09', 'MENU-SCHOOL-INFORMATION-LOOKUP', 'Y', 'ACTIVE')
ON CONFLICT (subject_type, subject_id, menu_id) DO NOTHING;
