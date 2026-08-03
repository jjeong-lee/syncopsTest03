package kr.ac.knue.fpe;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

record CurrentUser(String userId, String username, List<String> roles, boolean systemEnabled) {}
record PageResponse<T>(List<T> items, int page, int size, long totalElements, int totalPages) {}
record LoginRequest(String username, String password) {}
record UserUsageUpdateRequest(Boolean systemEnabled, String reason) {}
record RoleAssignmentRequest(String roleCode, LocalDate effectiveStartDate, LocalDate effectiveEndDate, String assignmentSource, String approvedBy) {}
record UserRolesReplaceRequest(List<RoleAssignmentRequest> roles, String reason) {}
record OrganizationRelationshipUpdateRequest(String parentOrganizationCode, LocalDate effectiveStartDate, LocalDate effectiveEndDate, String reason) {}
record RoleUpsertRequest(String roleCode, String roleName, String purpose, String grantCriteria, String defaultDataScope, String reason) {}
record GrantUserRoleRequest(String userId, String roleCode, String assignmentSource, LocalDate effectiveStartDate, LocalDate effectiveEndDate, String approvedBy, String reason) {}
record MenuStructureUpdateRequest(Long parentMenuId, Integer displayOrder, String reason) {}
record MenuUpsertRequest(Long parentMenuId, String menuLevel, String menuName, String screenId, String url, String icon, String businessCategory, String description, Integer displayOrder, String reason) {}
record MenuPermissionItem(Long menuId, String permissionEffect) {}
record SaveMenuPermissionsRequest(String principalType, String principalId, List<MenuPermissionItem> permissions, String reason) {}
record CodeGroupUpsertRequest(String groupId, String groupName, String description, String managingDepartment, String reason) {}
record DetailCodeUpsertRequest(String codeValue, String codeName, String parentCodeValue, Integer sortOrder, Map<String,Object> additionalAttributes, LocalDate validFrom, LocalDate validTo, String reason) {}
