package kr.ac.knue.fpe.common;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.net.URI;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
public class CommonController {
  private final CommonMapper mapper;
  private final ChangeHistoryService history;
  public CommonController(CommonMapper mapper, ChangeHistoryService history) { this.mapper = mapper; this.history = history; }

  @GetMapping("/api/health")
  public ApiResponse<Map<String, Object>> health() { return ApiResponse.ok(Map.of("status", "UP")); }

  @PostMapping("/api/auth/login")
  @Transactional
  public ApiResponse<Map<String, Object>> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
    Map<String, Object> user = mapper.findUserByLoginId(request.loginId());
    if (user == null || !String.valueOf(user.get("passwordHash")).equals(request.password()) || !"Y".equals(user.get("systemUseYn")) || !"ACTIVE".equals(user.get("accountStatus"))) {
      throw new BusinessException(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호를 확인해 주세요.");
    }
    String sessionId = UUID.randomUUID().toString();
    mapper.insertSession(sessionId, String.valueOf(user.get("userId")));
    history.record("user_session", sessionId, "CREATE", null, Map.of("status", "ACTIVE"), String.valueOf(user.get("userId")), "login");
    Cookie cookie = new Cookie("FPE_SESSION", sessionId);
    cookie.setHttpOnly(true); cookie.setPath("/"); cookie.setMaxAge(60 * 60 * 8);
    response.addCookie(cookie);
    return ApiResponse.ok(currentUser(user));
  }

  @PostMapping("/api/auth/logout")
  @Transactional
  public ApiResponse<Map<String, Object>> logout(@CookieValue("FPE_SESSION") String sessionId, HttpServletResponse response) {
    mapper.revokeSession(sessionId);
    history.record("user_session", sessionId, "REVOKE", Map.of("status", "ACTIVE"), Map.of("status", "REVOKED"), SessionContext.userId(), "logout");
    Cookie cookie = new Cookie("FPE_SESSION", ""); cookie.setHttpOnly(true); cookie.setPath("/"); cookie.setMaxAge(0); response.addCookie(cookie);
    return ApiResponse.ok(Map.of("status", "REVOKED"));
  }

  @GetMapping("/api/auth/me")
  public ApiResponse<Map<String, Object>> me() { return ApiResponse.ok(currentUser(SessionContext.user())); }

  @GetMapping("/api/users") public ApiResponse<List<Map<String, Object>>> users(@RequestParam Map<String, Object> p) { return ApiResponse.ok(mapper.listUsers(clean(p))); }

  @PatchMapping("/api/users/{userId}/usage") @Transactional
  public ApiResponse<Map<String, Object>> updateUserUsage(@PathVariable String userId, @RequestBody Map<String, Object> body) {
    String useYn = string(body, "systemUseYn");
    requireEnum("systemUseYn", useYn, List.of("Y","N"));
    Map<String, Object> before = requireFound(mapper.findUser(userId), "사용자를 찾을 수 없습니다.");
    mapper.updateUserUsage(userId, useYn);
    Map<String, Object> after = mapper.findUser(userId);
    history.record("app_user", userId, "UPDATE", before, after, SessionContext.userId(), string(body, "reason"));
    return ApiResponse.ok(after);
  }

  @PutMapping("/api/users/{userId}/roles") @Transactional
  public ApiResponse<Map<String, Object>> replaceUserRoles(@PathVariable String userId, @RequestBody Map<String, Object> body) {
    @SuppressWarnings("unchecked") List<String> roleCodes = (List<String>) body.getOrDefault("roleCodes", List.of());
    if (roleCodes.isEmpty()) throw fields("roleCodes", "하나 이상의 역할이 필요합니다.");
    for (String roleCode : roleCodes) if (mapper.findRole(roleCode) == null) throw fields("roleCodes", "존재하는 R01~R09 역할만 허용합니다.");
    Map<String, Object> before = requireFound(mapper.findUser(userId), "사용자를 찾을 수 없습니다.");
    mapper.revokeUserRoles(userId);
    for (String roleCode : roleCodes) mapper.insertUserRole(userId, roleCode, SessionContext.userId());
    Map<String, Object> after = mapper.findUser(userId);
    history.record("user_role_assignment", userId, "UPDATE", before, after, SessionContext.userId(), string(body, "reason"));
    return ApiResponse.ok(after);
  }

