ALTER TABLE menu
    ADD COLUMN IF NOT EXISTS created_by varchar(100);
ALTER TABLE menu
    ADD COLUMN IF NOT EXISTS updated_by varchar(100);
COMMENT ON COLUMN menu.created_by IS 'user_account.user_id 참조 의도 (FK 미선언)';
COMMENT ON COLUMN menu.updated_by IS 'user_account.user_id 참조 의도 (FK 미선언)';

INSERT INTO menu (
    menu_id, menu_name, parent_menu_id, display_order, screen_id, url, business_category, use_yn, exposure_start_at
)
VALUES (
    'MENU-MENU-USAGE-MANAGEMENT', '메뉴 사용 관리', 'MENU-MANAGEMENT', 3,
    'SCR-MENU-USAGE-MANAGEMENT', '/system/menus/usage', 'SYSTEM', 'Y', current_timestamp
)
ON CONFLICT (menu_id) DO NOTHING;

INSERT INTO menu_permission (menu_permission_id, subject_type, subject_id, menu_id, access_allowed, status)
VALUES ('PERMISSION-R09-MENU-MENU-USAGE-MANAGEMENT', 'ROLE', 'R09', 'MENU-MENU-USAGE-MANAGEMENT', 'Y', 'ACTIVE')
ON CONFLICT (subject_type, subject_id, menu_id) DO NOTHING;
