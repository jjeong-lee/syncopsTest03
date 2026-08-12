ALTER TABLE user_session
    ADD COLUMN IF NOT EXISTS login_at timestamp with time zone NOT NULL DEFAULT current_timestamp,
    ADD COLUMN IF NOT EXISTS last_activity_at timestamp with time zone NOT NULL DEFAULT current_timestamp,
    ADD COLUMN IF NOT EXISTS ip_address varchar(45) NOT NULL DEFAULT '0.0.0.0',
    ADD COLUMN IF NOT EXISTS termination_type varchar(30),
    ADD COLUMN IF NOT EXISTS terminated_at timestamp with time zone,
    ADD COLUMN IF NOT EXISTS terminated_by varchar(100),
    ADD COLUMN IF NOT EXISTS termination_reason text;

COMMENT ON COLUMN user_session.termination_type IS 'LOGOUT:로그아웃|IDLE_TIMEOUT:유휴만료|ABSOLUTE_TIMEOUT:절대만료|ADMIN_TERMINATED:관리자 강제종료';
COMMENT ON COLUMN user_session.terminated_by IS 'user_account.user_id 참조 의도 (FK 미선언)';

CREATE INDEX IF NOT EXISTS idx_user_session_status_studio_activity
    ON user_session (status, last_activity_at DESC);
CREATE INDEX IF NOT EXISTS idx_user_session_termination_history
    ON user_session (user_id, terminated_at DESC)
    WHERE terminated_at IS NOT NULL;
