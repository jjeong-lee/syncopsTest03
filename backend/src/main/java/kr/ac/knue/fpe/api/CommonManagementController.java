package kr.ac.knue.fpe.api;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import kr.ac.knue.fpe.common.*;
import kr.ac.knue.fpe.service.CommonManagementService;
import kr.ac.knue.fpe.ports.AuthenticationPort;

@RestController
@RequestMapping("/api")
@Validated
public class CommonManagementController {
    private final CommonManagementService service;
    private final AuthenticationPort authenticationPort;
    private final int ttlHours;

    public CommonManagementController(CommonManagementService service, AuthenticationPort authenticationPort,
            @Value("${app.session.ttl-hours:8}") int ttlHours) {
        this.service = service;
        this.authenticationPort = authenticationPort;
        this.ttlHours = ttlHours;
    }

    public record LoginRequest(@NotBlank String username, @NotBlank String password) {}

    @GetMapping("/health")
    public ApiResponse<Map<String, Object>> health() {
        return ApiResponse.ok(params("status", "UP"));
    }

    @PostMapping("/auth/login")
    @Transactional
    public ApiResponse<Map<String, Object>> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        var user = service.findUserByUsername(request.username());
        if (user == null || !"Y".equals(user.get("systemUseYn")) || !authenticationPort.matches(request.password(), String.valueOf(user.get("passwordHash")))) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "아이디 또는 비밀번호가 올바르지 않습니다.");
        }
        String sessionId = UUID.randomUUID().toString();
        String userId = String.valueOf(user.get("userId"));
        service.insertSession(sessionId, userId, OffsetDateTime.now().plusHours(ttlHours));
        Cookie cookie = new Cookie("SESSION", sessionId);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(ttlHours * 3600);
        cookie.setAttribute("SameSite", "Lax");
        response.addCookie(cookie);
        return ApiResponse.ok(currentUser(userId, String.valueOf(user.get("username"))));
    }

    @PostMapping("/auth/logout")
    @Transactional
    public ApiResponse<Void> logout(@CookieValue(name = "SESSION", required = false) String sessionId, HttpServletResponse response) {
        if (sessionId == null || sessionId.isBlank()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, ApiError.field("UNAUTHORIZED", "로그인이 필요합니다.", "SESSION", "세션 쿠키가 필요합니다."));
        }
        service.logoutSession(sessionId);
        Cookie cookie = new Cookie("SESSION", "");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setAttribute("SameSite", "Lax");
        response.addCookie(cookie);
        return ApiResponse.empty();
    }

    @GetMapping("/auth/me")
    public ApiResponse<Map<String, Object>> me() {
        var user = requireUser();
        return ApiResponse.ok(currentUser(user.userId(), user.username()));
    }

    @GetMapping("/users")
    public ApiResponse<Map<String, Object>> users(@RequestParam Map<String, Object> params) {
        var items = service.searchUsers(params).stream().map(this::withRoles).toList();
        return ApiResponse.ok(page(items));
    }

    @PatchMapping("/users/{userId}/usage")
    @Transactional
    public ApiResponse<Map<String, Object>> updateUserUsage(@PathVariable String userId, @RequestBody Map<String, Object> body) {
        requireAdmin();
        String yn = string(body, "systemUseYn", true);
        validateYn("systemUseYn", yn);
        if (body.containsKey("staffName") || body.containsKey("organizationCode") || body.containsKey("positionName")) {
            throw new ApiException(HttpStatus.BAD_REQUEST, ApiError.field("READONLY_SOURCE", "KORUS 원천 필드는 수정할 수 없습니다.", "korus", "조회 전용 필드입니다."));
        }
        service.updateUserUsage(userId, yn, string(body, "changeReason", false));
        return ApiResponse.ok(service.searchUsers(params("userId", userId)).stream().findFirst().map(this::withRoles).orElseThrow(() -> notFound("userId")));
    }

    @PutMapping("/users/{userId}/roles")
    @Transactional
    public ApiResponse<Map<String, Object>> updateUserRoles(@PathVariable String userId, @RequestBody Map<String, Object> body) {
        requireAdmin();
        List<?> roleCodes = list(body, "roleCodes");
        if (roleCodes.isEmpty()) throw field("roleCodes", "하나 이상의 역할을 선택해야 합니다.");
        String approvedBy = string(body, "approvedBy", true);
        LocalDate validFrom = date(body, "validFrom", true);
        LocalDate validTo = date(body, "validTo", false);
        validateDateRange(validFrom, validTo, "validTo");
        service.endManualUserRoles(userId, string(body, "changeReason", false));
        for (Object code : roleCodes) {
            String roleCode = String.valueOf(code);
            validateRole(roleCode);
            service.insertUserRole(userId, roleCode, "MANUAL", validFrom, validTo, approvedBy, string(body, "changeReason", false));
        }
        return ApiResponse.ok(service.searchUsers(params("userId", userId)).stream().findFirst().map(this::withRoles).orElseThrow(() -> notFound("userId")));
    }

    @GetMapping("/organizations")
    public ApiResponse<Map<String, Object>> organizations(@RequestParam Map<String, Object> params) { return ApiResponse.ok(page(service.searchOrganizations(params))); }

    @GetMapping("/organizations/tree")
    public ApiResponse<List<Map<String, Object>>> organizationTree() { return ApiResponse.ok(service.organizationTree()); }

    @PutMapping("/organizations/{organizationCode}/relation")
    @Transactional
    public ApiResponse<Map<String, Object>> updateOrganizationRelation(@PathVariable String organizationCode, @RequestBody Map<String, Object> body) {
        requireAdmin();
        String parent = string(body, "parentOrganizationCode", true);
        if (organizationCode.equals(parent)) throw field("parentOrganizationCode", "상위조직은 자기 자신일 수 없습니다.");
        LocalDate start = date(body, "effectiveStartDate", true);
        LocalDate end = date(body, "effectiveEndDate", false);
        validateDateRange(start, end, "effectiveEndDate");
        service.updateOrganizationRelation(organizationCode, parent, start, end, string(body, "relationChangeReason", false));
        return ApiResponse.ok(service.searchOrganizations(params("organizationCode", organizationCode)).stream().findFirst().orElseThrow(() -> notFound("organizationCode")));
    }

    @GetMapping("/roles")
    public ApiResponse<List<Map<String, Object>>> roles(@RequestParam(required = false) String filter) { return ApiResponse.ok(service.listRoles(filter)); }

    @PostMapping("/roles")
    @Transactional
    public ResponseEntity<ApiResponse<Map<String, Object>>> createRole(@RequestBody Map<String, Object> body) {
        requireAdmin(); validateRoleRequest(body); service.upsertRole(body);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(service.listRoles(string(body, "roleCode", true)).get(0)));
    }

    @PutMapping("/roles/{roleCode}")
    @Transactional
    public ApiResponse<Map<String, Object>> updateRole(@PathVariable String roleCode, @RequestBody Map<String, Object> body) {
        requireAdmin(); validateRoleRequest(body);
        if (!roleCode.equals(string(body, "roleCode", true))) throw field("roleCode", "path roleCode와 body roleCode가 일치해야 합니다.");
        service.upsertRole(body);
        return ApiResponse.ok(service.listRoles(roleCode).stream().findFirst().orElseThrow(() -> notFound("roleCode")));
    }

    @GetMapping("/user-roles")
    public ApiResponse<Map<String, Object>> userRoles(@RequestParam Map<String, Object> params) { return ApiResponse.ok(page(service.listUserRoles(params))); }

    @PostMapping("/user-roles/grants")
    @Transactional
    public ApiResponse<Map<String, Object>> grantUserRole(@RequestBody Map<String, Object> body) {
        requireAdmin();
        String userId = string(body, "userId", true); String roleCode = string(body, "roleCode", true); validateRole(roleCode);
        String assignmentType = string(body, "assignmentType", true);
        if (!List.of("POSITION_BASED", "MANUAL").contains(assignmentType)) throw field("assignmentType", "POSITION_BASED 또는 MANUAL만 허용됩니다.");
        LocalDate validFrom = date(body, "validFrom", true); LocalDate validTo = date(body, "validTo", false); validateDateRange(validFrom, validTo, "validTo");
        service.insertUserRole(userId, roleCode, assignmentType, validFrom, validTo, string(body, "approvedBy", true), string(body, "changeReason", false));
        return ApiResponse.ok(service.listUserRoles(params("userId", userId, "roleCode", roleCode)).stream().findFirst().orElseThrow(() -> notFound("userRoleId")));
    }

    @PostMapping("/user-roles/revocations")
    @Transactional
    public ApiResponse<Map<String, Object>> revokeUserRole(@RequestBody Map<String, Object> body) {
        requireAdmin();
        Long userRoleId = Long.valueOf(string(body, "userRoleId", true));
        if (service.findUserRole(userRoleId) == null) throw notFound("userRoleId");
        service.revokeUserRole(userRoleId, date(body, "validTo", true), string(body, "changeReason", true));
        return ApiResponse.ok(service.findUserRole(userRoleId));
    }

    @GetMapping("/menus/tree")
    public ApiResponse<List<Map<String, Object>>> menuTree() { return ApiResponse.ok(service.menuTree()); }

    @PutMapping("/menus/hierarchy")
    @Transactional
    public ApiResponse<List<Map<String, Object>>> updateMenuHierarchy(@RequestBody Map<String, Object> body) {
        requireAdmin();
        for (Object itemObj : list(body, "items")) {
            Map<?, ?> item = (Map<?, ?>) itemObj;
            String menuId = String.valueOf(item.get("menuId"));
            String parent = item.get("parentMenuId") == null ? null : String.valueOf(item.get("parentMenuId"));
            if (menuId.equals(parent)) throw field("parentMenuId", "부모 메뉴는 자기 자신일 수 없습니다.");
            service.updateMenuHierarchy(menuId, parent, Integer.valueOf(String.valueOf(item.get("displayOrder"))));
        }
        return ApiResponse.ok(service.menuTree());
    }

    @GetMapping("/menus")
    public ApiResponse<Map<String, Object>> menus(@RequestParam Map<String, Object> params) { return ApiResponse.ok(page(service.listMenus(params))); }

    @PostMapping("/menus")
    @Transactional
    public ResponseEntity<ApiResponse<Map<String, Object>>> createMenu(@RequestBody Map<String, Object> body) {
        requireAdmin(); validateMenu(body);
        String menuId = "MENU-" + UUID.randomUUID().toString().substring(0, 8);
        service.insertMenu(menuId, body);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(service.listMenus(params("menuId", menuId)).get(0)));
    }

    @PutMapping("/menus/{menuId}")
    @Transactional
    public ApiResponse<Map<String, Object>> updateMenu(@PathVariable String menuId, @RequestBody Map<String, Object> body) {
        requireAdmin(); validateMenu(body);
        if (service.listMenus(params("menuId", menuId)).isEmpty()) {
            service.insertMenu(menuId, body);
        } else {
            service.updateMenu(menuId, body);
        }
        return ApiResponse.ok(service.listMenus(params("menuId", menuId)).stream().findFirst().orElseThrow(() -> notFound("menuId")));
    }

    @GetMapping("/menu-permissions")
    public ApiResponse<Map<String, Object>> menuPermissions(@RequestParam Map<String, Object> params) { return ApiResponse.ok(page(service.listMenuPermissions(params))); }

    @PutMapping("/menu-permissions")
    @Transactional
    public ApiResponse<Map<String, Object>> saveMenuPermissions(@RequestBody Map<String, Object> body) {
        requireAdmin();
        String targetType = string(body, "targetType", true);
        if (!List.of("ROLE", "ORGANIZATION", "USER").contains(targetType)) throw field("targetType", "ROLE, ORGANIZATION, USER만 허용됩니다.");
        String targetId = string(body, "targetId", true);
        int saved = 0;
        for (Object permissionObj : list(body, "permissions")) {
            Map<String, Object> permission = new LinkedHashMap<>((Map<String, Object>) permissionObj);
            permission.put("targetType", targetType); permission.put("targetId", targetId);
            validateYn("accessAllowedYn", string(permission, "accessAllowedYn", true));
            validateYn("explicitDenyYn", string(permission, "explicitDenyYn", true));
            service.upsertMenuPermission(permission); saved++;
        }
        return ApiResponse.ok(params("savedCount", saved));
    }

    @GetMapping("/code-groups")
    public ApiResponse<Map<String, Object>> codeGroups(@RequestParam Map<String, Object> params) { return ApiResponse.ok(page(service.listCodeGroups(params))); }

    @PostMapping("/code-groups")
    @Transactional
    public ResponseEntity<ApiResponse<Map<String, Object>>> createCodeGroup(@RequestBody Map<String, Object> body) {
        requireAdmin(); validateRequired(body, "groupId", "groupName", "managementDepartment"); service.insertCodeGroup(body);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(service.listCodeGroups(params("groupId", string(body, "groupId", true))).get(0)));
    }

    @PutMapping("/code-groups/{groupId}")
    @Transactional
    public ApiResponse<Map<String, Object>> updateCodeGroup(@PathVariable String groupId, @RequestBody Map<String, Object> body) {
        requireAdmin(); validateRequired(body, "groupName", "managementDepartment");
        var upsertBody = new LinkedHashMap<>(body);
        upsertBody.put("groupId", groupId);
        if (service.listCodeGroups(params("groupId", groupId)).isEmpty()) {
            service.insertCodeGroup(upsertBody);
        } else {
            service.updateCodeGroup(groupId, upsertBody);
        }
        return ApiResponse.ok(service.listCodeGroups(params("groupId", groupId)).stream().findFirst().orElseThrow(() -> notFound("groupId")));
    }

    @GetMapping("/code-groups/{groupId}/codes")
    public ApiResponse<Map<String, Object>> detailCodes(@PathVariable String groupId, @RequestParam Map<String, Object> params) {
        var merged = new LinkedHashMap<String, Object>(params); merged.put("groupId", groupId);
        return ApiResponse.ok(page(service.listDetailCodes(merged)));
    }

    @PostMapping("/code-groups/{groupId}/codes")
    @Transactional
    public ResponseEntity<ApiResponse<Map<String, Object>>> createDetailCode(@PathVariable String groupId, @RequestBody Map<String, Object> body) {
        requireAdmin(); validateDetailCode(body); service.insertDetailCode(groupId, body);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(service.listDetailCodes(params("groupId", groupId, "codeValue", string(body, "codeValue", true))).get(0)));
    }

    @PutMapping("/code-groups/{groupId}/codes/{codeValue}")
    @Transactional
    public ApiResponse<Map<String, Object>> updateDetailCode(@PathVariable String groupId, @PathVariable String codeValue, @RequestBody Map<String, Object> body) {
        requireAdmin(); validateDetailCode(body);
        var upsertBody = new LinkedHashMap<>(body);
        upsertBody.put("codeValue", codeValue);
        if (service.listDetailCodes(params("groupId", groupId, "codeValue", codeValue)).isEmpty()) {
            service.insertDetailCode(groupId, upsertBody);
        } else {
            service.updateDetailCode(groupId, codeValue, upsertBody);
        }
        return ApiResponse.ok(service.listDetailCodes(params("groupId", groupId, "codeValue", codeValue)).stream().findFirst().orElseThrow(() -> notFound("codeValue")));
    }

    private Map<String, Object> currentUser(String userId, String username) {
        return Map.of("userId", userId, "username", username, "roles", service.selectRolesForUser(userId), "menus", service.selectMenusForUser(userId));
    }
    private Map<String, Object> page(List<Map<String, Object>> items) { return Map.of("items", items, "page", 0, "size", items.size(), "totalElements", items.size(), "totalPages", 1); }
    private Map<String, Object> params(Object... keysAndValues) { var map = new LinkedHashMap<String, Object>(); for (int i = 0; i < keysAndValues.length; i += 2) map.put(String.valueOf(keysAndValues[i]), keysAndValues[i + 1]); return map; }
    private Map<String, Object> withRoles(Map<String, Object> user) { var copy = new LinkedHashMap<>(user); copy.put("roles", service.selectRolesForUser(String.valueOf(user.get("userId")))); return copy; }
    private SessionUser requireUser() { var user = SessionContext.current(); if (user == null) throw new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "로그인이 필요합니다."); return user; }
    private void requireAdmin() { if (!requireUser().isAdmin()) throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "R09 시스템관리자 권한이 필요합니다."); }
    private String string(Map<?, ?> body, String key, boolean required) { Object value = body.get(key); if ((value == null || String.valueOf(value).isBlank()) && required) throw field(key, "필수 입력값입니다."); return value == null ? null : String.valueOf(value); }
    private List<?> list(Map<String, Object> body, String key) { Object value = body.get(key); if (!(value instanceof List<?> list)) throw field(key, "목록 입력값이 필요합니다."); return list; }
    private LocalDate date(Map<String, Object> body, String key, boolean required) { String value = string(body, key, required); return value == null || value.isBlank() ? null : LocalDate.parse(value); }
    private void validateDateRange(LocalDate start, LocalDate end, String field) { if (start != null && end != null && end.isBefore(start)) throw field(field, "종료일은 시작일보다 빠를 수 없습니다."); }
    private void validateYn(String field, String value) { if (!List.of("Y", "N").contains(value)) throw field(field, "Y 또는 N만 허용됩니다."); }
    private void validateRole(String roleCode) { if (!List.of("R01","R02","R03","R04","R05","R06","R07","R08","R09").contains(roleCode)) throw field("roleCode", "R01~R09만 허용됩니다."); }
    private void validateRoleRequest(Map<String, Object> body) { validateRequired(body, "roleCode", "roleName", "purpose", "grantCriteria", "dataScopeDefault"); validateRole(string(body, "roleCode", true)); }
    private void validateMenu(Map<String, Object> body) { validateRequired(body, "menuName"); String url = string(body, "url", false); if (url != null && !url.isBlank() && !url.startsWith("/system/")) throw field("url", "URL은 /system/... 범위여야 합니다."); }
    private void validateDetailCode(Map<String, Object> body) { validateRequired(body, "codeValue", "codeName", "sortOrder"); LocalDate start = date(body, "validFrom", false); LocalDate end = date(body, "validTo", false); validateDateRange(start, end, "validTo"); }
    private void validateRequired(Map<String, Object> body, String... keys) { for (String key : keys) string(body, key, true); }
    private ApiException field(String field, String message) { return new ApiException(HttpStatus.BAD_REQUEST, ApiError.field("VALIDATION_ERROR", "입력값을 확인해 주세요.", field, message)); }
    private ApiException notFound(String field) { return new ApiException(HttpStatus.NOT_FOUND, ApiError.field("NOT_FOUND", "대상을 찾을 수 없습니다.", field, "존재하지 않는 식별자입니다.")); }
}
