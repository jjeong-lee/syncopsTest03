ALTER TABLE user_session ADD COLUMN IF NOT EXISTS ip_address varchar(64) NOT NULL DEFAULT 'UNKNOWN';
ALTER TABLE user_session ADD COLUMN IF NOT EXISTS last_activity_at timestamp with time zone NOT NULL DEFAULT current_timestamp;
ALTER TABLE user_session ADD COLUMN IF NOT EXISTS ended_at timestamp with time zone;
ALTER TABLE user_session DROP CONSTRAINT IF EXISTS user_session_status_check;
ALTER TABLE user_session ADD CONSTRAINT user_session_status_check CHECK (status IN ('ACTIVE', 'TERMINATED', 'FORCED_TERMINATED', 'IDLE_EXPIRED', 'ABSOLUTE_EXPIRED'));
COMMENT ON COLUMN user_session.status IS 'ACTIVE:활성|TERMINATED:로그아웃종료|FORCED_TERMINATED:관리자강제종료|IDLE_EXPIRED:유휴만료|ABSOLUTE_EXPIRED:절대만료';
COMMENT ON COLUMN user_session.ip_address IS '로그인 요청 시 애플리케이션에서 기록';
COMMENT ON COLUMN user_session.last_activity_at IS '인증된 요청 처리 시 애플리케이션에서 갱신';
COMMENT ON COLUMN user_session.ended_at IS '세션 종료 처리 시 애플리케이션에서 기록';

CREATE TABLE IF NOT EXISTS session_end_history (
    session_end_history_id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    session_id varchar(128) NOT NULL,
    user_id varchar(100) NOT NULL REFERENCES user_account(user_id),
    login_at timestamp with time zone NOT NULL,
    ended_at timestamp with time zone NOT NULL,
    end_type varchar(30) NOT NULL,
    actor_user_id varchar(100) REFERENCES user_account(user_id),
    reason text,
    ip_address varchar(64) NOT NULL
);
COMMENT ON TABLE session_end_history IS '종료된 로그인 세션의 종료 유형과 감사 정보를 수정·삭제 없이 보존한다.';
COMMENT ON COLUMN session_end_history.session_id IS 'user_session.session_id 참조 의도 (FK 미선언)';
COMMENT ON COLUMN session_end_history.end_type IS 'LOGOUT:로그아웃|IDLE_EXPIRED:유휴만료|ABSOLUTE_EXPIRED:절대만료|ADMIN_FORCED:관리자강제종료';
COMMENT ON COLUMN session_end_history.actor_user_id IS 'user_account.user_id 참조 의도 (FK 미선언)';
CREATE INDEX IF NOT EXISTS idx_session_end_history_user_ended_at ON session_end_history (user_id, ended_at DESC);
CREATE INDEX IF NOT EXISTS idx_user_session_active_status ON user_session (status, created_at DESC);

INSERT INTO menu (menu_id, menu_name, parent_menu_id, display_order, screen_id, url, business_category, use_yn)
VALUES
    ('MENU-SECURITY-AUDIT', '보안·감사 관리', 'MENU-SYSTEM', 5, NULL, NULL, 'SYSTEM', 'Y'),
    ('MENU-SESSION-STATUS-MANAGEMENT', '접속현황 관리', 'MENU-SECURITY-AUDIT', 1, 'SCR-SESSION-STATUS-MANAGEMENT', '/system/security-audit/session-status', 'SYSTEM', 'Y')
ON CONFLICT (menu_id) DO NOTHING;

INSERT INTO menu_permission (menu_permission_id, subject_type, subject_id, menu_id, access_allowed, status)
VALUES ('PERMISSION-R09-MENU-SESSION-STATUS-MANAGEMENT', 'ROLE', 'R09', 'MENU-SESSION-STATUS-MANAGEMENT', 'Y', 'ACTIVE')
ON CONFLICT (subject_type, subject_id, menu_id) DO NOTHING;