package kr.ac.knue.fpe.common;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.Map;

@RestController
public class ApiController {
    private final ManagedRecordService service;
    private final SessionService sessionService;
    public ApiController(ManagedRecordService service, SessionService sessionService) { this.service = service; this.sessionService = sessionService; }

    @GetMapping("/api/health")
    ApiResponse healthCheck() { return ApiResponse.ok(Map.of("status", "UP", "checkedAt", OffsetDateTime.now().toString())); }

    @PostMapping("/api/auth/login")
    ApiResponse login(@RequestBody Map<String, String> body, HttpServletRequest request, HttpServletResponse response) {
        return ApiResponse.ok(sessionService.login(body.get("loginId"), body.get("password"), request, response));
    }

    @PostMapping("/api/auth/logout")
    ApiResponse logout(HttpServletRequest request, HttpServletResponse response) { sessionService.logout(request, response); return ApiResponse.ok(Map.of("status", "LOGGED_OUT")); }

    @GetMapping("/api/auth/me")
    ApiResponse getCurrentUser(HttpServletRequest request) { return ApiResponse.ok(currentUser(request)); }

    @PostMapping("/api/auth/session")
    ApiResponse manageAuthSession(HttpServletRequest request) { return ApiResponse.ok(currentUser(request)); }

    @PostMapping("/api/auth/session/request-body")
    ApiResponse validateAuthSessionRequestBody(@RequestBody(required = false) Map<String, Object> requestBody, HttpServletRequest request) {
        return ApiResponse.ok(Map.of("user", currentUser(request), "accepted", requestBody == null ? Map.of() : requestBody));
    }

    @GetMapping("/api/users")
    ApiResponse listUsers(@RequestParam(required = false) String keyword, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(service.list("users", keyword, page, size));
    }

    @GetMapping("/api/organizations")
    ApiResponse listOrganizations(@RequestParam(required = false) String keyword, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(service.list("organizations", keyword, page, size));
    }

    @GetMapping("/api/positions")
    ApiResponse listPositions(@RequestParam(required = false) String keyword, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(service.list("positions", keyword, page, size));
    }

    @GetMapping("/api/roles")
    ApiResponse listRoles(@RequestParam(required = false) String keyword, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(service.list("roles", keyword, page, size));
    }

    @GetMapping("/api/user-roles")
    ApiResponse listUserRoles(@RequestParam(required = false) String keyword, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(service.list("user-roles", keyword, page, size));
    }

    @GetMapping("/api/menu-permissions")
    ApiResponse listMenuPermissions(@RequestParam(required = false) String keyword, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(service.list("menu-permissions", keyword, page, size));
    }

    @GetMapping("/api/function-permissions")
    ApiResponse listFunctionPermissions(@RequestParam(required = false) String keyword, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(service.list("function-permissions", keyword, page, size));
    }

    @GetMapping("/api/data-scope-permissions")
    ApiResponse listDataScopePermissions(@RequestParam(required = false) String keyword, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(service.list("data-scope-permissions", keyword, page, size));
    }

    @GetMapping("/api/menus")
    ApiResponse listMenus(@RequestParam(required = false) String keyword, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(service.list("menus", keyword, page, size));
    }

    @GetMapping("/api/code-groups")
    ApiResponse listCodeGroups(@RequestParam(required = false) String keyword, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(service.list("code-groups", keyword, page, size));
    }

    @GetMapping("/api/detail-codes")
    ApiResponse listDetailCodes(@RequestParam(required = false) String keyword, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(service.list("detail-codes", keyword, page, size));
    }

    @GetMapping("/api/configurations")
    ApiResponse listConfigurations(@RequestParam(required = false) String keyword, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(service.list("configurations", keyword, page, size));
    }

    @GetMapping("/api/base-years")
    ApiResponse listBaseYears(@RequestParam(required = false) String keyword, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(service.list("base-years", keyword, page, size));
    }

    @GetMapping("/api/file-policies")
    ApiResponse listFilePolicies(@RequestParam(required = false) String keyword, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(service.list("file-policies", keyword, page, size));
    }

    @GetMapping("/api/notices")
    ApiResponse listNotices(@RequestParam(required = false) String keyword, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(service.list("notices", keyword, page, size));
    }

    @GetMapping("/api/attachments")
    ApiResponse listAttachments(@RequestParam(required = false) String keyword, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(service.list("attachments", keyword, page, size));
    }

    @GetMapping("/api/excel/templates")
    ApiResponse listExcelTemplates(@RequestParam(required = false) String keyword, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(service.list("excel/templates", keyword, page, size));
    }

    @GetMapping("/api/excel/uploads")
    ApiResponse listExcelUploads(@RequestParam(required = false) String keyword, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(service.list("excel/uploads", keyword, page, size));
    }

