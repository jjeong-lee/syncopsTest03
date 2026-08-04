package kr.ac.knue.fpe.persistence;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ManagementMapper {
    Map<String, Object> findUserByUsername(@Param("username") String username);
    Map<String, Object> findActiveSession(@Param("sessionId") String sessionId);
    void insertSession(@Param("sessionId") String sessionId, @Param("userId") String userId, @Param("expiresAt") OffsetDateTime expiresAt);
    void touchSession(@Param("sessionId") String sessionId);
    void logoutSession(@Param("sessionId") String sessionId);
    List<String> selectRolesForUser(@Param("userId") String userId);
    List<Map<String, Object>> selectMenusForUser(@Param("userId") String userId);
    List<Map<String, Object>> searchReadonlyStaff(@Param("staffNo") String staffNo, @Param("staffName") String staffName, @Param("organizationCode") String organizationCode);
    List<Map<String, Object>> searchUsers(Map<String, Object> params);
    void updateUserUsage(@Param("userId") String userId, @Param("systemUseYn") String systemUseYn, @Param("changeReason") String changeReason);
    void endManualUserRoles(@Param("userId") String userId, @Param("changeReason") String changeReason);
    void insertUserRole(@Param("userId") String userId, @Param("roleCode") String roleCode, @Param("assignmentType") String assignmentType, @Param("validFrom") LocalDate validFrom, @Param("validTo") LocalDate validTo, @Param("approvedBy") String approvedBy, @Param("changeReason") String changeReason);
    List<Map<String, Object>> searchOrganizations(Map<String, Object> params);
    List<Map<String, Object>> organizationTree();
    void updateOrganizationRelation(@Param("organizationCode") String organizationCode, @Param("parentOrganizationCode") String parentOrganizationCode, @Param("effectiveStartDate") LocalDate effectiveStartDate, @Param("effectiveEndDate") LocalDate effectiveEndDate, @Param("relationChangeReason") String relationChangeReason);
    List<Map<String, Object>> listRoles(@Param("filter") String filter);
    void upsertRole(Map<String, Object> body);
    void updateRole(@Param("roleCode") String roleCode, Map<String, Object> body);
    List<Map<String, Object>> listUserRoles(Map<String, Object> params);
    Map<String, Object> findUserRole(@Param("userRoleId") Long userRoleId);
    void revokeUserRole(@Param("userRoleId") Long userRoleId, @Param("validTo") LocalDate validTo, @Param("changeReason") String changeReason);
    List<Map<String, Object>> menuTree();
    List<Map<String, Object>> listMenus(Map<String, Object> params);
    void insertMenu(@Param("menuId") String menuId, Map<String, Object> body);
    void updateMenu(@Param("menuId") String menuId, Map<String, Object> body);
    void updateMenuHierarchy(@Param("menuId") String menuId, @Param("parentMenuId") String parentMenuId, @Param("displayOrder") Integer displayOrder);
    List<Map<String, Object>> listMenuPermissions(Map<String, Object> params);
    void upsertMenuPermission(Map<String, Object> permission);
    List<Map<String, Object>> listCodeGroups(Map<String, Object> params);
    void insertCodeGroup(Map<String, Object> body);
    void updateCodeGroup(@Param("groupId") String groupId, Map<String, Object> body);
    List<Map<String, Object>> listDetailCodes(Map<String, Object> params);
    void insertDetailCode(@Param("groupId") String groupId, Map<String, Object> body);
    void updateDetailCode(@Param("groupId") String groupId, @Param("codeValue") String codeValue, Map<String, Object> body);
}
