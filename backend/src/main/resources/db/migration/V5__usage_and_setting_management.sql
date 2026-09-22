ALTER TABLE menu
    ADD COLUMN IF NOT EXISTS exposure_start_at timestamp with time zone;

UPDATE menu
SET exposure_start_at = created_at
WHERE exposure_start_at IS NULL;

ALTER TABLE menu
    ALTER COLUMN exposure_start_at SET NOT NULL;

ALTER TABLE menu
    ADD COLUMN IF NOT EXISTS exposure_end_at timestamp with time zone;
COMMENT ON COLUMN menu.exposure_start_at IS '서버 시각 기준 메뉴 노출 시작일시';
COMMENT ON COLUMN menu.exposure_end_at IS '서버 시각 기준 메뉴 노출 종료일시';

ALTER TABLE detail_code
    ADD COLUMN IF NOT EXISTS effective_start_date date;

UPDATE detail_code
SET effective_start_date = created_at::date
WHERE effective_start_date IS NULL;

ALTER TABLE detail_code
    ALTER COLUMN effective_start_date SET NOT NULL;

ALTER TABLE detail_code
    ADD COLUMN IF NOT EXISTS effective_end_date date;
COMMENT ON COLUMN detail_code.effective_start_date IS '서버 일자 기준 상세코드 적용 시작일';
COMMENT ON COLUMN detail_code.effective_end_date IS '서버 일자 기준 상세코드 적용 종료일';

CREATE TABLE IF NOT EXISTS common_environment_setting (
    common_environment_setting_id varchar(100) PRIMARY KEY,
    setting_key varchar(100) NOT NULL UNIQUE,
    setting_value text NOT NULL,
    created_at timestamp with time zone NOT NULL DEFAULT current_timestamp,
    created_by varchar(100),
    updated_at timestamp with time zone NOT NULL DEFAULT current_timestamp,
    updated_by varchar(100),
    deleted_yn varchar(1) NOT NULL DEFAULT 'N' CHECK (deleted_yn IN ('Y', 'N'))
);
COMMENT ON TABLE common_environment_setting IS '공통 운영환경 항목별 설정값과 감사 이력을 보존한다.';
COMMENT ON COLUMN common_environment_setting.created_by IS 'user_account.user_id 참조 의도 (FK 미선언)';
COMMENT ON COLUMN common_environment_setting.updated_by IS 'user_account.user_id 참조 의도 (FK 미선언)';
COMMENT ON COLUMN common_environment_setting.deleted_yn IS 'Y:삭제표시|N:정상';

CREATE TABLE IF NOT EXISTS reference_year_setting (
    reference_year_setting_id varchar(100) PRIMARY KEY,
    current_evaluation_year integer,
    default_query_year integer,
    target_year integer,
    baseline_copy_yn varchar(3),
    initialization_yn varchar(3),
    created_at timestamp with time zone NOT NULL DEFAULT current_timestamp,
    created_by varchar(100),
    updated_at timestamp with time zone NOT NULL DEFAULT current_timestamp,
    updated_by varchar(100),
    deleted_yn varchar(1) NOT NULL DEFAULT 'N' CHECK (deleted_yn IN ('Y', 'N')),
    CHECK (
        (target_year IS NULL
            AND current_evaluation_year IS NOT NULL
            AND default_query_year IS NOT NULL
            AND baseline_copy_yn IS NULL
            AND initialization_yn IS NULL)
        OR
        (target_year IS NOT NULL
            AND current_evaluation_year IS NULL
            AND default_query_year IS NULL
            AND baseline_copy_yn IN ('예', '아니오')
            AND initialization_yn IN ('예', '아니오'))
    )
);
COMMENT ON TABLE reference_year_setting IS '현재·기본 조회연도와 대상 연도별 준비 선택값을 보존하며 실제 실행은 수행하지 않는다.';
COMMENT ON COLUMN reference_year_setting.baseline_copy_yn IS '예:설정값 저장|아니오:설정값 저장';
COMMENT ON COLUMN reference_year_setting.initialization_yn IS '예:설정값 저장|아니오:설정값 저장';
COMMENT ON COLUMN reference_year_setting.created_by IS 'user_account.user_id 참조 의도 (FK 미선언)';
COMMENT ON COLUMN reference_year_setting.updated_by IS 'user_account.user_id 참조 의도 (FK 미선언)';
COMMENT ON COLUMN reference_year_setting.deleted_yn IS 'Y:삭제표시|N:정상';

CREATE INDEX IF NOT EXISTS idx_menu_usage_period
    ON menu (use_yn, exposure_start_at, exposure_end_at);
CREATE INDEX IF NOT EXISTS idx_detail_code_usage_period
    ON detail_code (group_id, use_yn, effective_start_date, effective_end_date);
CREATE UNIQUE INDEX IF NOT EXISTS uq_reference_year_setting_configuration
    ON reference_year_setting ((1))
    WHERE deleted_yn = 'N' AND target_year IS NULL;
CREATE UNIQUE INDEX IF NOT EXISTS uq_reference_year_setting_target_year
    ON reference_year_setting (target_year)
    WHERE deleted_yn = 'N' AND target_year IS NOT NULL;
