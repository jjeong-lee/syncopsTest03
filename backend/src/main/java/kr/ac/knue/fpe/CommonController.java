package kr.ac.knue.fpe;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class CommonController {
    private final CommonService service;
    private final boolean secureCookie;
    public CommonController(CommonService service, @Value("${app.session-cookie-secure:false}") boolean secureCookie) { this.service=service; this.secureCookie=secureCookie; }

    @GetMapping("/health") public ApiEnvelope<Map<String,Object>> health(){ return ApiEnvelope.ok(service.health()); }
    @PostMapping("/auth/login") public ApiEnvelope<CurrentUser> login(@RequestBody LoginRequest request, HttpServletResponse response){ var result=service.login(request); ResponseCookie cookie=ResponseCookie.from("SESSION", result.sessionId()).httpOnly(true).secure(secureCookie).sameSite("Lax").path("/").maxAge(Duration.ofHours(8)).build(); response.addHeader("Set-Cookie", cookie.toString()); return ApiEnvelope.ok(result.currentUser()); }
    @PostMapping("/auth/logout") public ApiEnvelope<Void> logout(HttpServletRequest request, HttpServletResponse response){ service.logout(session(request)); response.addHeader("Set-Cookie", ResponseCookie.from("SESSION","").httpOnly(true).secure(secureCookie).sameSite("Lax").path("/").maxAge(0).build().toString()); return ApiEnvelope.ok(); }
    @GetMapping("/auth/me") public ApiEnvelope<CurrentUser> me(HttpServletRequest request){ return ApiEnvelope.ok(service.currentUser(current(request))); }

    @GetMapping("/admin/users") public ApiEnvelope<PageResponse<Map<String,Object>>> listUsers(@RequestParam Map<String,String> q){ return ApiEnvelope.ok(service.listUsers(q)); }
    @PatchMapping("/admin/users/{userId}/usage") public ApiEnvelope<Map<String,Object>> updateUserUsage(HttpServletRequest req,@PathVariable String userId,@RequestBody UserUsageUpdateRequest body){ return ApiEnvelope.ok(service.updateUserUsage(actor(req),userId,body)); }
    @PutMapping("/admin/users/{userId}/roles") public ApiEnvelope<Map<String,Object>> replaceUserRoles(HttpServletRequest req,@PathVariable String userId,@RequestBody UserRolesReplaceRequest body){ return ApiEnvelope.ok(service.replaceUserRoles(actor(req),userId,body)); }
    @GetMapping("/admin/organizations") public ApiEnvelope<PageResponse<Map<String,Object>>> listOrganizations(@RequestParam Map<String,String> q){ return ApiEnvelope.ok(service.listOrganizations(q)); }
    @PutMapping("/admin/organizations/{organizationCode}/relationships") public ApiEnvelope<Map<String,Object>> updateOrganization(HttpServletRequest req,@PathVariable String organizationCode,@RequestBody OrganizationRelationshipUpdateRequest body){ return ApiEnvelope.ok(service.updateOrganization(actor(req),organizationCode,body)); }
    @GetMapping("/admin/roles") public ApiEnvelope<PageResponse<Map<String,Object>>> listRoles(@RequestParam Map<String,String> q){ return ApiEnvelope.ok(service.listRoles(q)); }
    @PostMapping("/admin/roles") public ApiEnvelope<Map<String,Object>> createRole(HttpServletRequest req,@RequestBody RoleUpsertRequest body){ return ApiEnvelope.ok(service.createRole(actor(req),body)); }
    @PutMapping("/admin/roles/{roleCode}") public ApiEnvelope<Map<String,Object>> updateRole(HttpServletRequest req,@PathVariable String roleCode,@RequestBody RoleUpsertRequest body){ return ApiEnvelope.ok(service.updateRole(actor(req),roleCode,body)); }
    @GetMapping("/admin/user-roles") public ApiEnvelope<PageResponse<Map<String,Object>>> listUserRoles(@RequestParam Map<String,String> q){ return ApiEnvelope.ok(service.listUserRoles(q)); }
    @PostMapping("/admin/user-roles") public ApiEnvelope<Map<String,Object>> grantUserRole(HttpServletRequest req,@RequestBody GrantUserRoleRequest body){ return ApiEnvelope.ok(service.grantUserRole(actor(req),body)); }
    @DeleteMapping("/admin/user-roles/{assignmentId}") public ApiEnvelope<Map<String,Object>> revokeUserRole(HttpServletRequest req,@PathVariable Long assignmentId,@RequestParam(required=false) String reason){ return ApiEnvelope.ok(service.revokeUserRole(actor(req),assignmentId,reason)); }
    @GetMapping("/admin/menu-permissions") public ApiEnvelope<PageResponse<Map<String,Object>>> listMenuPermissions(@RequestParam Map<String,String> q){ return ApiEnvelope.ok(service.listMenuPermissions(q)); }
    @PutMapping("/admin/menu-permissions") public ApiEnvelope<PageResponse<Map<String,Object>>> saveMenuPermissions(HttpServletRequest req,@RequestBody SaveMenuPermissionsRequest body){ return ApiEnvelope.ok(service.saveMenuPermissions(actor(req),body)); }
    @GetMapping("/admin/menus/tree") public ApiEnvelope<PageResponse<Map<String,Object>>> menuTree(@RequestParam Map<String,String> q){ return ApiEnvelope.ok(service.listMenus(q)); }
    @PutMapping("/admin/menus/{menuId}/structure") public ApiEnvelope<Map<String,Object>> updateMenuStructure(HttpServletRequest req,@PathVariable Long menuId,@RequestBody MenuStructureUpdateRequest body){ return ApiEnvelope.ok(service.updateMenuStructure(actor(req),menuId,body)); }
    @GetMapping("/admin/menus") public ApiEnvelope<PageResponse<Map<String,Object>>> listMenus(@RequestParam Map<String,String> q){ return ApiEnvelope.ok(service.listMenus(q)); }
    @PostMapping("/admin/menus") public ApiEnvelope<Map<String,Object>> createMenu(HttpServletRequest req,@RequestBody MenuUpsertRequest body){ return ApiEnvelope.ok(service.createMenu(actor(req),body)); }
    @PutMapping("/admin/menus/{menuId}") public ApiEnvelope<Map<String,Object>> updateMenu(HttpServletRequest req,@PathVariable Long menuId,@RequestBody MenuUpsertRequest body){ return ApiEnvelope.ok(service.updateMenu(actor(req),menuId,body)); }
    @GetMapping("/admin/code-groups") public ApiEnvelope<PageResponse<Map<String,Object>>> listCodeGroups(@RequestParam Map<String,String> q){ return ApiEnvelope.ok(service.listCodeGroups(q)); }
    @PostMapping("/admin/code-groups") public ApiEnvelope<Map<String,Object>> createCodeGroup(HttpServletRequest req,@RequestBody CodeGroupUpsertRequest body){ return ApiEnvelope.ok(service.createCodeGroup(actor(req),body)); }
    @PutMapping("/admin/code-groups/{groupId}") public ApiEnvelope<Map<String,Object>> updateCodeGroup(HttpServletRequest req,@PathVariable String groupId,@RequestBody CodeGroupUpsertRequest body){ return ApiEnvelope.ok(service.updateCodeGroup(actor(req),groupId,body)); }
    @GetMapping("/admin/code-groups/{groupId}/codes") public ApiEnvelope<PageResponse<Map<String,Object>>> listDetailCodes(@PathVariable String groupId,@RequestParam Map<String,String> q){ return ApiEnvelope.ok(service.listDetailCodes(groupId,q)); }
    @PostMapping("/admin/code-groups/{groupId}/codes") public ApiEnvelope<Map<String,Object>> createDetailCode(HttpServletRequest req,@PathVariable String groupId,@RequestBody DetailCodeUpsertRequest body){ return ApiEnvelope.ok(service.createDetailCode(actor(req),groupId,body)); }
    @PutMapping("/admin/code-groups/{groupId}/codes/{codeValue}") public ApiEnvelope<Map<String,Object>> updateDetailCode(HttpServletRequest req,@PathVariable String groupId,@PathVariable String codeValue,@RequestBody DetailCodeUpsertRequest body){ return ApiEnvelope.ok(service.updateDetailCode(actor(req),groupId,codeValue,body)); }

    @SuppressWarnings("unchecked") private Map<String,Object> current(HttpServletRequest req){ return (Map<String,Object>) req.getAttribute("currentUser"); }
    private String actor(HttpServletRequest req){ return (String) current(req).get("userId"); }
    private String session(HttpServletRequest req){ if(req.getCookies()!=null) for(Cookie c:req.getCookies()) if("SESSION".equals(c.getName())) return c.getValue(); throw new ForbiddenException("세션이 필요합니다."); }
}
