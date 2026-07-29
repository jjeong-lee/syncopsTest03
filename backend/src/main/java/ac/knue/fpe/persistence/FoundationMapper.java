package ac.knue.fpe.persistence;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FoundationMapper {
  Map<String, Object> findUserByLoginId(@Param("loginId") String loginId);
  String findSessionUserId(@Param("sessionId") String sessionId);
  List<String> findActiveRoleCodes(@Param("userId") String userId);
  Map<String, Object> findCurrentUser(@Param("userId") String userId);
  void createSession(@Param("sessionId") String sessionId, @Param("userId") String userId);
  void revokeSession(@Param("sessionId") String sessionId);
  List<Map<String, Object>> selectCurrentMenus(@Param("userId") String userId, @Param("level") String level, @Param("parentMenuId") String parentMenuId);
  List<Map<String, Object>> searchUsers(Map<String, Object> params);
  Map<String, Object> findUser(@Param("userId") String userId);
  void updateUserAdministration(@Param("userId") String userId, @Param("systemUseYn") String systemUseYn);
  void revokeUserRoles(@Param("userId") String userId);
  void insertUserRole(@Param("userRoleId") String userRoleId, @Param("userId") String userId, @Param("roleCode") String roleCode, @Param("assignmentType") String assignmentType, @Param("validFrom") String validFrom, @Param("validTo") String validTo, @Param("approverUserId") String approverUserId);
  List<Map<String, Object>> listOrganizations(Map<String, Object> params);
  List<Map<String, Object>> listOrganizationTree(@Param("baseDate") String baseDate, @Param("rootOrganizationCode") String rootOrganizationCode);
  Map<String, Object> findOrganization(@Param("organizationId") String organizationId);
  void updateOrganizationRelationship(Map<String, Object> params);
  List<Map<String, Object>> listRoles(@Param("useYn") String useYn);
  Map<String, Object> findRole(@Param("roleCode") String roleCode);
  void updateRole(Map<String, Object> params);
  List<Map<String, Object>> listUserRoles(Map<String, Object> params);
  Map<String, Object> findUserRole(@Param("userRoleId") String userRoleId);
  void revokeUserRole(@Param("userRoleId") String userRoleId, @Param("revokeDate") String revokeDate);
  List<Map<String, Object>> listMenuPermissions(Map<String, Object> params);
  int updateMenuPermissionDecision(Map<String, Object> params);
  void insertMenuPermission(Map<String, Object> params);
  List<Map<String, Object>> listMenuTree(@Param("includeInactive") Boolean includeInactive, @Param("level") String level);
  Map<String, Object> findMenu(@Param("menuId") String menuId);
  void updateMenuStructure(Map<String, Object> params);
  void updateMenuSort(@Param("menuId") String menuId, @Param("sortOrder") int sortOrder);
  void updateMenuInfo(Map<String, Object> params);
  List<Map<String, Object>> listCodeGroups(Map<String, Object> params);
  Map<String, Object> findCodeGroup(@Param("groupId") String groupId);
  void insertCodeGroup(Map<String, Object> params);
  void updateCodeGroup(Map<String, Object> params);
  List<Map<String, Object>> listDetailCodes(Map<String, Object> params);
  Map<String, Object> findDetailCode(@Param("detailCodeId") String detailCodeId);
  void insertDetailCode(Map<String, Object> params);
  void updateDetailCode(Map<String, Object> params);
  void insertChangeHistory(@Param("historyId") String historyId, @Param("entityName") String entityName, @Param("entityId") String entityId, @Param("beforeValue") String beforeValue, @Param("afterValue") String afterValue, @Param("changedBy") String changedBy, @Param("changeReason") String changeReason);
}
