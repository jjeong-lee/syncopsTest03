INSERT INTO menu (
    menu_id,
    menu_name,
    parent_menu_id,
    display_order,
    screen_id,
    url,
    business_category,
    use_yn,
    exposure_start_at
)
VALUES (
    'MENU-REFERENCE-YEAR-MANAGEMENT',
    '기준연도 관리',
    'MENU-SYSTEM-SETTINGS',
    2,
    'SCR-REFERENCE-YEAR-MANAGEMENT',
    '/system/settings/reference-year',
    'SYSTEM',
    'Y',
    current_timestamp
)
ON CONFLICT (menu_id) DO NOTHING;

INSERT INTO menu_permission (menu_permission_id, subject_type, subject_id, menu_id, access_allowed, status)
VALUES (
    'PERMISSION-R09-MENU-REFERENCE-YEAR-MANAGEMENT',
    'ROLE',
    'R09',
    'MENU-REFERENCE-YEAR-MANAGEMENT',
    'Y',
    'ACTIVE'
)
ON CONFLICT (subject_type, subject_id, menu_id) DO NOTHING;
