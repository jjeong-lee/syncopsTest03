INSERT INTO menu (menu_id, menu_name, parent_menu_id, display_order, screen_id, url, business_category, use_yn, exposure_start_at)
VALUES
    ('MENU-SYSTEM-SETTINGS', '시스템 환경설정', 'MENU-SYSTEM', 5, NULL, NULL, 'SYSTEM', 'Y', current_timestamp),
    ('MENU-COMMON-ENVIRONMENT-SETTINGS', '공통 환경설정', 'MENU-SYSTEM-SETTINGS', 1, 'SCR-COMMON-ENVIRONMENT-SETTINGS', '/system/settings/common', 'SYSTEM', 'Y', current_timestamp)
ON CONFLICT (menu_id) DO NOTHING;

INSERT INTO menu_permission (menu_permission_id, subject_type, subject_id, menu_id, access_allowed, status)
VALUES ('PERMISSION-R09-MENU-COMMON-ENVIRONMENT-SETTINGS', 'ROLE', 'R09', 'MENU-COMMON-ENVIRONMENT-SETTINGS', 'Y', 'ACTIVE')
ON CONFLICT (subject_type, subject_id, menu_id) DO NOTHING;
