CREATE TABLE IF NOT EXISTS user_account (
    user_id varchar(100) PRIMARY KEY,
    personnel_no varchar(100) NOT NULL UNIQUE,
    password_hash varchar(255) NOT NULL,
    password_salt varchar(64) NOT NULL,
    use_yn varchar(1) NOT NULL DEFAULT 'Y' CHECK (use_yn IN ('Y', 'N')),
    status varchar(30) NOT NULL DEFAULT 'ACTIVE',
    created_at timestamp with time zone NOT NULL DEFAULT current_timestamp,
    updated_at timestamp with time zone NOT NULL DEFAULT current_timestamp
);
COMMENT ON TABLE user_account IS '로컬 시스템 로그인 계정과 사용 여부를 관리한다.';
COMMENT ON COLUMN user_account.use_yn IS 'Y:사용|N:미사용';
COMMENT ON COLUMN user_account.status IS 'ACTIVE:활성|INACTIVE:비활성';

CREATE TABLE IF NOT EXISTS organization (
    organization_id varchar(100) PRIMARY KEY,
    organization_code varchar(100) NOT NULL UNIQUE,
    organization_name varchar(200) NOT NULL,
    organization_type varchar(30) NOT NULL,
    use_yn varchar(1) NOT NULL DEFAULT 'Y' CHECK (use_yn IN ('Y', 'N')),
    created_at timestamp with time zone NOT NULL DEFAULT current_timestamp,
    updated_at timestamp with time zone NOT NULL DEFAULT current_timestamp
);
COMMENT ON TABLE organization IS '로컬 관리 대상 조직의 식별 정보와 사용 상태를 관리한다.';
COMMENT ON COLUMN organization.use_yn IS 'Y:사용|N:미사용';

CREATE TABLE IF NOT EXISTS korus_personnel_snapshot (
    personnel_no varchar(100) PRIMARY KEY,
    name varchar(100) NOT NULL,
    organization_code varchar(100),
    position_name varchar(100),
    employment_status varchar(30),
    retirement_date date,
    last_synced_at timestamp with time zone NOT NULL,
    use_yn varchar(1) NOT NULL DEFAULT 'Y' CHECK (use_yn IN ('Y', 'N')),
    created_at timestamp with time zone NOT NULL DEFAULT current_timestamp,
    updated_at timestamp with time zone NOT NULL DEFAULT current_timestamp
);
COMMENT ON TABLE korus_personnel_snapshot IS 'KORUS 원천 인사 정보를 모사하는 읽기 전용 스냅샷이다.';
COMMENT ON COLUMN korus_personnel_snapshot.organization_code IS 'organization.organization_code 참조 의도 (FK 미선언)';
COMMENT ON COLUMN korus_personnel_snapshot.use_yn IS 'Y:사용|N:미사용';

CREATE TABLE IF NOT EXISTS organization_relationship (
    organization_relationship_id varchar(100) PRIMARY KEY,
    organization_id varchar(100) NOT NULL REFERENCES organization(organization_id),
    parent_organization_id varchar(100) NOT NULL REFERENCES organization(organization_id),
    effective_start_date date NOT NULL,
    effective_end_date date,
    status varchar(30) NOT NULL DEFAULT 'ACTIVE',
    created_at timestamp with time zone NOT NULL DEFAULT current_timestamp,
    updated_at timestamp with time zone NOT NULL DEFAULT current_timestamp
);
COMMENT ON TABLE organization_relationship IS '조직의 내부 보정 상위 관계와 적용 기간을 보존한다.';
COMMENT ON COLUMN organization_relationship.status IS 'ACTIVE:활성|INACTIVE:비활성';

CREATE TABLE IF NOT EXISTS organization_user_mapping (
    organization_user_mapping_id varchar(100) PRIMARY KEY,
    organization_id varchar(100) NOT NULL REFERENCES organization(organization_id),
    user_id varchar(100) NOT NULL REFERENCES user_account(user_id),
    position_name varchar(100),
    use_yn varchar(1) NOT NULL DEFAULT 'Y' CHECK (use_yn IN ('Y', 'N')),
    created_at timestamp with time zone NOT NULL DEFAULT current_timestamp,
    updated_at timestamp with time zone NOT NULL DEFAULT current_timestamp
);
COMMENT ON TABLE organization_user_mapping IS '사용자와 조직 및 보직의 내부 매핑을 관리한다.';
COMMENT ON COLUMN organization_user_mapping.use_yn IS 'Y:사용|N:미사용';