    @GetMapping("/api/excel/downloads")
    ApiResponse listExcelDownloads(@RequestParam(required = false) String keyword, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(service.list("excel/downloads", keyword, page, size));
    }

    @GetMapping("/api/privacy/policies")
    ApiResponse listPrivacyPolicies(@RequestParam(required = false) String keyword, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(service.list("privacy/policies", keyword, page, size));
    }

    @GetMapping("/api/sessions")
    ApiResponse listSessions(@RequestParam(required = false) String keyword, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(service.list("sessions", keyword, page, size));
    }

    @GetMapping("/api/audit-logs")
    ApiResponse listAuditLogs(@RequestParam(required = false) String keyword, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(service.list("audit-logs", keyword, page, size));
    }

    @GetMapping("/api/batch-definitions")
    ApiResponse listBatchDefinitions(@RequestParam(required = false) String keyword, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(service.list("batch-definitions", keyword, page, size));
    }

    @GetMapping("/api/batch-executions")
    ApiResponse listBatchExecutions(@RequestParam(required = false) String keyword, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(service.list("batch-executions", keyword, page, size));
    }

    @GetMapping("/api/batch-results")
    ApiResponse listBatchResults(@RequestParam(required = false) String keyword, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(service.list("batch-results", keyword, page, size));
    }

    @PostMapping("/api/users")
    ApiResponse saveUsers(@Valid @RequestBody ManagedRecord record, HttpServletRequest request) {
        return ApiResponse.ok(service.save("users", record, currentUser(request)));
    }

    @PostMapping("/api/organizations")
    ApiResponse saveOrganizations(@Valid @RequestBody ManagedRecord record, HttpServletRequest request) {
        return ApiResponse.ok(service.save("organizations", record, currentUser(request)));
    }

    @PostMapping("/api/positions")
    ApiResponse savePositions(@Valid @RequestBody ManagedRecord record, HttpServletRequest request) {
        return ApiResponse.ok(service.save("positions", record, currentUser(request)));
    }

    @PostMapping("/api/roles")
    ApiResponse saveRoles(@Valid @RequestBody ManagedRecord record, HttpServletRequest request) {
        return ApiResponse.ok(service.save("roles", record, currentUser(request)));
    }

    @PostMapping("/api/user-roles")
    ApiResponse saveUserRoles(@Valid @RequestBody ManagedRecord record, HttpServletRequest request) {
        return ApiResponse.ok(service.save("user-roles", record, currentUser(request)));
    }

    @PostMapping("/api/menu-permissions")
    ApiResponse saveMenuPermissions(@Valid @RequestBody ManagedRecord record, HttpServletRequest request) {
        return ApiResponse.ok(service.save("menu-permissions", record, currentUser(request)));
    }

    @PostMapping("/api/function-permissions")
    ApiResponse saveFunctionPermissions(@Valid @RequestBody ManagedRecord record, HttpServletRequest request) {
        return ApiResponse.ok(service.save("function-permissions", record, currentUser(request)));
    }

    @PostMapping("/api/data-scope-permissions")
    ApiResponse saveDataScopePermissions(@Valid @RequestBody ManagedRecord record, HttpServletRequest request) {
        return ApiResponse.ok(service.save("data-scope-permissions", record, currentUser(request)));
    }

    @PostMapping("/api/menus")
    ApiResponse saveMenus(@Valid @RequestBody ManagedRecord record, HttpServletRequest request) {
        return ApiResponse.ok(service.save("menus", record, currentUser(request)));
    }

    @PostMapping("/api/code-groups")
    ApiResponse saveCodeGroups(@Valid @RequestBody ManagedRecord record, HttpServletRequest request) {
        return ApiResponse.ok(service.save("code-groups", record, currentUser(request)));
    }

    @PostMapping("/api/detail-codes")
    ApiResponse saveDetailCodes(@Valid @RequestBody ManagedRecord record, HttpServletRequest request) {
        return ApiResponse.ok(service.save("detail-codes", record, currentUser(request)));
    }

    @PostMapping("/api/configurations")
    ApiResponse saveConfigurations(@Valid @RequestBody ManagedRecord record, HttpServletRequest request) {
        return ApiResponse.ok(service.save("configurations", record, currentUser(request)));
    }

    @PostMapping("/api/base-years")
    ApiResponse saveBaseYears(@Valid @RequestBody ManagedRecord record, HttpServletRequest request) {
        return ApiResponse.ok(service.save("base-years", record, currentUser(request)));
    }

    @PostMapping("/api/file-policies")
    ApiResponse saveFilePolicies(@Valid @RequestBody ManagedRecord record, HttpServletRequest request) {
        return ApiResponse.ok(service.save("file-policies", record, currentUser(request)));
    }

