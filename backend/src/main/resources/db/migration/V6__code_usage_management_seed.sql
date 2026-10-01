INSERT INTO menu (
    menu_id,
    menu_name,
    parent_menu_id,
    display_order,
    screen_id,
    url,
    business_category,
    use_yn,
    exposure_start_at,
    exposure_end_at
)
VALUES (
    'MENU-CODE-USAGE-MANAGEMENT',
    '코드 사용 관리',
    'MENU-COMMON-CODES',
    3,
    'SCR-CODE-USAGE-MANAGEMENT',
    '/system/common-codes/usage',
    'SYSTEM',
    'Y',
    current_timestamp,
    NULL
)
ON CONFLICT (menu_id) DO NOTHING;

INSERT INTO menu_permission (menu_permission_id, subject_type, subject_id, menu_id, access_allowed, status)
VALUES (
    'PERMISSION-R09-MENU-CODE-USAGE-MANAGEMENT',
    'ROLE',
    'R09',
    'MENU-CODE-USAGE-MANAGEMENT',
    'Y',
    'ACTIVE'
)
ON CONFLICT (subject_type, subject_id, menu_id) DO NOTHING;
