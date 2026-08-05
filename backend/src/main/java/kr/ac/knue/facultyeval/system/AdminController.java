package kr.ac.knue.facultyeval.system;

import java.util.HashMap;
import java.util.Map;
import kr.ac.knue.facultyeval.common.ApiResponse;
import kr.ac.knue.facultyeval.common.PageResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AdminController {
  private final AdminService service;
  public AdminController(AdminService service) { this.service = service; }
  @GetMapping("/users") ApiResponse<PageResponse<Map<String,Object>>> users(@RequestParam Map<String,Object> p){ return ApiResponse.ok(service.users(p)); }
  @GetMapping("/users/{userId}") ApiResponse<Map<String,Object>> user(@PathVariable String userId){ return ApiResponse.ok(service.user(userId)); }
  @PatchMapping("/users/{userId}/usage") ApiResponse<Map<String,Object>> userUsage(@PathVariable String userId,@RequestBody Map<String,Object> b){ return ApiResponse.ok(service.updateUserUsage(userId,b)); }
  @PutMapping("/users/{userId}/roles") ApiResponse<Map<String,Object>> userRolesReplace(@PathVariable String userId,@RequestBody Map<String,Object> b){ return ApiResponse.ok(service.replaceUserRoles(userId,b)); }
  @GetMapping("/organizations") ApiResponse<PageResponse<Map<String,Object>>> organizations(@RequestParam Map<String,Object> p){ return ApiResponse.ok(service.organizations(p)); }
  @GetMapping("/organizations/tree") ApiResponse<Map<String,Object>> organizationTree(){ return ApiResponse.ok(service.organizationTree()); }
  @PutMapping("/organizations/{orgCode}/relationship") ApiResponse<Map<String,Object>> orgRelationship(@PathVariable String orgCode,@RequestBody Map<String,Object> b){ return ApiResponse.ok(service.updateOrganizationRelationship(orgCode,b)); }
  @GetMapping("/roles") ApiResponse<PageResponse<Map<String,Object>>> roles(@RequestParam Map<String,Object> p){ return ApiResponse.ok(service.roles(p)); }
  @PostMapping("/roles") ResponseEntity<ApiResponse<Map<String,Object>>> createRole(@RequestBody Map<String,Object> b){ return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(service.createRole(b))); }
  @PutMapping("/roles/{roleCode}") ApiResponse<Map<String,Object>> updateRole(@PathVariable String roleCode,@RequestBody Map<String,Object> b){ return ApiResponse.ok(service.updateRole(roleCode,b)); }
  @GetMapping("/user-roles") ApiResponse<PageResponse<Map<String,Object>>> userRoles(@RequestParam Map<String,Object> p){ return ApiResponse.ok(service.userRoles(p)); }
  @PostMapping("/user-roles") ResponseEntity<ApiResponse<Map<String,Object>>> createUserRole(@RequestBody Map<String,Object> b){ return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(service.createUserRole(b))); }
  @PutMapping("/user-roles/{assignmentId}") ApiResponse<Map<String,Object>> updateUserRole(@PathVariable String assignmentId,@RequestBody Map<String,Object> b){ return ApiResponse.ok(service.updateUserRole(assignmentId,b)); }
  @DeleteMapping("/user-roles/{assignmentId}") ApiResponse<Map<String,Object>> revokeUserRole(@PathVariable String assignmentId){ return ApiResponse.ok(service.revokeUserRole(assignmentId)); }
  @GetMapping("/menu-permissions") ApiResponse<PageResponse<Map<String,Object>>> menuPermissions(@RequestParam Map<String,Object> p){ return ApiResponse.ok(service.menuPermissions(p)); }
  @PutMapping("/menu-permissions") ApiResponse<PageResponse<Map<String,Object>>> saveMenuPermissions(@RequestBody Map<String,Object> b){ return ApiResponse.ok(service.saveMenuPermissions(b)); }
  @GetMapping("/menus/tree") ApiResponse<Map<String,Object>> menuTree(){ return ApiResponse.ok(service.menuTree()); }
  @PutMapping("/menus/{menuId}/parent") ApiResponse<Map<String,Object>> menuParent(@PathVariable String menuId,@RequestBody Map<String,Object> b){ return ApiResponse.ok(service.updateMenuParent(menuId,b)); }
  @PutMapping("/menus/reorder") ApiResponse<Map<String,Object>> reorderMenus(@RequestBody Map<String,Object> b){ return ApiResponse.ok(service.reorderMenus(b)); }
  @GetMapping("/menus") ApiResponse<PageResponse<Map<String,Object>>> menus(@RequestParam Map<String,Object> p){ return ApiResponse.ok(service.menus(p)); }
  @GetMapping("/menus/{menuId}") ApiResponse<Map<String,Object>> menu(@PathVariable String menuId){ return ApiResponse.ok(service.menu(menuId)); }
  @PostMapping("/menus") ResponseEntity<ApiResponse<Map<String,Object>>> createMenu(@RequestBody Map<String,Object> b){ return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(service.createMenu(b))); }
  @PutMapping("/menus/{menuId}") ApiResponse<Map<String,Object>> updateMenu(@PathVariable String menuId,@RequestBody Map<String,Object> b){ return ApiResponse.ok(service.updateMenu(menuId,b)); }
  @GetMapping("/code-groups") ApiResponse<PageResponse<Map<String,Object>>> codeGroups(@RequestParam Map<String,Object> p){ return ApiResponse.ok(service.codeGroups(p)); }
  @GetMapping("/code-groups/{groupId}") ApiResponse<Map<String,Object>> codeGroup(@PathVariable String groupId){ return ApiResponse.ok(service.codeGroup(groupId)); }
  @PostMapping("/code-groups") ResponseEntity<ApiResponse<Map<String,Object>>> createCodeGroup(@RequestBody Map<String,Object> b){ return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(service.createCodeGroup(b))); }
  @PutMapping("/code-groups/{groupId}") ApiResponse<Map<String,Object>> updateCodeGroup(@PathVariable String groupId,@RequestBody Map<String,Object> b){ return ApiResponse.ok(service.updateCodeGroup(groupId,b)); }
  @GetMapping("/code-groups/{groupId}/code-details") ApiResponse<PageResponse<Map<String,Object>>> codeDetailsByGroup(@PathVariable String groupId,@RequestParam Map<String,Object> p){ Map<String,Object> copy=new HashMap<>(p); copy.put("groupId",groupId); return ApiResponse.ok(service.codeDetails(copy)); }
  @GetMapping("/code-details") ApiResponse<PageResponse<Map<String,Object>>> codeDetails(@RequestParam Map<String,Object> p){ return ApiResponse.ok(service.codeDetails(p)); }
  @PostMapping("/code-details") ResponseEntity<ApiResponse<Map<String,Object>>> createCodeDetail(@RequestBody Map<String,Object> b){ return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(service.createCodeDetail(b))); }
  @PutMapping("/code-details/{codeId}") ApiResponse<Map<String,Object>> updateCodeDetail(@PathVariable String codeId,@RequestBody Map<String,Object> b){ return ApiResponse.ok(service.updateCodeDetail(codeId,b)); }
}