    @PostMapping("/api/notices")
    ApiResponse saveNotices(@Valid @RequestBody ManagedRecord record, HttpServletRequest request) {
        return ApiResponse.ok(service.save("notices", record, currentUser(request)));
    }

    @PostMapping("/api/attachments")
    ApiResponse saveAttachments(@Valid @RequestBody ManagedRecord record, HttpServletRequest request) {
        return ApiResponse.ok(service.save("attachments", record, currentUser(request)));
    }

    @PostMapping("/api/excel/templates")
    ApiResponse saveExcelTemplates(@Valid @RequestBody ManagedRecord record, HttpServletRequest request) {
        return ApiResponse.ok(service.save("excel/templates", record, currentUser(request)));
    }

    @PostMapping("/api/excel/uploads")
    ApiResponse saveExcelUploads(@Valid @RequestBody ManagedRecord record, HttpServletRequest request) {
        return ApiResponse.ok(service.save("excel/uploads", record, currentUser(request)));
    }

    @PostMapping("/api/excel/downloads")
    ApiResponse saveExcelDownloads(@Valid @RequestBody ManagedRecord record, HttpServletRequest request) {
        return ApiResponse.ok(service.save("excel/downloads", record, currentUser(request)));
    }

    @PostMapping("/api/privacy/policies")
    ApiResponse savePrivacyPolicies(@Valid @RequestBody ManagedRecord record, HttpServletRequest request) {
        return ApiResponse.ok(service.save("privacy/policies", record, currentUser(request)));
    }

    @PostMapping("/api/sessions")
    ApiResponse saveSessions(@Valid @RequestBody ManagedRecord record, HttpServletRequest request) {
        return ApiResponse.ok(service.save("sessions", record, currentUser(request)));
    }

    @PostMapping("/api/audit-logs")
    ApiResponse saveAuditLogs(@Valid @RequestBody ManagedRecord record, HttpServletRequest request) {
        return ApiResponse.ok(service.save("audit-logs", record, currentUser(request)));
    }

    @PostMapping("/api/batch-definitions")
    ApiResponse saveBatchDefinitions(@Valid @RequestBody ManagedRecord record, HttpServletRequest request) {
        return ApiResponse.ok(service.save("batch-definitions", record, currentUser(request)));
    }

    @PostMapping("/api/batch-executions")
    ApiResponse saveBatchExecutions(@Valid @RequestBody ManagedRecord record, HttpServletRequest request) {
        return ApiResponse.ok(service.save("batch-executions", record, currentUser(request)));
    }

    @PostMapping("/api/batch-results")
    ApiResponse saveBatchResults(@Valid @RequestBody ManagedRecord record, HttpServletRequest request) {
        return ApiResponse.ok(service.save("batch-results", record, currentUser(request)));
    }

    @PostMapping("/api/admin/active-sessions")
    ApiResponse manageActiveSessions(@Valid @RequestBody ManagedRecord record, HttpServletRequest request) {
        return ApiResponse.ok(service.save("admin/active-sessions", record, currentUser(request)));
    }

    @PostMapping("/api/admin/attachments")
    ApiResponse manageAttachments(@Valid @RequestBody ManagedRecord record, HttpServletRequest request) {
        return ApiResponse.ok(service.save("admin/attachments", record, currentUser(request)));
    }

    @PostMapping("/api/admin/audit-logs")
    ApiResponse manageAuditLogs(@Valid @RequestBody ManagedRecord record, HttpServletRequest request) {
        return ApiResponse.ok(service.save("admin/audit-logs", record, currentUser(request)));
    }

    @PostMapping("/api/admin/base-years")
    ApiResponse manageBaseYears(@Valid @RequestBody ManagedRecord record, HttpServletRequest request) {
        return ApiResponse.ok(service.save("admin/base-years", record, currentUser(request)));
    }

    @PostMapping("/api/admin/batch-definitions")
    ApiResponse manageBatchDefinitions(@Valid @RequestBody ManagedRecord record, HttpServletRequest request) {
        return ApiResponse.ok(service.save("admin/batch-definitions", record, currentUser(request)));
    }

    @PostMapping("/api/admin/batch-executions")
    ApiResponse manageBatchExecutions(@Valid @RequestBody ManagedRecord record, HttpServletRequest request) {
        return ApiResponse.ok(service.save("admin/batch-executions", record, currentUser(request)));
    }

    @PostMapping("/api/admin/batch-results")
    ApiResponse manageBatchResults(@Valid @RequestBody ManagedRecord record, HttpServletRequest request) {
        return ApiResponse.ok(service.save("admin/batch-results", record, currentUser(request)));
    }

