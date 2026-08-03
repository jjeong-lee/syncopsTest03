package kr.ac.knue.fpe;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;

@Service
public class CommonService {
    private final CommonMapper mapper;
    private final PasswordHasher hasher;
    private final ObjectMapper objectMapper;
    private final long sessionHours;

    public CommonService(CommonMapper mapper, PasswordHasher hasher, ObjectMapper objectMapper, @Value("${app.session-hours:8}") long sessionHours) {
        this.mapper = mapper; this.hasher = hasher; this.objectMapper = objectMapper; this.sessionHours = sessionHours;
    }

    public Map<String,Object> health() {
        return Map.of(
            "status", "UP",
            "service", "faculty-performance-common",
            "dataModelContracts", Map.of(
                "entityTableContracts", mapper.countEntityTableContracts(),
                "screenApiReferences", mapper.countScreenApiReferences()
            )
        );
    }

    @Transactional
    public LoginResult login(LoginRequest request) {
        requireText(request.username(), "username"); requireText(request.password(), "password");
        var account = mapper.findLoginAccount(request.username());
        if (account == null || !hasher.sha256(request.password()).equals(account.get("passwordHash"))) throw new ForbiddenException("credential이 일치하지 않습니다.");
        if (!Boolean.TRUE.equals(account.get("systemEnabled"))) throw new ForbiddenException("비활성 계정은 로그인할 수 없습니다.");
        String sessionId = UUID.randomUUID().toString().replace("-", "");
        mapper.insertSession(sessionId, (String) account.get("userId"), OffsetDateTime.now().plusHours(sessionHours));
        return new LoginResult(sessionId, currentUser((String) account.get("userId"), (String) account.get("username"), true));
    }

    @Transactional public void logout(String sessionId) { mapper.revokeSession(sessionId); }
    public CurrentUser currentUser(Map<String,Object> row) { return currentUser((String) row.get("userId"), (String) row.get("username"), (Boolean) row.get("systemEnabled")); }
    private CurrentUser currentUser(String userId, String username, boolean enabled) { return new CurrentUser(userId, username, mapper.findActiveRoleCodes(userId), enabled); }

    public PageResponse<Map<String,Object>> page(List<Map<String,Object>> items, Map<String,Object> p) { int size=(Integer)p.get("size"); int page=(Integer)p.get("page"); return new PageResponse<>(items,page,size,items.size(),items.size()==0?0:1); }
    public Map<String,Object> params(Map<String,String> request) { Map<String,Object> p=new HashMap<>(request); int page=parseInt(request.get("page"),0); int size=Math.min(parseInt(request.get("size"),20),200); p.put("page",page); p.put("size",size); p.put("offset",page*size); for (var e: request.entrySet()) if ("true".equalsIgnoreCase(e.getValue())||"false".equalsIgnoreCase(e.getValue())) p.put(e.getKey(), Boolean.parseBoolean(e.getValue())); return p; }

    public PageResponse<Map<String,Object>> listUsers(Map<String,String> q){ var p=params(q); return page(mapper.listUsers(p),p); }
    public PageResponse<Map<String,Object>> listOrganizations(Map<String,String> q){ var p=params(q); return page(mapper.listOrganizations(p),p); }
    public PageResponse<Map<String,Object>> listRoles(Map<String,String> q){ var p=params(q); return page(mapper.listRoles(p),p); }
    public PageResponse<Map<String,Object>> listUserRoles(Map<String,String> q){ var p=params(q); return page(mapper.listUserRoles(p),p); }
    public PageResponse<Map<String,Object>> listMenuPermissions(Map<String,String> q){ var p=params(q); return page(mapper.listMenuPermissions(p),p); }
    public PageResponse<Map<String,Object>> listMenus(Map<String,String> q){ var p=params(q); return page(mapper.listMenus(p),p); }
    public PageResponse<Map<String,Object>> listCodeGroups(Map<String,String> q){ var p=params(q); return page(mapper.listCodeGroups(p),p); }
    public PageResponse<Map<String,Object>> listDetailCodes(String groupId, Map<String,String> q){ var p=params(q); p.put("groupId", groupId); return page(mapper.listDetailCodes(p),p); }

