ALTER TABLE detail_code
    ADD COLUMN IF NOT EXISTS created_by varchar(100);
ALTER TABLE detail_code
    ADD COLUMN IF NOT EXISTS updated_by varchar(100);
COMMENT ON COLUMN detail_code.created_by IS 'user_account.user_id 참조 의도 (FK 미선언)';
COMMENT ON COLUMN detail_code.updated_by IS 'user_account.user_id 참조 의도 (FK 미선언)';

INSERT INTO menu (
    menu_id, menu_name, parent_menu_id, display_order, screen_id, url, business_category, use_yn, exposure_start_at
)
VALUES (
    'MENU-DETAIL-CODE-USAGE-MANAGEMENT', '코드 사용 관리', 'MENU-COMMON-CODES', 3,
    'SCR-DETAIL-CODE-USAGE-MANAGEMENT', '/system/common-codes/usage', 'SYSTEM', 'Y', current_timestamp
)
ON CONFLICT (menu_id) DO NOTHING;

INSERT INTO menu_permission (menu_permission_id, subject_type, subject_id, menu_id, access_allowed, status)
VALUES ('PERMISSION-R09-MENU-DETAIL-CODE-USAGE-MANAGEMENT', 'ROLE', 'R09', 'MENU-DETAIL-CODE-USAGE-MANAGEMENT', 'Y', 'ACTIVE')
ON CONFLICT (subject_type, subject_id, menu_id) DO NOTHING;