    @PostMapping("/api/admin/code-groups")
    ApiResponse manageCodeGroups(@Valid @RequestBody ManagedRecord record, HttpServletRequest request) {
        return ApiResponse.ok(service.save("admin/code-groups", record, currentUser(request)));
    }

    @PostMapping("/api/admin/codes")
    ApiResponse manageCodes(@Valid @RequestBody ManagedRecord record, HttpServletRequest request) {
        return ApiResponse.ok(service.save("admin/codes", record, currentUser(request)));
    }

    @PostMapping("/api/admin/data-scope-permissions")
    ApiResponse manageDataScopePermissions(@Valid @RequestBody ManagedRecord record, HttpServletRequest request) {
        return ApiResponse.ok(service.save("admin/data-scope-permissions", record, currentUser(request)));
    }

    @PostMapping("/api/admin/excel-downloads")
    ApiResponse manageExcelDownloads(@Valid @RequestBody ManagedRecord record, HttpServletRequest request) {
        return ApiResponse.ok(service.save("admin/excel-downloads", record, currentUser(request)));
    }

    @PostMapping("/api/admin/excel-uploads")
    ApiResponse manageExcelUploads(@Valid @RequestBody ManagedRecord record, HttpServletRequest request) {
        return ApiResponse.ok(service.save("admin/excel-uploads", record, currentUser(request)));
    }

    @PostMapping("/api/admin/file-policies")
    ApiResponse manageFilePolicies(@Valid @RequestBody ManagedRecord record, HttpServletRequest request) {
        return ApiResponse.ok(service.save("admin/file-policies", record, currentUser(request)));
    }

    @PostMapping("/api/admin/function-permissions")
    ApiResponse manageFunctionPermissions(@Valid @RequestBody ManagedRecord record, HttpServletRequest request) {
        return ApiResponse.ok(service.save("admin/function-permissions", record, currentUser(request)));
    }

    @PostMapping("/api/admin/menu-permissions")
    ApiResponse manageMenuPermissions(@Valid @RequestBody ManagedRecord record, HttpServletRequest request) {
        return ApiResponse.ok(service.save("admin/menu-permissions", record, currentUser(request)));
    }

    @PostMapping("/api/admin/menus")
    ApiResponse manageMenus(@Valid @RequestBody ManagedRecord record, HttpServletRequest request) {
        return ApiResponse.ok(service.save("admin/menus", record, currentUser(request)));
    }

    @PostMapping("/api/admin/notices")
    ApiResponse manageNotices(@Valid @RequestBody ManagedRecord record, HttpServletRequest request) {
        return ApiResponse.ok(service.save("admin/notices", record, currentUser(request)));
    }

    @PostMapping("/api/admin/organizations")
    ApiResponse manageOrganizations(@Valid @RequestBody ManagedRecord record, HttpServletRequest request) {
        return ApiResponse.ok(service.save("admin/organizations", record, currentUser(request)));
    }

    @PostMapping("/api/admin/personal-information")
    ApiResponse managePersonalInformation(@Valid @RequestBody ManagedRecord record, HttpServletRequest request) {
        return ApiResponse.ok(service.save("admin/personal-information", record, currentUser(request)));
    }

    @PostMapping("/api/admin/positions")
    ApiResponse managePositions(@Valid @RequestBody ManagedRecord record, HttpServletRequest request) {
        return ApiResponse.ok(service.save("admin/positions", record, currentUser(request)));
    }

    @PostMapping("/api/admin/roles")
    ApiResponse manageRoles(@Valid @RequestBody ManagedRecord record, HttpServletRequest request) {
        return ApiResponse.ok(service.save("admin/roles", record, currentUser(request)));
    }

    @PostMapping("/api/admin/system-settings")
    ApiResponse manageSystemSettings(@Valid @RequestBody ManagedRecord record, HttpServletRequest request) {
        return ApiResponse.ok(service.save("admin/system-settings", record, currentUser(request)));
    }

    @PostMapping("/api/admin/upload-templates")
    ApiResponse manageUploadTemplates(@Valid @RequestBody ManagedRecord record, HttpServletRequest request) {
        return ApiResponse.ok(service.save("admin/upload-templates", record, currentUser(request)));
    }

    @PostMapping("/api/admin/user-roles")
    ApiResponse manageUserRoles(@Valid @RequestBody ManagedRecord record, HttpServletRequest request) {
        return ApiResponse.ok(service.save("admin/user-roles", record, currentUser(request)));
    }

    @PostMapping("/api/admin/users")
    ApiResponse manageUsers(@Valid @RequestBody ManagedRecord record, HttpServletRequest request) {
        return ApiResponse.ok(service.save("admin/users", record, currentUser(request)));
    }

    private SessionUser currentUser(HttpServletRequest request) { return (SessionUser) request.getAttribute(SessionService.REQUEST_ATTR); }
}