    @Transactional
    public Map<String,Object> updateUserUsage(String actor, String userId, UserUsageUpdateRequest request) {
        if (request.systemEnabled()==null) throw new BadRequestException("systemEnabled", "사용여부는 필수입니다."); requireText(request.reason(), "reason");
        var before = mapper.findUserAccount(userId); if (before==null) throw new NotFoundException("사용자를 찾을 수 없습니다.");
        mapper.updateUserUsage(userId, request.systemEnabled());
        history("user_accounts", userId, "UPDATE", before, Map.of("systemEnabled", request.systemEnabled()), actor, request.reason());
        return listUsers(Map.of("staffNo", userId)).items().stream().findFirst().orElseThrow();
    }

    @Transactional
    public Map<String,Object> replaceUserRoles(String actor, String userId, UserRolesReplaceRequest request) {
        requireText(request.reason(), "reason"); if (request.roles()==null) throw new BadRequestException("roles", "역할 목록은 필수입니다.");
        mapper.revokeActiveRolesForUser(userId);
        for (RoleAssignmentRequest r: request.roles()) { validateRole(r.roleCode()); requireText(r.assignmentSource(), "assignmentSource"); requireDateOrder(r.effectiveStartDate(), r.effectiveEndDate(), "effectiveEndDate"); requireText(r.approvedBy(), "approvedBy"); mapper.insertUserRole(new RoleInsert(null,userId,r.roleCode(),r.assignmentSource(),r.effectiveStartDate(),r.effectiveEndDate(),r.approvedBy())); }
        history("user_roles", userId, "UPDATE", Map.of("status","previous ACTIVE"), Map.of("roles", request.roles()), actor, request.reason());
        return listUsers(Map.of("staffNo", userId)).items().stream().findFirst().orElseThrow();
    }

