INSERT INTO menu (menu_id, menu_name, parent_menu_id, display_order, screen_id, url, business_category, use_yn)
VALUES
    ('MENU-SECURITY-AUDIT', '보안·감사 관리', NULL, 5, NULL, NULL, 'SECURITY', 'Y'),
    ('MENU-ACCESS-RECORDS', '접속기록 관리', 'MENU-SECURITY-AUDIT', 1, NULL, NULL, 'SECURITY', 'Y'),
    ('MENU-SESSION-STATUS-MANAGEMENT', '접속현황 관리', 'MENU-ACCESS-RECORDS', 1, 'SCR-SESSION-STATUS-MANAGEMENT', '/security-audit/session-status', 'SECURITY', 'Y'),
    ('MENU-SESSION-TERMINATION-HISTORY', '세션 종료이력', 'MENU-ACCESS-RECORDS', 2, 'SCR-SESSION-TERMINATION-HISTORY', '/security-audit/session-termination-history', 'SECURITY', 'Y')
ON CONFLICT (menu_id) DO NOTHING;

INSERT INTO menu_permission (menu_permission_id, subject_type, subject_id, menu_id, access_allowed, status)
VALUES
    ('PERMISSION-R09-SESSION-STATUS', 'ROLE', 'R09', 'MENU-SESSION-STATUS-MANAGEMENT', 'Y', 'ACTIVE'),
    ('PERMISSION-R09-SESSION-HISTORY', 'ROLE', 'R09', 'MENU-SESSION-TERMINATION-HISTORY', 'Y', 'ACTIVE')
ON CONFLICT (subject_type, subject_id, menu_id) DO NOTHING;