CREATE TABLE IF NOT EXISTS role (
    role_code varchar(10) PRIMARY KEY,
    role_name varchar(100) NOT NULL,
    purpose varchar(500) NOT NULL,
    assignment_criteria text,
    default_data_scope text,
    use_yn varchar(1) NOT NULL DEFAULT 'Y' CHECK (use_yn IN ('Y', 'N')),
    created_at timestamp with time zone NOT NULL DEFAULT current_timestamp,
    updated_at timestamp with time zone NOT NULL DEFAULT current_timestamp
);
COMMENT ON TABLE role IS '시스템 업무 역할 코드와 역할별 운영 정보를 관리한다.';
COMMENT ON COLUMN role.use_yn IS 'Y:사용|N:미사용';

CREATE TABLE IF NOT EXISTS user_role (
    user_role_id varchar(100) PRIMARY KEY,
    user_id varchar(100) NOT NULL REFERENCES user_account(user_id),
    role_code varchar(10) NOT NULL REFERENCES role(role_code),
    approval_user_id varchar(100) NOT NULL REFERENCES user_account(user_id),
    effective_start_date date NOT NULL,
    effective_end_date date,
    assignment_type varchar(30) NOT NULL,
    status varchar(30) NOT NULL DEFAULT 'ACTIVE',
    created_at timestamp with time zone NOT NULL DEFAULT current_timestamp,
    updated_at timestamp with time zone NOT NULL DEFAULT current_timestamp
);
COMMENT ON TABLE user_role IS '사용자에게 부여된 역할과 승인 및 유효 기간을 보존한다.';
COMMENT ON COLUMN user_role.status IS 'ACTIVE:활성|REVOKED:회수|INACTIVE:비활성';

CREATE TABLE IF NOT EXISTS menu (
    menu_id varchar(100) PRIMARY KEY,
    menu_name varchar(200) NOT NULL,
    parent_menu_id varchar(100) REFERENCES menu(menu_id),
    display_order integer NOT NULL,
    screen_id varchar(100),
    url varchar(500),
    icon varchar(100),
    business_category varchar(100),
    description text,
    use_yn varchar(1) NOT NULL DEFAULT 'Y' CHECK (use_yn IN ('Y', 'N')),
    created_at timestamp with time zone NOT NULL DEFAULT current_timestamp,
    updated_at timestamp with time zone NOT NULL DEFAULT current_timestamp
);
COMMENT ON TABLE menu IS '시스템 관리 화면의 계층과 실행 정보를 관리한다.';
COMMENT ON COLUMN menu.parent_menu_id IS 'menu.menu_id 참조 의도 (자기 참조 FK 선언)';
COMMENT ON COLUMN menu.use_yn IS 'Y:사용|N:미사용';

CREATE TABLE IF NOT EXISTS menu_permission (
    menu_permission_id varchar(100) PRIMARY KEY,
    subject_type varchar(30) NOT NULL,
    subject_id varchar(100) NOT NULL,
    menu_id varchar(100) NOT NULL REFERENCES menu(menu_id),
    access_allowed varchar(1) NOT NULL CHECK (access_allowed IN ('Y', 'N')),
    status varchar(30) NOT NULL DEFAULT 'ACTIVE',
    created_at timestamp with time zone NOT NULL DEFAULT current_timestamp,
    updated_at timestamp with time zone NOT NULL DEFAULT current_timestamp,
    UNIQUE (subject_type, subject_id, menu_id)
);
COMMENT ON TABLE menu_permission IS '권한 대상별 메뉴 접근 허용 여부를 관리한다.';
COMMENT ON COLUMN menu_permission.subject_type IS 'ROLE:역할|ORGANIZATION:조직|USER:사용자';
COMMENT ON COLUMN menu_permission.access_allowed IS 'Y:허용|N:차단';
COMMENT ON COLUMN menu_permission.status IS 'ACTIVE:활성|INACTIVE:비활성';