    @Transactional public Map<String,Object> updateOrganization(String actor, String code, OrganizationRelationshipUpdateRequest r){ requireText(r.reason(),"reason"); if (code.equals(r.parentOrganizationCode())) throw new BadRequestException("parentOrganizationCode","자기 자신을 상위조직으로 지정할 수 없습니다."); requireDateOrder(r.effectiveStartDate(),r.effectiveEndDate(),"effectiveEndDate"); mapper.updateOrganization(code,r.parentOrganizationCode(),r.effectiveStartDate(),r.effectiveEndDate()); history("organizations",code,"UPDATE",Map.of(),Map.of("parentOrganizationCode",Objects.toString(r.parentOrganizationCode(),"")),actor,r.reason()); return listOrganizations(Map.of("organizationCode",code)).items().stream().findFirst().orElseThrow(); }
    @Transactional public Map<String,Object> createRole(String actor, RoleUpsertRequest r){ validateRole(r.roleCode()); requireText(r.roleName(),"roleName"); requireText(r.purpose(),"purpose"); requireText(r.reason(),"reason"); mapper.insertRole(r); history("roles",r.roleCode(),"CREATE",null,r,actor,r.reason()); return listRoles(Map.of("roleCode",r.roleCode())).items().stream().findFirst().orElseThrow(); }
    @Transactional public Map<String,Object> updateRole(String actor, String roleCode, RoleUpsertRequest r){ validateRole(roleCode); if(r.roleCode()!=null && !roleCode.equals(r.roleCode())) throw new BadRequestException("roleCode","roleCode는 path 값과 같아야 합니다."); requireText(r.roleName(),"roleName"); requireText(r.purpose(),"purpose"); requireText(r.reason(),"reason"); mapper.updateRole(roleCode,r.roleName(),r.purpose(),r.grantCriteria(),r.defaultDataScope()); history("roles",roleCode,"UPDATE",Map.of(),r,actor,r.reason()); return listRoles(Map.of("roleCode",roleCode)).items().stream().findFirst().orElseThrow(); }
    @Transactional public Map<String,Object> grantUserRole(String actor, GrantUserRoleRequest r){ requireText(r.userId(),"userId"); validateRole(r.roleCode()); requireText(r.assignmentSource(),"assignmentSource"); requireDateOrder(r.effectiveStartDate(),r.effectiveEndDate(),"effectiveEndDate"); requireText(r.approvedBy(),"approvedBy"); requireText(r.reason(),"reason"); var row=new RoleInsert(null,r.userId(),r.roleCode(),r.assignmentSource(),r.effectiveStartDate(),r.effectiveEndDate(),r.approvedBy()); mapper.insertUserRole(row); history("user_roles",r.userId()+":"+r.roleCode(),"CREATE",null,r,actor,r.reason()); return mapper.listUserRoles(params(Map.of("userId",r.userId(),"roleCode",r.roleCode()))).get(0); }
    @Transactional public Map<String,Object> revokeUserRole(String actor, Long id, String reason){ requireText(reason,"reason"); mapper.revokeUserRole(id); history("user_roles",String.valueOf(id),"REVOKE",Map.of("status","ACTIVE"),Map.of("status","REVOKED"),actor,reason); return mapper.listUserRoles(params(Map.of())).stream().filter(x -> Objects.equals(((Number)x.get("assignmentId")).longValue(), id)).findFirst().orElse(Map.of("assignmentId",id,"status","REVOKED")); }
    @Transactional public PageResponse<Map<String,Object>> saveMenuPermissions(String actor, SaveMenuPermissionsRequest r){ requireText(r.principalType(),"principalType"); requireText(r.principalId(),"principalId"); requireText(r.reason(),"reason"); if(r.permissions()==null) throw new BadRequestException("permissions","권한 목록은 필수입니다."); mapper.deletePermissionsForPrincipal(r.principalType(),r.principalId()); for(MenuPermissionItem i:r.permissions()){ if(i.menuId()==null) throw new BadRequestException("menuId","메뉴 ID는 필수입니다."); if(!Set.of("ALLOW","DENY").contains(i.permissionEffect())) throw new BadRequestException("permissionEffect","ALLOW 또는 DENY만 가능합니다."); mapper.insertPermission(r.principalType(),r.principalId(),i.menuId(),i.permissionEffect()); } history("menu_permissions",r.principalType()+":"+r.principalId(),"UPDATE",Map.of(),r,actor,r.reason()); return listMenuPermissions(Map.of("principalType",r.principalType(),"principalId",r.principalId())); }
    @Transactional public Map<String,Object> updateMenuStructure(String actor, Long id, MenuStructureUpdateRequest r){ if(r.displayOrder()==null) throw new BadRequestException("displayOrder","표시순서는 필수입니다."); requireText(r.reason(),"reason"); if(id.equals(r.parentMenuId())) throw new BadRequestException("parentMenuId","자기 자신을 parent로 지정할 수 없습니다."); mapper.updateMenuStructure(id,r.parentMenuId(),r.displayOrder()); history("menus",String.valueOf(id),"UPDATE",Map.of(),r,actor,r.reason()); return listMenus(Map.of()).items().stream().filter(m -> Objects.equals(((Number)m.get("menuId")).longValue(), id)).findFirst().orElseThrow(); }
    @Transactional public Map<String,Object> createMenu(String actor, MenuUpsertRequest r){ validateMenu(r); var row=new MenuInsert(null,r.parentMenuId(),r.menuLevel(),r.menuName(),r.screenId(),r.url(),r.icon(),r.businessCategory(),r.description(),r.displayOrder()); mapper.insertMenu(row); history("menus",r.menuName(),"CREATE",null,r,actor,r.reason()); return listMenus(Map.of()).items().stream().filter(m -> Objects.equals(m.get("menuName"), r.menuName()) && Objects.equals(m.get("screenId"), r.screenId())).findFirst().orElseThrow(); }
    @Transactional public Map<String,Object> updateMenu(String actor, Long id, MenuUpsertRequest r){ validateMenu(r); mapper.updateMenu(id,r.parentMenuId(),r.menuLevel(),r.menuName(),r.screenId(),r.url(),r.icon(),r.businessCategory(),r.description(),r.displayOrder()); history("menus",String.valueOf(id),"UPDATE",Map.of(),r,actor,r.reason()); return listMenus(Map.of()).items().stream().filter(m -> Objects.equals(((Number)m.get("menuId")).longValue(), id)).findFirst().orElseThrow(); }
    @Transactional public Map<String,Object> createCodeGroup(String actor, CodeGroupUpsertRequest r){ requireText(r.groupId(),"groupId"); requireText(r.groupName(),"groupName"); requireText(r.reason(),"reason"); mapper.insertCodeGroup(r); history("code_groups",r.groupId(),"CREATE",null,r,actor,r.reason()); return listCodeGroups(Map.of("groupId",r.groupId())).items().get(0); }
    @Transactional public Map<String,Object> updateCodeGroup(String actor, String groupId, CodeGroupUpsertRequest r){ requireText(r.groupName(),"groupName"); requireText(r.reason(),"reason"); mapper.updateCodeGroup(groupId,r.groupName(),r.description(),r.managingDepartment()); history("code_groups",groupId,"UPDATE",Map.of(),r,actor,r.reason()); return listCodeGroups(Map.of("groupId",groupId)).items().get(0); }
    @Transactional public Map<String,Object> createDetailCode(String actor, String groupId, DetailCodeUpsertRequest r){ validateDetail(r); var row=detailRow(groupId,r); mapper.insertDetailCode(row); history("detail_codes",groupId+":"+r.codeValue(),"CREATE",null,r,actor,r.reason()); return listDetailCodes(groupId,Map.of("codeValue",r.codeValue())).items().get(0); }
    @Transactional public Map<String,Object> updateDetailCode(String actor, String groupId, String codeValue, DetailCodeUpsertRequest r){ validateDetail(r); var row=new DetailCodeInsert(groupId,codeValue,r.codeName(),r.parentCodeValue(),r.sortOrder(),json(r.additionalAttributes()),r.validFrom(),r.validTo()); mapper.updateDetailCode(row); history("detail_codes",groupId+":"+codeValue,"UPDATE",Map.of(),r,actor,r.reason()); return listDetailCodes(groupId,Map.of("codeValue",codeValue)).items().get(0); }

