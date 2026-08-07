CREATE TABLE IF NOT EXISTS common_management_records (
  area varchar(80) NOT NULL,
  record_id varchar(120) NOT NULL,
  title varchar(300) NOT NULL,
  status varchar(40) NOT NULL DEFAULT 'ACTIVE',
  use_yn char(1) NOT NULL DEFAULT 'Y',
  payload jsonb NOT NULL DEFAULT '{}'::jsonb,
  created_at timestamp with time zone NOT NULL DEFAULT now(),
  updated_at timestamp with time zone NOT NULL DEFAULT now(),
  PRIMARY KEY (area, record_id)
);
COMMENT ON TABLE common_management_records IS '25개 공통 관리 화면의 API 계약형 데이터를 저장한다. 각 화면별 실제 관리 데이터는 area와 record_id로 구분한다.';
COMMENT ON COLUMN common_management_records.status IS 'ACTIVE:활성|INACTIVE:비활성|DELETED:논리삭제|PENDING:대기|COMPLETED:완료|FAILED:실패';
COMMENT ON COLUMN common_management_records.payload IS 'ManagedRecordService.save 시 API 요청의 도메인별 확장 필드를 저장한다.';
CREATE INDEX IF NOT EXISTS idx_common_management_records_area_title ON common_management_records(area, title);
CREATE TABLE IF NOT EXISTS k_o_r_u_s_mock_snapshot (
  employee_no varchar(120) PRIMARY KEY,
  name varchar(300) NOT NULL,
  organization_code varchar(120) NOT NULL,
  position_grade varchar(120),
  employment_status varchar(40) NOT NULL DEFAULT 'ACTIVE',
  retirement_date date,
  last_sync_at timestamp with time zone,
  use_yn char(1) NOT NULL DEFAULT 'Y',
  created_at timestamp with time zone NOT NULL DEFAULT now(),
  updated_at timestamp with time zone NOT NULL DEFAULT now(),
  CONSTRAINT chk_k_o_r_u_s_mock_snapshot_employment_status CHECK (employment_status IN ('ACTIVE', 'RETIRED', 'LEAVE')),
  CONSTRAINT chk_k_o_r_u_s_mock_snapshot_use_yn CHECK (use_yn IN ('Y', 'N'))
);
COMMENT ON TABLE k_o_r_u_s_mock_snapshot IS 'KORUS 교직원 Mock snapshot의 조회 전용 원천 인사정보를 보관한다. 외부 KORUS 연계 없이 사용자 관리 조회에 필요한 교번·성명·소속·직급·재직상태를 제공한다.';
COMMENT ON COLUMN k_o_r_u_s_mock_snapshot.employment_status IS 'ACTIVE:재직|RETIRED:퇴직|LEAVE:휴직';
COMMENT ON COLUMN k_o_r_u_s_mock_snapshot.use_yn IS 'Y:사용|N:미사용';
CREATE INDEX IF NOT EXISTS idx_k_o_r_u_s_mock_snapshot_organization ON k_o_r_u_s_mock_snapshot(organization_code);
CREATE INDEX IF NOT EXISTS idx_k_o_r_u_s_mock_snapshot_status ON k_o_r_u_s_mock_snapshot(employment_status, use_yn);
CREATE TABLE IF NOT EXISTS user_accounts (
  account_id varchar(120) PRIMARY KEY,
  employee_no varchar(120) NOT NULL,
  name varchar(300) NOT NULL,
  login_id varchar(120),
  system_use_yn char(1) NOT NULL DEFAULT 'Y',
  primary_role_code varchar(20),
  status varchar(40) NOT NULL DEFAULT 'ACTIVE',
  use_yn char(1) NOT NULL DEFAULT 'Y',
  payload jsonb NOT NULL DEFAULT '{}'::jsonb,
  created_at timestamp with time zone NOT NULL DEFAULT now(),
  updated_at timestamp with time zone NOT NULL DEFAULT now(),
  CONSTRAINT uq_user_accounts_employee_no UNIQUE (employee_no),
  CONSTRAINT uq_user_accounts_login_id UNIQUE (login_id),
  CONSTRAINT fk_user_accounts_korus_employee FOREIGN KEY (employee_no) REFERENCES k_o_r_u_s_mock_snapshot(employee_no),
  CONSTRAINT chk_user_accounts_system_use_yn CHECK (system_use_yn IN ('Y', 'N')),
  CONSTRAINT chk_user_accounts_use_yn CHECK (use_yn IN ('Y', 'N'))
);
COMMENT ON TABLE user_accounts IS 'KORUS 교직원 snapshot에 연결된 내부 사용자 계정을 관리한다. KORUS 원천 필드는 읽기 전용이며 시스템 사용여부와 주 역할만 로컬 DB에서 변경한다.';
COMMENT ON COLUMN user_accounts.status IS 'ACTIVE:활성|INACTIVE:비활성|DELETED:논리삭제|PENDING:대기|COMPLETED:완료|FAILED:실패';
COMMENT ON COLUMN user_accounts.system_use_yn IS 'Y:사용|N:미사용';
COMMENT ON COLUMN user_accounts.use_yn IS 'Y:사용|N:미사용';
COMMENT ON COLUMN user_accounts.employee_no IS 'k_o_r_u_s_mock_snapshot.employee_no 참조';
COMMENT ON COLUMN user_accounts.primary_role_code IS 'roles.id 참조 의도 (roles 테이블 생성 순서상 FK 미선언)';
COMMENT ON COLUMN user_accounts.payload IS 'ManagedRecordService.save 시 사용자 관리의 로컬 확장 필드를 저장한다.';
CREATE INDEX IF NOT EXISTS idx_user_accounts_status ON user_accounts(status);
CREATE INDEX IF NOT EXISTS idx_user_accounts_primary_role ON user_accounts(primary_role_code);
CREATE TABLE IF NOT EXISTS korus_personnel_snapshots (
  id varchar(120) PRIMARY KEY,
  name varchar(300) NOT NULL,
  status varchar(40) NOT NULL DEFAULT 'ACTIVE',
  use_yn char(1) NOT NULL DEFAULT 'Y',
  payload jsonb NOT NULL DEFAULT '{}'::jsonb,
  created_at timestamp with time zone NOT NULL DEFAULT now(),
  updated_at timestamp with time zone NOT NULL DEFAULT now()
);
COMMENT ON TABLE korus_personnel_snapshots IS '이전 생성 산출물 호환용 KORUS snapshot 테이블이다. 실제 사용자 관리 조회 계약은 k_o_r_u_s_mock_snapshot이 소유한다.';
COMMENT ON COLUMN korus_personnel_snapshots.status IS 'ACTIVE:활성|INACTIVE:비활성|DELETED:논리삭제|PENDING:대기|COMPLETED:완료|FAILED:실패';
COMMENT ON COLUMN korus_personnel_snapshots.payload IS '이전 호환 경로에서 애플리케이션 서비스 저장 시 도메인 확장 속성을 보존한다.';
CREATE INDEX IF NOT EXISTS idx_korus_personnel_snapshots_status ON korus_personnel_snapshots(status);
CREATE TABLE IF NOT EXISTS organizations (
  id varchar(120) PRIMARY KEY,
  name varchar(300) NOT NULL,
  status varchar(40) NOT NULL DEFAULT 'ACTIVE',
  use_yn char(1) NOT NULL DEFAULT 'Y',
  payload jsonb NOT NULL DEFAULT '{}'::jsonb,
  created_at timestamp with time zone NOT NULL DEFAULT now(),
  updated_at timestamp with time zone NOT NULL DEFAULT now()
);
COMMENT ON TABLE organizations IS '조직 코드와 계층 적용기간을 관리한다.';
COMMENT ON COLUMN organizations.status IS 'ACTIVE:활성|INACTIVE:비활성|DELETED:논리삭제|PENDING:대기|COMPLETED:완료|FAILED:실패';
COMMENT ON COLUMN organizations.payload IS '애플리케이션 서비스 저장 시 도메인 확장 속성을 보존한다.';
CREATE INDEX IF NOT EXISTS idx_organizations_status ON organizations(status);
CREATE TABLE IF NOT EXISTS position_assignments (
  id varchar(120) PRIMARY KEY,
  name varchar(300) NOT NULL,
  status varchar(40) NOT NULL DEFAULT 'ACTIVE',
  use_yn char(1) NOT NULL DEFAULT 'Y',
  payload jsonb NOT NULL DEFAULT '{}'::jsonb,
  created_at timestamp with time zone NOT NULL DEFAULT now(),
  updated_at timestamp with time zone NOT NULL DEFAULT now()
);
COMMENT ON TABLE position_assignments IS '보직과 사용자·조직·유효기간 매핑을 관리한다.';
COMMENT ON COLUMN position_assignments.status IS 'ACTIVE:활성|INACTIVE:비활성|DELETED:논리삭제|PENDING:대기|COMPLETED:완료|FAILED:실패';
COMMENT ON COLUMN position_assignments.payload IS '애플리케이션 서비스 저장 시 도메인 확장 속성을 보존한다.';
CREATE INDEX IF NOT EXISTS idx_position_assignments_status ON position_assignments(status);
CREATE TABLE IF NOT EXISTS roles (
  id varchar(120) PRIMARY KEY,
  name varchar(300) NOT NULL,
  status varchar(40) NOT NULL DEFAULT 'ACTIVE',
  use_yn char(1) NOT NULL DEFAULT 'Y',
  payload jsonb NOT NULL DEFAULT '{}'::jsonb,
  created_at timestamp with time zone NOT NULL DEFAULT now(),
  updated_at timestamp with time zone NOT NULL DEFAULT now()
);
COMMENT ON TABLE roles IS 'R01~R09 역할의 목적과 기본 데이터 범위를 관리한다.';
COMMENT ON COLUMN roles.status IS 'ACTIVE:활성|INACTIVE:비활성|DELETED:논리삭제|PENDING:대기|COMPLETED:완료|FAILED:실패';
COMMENT ON COLUMN roles.payload IS '애플리케이션 서비스 저장 시 도메인 확장 속성을 보존한다.';
CREATE INDEX IF NOT EXISTS idx_roles_status ON roles(status);
CREATE TABLE IF NOT EXISTS user_roles (
  id varchar(120) PRIMARY KEY,
  name varchar(300) NOT NULL,
  status varchar(40) NOT NULL DEFAULT 'ACTIVE',
  use_yn char(1) NOT NULL DEFAULT 'Y',
  payload jsonb NOT NULL DEFAULT '{}'::jsonb,
  created_at timestamp with time zone NOT NULL DEFAULT now(),
  updated_at timestamp with time zone NOT NULL DEFAULT now()
);
COMMENT ON TABLE user_roles IS '사용자별 역할 부여·회수와 유효기간을 관리한다.';
COMMENT ON COLUMN user_roles.status IS 'ACTIVE:활성|INACTIVE:비활성|DELETED:논리삭제|PENDING:대기|COMPLETED:완료|FAILED:실패';
COMMENT ON COLUMN user_roles.payload IS '애플리케이션 서비스 저장 시 도메인 확장 속성을 보존한다.';
CREATE INDEX IF NOT EXISTS idx_user_roles_status ON user_roles(status);
CREATE TABLE IF NOT EXISTS menus (
  id varchar(120) PRIMARY KEY,
  name varchar(300) NOT NULL,
  status varchar(40) NOT NULL DEFAULT 'ACTIVE',
  use_yn char(1) NOT NULL DEFAULT 'Y',
  payload jsonb NOT NULL DEFAULT '{}'::jsonb,
  created_at timestamp with time zone NOT NULL DEFAULT now(),
  updated_at timestamp with time zone NOT NULL DEFAULT now()
);
COMMENT ON TABLE menus IS '대·중·소메뉴 계층과 화면 실행 정보를 관리한다.';
COMMENT ON COLUMN menus.status IS 'ACTIVE:활성|INACTIVE:비활성|DELETED:논리삭제|PENDING:대기|COMPLETED:완료|FAILED:실패';
COMMENT ON COLUMN menus.payload IS '애플리케이션 서비스 저장 시 도메인 확장 속성을 보존한다.';
CREATE INDEX IF NOT EXISTS idx_menus_status ON menus(status);
CREATE TABLE IF NOT EXISTS menu_permissions (
  id varchar(120) PRIMARY KEY,
  name varchar(300) NOT NULL,
  status varchar(40) NOT NULL DEFAULT 'ACTIVE',
  use_yn char(1) NOT NULL DEFAULT 'Y',
  payload jsonb NOT NULL DEFAULT '{}'::jsonb,
  created_at timestamp with time zone NOT NULL DEFAULT now(),
  updated_at timestamp with time zone NOT NULL DEFAULT now()
);
COMMENT ON TABLE menu_permissions IS '역할·조직·사용자별 메뉴 접근 허용 여부를 관리한다.';
COMMENT ON COLUMN menu_permissions.status IS 'ACTIVE:활성|INACTIVE:비활성|DELETED:논리삭제|PENDING:대기|COMPLETED:완료|FAILED:실패';
COMMENT ON COLUMN menu_permissions.payload IS '애플리케이션 서비스 저장 시 도메인 확장 속성을 보존한다.';
CREATE INDEX IF NOT EXISTS idx_menu_permissions_status ON menu_permissions(status);
CREATE TABLE IF NOT EXISTS function_permissions (
  id varchar(120) PRIMARY KEY,
  name varchar(300) NOT NULL,
  status varchar(40) NOT NULL DEFAULT 'ACTIVE',
  use_yn char(1) NOT NULL DEFAULT 'Y',
  payload jsonb NOT NULL DEFAULT '{}'::jsonb,
  created_at timestamp with time zone NOT NULL DEFAULT now(),
  updated_at timestamp with time zone NOT NULL DEFAULT now()
);
COMMENT ON TABLE function_permissions IS '역할별 화면 기능 권한을 관리한다.';
COMMENT ON COLUMN function_permissions.status IS 'ACTIVE:활성|INACTIVE:비활성|DELETED:논리삭제|PENDING:대기|COMPLETED:완료|FAILED:실패';
COMMENT ON COLUMN function_permissions.payload IS '애플리케이션 서비스 저장 시 도메인 확장 속성을 보존한다.';
CREATE INDEX IF NOT EXISTS idx_function_permissions_status ON function_permissions(status);
CREATE TABLE IF NOT EXISTS data_scope_permissions (
  id varchar(120) PRIMARY KEY,
  name varchar(300) NOT NULL,
  status varchar(40) NOT NULL DEFAULT 'ACTIVE',
  use_yn char(1) NOT NULL DEFAULT 'Y',
  payload jsonb NOT NULL DEFAULT '{}'::jsonb,
  created_at timestamp with time zone NOT NULL DEFAULT now(),
  updated_at timestamp with time zone NOT NULL DEFAULT now()
);
COMMENT ON TABLE data_scope_permissions IS '역할별 데이터 범위와 서버 조회 강제 조건을 관리한다.';
COMMENT ON COLUMN data_scope_permissions.status IS 'ACTIVE:활성|INACTIVE:비활성|DELETED:논리삭제|PENDING:대기|COMPLETED:완료|FAILED:실패';
COMMENT ON COLUMN data_scope_permissions.payload IS '애플리케이션 서비스 저장 시 도메인 확장 속성을 보존한다.';
CREATE INDEX IF NOT EXISTS idx_data_scope_permissions_status ON data_scope_permissions(status);
CREATE TABLE IF NOT EXISTS code_groups (
  id varchar(120) PRIMARY KEY,
  name varchar(300) NOT NULL,
  status varchar(40) NOT NULL DEFAULT 'ACTIVE',
  use_yn char(1) NOT NULL DEFAULT 'Y',
  payload jsonb NOT NULL DEFAULT '{}'::jsonb,
  created_at timestamp with time zone NOT NULL DEFAULT now(),
  updated_at timestamp with time zone NOT NULL DEFAULT now()
);
COMMENT ON TABLE code_groups IS '공통코드 그룹 정보를 관리한다.';
COMMENT ON COLUMN code_groups.status IS 'ACTIVE:활성|INACTIVE:비활성|DELETED:논리삭제|PENDING:대기|COMPLETED:완료|FAILED:실패';
COMMENT ON COLUMN code_groups.payload IS '애플리케이션 서비스 저장 시 도메인 확장 속성을 보존한다.';
CREATE INDEX IF NOT EXISTS idx_code_groups_status ON code_groups(status);
CREATE TABLE IF NOT EXISTS detail_codes (
  id varchar(120) PRIMARY KEY,
  name varchar(300) NOT NULL,
  status varchar(40) NOT NULL DEFAULT 'ACTIVE',
  use_yn char(1) NOT NULL DEFAULT 'Y',
  payload jsonb NOT NULL DEFAULT '{}'::jsonb,
  created_at timestamp with time zone NOT NULL DEFAULT now(),
  updated_at timestamp with time zone NOT NULL DEFAULT now()
);
COMMENT ON TABLE detail_codes IS '코드그룹별 상세 코드와 계층 속성을 관리한다.';
COMMENT ON COLUMN detail_codes.status IS 'ACTIVE:활성|INACTIVE:비활성|DELETED:논리삭제|PENDING:대기|COMPLETED:완료|FAILED:실패';
COMMENT ON COLUMN detail_codes.payload IS '애플리케이션 서비스 저장 시 도메인 확장 속성을 보존한다.';
CREATE INDEX IF NOT EXISTS idx_detail_codes_status ON detail_codes(status);
CREATE TABLE IF NOT EXISTS system_configurations (
  id varchar(120) PRIMARY KEY,
  name varchar(300) NOT NULL,
  status varchar(40) NOT NULL DEFAULT 'ACTIVE',
  use_yn char(1) NOT NULL DEFAULT 'Y',
  payload jsonb NOT NULL DEFAULT '{}'::jsonb,
  created_at timestamp with time zone NOT NULL DEFAULT now(),
  updated_at timestamp with time zone NOT NULL DEFAULT now()
);
COMMENT ON TABLE system_configurations IS '공통 환경설정 값을 관리한다.';
COMMENT ON COLUMN system_configurations.status IS 'ACTIVE:활성|INACTIVE:비활성|DELETED:논리삭제|PENDING:대기|COMPLETED:완료|FAILED:실패';
COMMENT ON COLUMN system_configurations.payload IS '애플리케이션 서비스 저장 시 도메인 확장 속성을 보존한다.';
CREATE INDEX IF NOT EXISTS idx_system_configurations_status ON system_configurations(status);
CREATE TABLE IF NOT EXISTS base_years (
  id varchar(120) PRIMARY KEY,
  name varchar(300) NOT NULL,
  status varchar(40) NOT NULL DEFAULT 'ACTIVE',
  use_yn char(1) NOT NULL DEFAULT 'Y',
  payload jsonb NOT NULL DEFAULT '{}'::jsonb,
  created_at timestamp with time zone NOT NULL DEFAULT now(),
  updated_at timestamp with time zone NOT NULL DEFAULT now()
);
COMMENT ON TABLE base_years IS '평가 기준연도와 기본 조회연도 설정을 관리한다.';
COMMENT ON COLUMN base_years.status IS 'ACTIVE:활성|INACTIVE:비활성|DELETED:논리삭제|PENDING:대기|COMPLETED:완료|FAILED:실패';
COMMENT ON COLUMN base_years.payload IS '애플리케이션 서비스 저장 시 도메인 확장 속성을 보존한다.';
CREATE INDEX IF NOT EXISTS idx_base_years_status ON base_years(status);
CREATE TABLE IF NOT EXISTS file_policies (
  id varchar(120) PRIMARY KEY,
  name varchar(300) NOT NULL,
  status varchar(40) NOT NULL DEFAULT 'ACTIVE',
  use_yn char(1) NOT NULL DEFAULT 'Y',
  payload jsonb NOT NULL DEFAULT '{}'::jsonb,
  created_at timestamp with time zone NOT NULL DEFAULT now(),
  updated_at timestamp with time zone NOT NULL DEFAULT now()
);
COMMENT ON TABLE file_policies IS '업무별 파일 업로드 정책을 관리한다.';
COMMENT ON COLUMN file_policies.status IS 'ACTIVE:활성|INACTIVE:비활성|DELETED:논리삭제|PENDING:대기|COMPLETED:완료|FAILED:실패';
COMMENT ON COLUMN file_policies.payload IS '애플리케이션 서비스 저장 시 도메인 확장 속성을 보존한다.';
CREATE INDEX IF NOT EXISTS idx_file_policies_status ON file_policies(status);
CREATE TABLE IF NOT EXISTS notices (
  id varchar(120) PRIMARY KEY,
  name varchar(300) NOT NULL,
  status varchar(40) NOT NULL DEFAULT 'ACTIVE',
  use_yn char(1) NOT NULL DEFAULT 'Y',
  payload jsonb NOT NULL DEFAULT '{}'::jsonb,
  created_at timestamp with time zone NOT NULL DEFAULT now(),
  updated_at timestamp with time zone NOT NULL DEFAULT now()
);
COMMENT ON TABLE notices IS '대상 역할·조직과 게시기간을 가진 공지사항을 관리한다.';
COMMENT ON COLUMN notices.status IS 'ACTIVE:활성|INACTIVE:비활성|DELETED:논리삭제|PENDING:대기|COMPLETED:완료|FAILED:실패';
COMMENT ON COLUMN notices.payload IS '애플리케이션 서비스 저장 시 도메인 확장 속성을 보존한다.';
CREATE INDEX IF NOT EXISTS idx_notices_status ON notices(status);
CREATE TABLE IF NOT EXISTS attachment_files (
  id varchar(120) PRIMARY KEY,
  name varchar(300) NOT NULL,
  status varchar(40) NOT NULL DEFAULT 'ACTIVE',
  use_yn char(1) NOT NULL DEFAULT 'Y',
  payload jsonb NOT NULL DEFAULT '{}'::jsonb,
  created_at timestamp with time zone NOT NULL DEFAULT now(),
  updated_at timestamp with time zone NOT NULL DEFAULT now()
);
COMMENT ON TABLE attachment_files IS '첨부파일 메타데이터와 논리삭제 상태를 관리한다.';
COMMENT ON COLUMN attachment_files.status IS 'ACTIVE:활성|INACTIVE:비활성|DELETED:논리삭제|PENDING:대기|COMPLETED:완료|FAILED:실패';
COMMENT ON COLUMN attachment_files.payload IS '애플리케이션 서비스 저장 시 도메인 확장 속성을 보존한다.';
CREATE INDEX IF NOT EXISTS idx_attachment_files_status ON attachment_files(status);
CREATE TABLE IF NOT EXISTS excel_templates (
  id varchar(120) PRIMARY KEY,
  name varchar(300) NOT NULL,
  status varchar(40) NOT NULL DEFAULT 'ACTIVE',
  use_yn char(1) NOT NULL DEFAULT 'Y',
  payload jsonb NOT NULL DEFAULT '{}'::jsonb,
  created_at timestamp with time zone NOT NULL DEFAULT now(),
  updated_at timestamp with time zone NOT NULL DEFAULT now()
);
COMMENT ON TABLE excel_templates IS '업무별 엑셀 업로드 양식 버전을 관리한다.';
COMMENT ON COLUMN excel_templates.status IS 'ACTIVE:활성|INACTIVE:비활성|DELETED:논리삭제|PENDING:대기|COMPLETED:완료|FAILED:실패';
COMMENT ON COLUMN excel_templates.payload IS '애플리케이션 서비스 저장 시 도메인 확장 속성을 보존한다.';
CREATE INDEX IF NOT EXISTS idx_excel_templates_status ON excel_templates(status);
CREATE TABLE IF NOT EXISTS excel_upload_histories (
  id varchar(120) PRIMARY KEY,
  name varchar(300) NOT NULL,
  status varchar(40) NOT NULL DEFAULT 'ACTIVE',
  use_yn char(1) NOT NULL DEFAULT 'Y',
  payload jsonb NOT NULL DEFAULT '{}'::jsonb,
  created_at timestamp with time zone NOT NULL DEFAULT now(),
  updated_at timestamp with time zone NOT NULL DEFAULT now()
);
COMMENT ON TABLE excel_upload_histories IS '엑셀 업로드 검증·등록 이력을 관리한다.';
COMMENT ON COLUMN excel_upload_histories.status IS 'ACTIVE:활성|INACTIVE:비활성|DELETED:논리삭제|PENDING:대기|COMPLETED:완료|FAILED:실패';
COMMENT ON COLUMN excel_upload_histories.payload IS '애플리케이션 서비스 저장 시 도메인 확장 속성을 보존한다.';
CREATE INDEX IF NOT EXISTS idx_excel_upload_histories_status ON excel_upload_histories(status);
CREATE TABLE IF NOT EXISTS excel_upload_errors (
  id varchar(120) PRIMARY KEY,
  name varchar(300) NOT NULL,
  status varchar(40) NOT NULL DEFAULT 'ACTIVE',
  use_yn char(1) NOT NULL DEFAULT 'Y',
  payload jsonb NOT NULL DEFAULT '{}'::jsonb,
  created_at timestamp with time zone NOT NULL DEFAULT now(),
  updated_at timestamp with time zone NOT NULL DEFAULT now()
);
COMMENT ON TABLE excel_upload_errors IS '엑셀 업로드 오류 행 상세를 관리한다.';
COMMENT ON COLUMN excel_upload_errors.status IS 'ACTIVE:활성|INACTIVE:비활성|DELETED:논리삭제|PENDING:대기|COMPLETED:완료|FAILED:실패';
COMMENT ON COLUMN excel_upload_errors.payload IS '애플리케이션 서비스 저장 시 도메인 확장 속성을 보존한다.';
CREATE INDEX IF NOT EXISTS idx_excel_upload_errors_status ON excel_upload_errors(status);
CREATE TABLE IF NOT EXISTS privacy_policies (
  id varchar(120) PRIMARY KEY,
  name varchar(300) NOT NULL,
  status varchar(40) NOT NULL DEFAULT 'ACTIVE',
  use_yn char(1) NOT NULL DEFAULT 'Y',
  payload jsonb NOT NULL DEFAULT '{}'::jsonb,
  created_at timestamp with time zone NOT NULL DEFAULT now(),
  updated_at timestamp with time zone NOT NULL DEFAULT now()
);
COMMENT ON TABLE privacy_policies IS '개인정보 항목별 암호화·마스킹 정책을 관리한다.';
COMMENT ON COLUMN privacy_policies.status IS 'ACTIVE:활성|INACTIVE:비활성|DELETED:논리삭제|PENDING:대기|COMPLETED:완료|FAILED:실패';
COMMENT ON COLUMN privacy_policies.payload IS '애플리케이션 서비스 저장 시 도메인 확장 속성을 보존한다.';
CREATE INDEX IF NOT EXISTS idx_privacy_policies_status ON privacy_policies(status);
CREATE TABLE IF NOT EXISTS privacy_permissions (
  id varchar(120) PRIMARY KEY,
  name varchar(300) NOT NULL,
  status varchar(40) NOT NULL DEFAULT 'ACTIVE',
  use_yn char(1) NOT NULL DEFAULT 'Y',
  payload jsonb NOT NULL DEFAULT '{}'::jsonb,
  created_at timestamp with time zone NOT NULL DEFAULT now(),
  updated_at timestamp with time zone NOT NULL DEFAULT now()
);
COMMENT ON TABLE privacy_permissions IS '역할별 개인정보 원문·마스킹·출력 권한을 관리한다.';
COMMENT ON COLUMN privacy_permissions.status IS 'ACTIVE:활성|INACTIVE:비활성|DELETED:논리삭제|PENDING:대기|COMPLETED:완료|FAILED:실패';
COMMENT ON COLUMN privacy_permissions.payload IS '애플리케이션 서비스 저장 시 도메인 확장 속성을 보존한다.';
CREATE INDEX IF NOT EXISTS idx_privacy_permissions_status ON privacy_permissions(status);
CREATE TABLE IF NOT EXISTS privacy_access_histories (
  id varchar(120) PRIMARY KEY,
  name varchar(300) NOT NULL,
  status varchar(40) NOT NULL DEFAULT 'ACTIVE',
  use_yn char(1) NOT NULL DEFAULT 'Y',
  payload jsonb NOT NULL DEFAULT '{}'::jsonb,
  created_at timestamp with time zone NOT NULL DEFAULT now(),
  updated_at timestamp with time zone NOT NULL DEFAULT now()
);
COMMENT ON TABLE privacy_access_histories IS '개인정보 조회·출력·다운로드 처리 이력을 변경 불가로 저장한다.';
COMMENT ON COLUMN privacy_access_histories.status IS 'ACTIVE:활성|INACTIVE:비활성|DELETED:논리삭제|PENDING:대기|COMPLETED:완료|FAILED:실패';
COMMENT ON COLUMN privacy_access_histories.payload IS '애플리케이션 서비스 저장 시 도메인 확장 속성을 보존한다.';
CREATE INDEX IF NOT EXISTS idx_privacy_access_histories_status ON privacy_access_histories(status);
CREATE TABLE IF NOT EXISTS session_end_histories (
  id varchar(120) PRIMARY KEY,
  name varchar(300) NOT NULL,
  status varchar(40) NOT NULL DEFAULT 'ACTIVE',
  use_yn char(1) NOT NULL DEFAULT 'Y',
  payload jsonb NOT NULL DEFAULT '{}'::jsonb,
  created_at timestamp with time zone NOT NULL DEFAULT now(),
  updated_at timestamp with time zone NOT NULL DEFAULT now()
);
COMMENT ON TABLE session_end_histories IS '접속 종료 이력을 변경 불가로 저장한다.';
COMMENT ON COLUMN session_end_histories.status IS 'ACTIVE:활성|INACTIVE:비활성|DELETED:논리삭제|PENDING:대기|COMPLETED:완료|FAILED:실패';
COMMENT ON COLUMN session_end_histories.payload IS '애플리케이션 서비스 저장 시 도메인 확장 속성을 보존한다.';
CREATE INDEX IF NOT EXISTS idx_session_end_histories_status ON session_end_histories(status);
CREATE TABLE IF NOT EXISTS batch_definitions (
  id varchar(120) PRIMARY KEY,
  name varchar(300) NOT NULL,
  status varchar(40) NOT NULL DEFAULT 'ACTIVE',
  use_yn char(1) NOT NULL DEFAULT 'Y',
  payload jsonb NOT NULL DEFAULT '{}'::jsonb,
  created_at timestamp with time zone NOT NULL DEFAULT now(),
  updated_at timestamp with time zone NOT NULL DEFAULT now()
);
COMMENT ON TABLE batch_definitions IS '배치 정의와 실행 조건을 관리한다.';
COMMENT ON COLUMN batch_definitions.status IS 'ACTIVE:활성|INACTIVE:비활성|DELETED:논리삭제|PENDING:대기|COMPLETED:완료|FAILED:실패';
COMMENT ON COLUMN batch_definitions.payload IS '애플리케이션 서비스 저장 시 도메인 확장 속성을 보존한다.';
CREATE INDEX IF NOT EXISTS idx_batch_definitions_status ON batch_definitions(status);
CREATE TABLE IF NOT EXISTS batch_execution_histories (
  id varchar(120) PRIMARY KEY,
  name varchar(300) NOT NULL,
  status varchar(40) NOT NULL DEFAULT 'ACTIVE',
  use_yn char(1) NOT NULL DEFAULT 'Y',
  payload jsonb NOT NULL DEFAULT '{}'::jsonb,
  created_at timestamp with time zone NOT NULL DEFAULT now(),
  updated_at timestamp with time zone NOT NULL DEFAULT now()
);
COMMENT ON TABLE batch_execution_histories IS '배치 수동실행·중지·재실행 요청 이력을 관리한다.';
COMMENT ON COLUMN batch_execution_histories.status IS 'ACTIVE:활성|INACTIVE:비활성|DELETED:논리삭제|PENDING:대기|COMPLETED:완료|FAILED:실패';
COMMENT ON COLUMN batch_execution_histories.payload IS '애플리케이션 서비스 저장 시 도메인 확장 속성을 보존한다.';
CREATE INDEX IF NOT EXISTS idx_batch_execution_histories_status ON batch_execution_histories(status);
CREATE TABLE IF NOT EXISTS batch_results (
  id varchar(120) PRIMARY KEY,
  name varchar(300) NOT NULL,
  status varchar(40) NOT NULL DEFAULT 'ACTIVE',
  use_yn char(1) NOT NULL DEFAULT 'Y',
  payload jsonb NOT NULL DEFAULT '{}'::jsonb,
  created_at timestamp with time zone NOT NULL DEFAULT now(),
  updated_at timestamp with time zone NOT NULL DEFAULT now()
);
COMMENT ON TABLE batch_results IS '배치 실행 결과와 로그 참조를 관리한다.';
COMMENT ON COLUMN batch_results.status IS 'ACTIVE:활성|INACTIVE:비활성|DELETED:논리삭제|PENDING:대기|COMPLETED:완료|FAILED:실패';
COMMENT ON COLUMN batch_results.payload IS '애플리케이션 서비스 저장 시 도메인 확장 속성을 보존한다.';
CREATE INDEX IF NOT EXISTS idx_batch_results_status ON batch_results(status);
CREATE TABLE IF NOT EXISTS app_sessions (
  session_id varchar(120) PRIMARY KEY,
  login_id varchar(120) NOT NULL,
  user_name varchar(120) NOT NULL,
  role_code varchar(20) NOT NULL,
  ip_address varchar(80),
  session_status varchar(40) NOT NULL DEFAULT 'ACTIVE',
  end_reason varchar(500),
  created_at timestamp with time zone NOT NULL DEFAULT now(),
  updated_at timestamp with time zone NOT NULL DEFAULT now(),
  ended_at timestamp with time zone
);
COMMENT ON TABLE app_sessions IS 'HttpOnly 세션 쿠키 기반 로그인 세션을 관리한다. 로컬 관리자 검증과 접속현황 화면의 현재 세션 조회에 사용한다.';
COMMENT ON COLUMN app_sessions.session_status IS 'ACTIVE:활성|LOGGED_OUT:로그아웃|EXPIRED:만료|FORCED_END:강제종료';
CREATE INDEX IF NOT EXISTS idx_app_sessions_status ON app_sessions(session_status, created_at);
CREATE TABLE IF NOT EXISTS audit_logs (
  audit_id bigserial PRIMARY KEY,
  audit_type varchar(40) NOT NULL,
  target_area varchar(80) NOT NULL,
  target_id varchar(120) NOT NULL,
  actor_login_id varchar(120) NOT NULL,
  action varchar(80) NOT NULL,
  reason varchar(500),
  before_payload jsonb NOT NULL DEFAULT '{}'::jsonb,
  after_payload jsonb NOT NULL DEFAULT '{}'::jsonb,
  status varchar(40) NOT NULL DEFAULT 'RECORDED',
  created_at timestamp with time zone NOT NULL DEFAULT now(),
  updated_at timestamp with time zone NOT NULL DEFAULT now()
);
COMMENT ON TABLE audit_logs IS '업무처리·중요정보·권한변경 이력을 변경 불가 감사로그로 기록한다. 관리 화면 저장 side effect와 감사 로그 화면 조회 근거로 사용한다.';
COMMENT ON COLUMN audit_logs.status IS 'RECORDED:기록됨|REVIEWED:검토됨';
COMMENT ON COLUMN audit_logs.before_payload IS 'ManagedRecordService.save 성공 직전 기존 값을 애플리케이션에서 저장한다.';
COMMENT ON COLUMN audit_logs.after_payload IS 'ManagedRecordService.save 성공 직후 변경 값을 애플리케이션에서 저장한다.';
CREATE INDEX IF NOT EXISTS idx_audit_logs_target ON audit_logs(target_area, target_id, created_at);
INSERT INTO k_o_r_u_s_mock_snapshot(employee_no, name, organization_code, position_grade, employment_status, last_sync_at) VALUES ('USR-ADMIN', '시드 시스템관리자', 'KNUE', '시스템관리자', 'ACTIVE', now()) ON CONFLICT(employee_no) DO NOTHING;
INSERT INTO roles(id, name, payload) VALUES ('R01', '교원', '{"roleCode": "R01", "defaultDataScope": "ALL"}'::jsonb) ON CONFLICT(id) DO NOTHING;
INSERT INTO roles(id, name, payload) VALUES ('R02', '학과장', '{"roleCode": "R02", "defaultDataScope": "ALL"}'::jsonb) ON CONFLICT(id) DO NOTHING;
INSERT INTO roles(id, name, payload) VALUES ('R03', '단과대학(원) 행정실', '{"roleCode": "R03", "defaultDataScope": "ALL"}'::jsonb) ON CONFLICT(id) DO NOTHING;
INSERT INTO roles(id, name, payload) VALUES ('R04', '교수지원과', '{"roleCode": "R04", "defaultDataScope": "ALL"}'::jsonb) ON CONFLICT(id) DO NOTHING;
INSERT INTO roles(id, name, payload) VALUES ('R05', '산학협력단', '{"roleCode": "R05", "defaultDataScope": "ALL"}'::jsonb) ON CONFLICT(id) DO NOTHING;
INSERT INTO roles(id, name, payload) VALUES ('R06', '입학인재관리과', '{"roleCode": "R06", "defaultDataScope": "ALL"}'::jsonb) ON CONFLICT(id) DO NOTHING;
INSERT INTO roles(id, name, payload) VALUES ('R07', '실적부서', '{"roleCode": "R07", "defaultDataScope": "ALL"}'::jsonb) ON CONFLICT(id) DO NOTHING;
INSERT INTO roles(id, name, payload) VALUES ('R08', '점수산출 감사자', '{"roleCode": "R08", "defaultDataScope": "ALL"}'::jsonb) ON CONFLICT(id) DO NOTHING;
INSERT INTO roles(id, name, payload) VALUES ('R09', '시스템관리자', '{"roleCode": "R09", "defaultDataScope": "ALL"}'::jsonb) ON CONFLICT(id) DO NOTHING;
INSERT INTO user_accounts(account_id, employee_no, name, login_id, system_use_yn, primary_role_code, payload) VALUES ('USR-ADMIN', 'USR-ADMIN', '시드 시스템관리자', 'admin', 'Y', 'R09', '{"roleCodes": ["R09"], "systemUseYn": "Y", "primaryRoleCode": "R09"}'::jsonb) ON CONFLICT(account_id) DO NOTHING;
INSERT INTO organizations(id, name, payload) VALUES ('ORG-KNUE', '한국교원대학교', '{"organizationCode": "KNUE", "organizationType": "UNIVERSITY"}'::jsonb) ON CONFLICT(id) DO NOTHING;
INSERT INTO position_assignments(id, name, payload) VALUES ('POS-DEAN-001', '예시 보직', '{"positionCode": "DEAN", "employeeNo": "USR-ADMIN", "organizationCode": "KNUE"}'::jsonb) ON CONFLICT(id) DO NOTHING;
INSERT INTO code_groups(id, name, payload) VALUES ('CG-COMMON', '공통 코드그룹', '{"description": "예시 코드그룹", "managedDepartment": "시스템관리"}'::jsonb) ON CONFLICT(id) DO NOTHING;
INSERT INTO detail_codes(id, name, payload) VALUES ('DC-ACTIVE', '활성', '{"codeGroupId": "CG-COMMON", "codeValue": "ACTIVE", "sortOrder": 1}'::jsonb) ON CONFLICT(id) DO NOTHING;
INSERT INTO system_configurations(id, name, payload) VALUES ('CFG-PAGE-SIZE', '페이지당 조회건수', '{"configValue": "20", "unit": "건"}'::jsonb) ON CONFLICT(id) DO NOTHING;
INSERT INTO base_years(id, name, payload) VALUES ('2026', '2026 기준연도', '{"currentEvaluationYearYn": "Y", "defaultSearchYearYn": "Y"}'::jsonb) ON CONFLICT(id) DO NOTHING;
INSERT INTO file_policies(id, name, payload) VALUES ('FP-COMMON', '공통 파일정책', '{"businessArea": "COMMON", "allowedExtensions": "xlsx,pdf", "maxSingleFileSize": 10485760}'::jsonb) ON CONFLICT(id) DO NOTHING;
INSERT INTO batch_definitions(id, name, payload) VALUES ('BATCH-MOCK-001', 'Mock Job', '{"schedule": "MANUAL", "ownerUserId": "USR-ADMIN"}'::jsonb) ON CONFLICT(id) DO NOTHING;
INSERT INTO common_management_records(area, record_id, title, payload) VALUES ('users', 'USERS-001', '사용자 관리', '{"description": "사용자 관리 기본 시드", "roleCode": "R09"}'::jsonb) ON CONFLICT(area, record_id) DO NOTHING;
INSERT INTO common_management_records(area, record_id, title, payload) VALUES ('organizations', 'ORGANIZATIONS-001', '조직 관리', '{"description": "조직 관리 기본 시드", "roleCode": "R09"}'::jsonb) ON CONFLICT(area, record_id) DO NOTHING;
INSERT INTO common_management_records(area, record_id, title, payload) VALUES ('positions', 'POSITIONS-001', '보직 관리', '{"description": "보직 관리 기본 시드", "roleCode": "R09"}'::jsonb) ON CONFLICT(area, record_id) DO NOTHING;
INSERT INTO common_management_records(area, record_id, title, payload) VALUES ('roles', 'ROLES-001', '역할 관리', '{"description": "역할 관리 기본 시드", "roleCode": "R09"}'::jsonb) ON CONFLICT(area, record_id) DO NOTHING;
INSERT INTO common_management_records(area, record_id, title, payload) VALUES ('user-roles', 'USER-ROLES-001', '사용자 역할 관리', '{"description": "사용자 역할 관리 기본 시드", "roleCode": "R09"}'::jsonb) ON CONFLICT(area, record_id) DO NOTHING;
INSERT INTO common_management_records(area, record_id, title, payload) VALUES ('menu-permissions', 'MENU-PERMISSIONS-001', '메뉴 권한 관리', '{"description": "메뉴 권한 관리 기본 시드", "roleCode": "R09"}'::jsonb) ON CONFLICT(area, record_id) DO NOTHING;
INSERT INTO common_management_records(area, record_id, title, payload) VALUES ('function-permissions', 'FUNCTION-PERMISSIONS-001', '기능 권한 관리', '{"description": "기능 권한 관리 기본 시드", "roleCode": "R09"}'::jsonb) ON CONFLICT(area, record_id) DO NOTHING;
INSERT INTO common_management_records(area, record_id, title, payload) VALUES ('data-scope-permissions', 'DATA-SCOPE-PERMISSIONS-001', '데이터 범위 권한', '{"description": "데이터 범위 권한 기본 시드", "roleCode": "R09"}'::jsonb) ON CONFLICT(area, record_id) DO NOTHING;
INSERT INTO common_management_records(area, record_id, title, payload) VALUES ('menus', 'MENUS-001', '메뉴 관리', '{"description": "메뉴 관리 기본 시드", "roleCode": "R09"}'::jsonb) ON CONFLICT(area, record_id) DO NOTHING;
INSERT INTO common_management_records(area, record_id, title, payload) VALUES ('code-groups', 'CODE-GROUPS-001', '코드그룹 관리', '{"description": "코드그룹 관리 기본 시드", "roleCode": "R09"}'::jsonb) ON CONFLICT(area, record_id) DO NOTHING;
INSERT INTO common_management_records(area, record_id, title, payload) VALUES ('detail-codes', 'DETAIL-CODES-001', '상세코드 관리', '{"description": "상세코드 관리 기본 시드", "roleCode": "R09"}'::jsonb) ON CONFLICT(area, record_id) DO NOTHING;
INSERT INTO common_management_records(area, record_id, title, payload) VALUES ('configurations', 'CONFIGURATIONS-001', '공통 환경설정', '{"description": "공통 환경설정 기본 시드", "roleCode": "R09"}'::jsonb) ON CONFLICT(area, record_id) DO NOTHING;
INSERT INTO common_management_records(area, record_id, title, payload) VALUES ('base-years', 'BASE-YEARS-001', '기준연도 관리', '{"description": "기준연도 관리 기본 시드", "roleCode": "R09"}'::jsonb) ON CONFLICT(area, record_id) DO NOTHING;
INSERT INTO common_management_records(area, record_id, title, payload) VALUES ('file-policies', 'FILE-POLICIES-001', '파일정책 관리', '{"description": "파일정책 관리 기본 시드", "roleCode": "R09"}'::jsonb) ON CONFLICT(area, record_id) DO NOTHING;
INSERT INTO common_management_records(area, record_id, title, payload) VALUES ('notices', 'NOTICES-001', '공지사항 관리', '{"description": "공지사항 관리 기본 시드", "roleCode": "R09"}'::jsonb) ON CONFLICT(area, record_id) DO NOTHING;
INSERT INTO common_management_records(area, record_id, title, payload) VALUES ('attachments', 'ATTACHMENTS-001', '첨부파일 관리', '{"description": "첨부파일 관리 기본 시드", "roleCode": "R09"}'::jsonb) ON CONFLICT(area, record_id) DO NOTHING;
INSERT INTO common_management_records(area, record_id, title, payload) VALUES ('excel/templates', 'EXCEL-TEMPLATES-001', '업로드 양식 관리', '{"description": "업로드 양식 관리 기본 시드", "roleCode": "R09"}'::jsonb) ON CONFLICT(area, record_id) DO NOTHING;
INSERT INTO common_management_records(area, record_id, title, payload) VALUES ('excel/uploads', 'EXCEL-UPLOADS-001', '엑셀 업로드', '{"description": "엑셀 업로드 기본 시드", "roleCode": "R09"}'::jsonb) ON CONFLICT(area, record_id) DO NOTHING;
INSERT INTO common_management_records(area, record_id, title, payload) VALUES ('excel/downloads', 'EXCEL-DOWNLOADS-001', '엑셀 다운로드', '{"description": "엑셀 다운로드 기본 시드", "roleCode": "R09"}'::jsonb) ON CONFLICT(area, record_id) DO NOTHING;
INSERT INTO common_management_records(area, record_id, title, payload) VALUES ('privacy/policies', 'PRIVACY-POLICIES-001', '개인정보 관리', '{"description": "개인정보 관리 기본 시드", "roleCode": "R09"}'::jsonb) ON CONFLICT(area, record_id) DO NOTHING;
INSERT INTO common_management_records(area, record_id, title, payload) VALUES ('sessions', 'SESSIONS-001', '접속현황 관리', '{"description": "접속현황 관리 기본 시드", "roleCode": "R09"}'::jsonb) ON CONFLICT(area, record_id) DO NOTHING;
INSERT INTO common_management_records(area, record_id, title, payload) VALUES ('audit-logs', 'AUDIT-LOGS-001', '감사 로그 관리', '{"description": "감사 로그 관리 기본 시드", "roleCode": "R09"}'::jsonb) ON CONFLICT(area, record_id) DO NOTHING;
INSERT INTO common_management_records(area, record_id, title, payload) VALUES ('batch-definitions', 'BATCH-DEFINITIONS-001', '배치 정의 관리', '{"description": "배치 정의 관리 기본 시드", "roleCode": "R09"}'::jsonb) ON CONFLICT(area, record_id) DO NOTHING;
INSERT INTO common_management_records(area, record_id, title, payload) VALUES ('batch-executions', 'BATCH-EXECUTIONS-001', '배치 실행 관리', '{"description": "배치 실행 관리 기본 시드", "roleCode": "R09"}'::jsonb) ON CONFLICT(area, record_id) DO NOTHING;
INSERT INTO common_management_records(area, record_id, title, payload) VALUES ('batch-results', 'BATCH-RESULTS-001', '배치 결과 조회', '{"description": "배치 결과 조회 기본 시드", "roleCode": "R09"}'::jsonb) ON CONFLICT(area, record_id) DO NOTHING;
INSERT INTO common_management_records(area, record_id, title, payload) VALUES ('roles', 'R01', '교원', '{"roleCode": "R01", "defaultDataScope": "ALL"}'::jsonb) ON CONFLICT(area, record_id) DO NOTHING;
INSERT INTO common_management_records(area, record_id, title, payload) VALUES ('roles', 'R02', '학과장', '{"roleCode": "R02", "defaultDataScope": "ALL"}'::jsonb) ON CONFLICT(area, record_id) DO NOTHING;
INSERT INTO common_management_records(area, record_id, title, payload) VALUES ('roles', 'R03', '단과대학(원) 행정실', '{"roleCode": "R03", "defaultDataScope": "ALL"}'::jsonb) ON CONFLICT(area, record_id) DO NOTHING;
INSERT INTO common_management_records(area, record_id, title, payload) VALUES ('roles', 'R04', '교수지원과', '{"roleCode": "R04", "defaultDataScope": "ALL"}'::jsonb) ON CONFLICT(area, record_id) DO NOTHING;
INSERT INTO common_management_records(area, record_id, title, payload) VALUES ('roles', 'R05', '산학협력단', '{"roleCode": "R05", "defaultDataScope": "ALL"}'::jsonb) ON CONFLICT(area, record_id) DO NOTHING;
INSERT INTO common_management_records(area, record_id, title, payload) VALUES ('roles', 'R06', '입학인재관리과', '{"roleCode": "R06", "defaultDataScope": "ALL"}'::jsonb) ON CONFLICT(area, record_id) DO NOTHING;
INSERT INTO common_management_records(area, record_id, title, payload) VALUES ('roles', 'R07', '실적부서', '{"roleCode": "R07", "defaultDataScope": "ALL"}'::jsonb) ON CONFLICT(area, record_id) DO NOTHING;
INSERT INTO common_management_records(area, record_id, title, payload) VALUES ('roles', 'R08', '점수산출 감사자', '{"roleCode": "R08", "defaultDataScope": "ALL"}'::jsonb) ON CONFLICT(area, record_id) DO NOTHING;
INSERT INTO common_management_records(area, record_id, title, payload) VALUES ('roles', 'R09', '시스템관리자', '{"roleCode": "R09", "defaultDataScope": "ALL"}'::jsonb) ON CONFLICT(area, record_id) DO NOTHING;
INSERT INTO common_management_records(area, record_id, title, payload) VALUES ('users', 'USR-ADMIN', '시드 시스템관리자', '{"loginId": "admin", "roleCodes": ["R09"], "systemUseYn": "Y"}'::jsonb) ON CONFLICT(area, record_id) DO NOTHING;
INSERT INTO common_management_records(area, record_id, title, payload) VALUES ('organizations', 'ORG-KNUE', '한국교원대학교', '{"organizationCode": "KNUE", "organizationType": "UNIVERSITY"}'::jsonb) ON CONFLICT(area, record_id) DO NOTHING;
INSERT INTO common_management_records(area, record_id, title, payload) VALUES ('positions', 'POS-DEAN-001', '예시 보직', '{"positionCode": "DEAN", "userId": "USR-ADMIN", "organizationCode": "KNUE"}'::jsonb) ON CONFLICT(area, record_id) DO NOTHING;