CREATE TABLE IF NOT EXISTS code_group (
    group_id varchar(100) PRIMARY KEY,
    group_name varchar(200) NOT NULL,
    description text,
    management_department varchar(200),
    use_yn varchar(1) NOT NULL DEFAULT 'Y' CHECK (use_yn IN ('Y', 'N')),
    created_at timestamp with time zone NOT NULL DEFAULT current_timestamp,
    updated_at timestamp with time zone NOT NULL DEFAULT current_timestamp
);
COMMENT ON TABLE code_group IS '공통 상세코드를 묶는 로컬 관리 코드그룹이다.';
COMMENT ON COLUMN code_group.use_yn IS 'Y:사용|N:미사용';

CREATE TABLE IF NOT EXISTS detail_code (
    detail_code_id varchar(100) PRIMARY KEY,
    group_id varchar(100) NOT NULL REFERENCES code_group(group_id),
    code_value varchar(100) NOT NULL,
    code_name varchar(200) NOT NULL,
    parent_detail_code_id varchar(100) REFERENCES detail_code(detail_code_id),
    display_order integer NOT NULL,
    additional_attributes jsonb,
    use_yn varchar(1) NOT NULL DEFAULT 'Y' CHECK (use_yn IN ('Y', 'N')),
    created_at timestamp with time zone NOT NULL DEFAULT current_timestamp,
    updated_at timestamp with time zone NOT NULL DEFAULT current_timestamp,
    UNIQUE (group_id, code_value)
);
COMMENT ON TABLE detail_code IS '코드그룹별 상세코드 계층과 연계 속성을 관리한다.';
COMMENT ON COLUMN detail_code.parent_detail_code_id IS 'detail_code.detail_code_id 참조 의도 (자기 참조 FK 선언)';
COMMENT ON COLUMN detail_code.additional_attributes IS '상세코드 저장 시 애플리케이션에서 갱신';
COMMENT ON COLUMN detail_code.use_yn IS 'Y:사용|N:미사용';

CREATE TABLE IF NOT EXISTS user_session (
    session_id varchar(128) PRIMARY KEY,
    user_id varchar(100) NOT NULL REFERENCES user_account(user_id),
    status varchar(30) NOT NULL DEFAULT 'ACTIVE',
    created_at timestamp with time zone NOT NULL DEFAULT current_timestamp,
    updated_at timestamp with time zone NOT NULL DEFAULT current_timestamp
);
COMMENT ON TABLE user_session IS 'HttpOnly 세션 쿠키에 연결되는 로그인 세션을 보존한다.';
COMMENT ON COLUMN user_session.status IS 'ACTIVE:활성|TERMINATED:종료';

CREATE TABLE IF NOT EXISTS change_history (
    change_history_id varchar(100) PRIMARY KEY,
    entity_name varchar(100) NOT NULL,
    entity_id varchar(100) NOT NULL,
    before_value jsonb,
    after_value jsonb,
    actor_user_id varchar(100) NOT NULL REFERENCES user_account(user_id),
    changed_at timestamp with time zone NOT NULL DEFAULT current_timestamp,
    reason text,
    status varchar(30) NOT NULL DEFAULT 'RECORDED',
    created_at timestamp with time zone NOT NULL DEFAULT current_timestamp,
    updated_at timestamp with time zone NOT NULL DEFAULT current_timestamp
);
COMMENT ON TABLE change_history IS '변경 전후 값과 처리자, 처리 시각, 사유를 보존하는 내부 추적 구조다.';
COMMENT ON COLUMN change_history.status IS 'RECORDED:기록됨';

CREATE INDEX IF NOT EXISTS idx_korus_personnel_snapshot_organization_code ON korus_personnel_snapshot (organization_code);
CREATE INDEX IF NOT EXISTS idx_organization_relationship_organization_id ON organization_relationship (organization_id);
CREATE INDEX IF NOT EXISTS idx_organization_user_mapping_user_id ON organization_user_mapping (user_id);
CREATE INDEX IF NOT EXISTS idx_user_role_user_id ON user_role (user_id);
CREATE INDEX IF NOT EXISTS idx_menu_permission_subject ON menu_permission (subject_type, subject_id);
CREATE INDEX IF NOT EXISTS idx_user_session_active_user ON user_session (user_id, status);
