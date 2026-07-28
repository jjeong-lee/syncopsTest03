package kr.ac.knue.fpe.admin;

import kr.ac.knue.fpe.common.api.ApiException;
import kr.ac.knue.fpe.common.api.PageResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
public class AdminService {
    private static final Logger log = LoggerFactory.getLogger(AdminService.class);
    private final AdminMapper mapper;
    public AdminService(AdminMapper mapper) { this.mapper = mapper; }
    public PageResult<Map<String,Object>> page(List<Map<String,Object>> items, Map<String,Object> p) { return PageResult.of(items, (int)p.get("page"), (int)p.get("size")); }
    public Map<String,Object> params(Map<String,String> raw) { Map<String,Object> p=new HashMap<>(raw); int page=parse(raw.get("page"),0); int size=Math.min(parse(raw.get("size"),20),100); p.put("page",page); p.put("size",size); p.put("offset",page*size); p.replaceAll((k,v)-> v instanceof String s && s.isBlank()? null : v); return p; }
    private int parse(String s, int d) { try { return s==null?d:Integer.parseInt(s); } catch(Exception e) { return d; } }
    public List<Map<String,Object>> tree(List<Map<String,Object>> rows, String idKey, String parentKey, String childrenKey) {
        Map<Object, Map<String,Object>> byId=new LinkedHashMap<>(); rows.forEach(r -> { r.put(childrenKey, new ArrayList<>()); byId.put(r.get(idKey), r); });
        List<Map<String,Object>> roots=new ArrayList<>();
        for (Map<String,Object> row: rows) { Object parent=row.get(parentKey); if (parent==null || !byId.containsKey(parent)) roots.add(row); else ((List<Map<String,Object>>)byId.get(parent).get(childrenKey)).add(row); }
        return roots;
    }
    @Transactional public Map<String,Object> updateUserUsage(String userId, Map<String,Object> body, String requestId) { body.put("userId", userId); require(body,"reason"); if(mapper.updateUserUsage(body)==0) throw notFound(); log.info("event=admin_mutation action=updateUserUsage user_id={} request_id={}", userId, requestId); return mapper.userById(userId); }
    @Transactional public Map<String,Object> updateOrganizationRelation(String code, Map<String,Object> body, String requestId) { body.put("organizationCode", code); require(body,"reason"); validatePeriod(body); if(mapper.updateOrganizationRelation(body)==0) throw notFound(); log.info("event=admin_mutation action=updateOrganizationRelation organization_code={} request_id={}", code, requestId); return mapper.organizationByCode(code); }
    @Transactional public Map<String,Object> createRole(Map<String,Object> body, String requestId) { require(body,"roleCode","roleName","rolePurpose","defaultDataScope","useYn"); try { mapper.createRole(body); } catch(DuplicateKeyException e) { throw conflict("역할코드가 이미 존재합니다."); } log.info("event=admin_mutation action=createRole role_code={} request_id={}", body.get("roleCode"), requestId); return mapper.roleByCode(String.valueOf(body.get("roleCode"))); }
    @Transactional public Map<String,Object> updateRole(String code, Map<String,Object> body, String requestId) { body.put("roleCode", code); require(body,"roleName","rolePurpose","defaultDataScope","useYn"); if(mapper.updateRole(body)==0) throw notFound(); log.info("event=admin_mutation action=updateRole role_code={} request_id={}", code, requestId); return mapper.roleByCode(code); }
    @Transactional public Map<String,Object> assignUserRole(Map<String,Object> body, String requestId) { require(body,"userId","roleCode","roleSource","validFrom","reason"); validatePeriod(body); body.put("userRoleId", UUID.randomUUID().toString()); mapper.assignUserRole(body); log.info("event=admin_mutation action=assignUserRole role_code={} request_id={}", body.get("roleCode"), requestId); return mapper.userRoleById(String.valueOf(body.get("userRoleId"))); }
    @Transactional public Map<String,Object> revokeUserRole(String id, Map<String,Object> body, String requestId) { body.put("userRoleId", id); if(mapper.revokeUserRole(body)==0) throw conflict("회수할 활성 역할이 없습니다."); log.info("event=admin_mutation action=revokeUserRole user_role_id={} request_id={}", id, requestId); return mapper.userRoleById(id); }
    @Transactional public Map<String,Object> createMenu(Map<String,Object> body, String requestId) { require(body,"menuLevel","displayOrder","menuName","useYn","reason"); body.put("menuId", UUID.randomUUID().toString()); mapper.createMenu(body); log.info("event=admin_mutation action=createMenu menu_name={} request_id={}", body.get("menuName"), requestId); return mapper.menuById(String.valueOf(body.get("menuId"))); }
    @Transactional public Map<String,Object> updateMenu(String id, Map<String,Object> body, String requestId) { body.put("menuId", id); require(body,"menuLevel","displayOrder","menuName","useYn","reason"); if (body.get("urlPath") != null && !String.valueOf(body.get("urlPath")).startsWith("/")) throw bad("URL은 relative route여야 합니다."); if(mapper.updateMenu(body)==0) throw notFound(); log.info("event=admin_mutation action=updateMenu menu_id={} request_id={}", id, requestId); return mapper.menuById(id); }
    @Transactional public void reorderMenus(Map<String,Object> body, String requestId) { require(body,"orders","reason"); List<Map<String,Object>> orders=(List<Map<String,Object>>) body.get("orders"); for(Map<String,Object> row: orders) mapper.updateMenuOrder(row); log.info("event=admin_mutation action=reorderMenus count={} request_id={}", orders.size(), requestId); }
    @Transactional public void savePermissions(Map<String,Object> body, String requestId) { require(body,"targetType","targetId","permissions","reason"); mapper.deletePermissions(body); for(Map<String,Object> row: (List<Map<String,Object>>)body.get("permissions")) { row.put("targetType", body.get("targetType")); row.put("targetId", body.get("targetId")); mapper.insertPermission(row); } log.info("event=admin_mutation action=saveMenuPermissions target_type={} target_id={} request_id={}", body.get("targetType"), body.get("targetId"), requestId); }
    @Transactional public Map<String,Object> createCodeGroup(Map<String,Object> body, String requestId) { require(body,"groupId","groupName","useYn","reason"); try { mapper.createCodeGroup(body); } catch(DuplicateKeyException e) { throw conflict("코드그룹이 이미 존재합니다."); } log.info("event=admin_mutation action=createCodeGroup group_id={} request_id={}", body.get("groupId"), requestId); return mapper.codeGroupById(String.valueOf(body.get("groupId"))); }
    @Transactional public Map<String,Object> updateCodeGroup(String id, Map<String,Object> body, String requestId) { body.put("groupId", id); require(body,"groupName","useYn","reason"); if(mapper.updateCodeGroup(body)==0) throw notFound(); log.info("event=admin_mutation action=updateCodeGroup group_id={} request_id={}", id, requestId); return mapper.codeGroupById(id); }
    @Transactional public Map<String,Object> createDetailCode(Map<String,Object> body, String requestId) { require(body,"groupId","codeValue","codeName","sortOrder","useYn","reason"); validatePeriod(body); body.put("detailCodeId", UUID.randomUUID().toString()); body.putIfAbsent("additionalAttributes", "{}"); try { mapper.createDetailCode(body); } catch(DuplicateKeyException e) { throw conflict("상세코드가 이미 존재합니다."); } log.info("event=admin_mutation action=createDetailCode group_id={} code_value={} request_id={}", body.get("groupId"), body.get("codeValue"), requestId); return mapper.detailCodeById(String.valueOf(body.get("detailCodeId"))); }
    @Transactional public Map<String,Object> updateDetailCode(String id, Map<String,Object> body, String requestId) { body.put("detailCodeId", id); require(body,"groupId","codeValue","codeName","sortOrder","useYn","reason"); validatePeriod(body); body.putIfAbsent("additionalAttributes", "{}"); if(mapper.updateDetailCode(body)==0) throw notFound(); log.info("event=admin_mutation action=updateDetailCode detail_code_id={} request_id={}", id, requestId); return mapper.detailCodeById(id); }
    private void validatePeriod(Map<String,Object> body) { Object from=body.getOrDefault("validFrom", body.get("effectiveStartDate")); Object to=body.getOrDefault("validTo", body.get("effectiveEndDate")); if(from!=null && to!=null && !String.valueOf(to).isBlank() && LocalDate.parse(String.valueOf(to)).isBefore(LocalDate.parse(String.valueOf(from)))) throw bad("종료일은 시작일보다 빠를 수 없습니다."); }
    private void require(Map<String,Object> body, String... keys) { for(String k: keys) if(body.get(k)==null || String.valueOf(body.get(k)).isBlank()) throw bad(k + " 값은 필수입니다."); }
    private ApiException bad(String m) { return new ApiException(HttpStatus.BAD_REQUEST,"VALIDATION_ERROR",m); }
    private ApiException notFound() { return new ApiException(HttpStatus.NOT_FOUND,"NOT_FOUND","대상을 찾을 수 없습니다."); }
    private ApiException conflict(String m) { return new ApiException(HttpStatus.CONFLICT,"CONFLICT",m); }
}