  @GetMapping("/api/organizations") public ApiResponse<List<Map<String, Object>>> organizations(@RequestParam Map<String, Object> p) { return ApiResponse.ok(mapper.listOrganizations(clean(p))); }
  @GetMapping("/api/organizations/tree") public ApiResponse<List<Map<String, Object>>> orgTree(@RequestParam Map<String, Object> p) { return ApiResponse.ok(mapper.listOrganizationTree(clean(p))); }
  @PostMapping("/api/organization-relations") @Transactional
  public ApiResponse<Map<String, Object>> createOrganizationRelation(@RequestBody Map<String, Object> body) {
    requireField("organizationId", body); requireField("validFrom", body);
    body.putIfAbsent("relationId", UUID.randomUUID().toString());
    mapper.insertOrganizationRelation(body);
    history.record("organization_relation_history", String.valueOf(body.get("relationId")), "CREATE", null, body, SessionContext.userId(), string(body, "changeReason"));
    return ApiResponse.ok(Map.of("relationId", body.get("relationId")));
  }

  @GetMapping("/api/roles") public ApiResponse<List<Map<String, Object>>> roles(@RequestParam Map<String, Object> p) { return ApiResponse.ok(mapper.listRoles(clean(p))); }
  @PostMapping("/api/roles") @Transactional
  public ApiResponse<Map<String, Object>> createRole(@RequestBody Map<String, Object> body) { validateRole(body); if (mapper.findRole(string(body,"roleCode")) != null) throw new BusinessException(HttpStatus.CONFLICT, "이미 존재하는 역할 코드입니다."); mapper.insertRole(body); Map<String,Object> after = mapper.findRole(string(body,"roleCode")); history.record("role", string(body,"roleCode"), "CREATE", null, after, SessionContext.userId(), string(body,"reason")); return ApiResponse.ok(after); }
  @PutMapping("/api/roles/{roleCode}") @Transactional
  public ApiResponse<Map<String, Object>> updateRole(@PathVariable String roleCode, @RequestBody Map<String, Object> body) { validateRole(body); body.put("roleCode", roleCode); Map<String,Object> before = requireFound(mapper.findRole(roleCode), "역할을 찾을 수 없습니다."); mapper.updateRole(body); Map<String,Object> after = mapper.findRole(roleCode); history.record("role", roleCode, "UPDATE", before, after, SessionContext.userId(), string(body,"reason")); return ApiResponse.ok(after); }

  @GetMapping("/api/user-roles") public ApiResponse<List<Map<String, Object>>> userRoles(@RequestParam Map<String, Object> p) { return ApiResponse.ok(mapper.listUserRoles(clean(p))); }
  @PostMapping("/api/user-roles") @Transactional
  public ApiResponse<Map<String, Object>> assignUserRole(@RequestBody Map<String, Object> body) { requireField("userId", body); requireField("roleCode", body); requireField("validFrom", body); body.putIfAbsent("assignmentId", UUID.randomUUID().toString()); body.putIfAbsent("grantType", "MANUAL"); body.putIfAbsent("status", "ACTIVE"); body.putIfAbsent("approverUserId", SessionContext.userId()); mapper.insertUserRoleAssignment(body); Map<String,Object> after = mapper.findUserRole(String.valueOf(body.get("assignmentId"))); history.record("user_role_assignment", String.valueOf(body.get("assignmentId")), "CREATE", null, after, SessionContext.userId(), string(body,"reason")); return ApiResponse.ok(after); }
  @DeleteMapping("/api/user-roles/{assignmentId}") @Transactional
  public ApiResponse<Map<String, Object>> revokeUserRole(@PathVariable String assignmentId, @RequestParam(required = false) String reason) { if (reason == null || reason.isBlank()) throw fields("reason", "회수 사유가 필요합니다."); Map<String,Object> before = requireFound(mapper.findUserRole(assignmentId), "사용자 역할을 찾을 수 없습니다."); if ("REVOKED".equals(before.get("status"))) throw new BusinessException(HttpStatus.CONFLICT, "이미 회수된 역할입니다."); mapper.revokeUserRole(assignmentId); Map<String,Object> after = mapper.findUserRole(assignmentId); history.record("user_role_assignment", assignmentId, "REVOKE", before, after, SessionContext.userId(), reason); return ApiResponse.ok(after); }

