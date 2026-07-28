package kr.ac.knue.fpe.admin;

import jakarta.servlet.http.HttpServletRequest;
import kr.ac.knue.fpe.common.api.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
public class AdminController {
    private final AdminMapper mapper;
    private final AdminService service;
    public AdminController(AdminMapper mapper, AdminService service) { this.mapper = mapper; this.service = service; }
    @GetMapping("/users") ApiResponse<?> users(@RequestParam Map<String,String> q, HttpServletRequest r) { var p=service.params(q); return ok(service.page(mapper.listUsers(p), p), r); }
    @PatchMapping("/users/{userId}/usage") ApiResponse<?> userUsage(@PathVariable String userId, @RequestBody Map<String,Object> b, HttpServletRequest r) { return ok(service.updateUserUsage(userId,b,rid(r)), r); }
    @GetMapping("/organizations") ApiResponse<?> organizations(@RequestParam Map<String,String> q, HttpServletRequest r) { var p=service.params(q); return ok(service.page(mapper.listOrganizations(p), p), r); }
    @GetMapping("/organizations/tree") ApiResponse<?> organizationTree(HttpServletRequest r) { return ok(service.tree(mapper.organizationTreeRows(), "organizationCode", "parentOrganizationCode", "children"), r); }
    @PutMapping("/organizations/{organizationCode}/relation") ApiResponse<?> organizationRelation(@PathVariable String organizationCode, @RequestBody Map<String,Object> b, HttpServletRequest r) { return ok(service.updateOrganizationRelation(organizationCode,b,rid(r)), r); }
    @GetMapping("/roles") ApiResponse<?> roles(@RequestParam Map<String,String> q, HttpServletRequest r) { var p=service.params(q); return ok(service.page(mapper.listRoles(p), p), r); }
    @PostMapping("/roles") @ResponseStatus(HttpStatus.CREATED) ApiResponse<?> createRole(@RequestBody Map<String,Object> b, HttpServletRequest r) { return ok(service.createRole(b,rid(r)), r); }
    @PatchMapping("/roles/{roleCode}") ApiResponse<?> updateRole(@PathVariable String roleCode, @RequestBody Map<String,Object> b, HttpServletRequest r) { return ok(service.updateRole(roleCode,b,rid(r)), r); }
    @GetMapping("/user-roles") ApiResponse<?> userRoles(@RequestParam Map<String,String> q, HttpServletRequest r) { var p=service.params(q); return ok(service.page(mapper.listUserRoles(p), p), r); }
    @PostMapping("/user-roles") @ResponseStatus(HttpStatus.CREATED) ApiResponse<?> assignUserRole(@RequestBody Map<String,Object> b, HttpServletRequest r) { return ok(service.assignUserRole(b,rid(r)), r); }
    @PatchMapping("/user-roles/{userRoleId}/revoke") ApiResponse<?> revokeUserRole(@PathVariable String userRoleId, @RequestBody(required=false) Map<String,Object> b, HttpServletRequest r) { return ok(service.revokeUserRole(userRoleId,b==null?new HashMap<>():b,rid(r)), r); }
    @GetMapping("/menus") ApiResponse<?> menus(@RequestParam Map<String,String> q, HttpServletRequest r) { var p=service.params(q); return ok(service.page(mapper.listMenus(p), p), r); }
    @PostMapping("/menus") @ResponseStatus(HttpStatus.CREATED) ApiResponse<?> createMenu(@RequestBody Map<String,Object> b, HttpServletRequest r) { return ok(service.createMenu(b,rid(r)), r); }
    @GetMapping("/menus/tree") ApiResponse<?> menuTree(HttpServletRequest r) { return ok(service.tree(mapper.menuTreeRows(), "menuId", "parentMenuId", "children"), r); }
    @PatchMapping("/menus/{menuId}") ApiResponse<?> updateMenu(@PathVariable String menuId, @RequestBody Map<String,Object> b, HttpServletRequest r) { return ok(service.updateMenu(menuId,b,rid(r)), r); }
    @PatchMapping("/menus/reorder") ApiResponse<?> reorderMenus(@RequestBody Map<String,Object> b, HttpServletRequest r) { service.reorderMenus(b,rid(r)); return ok(null, r); }
    @GetMapping("/menu-permissions") ApiResponse<?> permissions(@RequestParam Map<String,String> q, HttpServletRequest r) { var p=service.params(q); return ok(service.page(mapper.listMenuPermissions(p), p), r); }
    @PutMapping("/menu-permissions") ApiResponse<?> savePermissions(@RequestBody Map<String,Object> b, HttpServletRequest r) { service.savePermissions(b,rid(r)); return ok(null, r); }
    @GetMapping("/code-groups") ApiResponse<?> codeGroups(@RequestParam Map<String,String> q, HttpServletRequest r) { var p=service.params(q); return ok(service.page(mapper.listCodeGroups(p), p), r); }
    @PostMapping("/code-groups") @ResponseStatus(HttpStatus.CREATED) ApiResponse<?> createCodeGroup(@RequestBody Map<String,Object> b, HttpServletRequest r) { return ok(service.createCodeGroup(b,rid(r)), r); }
    @PatchMapping("/code-groups/{groupId}") ApiResponse<?> updateCodeGroup(@PathVariable String groupId, @RequestBody Map<String,Object> b, HttpServletRequest r) { return ok(service.updateCodeGroup(groupId,b,rid(r)), r); }
    @GetMapping("/detail-codes") ApiResponse<?> detailCodes(@RequestParam Map<String,String> q, HttpServletRequest r) { var p=service.params(q); p.putIfAbsent("groupId", "ROLE_SOURCE"); return ok(service.page(mapper.listDetailCodes(p), p), r); }
    @PostMapping("/detail-codes") @ResponseStatus(HttpStatus.CREATED) ApiResponse<?> createDetailCode(@RequestBody Map<String,Object> b, HttpServletRequest r) { return ok(service.createDetailCode(b,rid(r)), r); }
    @PatchMapping("/detail-codes/{detailCodeId}") ApiResponse<?> updateDetailCode(@PathVariable String detailCodeId, @RequestBody Map<String,Object> b, HttpServletRequest r) { return ok(service.updateDetailCode(detailCodeId,b,rid(r)), r); }
    private ApiResponse<Object> ok(Object data, HttpServletRequest r) { return ApiResponse.ok(data, rid(r)); }
    private String rid(HttpServletRequest r) { return String.valueOf(r.getAttribute("request_id")); }
}
