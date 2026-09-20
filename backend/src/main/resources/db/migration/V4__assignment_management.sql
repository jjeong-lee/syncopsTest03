CREATE TABLE IF NOT EXISTS position_assignment (
    position_assignment_id varchar(100) PRIMARY KEY,
    position_code varchar(100) NOT NULL,
    user_id varchar(100) NOT NULL REFERENCES user_account(user_id),
    organization_id varchar(100) NOT NULL REFERENCES organization(organization_id),
    effective_start_date date NOT NULL,
    effective_end_date date,
    created_at timestamp with time zone NOT NULL DEFAULT current_timestamp,
    updated_at timestamp with time zone NOT NULL DEFAULT current_timestamp
);
COMMENT ON TABLE position_assignment IS '보직코드별 대상 사용자와 소속조직의 유효기간을 관리한다.';
COMMENT ON COLUMN position_assignment.user_id IS 'user_account.user_id 참조';
COMMENT ON COLUMN position_assignment.organization_id IS 'organization.organization_id 참조';

CREATE TABLE IF NOT EXISTS work_assignment (
    work_assignment_id varchar(100) PRIMARY KEY,
    organization_id varchar(100) NOT NULL REFERENCES organization(organization_id),
    user_id varchar(100) NOT NULL REFERENCES user_account(user_id),
    work_area varchar(100) NOT NULL,
    data_scope_type varchar(30) NOT NULL,
    process_permission varchar(1) NOT NULL CHECK (process_permission IN ('Y', 'N')),
    effective_start_date date NOT NULL,
    effective_end_date date,
    created_at timestamp with time zone NOT NULL DEFAULT current_timestamp,
    updated_at timestamp with time zone NOT NULL DEFAULT current_timestamp
);
COMMENT ON TABLE work_assignment IS '업무조직별 담당자, 업무영역, 데이터 범위와 처리권한의 유효기간을 관리한다.';
COMMENT ON COLUMN work_assignment.organization_id IS 'organization.organization_id 참조';
COMMENT ON COLUMN work_assignment.user_id IS 'user_account.user_id 참조';
COMMENT ON COLUMN work_assignment.data_scope_type IS '본인:본인|소속학과:소속학과|단과대학:단과대학|담당업무:담당업무|전체:전체';
COMMENT ON COLUMN work_assignment.process_permission IS 'Y:허용|N:미허용';

CREATE TABLE IF NOT EXISTS role_data_scope (
    role_data_scope_id varchar(100) PRIMARY KEY,
    role_code varchar(10) NOT NULL REFERENCES role(role_code),
    data_scope_type varchar(30) NOT NULL,
    organization_code varchar(100),
    work_area varchar(100),
    created_at timestamp with time zone NOT NULL DEFAULT current_timestamp,
    updated_at timestamp with time zone NOT NULL DEFAULT current_timestamp,
    UNIQUE (role_code, data_scope_type, organization_code, work_area)
);
COMMENT ON TABLE role_data_scope IS '역할별 서버 데이터 범위 유형과 조직코드 및 업무영역 조건을 관리한다.';
COMMENT ON COLUMN role_data_scope.data_scope_type IS '본인:본인|소속학과:소속학과|단과대학:단과대학|담당업무:담당업무|전체:전체';
COMMENT ON COLUMN role_data_scope.organization_code IS 'organization.organization_code 참조 의도 (FK 미선언)';

CREATE INDEX IF NOT EXISTS idx_position_assignment_key_dates ON position_assignment (position_code, user_id, organization_id, effective_start_date, effective_end_date);
CREATE INDEX IF NOT EXISTS idx_work_assignment_key_dates ON work_assignment (organization_id, user_id, work_area, effective_start_date, effective_end_date);
CREATE INDEX IF NOT EXISTS idx_role_data_scope_filters ON role_data_scope (role_code, data_scope_type, organization_code, work_area);

INSERT INTO menu (menu_id, menu_name, parent_menu_id, display_order, screen_id, url, business_category, use_yn)
VALUES
    ('MENU-POSITION-ASSIGNMENT-MANAGEMENT', '보직 관리', 'MENU-USER-ORGANIZATION', 3, 'SCR-POSITION-ASSIGNMENT-MANAGEMENT', '/system/user-organization/positions', 'SYSTEM', 'Y'),
    ('MENU-WORK-ASSIGNMENT-MANAGEMENT', '업무담당자 관리', 'MENU-USER-ORGANIZATION', 4, 'SCR-WORK-ASSIGNMENT-MANAGEMENT', '/system/user-organization/work-assignments', 'SYSTEM', 'Y'),
    ('MENU-ROLE-DATA-SCOPE-MANAGEMENT', '데이터 범위 권한', 'MENU-ROLES-PERMISSIONS', 4, 'SCR-ROLE-DATA-SCOPE-MANAGEMENT', '/system/roles-permissions/data-scopes', 'SYSTEM', 'Y')
ON CONFLICT (menu_id) DO NOTHING;

INSERT INTO menu_permission (menu_permission_id, subject_type, subject_id, menu_id, access_allowed, status)
VALUES
    ('PERMISSION-R09-POSITION-ASSIGNMENT', 'ROLE', 'R09', 'MENU-POSITION-ASSIGNMENT-MANAGEMENT', 'Y', 'ACTIVE'),
    ('PERMISSION-R09-WORK-ASSIGNMENT', 'ROLE', 'R09', 'MENU-WORK-ASSIGNMENT-MANAGEMENT', 'Y', 'ACTIVE'),
    ('PERMISSION-R09-ROLE-DATA-SCOPE', 'ROLE', 'R09', 'MENU-ROLE-DATA-SCOPE-MANAGEMENT', 'Y', 'ACTIVE')
ON CONFLICT (subject_type, subject_id, menu_id) DO NOTHING;