  @GetMapping("/api/menus") public ApiResponse<List<Map<String, Object>>> menus(@RequestParam Map<String, Object> p) { return ApiResponse.ok(mapper.listMenus(clean(p))); }
  @GetMapping("/api/menus/tree") public ApiResponse<List<Map<String, Object>>> menuTree(@RequestParam Map<String, Object> p) { return ApiResponse.ok(mapper.listMenus(clean(p))); }
  @PostMapping("/api/menus") @Transactional public ApiResponse<Map<String,Object>> createMenu(@RequestBody Map<String,Object> body) { validateMenu(body); body.putIfAbsent("menuId", UUID.randomUUID().toString()); mapper.insertMenu(body); Map<String,Object> after = mapper.findMenu(String.valueOf(body.get("menuId"))); history.record("menu", String.valueOf(body.get("menuId")), "CREATE", null, after, SessionContext.userId(), string(body,"reason")); return ApiResponse.ok(after); }
  @PutMapping("/api/menus/{menuId}") @Transactional public ApiResponse<Map<String,Object>> updateMenu(@PathVariable String menuId, @RequestBody Map<String,Object> body) { validateMenu(body); body.put("menuId", menuId); Map<String,Object> before = requireFound(mapper.findMenu(menuId), "메뉴를 찾을 수 없습니다."); mapper.updateMenu(body); Map<String,Object> after = mapper.findMenu(menuId); history.record("menu", menuId, "UPDATE", before, after, SessionContext.userId(), string(body,"reason")); return ApiResponse.ok(after); }
  @PatchMapping("/api/menus/{menuId}/parent") @Transactional public ApiResponse<Map<String,Object>> updateMenuParent(@PathVariable String menuId, @RequestBody Map<String,Object> body) { if (menuId.equals(string(body,"parentMenuId"))) throw fields("parentMenuId", "자기 자신을 상위 메뉴로 지정할 수 없습니다."); body.put("menuId", menuId); Map<String,Object> before = requireFound(mapper.findMenu(menuId), "메뉴를 찾을 수 없습니다."); mapper.updateMenuParent(body); Map<String,Object> after = mapper.findMenu(menuId); history.record("menu", menuId, "UPDATE", before, after, SessionContext.userId(), string(body,"reason")); return ApiResponse.ok(after); }
  @PatchMapping("/api/menus/reorder") @Transactional public ApiResponse<Map<String,Object>> reorderMenus(@RequestBody Map<String,Object> body) { @SuppressWarnings("unchecked") List<String> ids = (List<String>) body.getOrDefault("orderedMenuIds", List.of()); if (ids.size() != ids.stream().distinct().count()) throw fields("orderedMenuIds", "중복 메뉴가 있습니다."); int i=10; for (String id: ids) { mapper.updateMenuOrder(id, i); i += 10; } history.record("menu", "reorder", "UPDATE", null, body, SessionContext.userId(), string(body,"reason")); return ApiResponse.ok(ids.isEmpty()?Map.of():mapper.findMenu(ids.get(0))); }

  @GetMapping("/api/menu-permissions") public ApiResponse<List<Map<String, Object>>> permissions(@RequestParam Map<String, Object> p) { return ApiResponse.ok(mapper.listMenuPermissions(clean(p))); }
  @PutMapping("/api/menu-permissions") @Transactional public ApiResponse<Map<String,Object>> savePermission(@RequestBody Map<String,Object> body) { requireEnum("subjectType", string(body,"subjectType"), List.of("ROLE","ORGANIZATION","USER")); requireField("menuId", body); body.putIfAbsent("permissionId", UUID.randomUUID().toString()); body.putIfAbsent("accessAllowed", true); body.putIfAbsent("functionAllowed", true); body.putIfAbsent("decisionEffect", "ALLOW"); mapper.upsertMenuPermission(body); history.record("menu_permission", String.valueOf(body.get("menuId")), "UPDATE", null, body, SessionContext.userId(), string(body,"reason")); return ApiResponse.ok(body); }

  @GetMapping("/api/code-groups") public ApiResponse<List<Map<String, Object>>> codeGroups(@RequestParam Map<String, Object> p) { return ApiResponse.ok(mapper.listCodeGroups(clean(p))); }
  @PostMapping("/api/code-groups") @Transactional public ApiResponse<Map<String,Object>> createCodeGroup(@RequestBody Map<String,Object> body) { requireField("groupId", body); requireField("groupName", body); if (mapper.findCodeGroup(string(body,"groupId")) != null) throw new BusinessException(HttpStatus.CONFLICT, "이미 존재하는 코드그룹입니다."); mapper.insertCodeGroup(body); Map<String,Object> after = mapper.findCodeGroup(string(body,"groupId")); history.record("code_group", string(body,"groupId"), "CREATE", null, after, SessionContext.userId(), string(body,"reason")); return ApiResponse.ok(after); }
  @PutMapping("/api/code-groups/{groupId}") @Transactional public ApiResponse<Map<String,Object>> updateCodeGroup(@PathVariable String groupId, @RequestBody Map<String,Object> body) { requireField("groupName", body); body.put("groupId", groupId); Map<String,Object> before = requireFound(mapper.findCodeGroup(groupId), "코드그룹을 찾을 수 없습니다."); mapper.updateCodeGroup(body); Map<String,Object> after = mapper.findCodeGroup(groupId); history.record("code_group", groupId, "UPDATE", before, after, SessionContext.userId(), string(body,"reason")); return ApiResponse.ok(after); }

