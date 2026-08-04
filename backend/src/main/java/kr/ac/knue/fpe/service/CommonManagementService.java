package kr.ac.knue.fpe.service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import kr.ac.knue.fpe.persistence.ManagementMapper;

@Service
public class CommonManagementService {
    private final ManagementMapper mapper;

    public CommonManagementService(ManagementMapper mapper) {
        this.mapper = mapper;
    }

    public Map<String, Object> findUserByUsername(String username) { return mapper.findUserByUsername(username); }
    public Map<String, Object> findActiveSession(String sessionId) { return mapper.findActiveSession(sessionId); }
    public void insertSession(String sessionId, String userId, OffsetDateTime expiresAt) { mapper.insertSession(sessionId, userId, expiresAt); }
    public void touchSession(String sessionId) { mapper.touchSession(sessionId); }
    public void logoutSession(String sessionId) { mapper.logoutSession(sessionId); }
    public List<String> selectRolesForUser(String userId) { return mapper.selectRolesForUser(userId); }
    public List<Map<String, Object>> selectMenusForUser(String userId) { return mapper.selectMenusForUser(userId); }
    public List<Map<String, Object>> searchUsers(Map<String, Object> params) { return mapper.searchUsers(params); }
    public void updateUserUsage(String userId, String systemUseYn, String changeReason) { mapper.updateUserUsage(userId, systemUseYn, changeReason); }
    public void endManualUserRoles(String userId, String changeReason) { mapper.endManualUserRoles(userId, changeReason); }
    public void insertUserRole(String userId, String roleCode, String assignmentType, LocalDate validFrom, LocalDate validTo, String approvedBy, String changeReason) { mapper.insertUserRole(userId, roleCode, assignmentType, validFrom, validTo, approvedBy, changeReason); }
    public List<Map<String, Object>> searchOrganizations(Map<String, Object> params) { return mapper.searchOrganizations(params); }
    public List<Map<String, Object>> organizationTree() { return mapper.organizationTree(); }
    public void updateOrganizationRelation(String organizationCode, String parentOrganizationCode, LocalDate effectiveStartDate, LocalDate effectiveEndDate, String relationChangeReason) { mapper.updateOrganizationRelation(organizationCode, parentOrganizationCode, effectiveStartDate, effectiveEndDate, relationChangeReason); }
    public List<Map<String, Object>> listRoles(String filter) { return mapper.listRoles(filter); }
    public void upsertRole(Map<String, Object> body) { mapper.upsertRole(body); }
    public void updateRole(String roleCode, Map<String, Object> body) { mapper.updateRole(roleCode, body); }
    public List<Map<String, Object>> listUserRoles(Map<String, Object> params) { return mapper.listUserRoles(params); }
    public Map<String, Object> findUserRole(Long userRoleId) { return mapper.findUserRole(userRoleId); }
    public void revokeUserRole(Long userRoleId, LocalDate validTo, String changeReason) { mapper.revokeUserRole(userRoleId, validTo, changeReason); }
    public List<Map<String, Object>> menuTree() { return mapper.menuTree(); }
    public List<Map<String, Object>> listMenus(Map<String, Object> params) { return mapper.listMenus(params); }
    public void insertMenu(String menuId, Map<String, Object> body) { mapper.insertMenu(menuId, body); }
    public void updateMenu(String menuId, Map<String, Object> body) { mapper.updateMenu(menuId, body); }
    public void updateMenuHierarchy(String menuId, String parentMenuId, Integer displayOrder) { mapper.updateMenuHierarchy(menuId, parentMenuId, displayOrder); }
    public List<Map<String, Object>> listMenuPermissions(Map<String, Object> params) { return mapper.listMenuPermissions(params); }
    public void upsertMenuPermission(Map<String, Object> permission) { mapper.upsertMenuPermission(permission); }
    public List<Map<String, Object>> listCodeGroups(Map<String, Object> params) { return mapper.listCodeGroups(params); }
    public void insertCodeGroup(Map<String, Object> body) { mapper.insertCodeGroup(body); }
    public void updateCodeGroup(String groupId, Map<String, Object> body) { mapper.updateCodeGroup(groupId, body); }
    public List<Map<String, Object>> listDetailCodes(Map<String, Object> params) { return mapper.listDetailCodes(params); }
    public void insertDetailCode(String groupId, Map<String, Object> body) { mapper.insertDetailCode(groupId, body); }
    public void updateDetailCode(String groupId, String codeValue, Map<String, Object> body) { mapper.updateDetailCode(groupId, codeValue, body); }
}
