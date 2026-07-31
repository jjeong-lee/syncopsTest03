package kr.ac.knue.fpe.common;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CommonMapper {
  Map<String, Object> findUserByLoginId(String loginId);
  Map<String, Object> findUserBySession(String sessionId);
  List<String> findActiveRoleCodes(String userId);
  List<Map<String, Object>> listUsers(Map<String, Object> params);
  Map<String, Object> findUser(String userId);
  int updateUserUsage(@Param("userId") String userId, @Param("systemUseYn") String systemUseYn);
  int revokeUserRoles(String userId);
  int insertUserRole(@Param("userId") String userId, @Param("roleCode") String roleCode, @Param("approverUserId") String approverUserId);
  List<Map<String, Object>> listOrganizations(Map<String, Object> params);
  List<Map<String, Object>> listOrganizationTree(Map<String, Object> params);
  int insertOrganizationRelation(Map<String, Object> params);
  List<Map<String, Object>> listRoles(Map<String, Object> params);
  Map<String, Object> findRole(String roleCode);
  int insertRole(Map<String, Object> params);
  int updateRole(Map<String, Object> params);
  List<Map<String, Object>> listUserRoles(Map<String, Object> params);
  Map<String, Object> findUserRole(String assignmentId);
  int insertUserRoleAssignment(Map<String, Object> params);
  int revokeUserRole(@Param("assignmentId") String assignmentId);
  List<Map<String, Object>> listMenus(Map<String, Object> params);
  Map<String, Object> findMenu(String menuId);
  int insertMenu(Map<String, Object> params);
  int updateMenu(Map<String, Object> params);
  int updateMenuParent(Map<String, Object> params);
  int updateMenuOrder(@Param("menuId") String menuId, @Param("displayOrder") int displayOrder);
  List<Map<String, Object>> listMenuPermissions(Map<String, Object> params);
  int upsertMenuPermission(Map<String, Object> params);
  List<Map<String, Object>> listCodeGroups(Map<String, Object> params);
  Map<String, Object> findCodeGroup(String groupId);
  int insertCodeGroup(Map<String, Object> params);
  int updateCodeGroup(Map<String, Object> params);
  List<Map<String, Object>> listDetailCodes(Map<String, Object> params);
  Map<String, Object> findDetailCode(@Param("groupId") String groupId, @Param("codeValue") String codeValue);
  int insertDetailCode(Map<String, Object> params);
  int updateDetailCode(Map<String, Object> params);
  int insertSession(@Param("sessionId") String sessionId, @Param("userId") String userId);
  int revokeSession(String sessionId);
  int insertHistory(Map<String, Object> params);
}
