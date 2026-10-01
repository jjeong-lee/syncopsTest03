ALTER TABLE menu
    ADD COLUMN IF NOT EXISTS exposure_start_at timestamp with time zone,
    ADD COLUMN IF NOT EXISTS exposure_end_at timestamp with time zone;

ALTER TABLE menu
    DROP CONSTRAINT IF EXISTS chk_menu_exposure_period;

ALTER TABLE menu
    ADD CONSTRAINT chk_menu_exposure_period
        CHECK (
            exposure_start_at IS NULL
            OR exposure_end_at IS NULL
            OR exposure_start_at <= exposure_end_at
        );

COMMENT ON COLUMN menu.exposure_start_at IS '메뉴 노출기간의 시작 시각; 기존 행은 이력 호환을 위해 NULL을 허용하며 변경 저장 시 서버가 필수 여부를 검증';
COMMENT ON COLUMN menu.exposure_end_at IS '메뉴 노출기간의 종료 시각; NULL이면 상한 없이 유효';

ALTER TABLE detail_code
    ADD COLUMN IF NOT EXISTS application_start_date date,
    ADD COLUMN IF NOT EXISTS application_end_date date;

ALTER TABLE detail_code
    DROP CONSTRAINT IF EXISTS chk_detail_code_application_period;

ALTER TABLE detail_code
    ADD CONSTRAINT chk_detail_code_application_period
        CHECK (
            application_start_date IS NULL
            OR application_end_date IS NULL
            OR application_start_date <= application_end_date
        );

COMMENT ON COLUMN detail_code.application_start_date IS '상세코드 적용기간의 시작 일자; 기존 행은 이력 호환을 위해 NULL을 허용하며 변경 저장 시 서버가 필수 여부를 검증';
COMMENT ON COLUMN detail_code.application_end_date IS '상세코드 적용기간의 종료 일자; NULL이면 상한 없이 유효';

CREATE TABLE IF NOT EXISTS system_setting (
    setting_key varchar(100) PRIMARY KEY,
    setting_value varchar(500) NOT NULL,
    target_year integer,
    created_at timestamp with time zone NOT NULL DEFAULT current_timestamp,
    created_by varchar(100) NOT NULL,
    updated_at timestamp with time zone NOT NULL DEFAULT current_timestamp,
    updated_by varchar(100) NOT NULL
);

COMMENT ON TABLE system_setting IS '공통 환경설정과 기준연도 설정값 및 생성·수정 감사를 관리한다.';
COMMENT ON COLUMN system_setting.target_year IS '설정값이 적용되는 대상 연도; 대상 연도별 설정이 아닌 항목은 NULL';

CREATE INDEX IF NOT EXISTS idx_system_setting_target_year ON system_setting (target_year);

ALTER TABLE user_session
    ADD COLUMN IF NOT EXISTS idle_minutes integer NOT NULL DEFAULT 480;

COMMENT ON COLUMN user_session.idle_minutes IS '세션 생성 시 적용된 유휴시간(분); 공통 환경설정 변경은 이미 열린 세션 값을 변경하지 않음';