    private void validateMenu(MenuUpsertRequest r){ requireText(r.menuLevel(),"menuLevel"); requireText(r.menuName(),"menuName"); if(r.displayOrder()==null) throw new BadRequestException("displayOrder","표시순서는 필수입니다."); requireText(r.reason(),"reason"); if("SCREEN".equals(r.menuLevel()) && (isBlank(r.screenId())||isBlank(r.url()))) throw new BadRequestException("url","SCREEN 메뉴는 화면ID와 URL이 필요합니다."); }
    private void validateDetail(DetailCodeUpsertRequest r){ requireText(r.codeValue(),"codeValue"); requireText(r.codeName(),"codeName"); if(r.sortOrder()==null) throw new BadRequestException("sortOrder","정렬순서는 필수입니다."); requireOptionalDateOrder(r.validFrom(),r.validTo(),"validTo"); requireText(r.reason(),"reason"); }
    private DetailCodeInsert detailRow(String groupId, DetailCodeUpsertRequest r){ return new DetailCodeInsert(groupId,r.codeValue(),r.codeName(),r.parentCodeValue(),r.sortOrder(),json(r.additionalAttributes()),r.validFrom(),r.validTo()); }
    private void history(String entity,String key,String type,Object before,Object after,String actor,String reason){ mapper.insertHistory(entity,key,type,json(before),json(after),actor,reason); }
    private String json(Object v){ if(v==null) return null; try { return objectMapper.writeValueAsString(v); } catch (JsonProcessingException e) { throw new IllegalStateException(e); } }
    private void requireText(String v,String field){ if(isBlank(v)) throw new BadRequestException(field, field+"은(는) 필수입니다."); }
    private boolean isBlank(String v){ return v==null || v.isBlank(); }
    private int parseInt(String v,int d){ try{return v==null?d:Integer.parseInt(v);}catch(Exception e){return d;} }
    private void requireDateOrder(LocalDate start, LocalDate end, String field){ if(start==null) throw new BadRequestException("effectiveStartDate","시작일은 필수입니다."); if(end!=null && end.isBefore(start)) throw new BadRequestException(field,"종료일은 시작일보다 빠를 수 없습니다."); }
    private void requireOptionalDateOrder(LocalDate start, LocalDate end, String field){ if(start!=null && end!=null && end.isBefore(start)) throw new BadRequestException(field,"종료일은 시작일보다 빠를 수 없습니다."); }
    private void validateRole(String code){ if(!Set.of("R01","R02","R03","R04","R05","R06","R07","R08","R09").contains(code)) throw new BadRequestException("roleCode","R01~R09 역할코드만 허용합니다."); }
}
record LoginResult(String sessionId, CurrentUser currentUser) {}
