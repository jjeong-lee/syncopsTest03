package ac.knue.fpe.api;

import ac.knue.fpe.security.SecurityContext;
import ac.knue.fpe.service.FoundationService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@Validated
public class FoundationController {
  private final FoundationService foundationService;
  public FoundationController(FoundationService foundationService) { this.foundationService = foundationService; }

  @GetMapping("/health")
  public ApiResponse<Map<String, Object>> health() { return ApiResponse.ok(Map.of("status", "UP")); }

  @PostMapping("/auth/login")
  @Transactional
  public ResponseEntity<ApiResponse<?>> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
    Map<String, Object> user = foundationService.findUserByLoginId(request.loginId());
    if (user == null || !Objects.equals(user.get("passwordHash"), request.password()) || !Objects.equals(user.get("accountStatus"), "ACTIVE")) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.fail("UNAUTHORIZED", "계정 정보가 올바르지 않습니다."));
    }
    String sessionId = UUID.randomUUID().toString();
    foundationService.createSession(sessionId, (String) user.get("userId"));
    Cookie cookie = new Cookie("FPESESSION", sessionId);
    cookie.setHttpOnly(true); cookie.setPath("/"); cookie.setMaxAge(8 * 60 * 60);
    response.addCookie(cookie);
    return ResponseEntity.<ApiResponse<?>>ok(ApiResponse.ok(currentUser((String) user.get("userId"))));
  }

  @PostMapping("/auth/logout")
  @Transactional
  public ApiResponse<Map<String, Object>> logout(HttpServletRequest request, HttpServletResponse response) {
    SecurityContext ctx = context(request);
    foundationService.revokeSession(ctx.sessionId());
    Cookie cookie = new Cookie("FPESESSION", ""); cookie.setPath("/"); cookie.setMaxAge(0); response.addCookie(cookie);
    return ApiResponse.ok(Map.of("loggedOut", true));
  }

  @GetMapping("/auth/me")
  public ApiResponse<Map<String, Object>> me(HttpServletRequest request) { return ApiResponse.ok(currentUser(context(request).userId())); }

  @GetMapping("/menus/current")
  public ApiResponse<List<Map<String, Object>>> currentMenus(HttpServletRequest request, @RequestParam(required=false) String level, @RequestParam(required=false) String parentMenuId) {
    return ApiResponse.ok(foundationService.selectCurrentMenus(context(request).userId(), level, parentMenuId));
  }

  @GetMapping("/users")
  public ApiResponse<Map<String, Object>> users(@RequestParam Map<String, Object> params) { return page(foundationService.searchUsers(params)); }

  @PatchMapping("/users/{userId}/administration")
  @Transactional
  public ResponseEntity<ApiResponse<?>> updateUser(@PathVariable String userId, @RequestParam(required=false) String changeReason, @Valid @RequestBody UpdateUserAdministrationRequest request, HttpServletRequest http) {
    Map<String, Object> before = require(foundationService.findUser(userId), "사용자를 찾을 수 없습니다.");
    foundationService.updateUserAdministration(userId, request.systemUseYn());
    history("users", userId, before, request.toString(), context(http).userId(), changeReason);
    return ResponseEntity.<ApiResponse<?>>ok(ApiResponse.ok(require(foundationService.findUser(userId), "사용자를 찾을 수 없습니다.")));
  }

  @GetMapping("/organizations")
  public ApiResponse<Map<String, Object>> organizations(@RequestParam Map<String, Object> params) { return page(foundationService.listOrganizations(params)); }
  @GetMapping("/organizations/tree")
  public ApiResponse<List<Map<String, Object>>> organizationTree(@RequestParam(required=false) String baseDate, @RequestParam(required=false) String rootOrganizationCode) { return ApiResponse.ok(foundationService.listOrganizationTree(baseDate, rootOrganizationCode)); }
  @PutMapping("/organizations/{organizationId}/relationship")
  @Transactional
  public ApiResponse<Map<String, Object>> saveOrganization(@PathVariable String organizationId, @RequestParam(required=false) String changeReason, @RequestBody Map<String, Object> request, HttpServletRequest http) {
    if (Objects.equals(organizationId, request.get("parentOrganizationId"))) throw new IllegalArgumentException("parentOrganizationId는 자기 자신일 수 없습니다.");
    validatePeriod(request.get("effectiveStartDate"), request.get("effectiveEndDate"));
    Map<String,Object> before=require(foundationService.findOrganization(organizationId), "조직을 찾을 수 없습니다.");
    request.put("organizationId", organizationId); request.putIfAbsent("status", "ACTIVE"); foundationService.updateOrganizationRelationship(request); history("organizations", organizationId, before, request.toString(), context(http).userId(), changeReason);
    return ApiResponse.ok(foundationService.findOrganization(organizationId));
  }

  @GetMapping("/roles") public ApiResponse<Map<String,Object>> roles(@RequestParam(required=false) String useYn) { return page(foundationService.listRoles(useYn)); }
  @PutMapping("/roles/{roleCode}") @Transactional public ApiResponse<Map<String,Object>> updateRole(@PathVariable String roleCode, @RequestParam(required=false) String changeReason, @Valid @RequestBody UpdateRoleRequest body, HttpServletRequest http) {
    if (!roleCode.equals(body.roleCode())) throw new IllegalArgumentException("path roleCode와 body roleCode가 일치해야 합니다.");
    Map<String,Object> before=require(foundationService.findRole(roleCode), "역할을 찾을 수 없습니다."); Map<String,Object> p=new LinkedHashMap<>(); p.put("roleCode", roleCode); p.put("roleName", body.roleName()); p.put("purpose", body.purpose()); p.put("grantCriteria", body.grantCriteria()); p.put("defaultDataScope", body.defaultDataScope()); p.put("useYn", body.useYn()); foundationService.updateRole(p); history("roles", roleCode, before, p.toString(), context(http).userId(), changeReason); return ApiResponse.ok(foundationService.findRole(roleCode)); }

  @GetMapping("/user-roles") public ApiResponse<Map<String,Object>> userRoles(@RequestParam Map<String,Object> params) { if ("true".equals(params.get("activeOnly"))) params.put("activeOnly", true); return page(foundationService.listUserRoles(params)); }
  @PostMapping("/user-roles/assignments") @ResponseStatus(HttpStatus.CREATED) @Transactional public ApiResponse<List<Map<String,Object>>> assign(@RequestParam(required=false) String changeReason, @Valid @RequestBody AssignUserRolesRequest body, HttpServletRequest http) { validatePeriod(body.validFrom(), body.validTo()); for (String roleCode : body.roleCodes()) foundationService.insertUserRole(id(), body.userId(), roleCode, body.assignmentType(), body.validFrom(), body.validTo(), body.approverUserId()); history("user_roles", body.userId(), Map.of(), body.toString(), context(http).userId(), changeReason); return ApiResponse.ok(foundationService.listUserRoles(Map.of("userId", body.userId()))); }
  @PostMapping("/user-roles/{userRoleId}/revoke") @Transactional public ApiResponse<Map<String,Object>> revoke(@PathVariable String userRoleId, @RequestBody Map<String,Object> request, @RequestParam(required=false) String changeReason, HttpServletRequest http) { Map<String,Object> before=require(foundationService.findUserRole(userRoleId), "사용자 역할을 찾을 수 없습니다."); if (Objects.equals(before.get("status"), "REVOKED")) throw new IllegalArgumentException("이미 회수된 역할입니다."); foundationService.revokeUserRole(userRoleId, string(request.getOrDefault("revokeDate", LocalDate.now().toString()))); history("user_roles", userRoleId, before, request.toString(), context(http).userId(), changeReason); return ApiResponse.ok(foundationService.findUserRole(userRoleId)); }

  @GetMapping("/menu-permissions") public ApiResponse<Map<String,Object>> permissions(@RequestParam Map<String,Object> params) { return page(foundationService.listMenuPermissions(params)); }
  @PutMapping("/menu-permissions/bulk") @Transactional public ApiResponse<List<Map<String,Object>>> savePermissions(@RequestParam(required=false) String changeReason, @Valid @RequestBody SaveMenuPermissionsRequest body, HttpServletRequest http) { List<Map<String,Object>> saved = new java.util.ArrayList<>(); for (Map<String,Object> item: body.items()) { item.put("permissionId", id()); item.put("targetType", body.targetType()); item.put("targetId", body.targetId()); foundationService.upsertMenuPermission(item); saved.addAll(foundationService.listMenuPermissions(Map.of("targetType", body.targetType(), "targetId", body.targetId(), "menuId", item.get("menuId")))); } history("menu_permissions", body.targetType()+":"+body.targetId(), Map.of(), body.toString(), context(http).userId(), changeReason); return ApiResponse.ok(saved); }

  @GetMapping("/menus/tree") public ApiResponse<List<Map<String,Object>>> menuTree(@RequestParam(required=false) Boolean includeInactive, @RequestParam(required=false) String level) { return ApiResponse.ok(foundationService.listMenuTree(includeInactive, level)); }
  @PutMapping("/menus/{menuId}/structure") @Transactional public ApiResponse<Map<String,Object>> menuStructure(@PathVariable String menuId, @RequestParam(required=false) String changeReason, @RequestBody Map<String,Object> body, HttpServletRequest http) { if (Objects.equals(menuId, body.get("parentMenuId"))) throw new IllegalArgumentException("parentMenuId는 자기 자신일 수 없습니다."); Map<String,Object> before=require(foundationService.findMenu(menuId), "메뉴를 찾을 수 없습니다."); body.put("menuId", menuId); body.putIfAbsent("status", "ACTIVE"); foundationService.updateMenuStructure(body); history("menus", menuId, before, body.toString(), context(http).userId(), changeReason); return ApiResponse.ok(foundationService.findMenu(menuId)); }
  @PutMapping("/menus/reorder") @Transactional public ApiResponse<List<Map<String,Object>>> reorder(@Valid @RequestBody ReorderMenusRequest body, @RequestParam(required=false) String changeReason, HttpServletRequest http) { int order=1; for (String menuId: body.menuIds()) foundationService.updateMenuSort(menuId, order++); history("menus", "reorder", Map.of(), body.toString(), context(http).userId(), changeReason); return ApiResponse.ok(foundationService.listMenuTree(false, null)); }
  @PutMapping("/menus/{menuId}/info") @Transactional public ApiResponse<Map<String,Object>> menuInfo(@PathVariable String menuId, @RequestParam(required=false) String changeReason, @Valid @RequestBody SaveMenuInfoRequest body, HttpServletRequest http) { Map<String,Object> before=require(foundationService.findMenu(menuId), "메뉴를 찾을 수 없습니다."); Map<String,Object> p=new LinkedHashMap<>(); p.put("menuId", menuId); p.put("menuName", body.menuName()); p.put("screenId", body.screenId()); p.put("url", body.url()); p.put("icon", body.icon()); p.put("businessCategory", body.businessCategory()); p.put("description", body.description()); foundationService.updateMenuInfo(p); history("menus", menuId, before, p.toString(), context(http).userId(), changeReason); return ApiResponse.ok(foundationService.findMenu(menuId)); }

  @GetMapping("/code-groups") public ApiResponse<Map<String,Object>> codeGroups(@RequestParam Map<String,Object> params) { return page(foundationService.listCodeGroups(params)); }
  @PostMapping("/code-groups") @ResponseStatus(HttpStatus.CREATED) @Transactional public ApiResponse<Map<String,Object>> createGroup(@RequestParam(required=false) String changeReason, @Valid @RequestBody SaveCodeGroupRequest body, HttpServletRequest http) { if (foundationService.findCodeGroup(body.groupId()) != null) throw new IllegalArgumentException("중복 code group입니다."); Map<String,Object> p=body.toMap(); foundationService.insertCodeGroup(p); history("code_groups", body.groupId(), Map.of(), p.toString(), context(http).userId(), changeReason); return ApiResponse.ok(foundationService.findCodeGroup(body.groupId())); }
  @PutMapping("/code-groups/{groupId}") @Transactional public ApiResponse<Map<String,Object>> updateGroup(@PathVariable String groupId, @RequestParam(required=false) String changeReason, @Valid @RequestBody SaveCodeGroupRequest body, HttpServletRequest http) { if (!groupId.equals(body.groupId())) throw new IllegalArgumentException("path groupId와 body groupId가 일치해야 합니다."); Map<String,Object> before=require(foundationService.findCodeGroup(groupId), "코드그룹을 찾을 수 없습니다."); foundationService.updateCodeGroup(body.toMap()); history("code_groups", groupId, before, body.toString(), context(http).userId(), changeReason); return ApiResponse.ok(foundationService.findCodeGroup(groupId)); }
  @GetMapping("/code-groups/{groupId}/detail-codes") public ApiResponse<Map<String,Object>> details(@PathVariable String groupId, @RequestParam Map<String,Object> params) { params.put("groupId", groupId); return page(foundationService.listDetailCodes(params)); }
  @PostMapping("/code-groups/{groupId}/detail-codes") @ResponseStatus(HttpStatus.CREATED) @Transactional public ApiResponse<Map<String,Object>> createDetail(@PathVariable String groupId, @RequestParam(required=false) String changeReason, @Valid @RequestBody SaveDetailCodeRequest body, HttpServletRequest http) { Map<String,Object> p=body.toMap(); p.put("detailCodeId", id()); p.put("groupId", groupId); foundationService.insertDetailCode(p); history("detail_codes", string(p.get("detailCodeId")), Map.of(), p.toString(), context(http).userId(), changeReason); return ApiResponse.ok(foundationService.findDetailCode(string(p.get("detailCodeId")))); }
  @PutMapping("/detail-codes/{detailCodeId}") @Transactional public ApiResponse<Map<String,Object>> updateDetail(@PathVariable String detailCodeId, @RequestParam(required=false) String changeReason, @Valid @RequestBody SaveDetailCodeRequest body, HttpServletRequest http) { Map<String,Object> before=require(foundationService.findDetailCode(detailCodeId), "상세코드를 찾을 수 없습니다."); Map<String,Object> p=body.toMap(); p.put("detailCodeId", detailCodeId); foundationService.updateDetailCode(p); history("detail_codes", detailCodeId, before, p.toString(), context(http).userId(), changeReason); return ApiResponse.ok(foundationService.findDetailCode(detailCodeId)); }

  private Map<String,Object> currentUser(String userId) { Map<String,Object> user=foundationService.findCurrentUser(userId); user.put("roleCodes", foundationService.findActiveRoleCodes(userId)); return user; }
  private SecurityContext context(HttpServletRequest request) { return (SecurityContext) request.getAttribute("securityContext"); }
  private ApiResponse<Map<String,Object>> page(List<Map<String,Object>> content) { return ApiResponse.ok(Map.of("content", content, "totalElements", content.size(), "page", 0, "size", content.size())); }
  private void validatePeriod(Object from, Object to) { if (from != null && to != null && !string(from).isBlank() && !string(to).isBlank() && LocalDate.parse(string(from)).isAfter(LocalDate.parse(string(to)))) throw new IllegalArgumentException("시작일은 종료일보다 늦을 수 없습니다."); }
  private Map<String,Object> require(Map<String,Object> row, String message) { if (row == null) throw new IllegalArgumentException(message); return row; }
  private void history(String entity, String id, Object before, Object after, String by, String reason) { foundationService.insertChangeHistory(id(), entity, id, jsonish(before), jsonish(after), by, reason); }
  private String id() { return UUID.randomUUID().toString(); }
  private String string(Object v) { return v == null ? null : String.valueOf(v); }
  private String jsonish(Object value) {
    String escaped = java.util.Objects.toString(value, "null")
        .replace("\\", "\\\\")
        .replace("\"", "'");
    return "{\"value\":\"" + escaped + "\"}";
  }

  public record LoginRequest(@NotBlank String loginId, @NotBlank String password) {}
  public record UpdateUserAdministrationRequest(@NotBlank String systemUseYn, @NotEmpty List<String> roleCodes) {}
  public record UpdateRoleRequest(@NotBlank String roleCode, @NotBlank String roleName, String purpose, String grantCriteria, String defaultDataScope, String useYn) {}
  public record AssignUserRolesRequest(@NotBlank String userId, @NotEmpty List<String> roleCodes, String assignmentType, String validFrom, String validTo, String approverUserId) {}
  public record SaveMenuPermissionsRequest(@NotBlank String targetType, @NotBlank String targetId, @NotEmpty List<Map<String,Object>> items) {}
  public record ReorderMenusRequest(@NotEmpty List<String> menuIds) {}
  public record SaveMenuInfoRequest(@NotBlank String menuName, String screenId, String url, String icon, String businessCategory, String description) {}
  public record SaveCodeGroupRequest(@NotBlank String groupId, @NotBlank String groupName, String description, String managingDepartment, String useYn) { Map<String,Object> toMap(){ Map<String,Object> m=new LinkedHashMap<>(); m.put("groupId", groupId); m.put("groupName", groupName); m.put("description", description); m.put("managingDepartment", managingDepartment); m.put("useYn", useYn == null ? "Y" : useYn); return m; }}
  public record SaveDetailCodeRequest(@NotBlank String codeValue, @NotBlank String codeName, String parentDetailCodeId, Integer sortOrder, String additionalAttributes, String validFrom, String validTo, String useYn) { Map<String,Object> toMap(){ Map<String,Object> m=new LinkedHashMap<>(); m.put("codeValue", codeValue); m.put("codeName", codeName); m.put("parentDetailCodeId", parentDetailCodeId); m.put("sortOrder", sortOrder == null ? 1 : sortOrder); m.put("additionalAttributes", additionalAttributes == null ? "{}" : additionalAttributes); m.put("validFrom", validFrom); m.put("validTo", validTo); m.put("useYn", useYn == null ? "Y" : useYn); return m; }}
}
