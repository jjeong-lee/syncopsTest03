package kr.ac.knue.fpe.common.domain;

import java.util.List;

public final class FeatureCatalog {
    private FeatureCatalog() {}
    public record Screen(String route, String apiPath, String screenId, String menuPath, String primaryEntity, String operationId) {}
    public static final List<Screen> SCREENS = List.of(
        new Screen("/admin/cmn/fr/001", "/api/admin/cmn/fr/001", "SCR-CMN-FR-001", "시스템 관리 > 사용자·조직 관리 > 사용자 관리", "user_account", "cmn_fr_001_search"),
        new Screen("/admin/cmn/fr/002", "/api/admin/cmn/fr/002", "SCR-CMN-FR-002", "시스템 관리 > 사용자·조직 관리 > 조직 관리", "organization", "cmn_fr_002_search"),
        new Screen("/admin/cmn/fr/003", "/api/admin/cmn/fr/003", "SCR-CMN-FR-003", "시스템 관리 > 사용자·조직 관리 > 보직 관리", "appointment", "cmn_fr_003_search"),
        new Screen("/admin/cmn/fr/005", "/api/admin/cmn/fr/005", "SCR-CMN-FR-005", "시스템 관리 > 역할·권한 관리 > 역할 관리", "role", "cmn_fr_005_search"),
        new Screen("/admin/cmn/fr/006", "/api/admin/cmn/fr/006", "SCR-CMN-FR-006", "시스템 관리 > 역할·권한 관리 > 사용자 역할 관리", "user_role", "cmn_fr_006_search"),
        new Screen("/admin/cmn/fr/007", "/api/admin/cmn/fr/007", "SCR-CMN-FR-007", "시스템 관리 > 역할·권한 관리 > 메뉴 권한 관리", "menu_permission", "cmn_fr_007_search"),
        new Screen("/admin/cmn/fr/008", "/api/admin/cmn/fr/008", "SCR-CMN-FR-008", "시스템 관리 > 역할·권한 관리 > 기능 권한 관리", "function_permission", "cmn_fr_008_search"),
        new Screen("/admin/cmn/fr/009", "/api/admin/cmn/fr/009", "SCR-CMN-FR-009", "시스템 관리 > 역할·권한 관리 > 데이터 범위 권한", "data_scope_permission", "cmn_fr_009_search"),
        new Screen("/admin/cmn/fr/013/014", "/api/admin/cmn/fr/013/014", "SCR-CMN-FR-013-014", "시스템 관리 > 메뉴 관리 > 메뉴 관리", "menu", "cmn_fr_013_014_search"),
        new Screen("/admin/cmn/fr/016", "/api/admin/cmn/fr/016", "SCR-CMN-FR-016", "시스템 관리 > 공통코드 관리 > 코드그룹 관리", "code_group", "cmn_fr_016_search"),
        new Screen("/admin/cmn/fr/017", "/api/admin/cmn/fr/017", "SCR-CMN-FR-017", "시스템 관리 > 공통코드 관리 > 상세코드 관리", "detail_code", "cmn_fr_017_search"),
        new Screen("/admin/cmn/fr/019", "/api/admin/cmn/fr/019", "SCR-CMN-FR-019", "시스템 관리 > 시스템 환경설정 > 공통 환경설정", "system_setting", "cmn_fr_019_search"),
        new Screen("/admin/cmn/fr/020", "/api/admin/cmn/fr/020", "SCR-CMN-FR-020", "시스템 관리 > 시스템 환경설정 > 기준연도 관리", "base_year", "cmn_fr_020_search"),
        new Screen("/admin/cmn/fr/021", "/api/admin/cmn/fr/021", "SCR-CMN-FR-021", "시스템 관리 > 시스템 환경설정 > 파일정책 관리", "file_policy", "cmn_fr_021_search"),
        new Screen("/admin/cmn/fr/023", "/api/admin/cmn/fr/023", "SCR-CMN-FR-023", "시스템 관리 > 공지·도움말 관리 > 공지사항 관리", "notice", "cmn_fr_023_search"),
        new Screen("/admin/cmn/fr/052/053/054", "/api/admin/cmn/fr/052/053/054", "SCR-CMN-FR-052-053-054", "파일·데이터 관리 > 첨부파일 관리 > 첨부파일 관리", "attachment_metadata", "cmn_fr_052_053_054_search"),
        new Screen("/admin/cmn/fr/055", "/api/admin/cmn/fr/055", "SCR-CMN-FR-055", "파일·데이터 관리 > 엑셀 관리 > 업로드 양식 관리", "excel_upload_template", "cmn_fr_055_search"),
        new Screen("/admin/cmn/fr/056", "/api/admin/cmn/fr/056", "SCR-CMN-FR-056", "파일·데이터 관리 > 엑셀 관리 > 엑셀 업로드", "excel_upload_history", "cmn_fr_056_search"),
        new Screen("/admin/cmn/fr/059", "/api/admin/cmn/fr/059", "SCR-CMN-FR-059", "파일·데이터 관리 > 엑셀 관리 > 엑셀 다운로드", "excel_download_request", "cmn_fr_059_search"),
        new Screen("/admin/cmn/fr/071/072/073", "/api/admin/cmn/fr/071/072/073", "SCR-CMN-FR-071-072-073", "보안·감사 관리 > 개인정보 관리 > 개인정보 관리", "privacy_field_policy", "cmn_fr_071_072_073_search"),
        new Screen("/admin/cmn/fr/074", "/api/admin/cmn/fr/074", "SCR-CMN-FR-074", "보안·감사 관리 > 접속기록 관리 > 접속현황 관리", "session", "cmn_fr_074_search"),
        new Screen("/admin/cmn/fr/076/077/078", "/api/admin/cmn/fr/076/077/078", "SCR-CMN-FR-076-077-078", "보안·감사 관리 > 감사로그 관리 > 감사 로그 관리", "audit_log", "cmn_fr_076_077_078_search"),
        new Screen("/admin/cmn/fr/079", "/api/admin/cmn/fr/079", "SCR-CMN-FR-079", "시스템 운영 관리 > 배치작업 관리 > 배치 정의 관리", "batch_definition", "cmn_fr_079_search"),
        new Screen("/admin/cmn/fr/080", "/api/admin/cmn/fr/080", "SCR-CMN-FR-080", "시스템 운영 관리 > 배치작업 관리 > 배치 실행 관리", "batch_execution_history", "cmn_fr_080_search"),
        new Screen("/admin/cmn/fr/081", "/api/admin/cmn/fr/081", "SCR-CMN-FR-081", "시스템 운영 관리 > 배치작업 관리 > 배치 결과 조회", "batch_execution_result", "cmn_fr_081_search")
    );
    public static Screen byApiPath(String path) {
        return SCREENS.stream().filter(s -> path.startsWith(s.apiPath())).findFirst().orElse(SCREENS.get(0));
    }
}
