package ac.knue.fpe.service;

import ac.knue.fpe.persistence.FoundationMapper;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class FoundationService {
  private final FoundationMapper mapper;

  public FoundationService(FoundationMapper mapper) {
    this.mapper = mapper;
  }

  public Map<String, Object> findUserByLoginId(String loginId) {
    return row(mapper.findUserByLoginId(loginId));
  }

  public List<String> findActiveRoleCodes(String userId) {
    return mapper.findActiveRoleCodes(userId);
  }

  public Map<String, Object> findCurrentUser(String userId) {
    return row(mapper.findCurrentUser(userId));
  }

  public void createSession(String sessionId, String userId) {
    mapper.createSession(sessionId, userId);
  }

  public void revokeSession(String sessionId) {
    mapper.revokeSession(sessionId);
  }

  public List<Map<String, Object>> selectCurrentMenus(String userId, String level, String parentMenuId) {
    return rows(mapper.selectCurrentMenus(userId, level, parentMenuId));
  }

  public List<Map<String, Object>> searchUsers(Map<String, Object> params) {
    return rows(mapper.searchUsers(params));
  }

  public Map<String, Object> findUser(String userId) {
    return row(mapper.findUser(userId));
  }

  public void updateUserAdministration(String userId, String systemUseYn) {
    mapper.updateUserAdministration(userId, systemUseYn);
  }

  public void revokeUserRoles(String userId) {
    mapper.revokeUserRoles(userId);
  }

  public void insertUserRole(String userRoleId, String userId, String roleCode, String assignmentType, String validFrom, String validTo, String approverUserId) {
    mapper.insertUserRole(userRoleId, userId, roleCode, assignmentType, validFrom, validTo, approverUserId);
  }

  public List<Map<String, Object>> listOrganizations(Map<String, Object> params) {
    return rows(mapper.listOrganizations(params));
  }

  public List<Map<String, Object>> listOrganizationTree(String baseDate, String rootOrganizationCode) {
    return rows(mapper.listOrganizationTree(baseDate, rootOrganizationCode));
  }

  public Map<String, Object> findOrganization(String organizationId) {
    return row(mapper.findOrganization(organizationId));
  }

  public void updateOrganizationRelationship(Map<String, Object> params) {
    mapper.updateOrganizationRelationship(params);
  }

  public List<Map<String, Object>> listRoles(String useYn) {
    return rows(mapper.listRoles(useYn));
  }

  public Map<String, Object> findRole(String roleCode) {
    return row(mapper.findRole(roleCode));
  }

  public void updateRole(Map<String, Object> params) {
    mapper.updateRole(params);
  }

  public List<Map<String, Object>> listUserRoles(Map<String, Object> params) {
    return rows(mapper.listUserRoles(params));
  }

  public Map<String, Object> findUserRole(String userRoleId) {
    return row(mapper.findUserRole(userRoleId));
  }

  public void revokeUserRole(String userRoleId, String revokeDate) {
    mapper.revokeUserRole(userRoleId, revokeDate);
  }

  public List<Map<String, Object>> listMenuPermissions(Map<String, Object> params) {
    return rows(mapper.listMenuPermissions(params));
  }

  public void upsertMenuPermission(Map<String, Object> params) {
    if (mapper.updateMenuPermissionDecision(params) == 0) {
      mapper.insertMenuPermission(params);
    }
  }

  public List<Map<String, Object>> listMenuTree(Boolean includeInactive, String level) {
    return rows(mapper.listMenuTree(includeInactive, level));
  }

  public Map<String, Object> findMenu(String menuId) {
    return row(mapper.findMenu(menuId));
  }

  public void updateMenuStructure(Map<String, Object> params) {
    mapper.updateMenuStructure(params);
  }

  public void updateMenuSort(String menuId, int sortOrder) {
    mapper.updateMenuSort(menuId, sortOrder);
  }

  public void updateMenuInfo(Map<String, Object> params) {
    mapper.updateMenuInfo(params);
  }

  public List<Map<String, Object>> listCodeGroups(Map<String, Object> params) {
    return rows(mapper.listCodeGroups(params));
  }

  public Map<String, Object> findCodeGroup(String groupId) {
    return row(mapper.findCodeGroup(groupId));
  }

  public void insertCodeGroup(Map<String, Object> params) {
    mapper.insertCodeGroup(params);
  }

  public void updateCodeGroup(Map<String, Object> params) {
    mapper.updateCodeGroup(params);
  }

  public List<Map<String, Object>> listDetailCodes(Map<String, Object> params) {
    return rows(mapper.listDetailCodes(params));
  }

  public Map<String, Object> findDetailCode(String detailCodeId) {
    return row(mapper.findDetailCode(detailCodeId));
  }

  public void insertDetailCode(Map<String, Object> params) {
    mapper.insertDetailCode(params);
  }

  public void updateDetailCode(Map<String, Object> params) {
    mapper.updateDetailCode(params);
  }

  public void insertChangeHistory(String historyId, String entityName, String entityId, String beforeValue, String afterValue, String changedBy, String changeReason) {
    mapper.insertChangeHistory(historyId, entityName, entityId, beforeValue, afterValue, changedBy, changeReason);
  }

  private List<Map<String, Object>> rows(List<Map<String, Object>> rows) {
    return rows.stream().map(this::row).toList();
  }

  private Map<String, Object> row(Map<String, Object> row) {
    if (row == null) return null;
    Map<String, Object> normalized = new LinkedHashMap<>();
    row.forEach((key, value) -> normalized.put(camel(String.valueOf(key)), value));
    return normalized;
  }

  private String camel(String key) {
    String lower = key.toLowerCase();
    StringBuilder out = new StringBuilder();
    boolean upperNext = false;
    for (char ch : lower.toCharArray()) {
      if (ch == '_') {
        upperNext = true;
      } else if (upperNext) {
        out.append(Character.toUpperCase(ch));
        upperNext = false;
      } else {
        out.append(ch);
      }
    }
    return out.toString();
  }
}