  @GetMapping("/api/code-groups/{groupId}/codes") public ApiResponse<List<Map<String, Object>>> detailCodes(@PathVariable String groupId, @RequestParam Map<String, Object> p) { p.put("groupId", groupId); return ApiResponse.ok(mapper.listDetailCodes(clean(p))); }
  @PostMapping("/api/code-groups/{groupId}/codes") @Transactional public ApiResponse<Map<String,Object>> createDetailCode(@PathVariable String groupId, @RequestBody Map<String,Object> body) { requireField("codeValue", body); requireField("codeName", body); body.put("groupId", groupId); body.putIfAbsent("detailCodeId", UUID.randomUUID().toString()); body.putIfAbsent("sortOrder", 10); mapper.insertDetailCode(body); Map<String,Object> after = mapper.findDetailCode(groupId, string(body,"codeValue")); history.record("detail_code", string(body,"detailCodeId"), "CREATE", null, after, SessionContext.userId(), string(body,"reason")); return ApiResponse.ok(after); }
  @PutMapping("/api/code-groups/{groupId}/codes/{codeValue}") @Transactional public ApiResponse<Map<String,Object>> updateDetailCode(@PathVariable String groupId, @PathVariable String codeValue, @RequestBody Map<String,Object> body) { requireField("codeName", body); body.put("groupId", groupId); body.put("codeValue", codeValue); Map<String,Object> before = requireFound(mapper.findDetailCode(groupId, codeValue), "상세코드를 찾을 수 없습니다."); mapper.updateDetailCode(body); Map<String,Object> after = mapper.findDetailCode(groupId, codeValue); history.record("detail_code", String.valueOf(before.get("detailCodeId")), "UPDATE", before, after, SessionContext.userId(), string(body,"reason")); return ApiResponse.ok(after); }

  private Map<String,Object> currentUser(Map<String,Object> user) { Map<String,Object> out = new LinkedHashMap<>(user); out.remove("passwordHash"); out.put("roles", mapper.findActiveRoleCodes(String.valueOf(user.get("userId")))); out.put("menus", mapper.listMenus(Map.of("useYn", "Y"))); return out; }
  private Map<String,Object> clean(Map<String,Object> p) { p.entrySet().removeIf(e -> e.getValue() == null || String.valueOf(e.getValue()).isBlank()); return p; }
  private String string(Map<String,Object> body, String key) { Object v = body.get(key); return v == null ? null : String.valueOf(v); }
  private void requireField(String key, Map<String,Object> body) { if (string(body, key) == null || string(body, key).isBlank()) throw fields(key, "필수값입니다."); }
  private void requireEnum(String key, String value, List<String> allowed) { if (value == null || !allowed.contains(value)) throw fields(key, "허용값: " + allowed); }
  private BusinessException fields(String key, String message) { return new BusinessException(HttpStatus.BAD_REQUEST, "입력값을 확인해 주세요.", Map.of(key, message)); }
  private Map<String,Object> requireFound(Map<String,Object> found, String message) { if (found == null) throw new BusinessException(HttpStatus.NOT_FOUND, message); return found; }
  private void validateRole(Map<String,Object> body) { requireField("roleCode", body); requireField("roleName", body); requireField("purpose", body); requireEnum("defaultDataScope", string(body,"defaultDataScope"), List.of("SELF","DEPARTMENT","COLLEGE","ADMIN","ALL")); }
  private void validateMenu(Map<String,Object> body) { requireField("menuName", body); String url = string(body,"urlPath"); if (url != null && (!url.startsWith("/") || url.contains("backend:") || url.contains("localhost"))) throw fields("urlPath", "상대 route만 허용합니다."); body.putIfAbsent("menuLevel", 3); body.putIfAbsent("displayOrder", 90); }
  public record LoginRequest(@NotBlank String loginId, @NotBlank String password) {}
}
