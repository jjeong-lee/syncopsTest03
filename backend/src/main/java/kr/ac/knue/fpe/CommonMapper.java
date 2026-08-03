package kr.ac.knue.fpe;

import org.apache.ibatis.annotations.*;
import org.apache.ibatis.jdbc.SQL;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface CommonMapper {
    @Select("select u.user_id as \"userId\", u.username as \"username\", u.system_enabled as \"systemEnabled\" from sessions s join user_accounts u on u.user_id=s.user_id where s.session_id=#{sessionId} and s.status='ACTIVE' and s.expires_at > now()")
    Map<String,Object> findCurrentUser(String sessionId);

    @Select("select role_code from user_roles where user_id=#{userId} and status='ACTIVE' and (effective_end_date is null or effective_end_date >= current_date) order by role_code")
    List<String> findActiveRoleCodes(String userId);

    @Select("select user_id as \"userId\", username, password_hash as \"passwordHash\", system_enabled as \"systemEnabled\" from user_accounts where username=#{username}")
    Map<String,Object> findLoginAccount(String username);

    @Select("select count(*) from entity_table_contract")
    long countEntityTableContracts();

    @Select("select count(*) from screen_a_p_i_reference")
    long countScreenApiReferences();

    @Insert("insert into sessions(session_id,user_id,status,created_at,expires_at,updated_at) values(#{sessionId},#{userId},'ACTIVE',now(),#{expiresAt},now())")
    void insertSession(@Param("sessionId") String sessionId, @Param("userId") String userId, @Param("expiresAt") OffsetDateTime expiresAt);

    @Update("update sessions set status='REVOKED', updated_at=now() where session_id=#{sessionId}")
    void revokeSession(String sessionId);

    @SelectProvider(type=SqlProvider.class, method="users")
    List<Map<String,Object>> listUsers(Map<String,Object> p);

    @SelectProvider(type=SqlProvider.class, method="organizations")
    List<Map<String,Object>> listOrganizations(Map<String,Object> p);

    @SelectProvider(type=SqlProvider.class, method="roles")
    List<Map<String,Object>> listRoles(Map<String,Object> p);

    @SelectProvider(type=SqlProvider.class, method="userRoles")
    List<Map<String,Object>> listUserRoles(Map<String,Object> p);

    @SelectProvider(type=SqlProvider.class, method="menuPermissions")
    List<Map<String,Object>> listMenuPermissions(Map<String,Object> p);

    @SelectProvider(type=SqlProvider.class, method="menus")
    List<Map<String,Object>> listMenus(Map<String,Object> p);

    @SelectProvider(type=SqlProvider.class, method="codeGroups")
    List<Map<String,Object>> listCodeGroups(Map<String,Object> p);

    @SelectProvider(type=SqlProvider.class, method="detailCodes")
    List<Map<String,Object>> listDetailCodes(Map<String,Object> p);

    @Select("select user_id as \"userId\", system_enabled as \"systemEnabled\" from user_accounts where user_id=#{userId}")
    Map<String,Object> findUserAccount(String userId);

    @Update("update user_accounts set system_enabled=#{systemEnabled}, updated_at=now() where user_id=#{userId}")
    void updateUserUsage(@Param("userId") String userId, @Param("systemEnabled") boolean systemEnabled);

    @Update("update user_roles set status='REVOKED', updated_at=now() where user_id=#{userId} and status='ACTIVE'")
    void revokeActiveRolesForUser(String userId);

    @Insert("insert into user_roles(user_id,role_code,assignment_source,effective_start_date,effective_end_date,approved_by,status,created_at,updated_at) values(#{userId},#{roleCode},#{assignmentSource},#{effectiveStartDate},#{effectiveEndDate},#{approvedBy},'ACTIVE',now(),now())")
    void insertUserRole(RoleInsert row);

    @Update("update organizations set parent_organization_code=#{parentOrganizationCode}, effective_start_date=#{effectiveStartDate}, effective_end_date=#{effectiveEndDate}, updated_at=now() where organization_code=#{organizationCode}")
    void updateOrganization(@Param("organizationCode") String organizationCode, @Param("parentOrganizationCode") String parentOrganizationCode, @Param("effectiveStartDate") LocalDate effectiveStartDate, @Param("effectiveEndDate") LocalDate effectiveEndDate);

    @Insert("insert into roles(role_code,role_name,purpose,grant_criteria,default_data_scope,is_active,created_at,updated_at) values(#{roleCode},#{roleName},#{purpose},#{grantCriteria},#{defaultDataScope},true,now(),now())")
    void insertRole(RoleUpsertRequest request);

    @Update("update roles set role_name=#{roleName}, purpose=#{purpose}, grant_criteria=#{grantCriteria}, default_data_scope=#{defaultDataScope}, updated_at=now() where role_code=#{roleCode}")
    void updateRole(@Param("roleCode") String roleCode, @Param("roleName") String roleName, @Param("purpose") String purpose, @Param("grantCriteria") String grantCriteria, @Param("defaultDataScope") String defaultDataScope);

    @Update("update user_roles set status='REVOKED', updated_at=now() where assignment_id=#{assignmentId}")
    void revokeUserRole(Long assignmentId);

    @Insert("insert into menus(parent_menu_id,menu_level,menu_name,screen_id,url,icon,business_category,description,display_order,is_active,created_at,updated_at) values(#{parentMenuId},#{menuLevel},#{menuName},#{screenId},#{url},#{icon},#{businessCategory},#{description},#{displayOrder},true,now(),now())")
    void insertMenu(MenuInsert row);

    @Update("update menus set parent_menu_id=#{parentMenuId}, display_order=#{displayOrder}, updated_at=now() where menu_id=#{menuId}")
    void updateMenuStructure(@Param("menuId") Long menuId, @Param("parentMenuId") Long parentMenuId, @Param("displayOrder") Integer displayOrder);

    @Update("update menus set parent_menu_id=#{parentMenuId}, menu_level=#{menuLevel}, menu_name=#{menuName}, screen_id=#{screenId}, url=#{url}, icon=#{icon}, business_category=#{businessCategory}, description=#{description}, display_order=#{displayOrder}, updated_at=now() where menu_id=#{menuId}")
    void updateMenu(@Param("menuId") Long menuId, @Param("parentMenuId") Long parentMenuId, @Param("menuLevel") String menuLevel, @Param("menuName") String menuName, @Param("screenId") String screenId, @Param("url") String url, @Param("icon") String icon, @Param("businessCategory") String businessCategory, @Param("description") String description, @Param("displayOrder") Integer displayOrder);

    @Delete("delete from menu_permissions where principal_type=#{principalType} and principal_id=#{principalId}")
    void deletePermissionsForPrincipal(@Param("principalType") String principalType, @Param("principalId") String principalId);

    @Insert("insert into menu_permissions(principal_type,principal_id,menu_id,permission_effect,is_active,created_at,updated_at) values(#{principalType},#{principalId},#{menuId},#{permissionEffect},true,now(),now())")
    void insertPermission(@Param("principalType") String principalType, @Param("principalId") String principalId, @Param("menuId") Long menuId, @Param("permissionEffect") String permissionEffect);

    @Insert("insert into code_groups(group_id,group_name,description,managing_department,is_active,created_at,updated_at) values(#{groupId},#{groupName},#{description},#{managingDepartment},true,now(),now())")
    void insertCodeGroup(CodeGroupUpsertRequest request);

    @Update("update code_groups set group_name=#{groupName}, description=#{description}, managing_department=#{managingDepartment}, updated_at=now() where group_id=#{groupId}")
    void updateCodeGroup(@Param("groupId") String groupId, @Param("groupName") String groupName, @Param("description") String description, @Param("managingDepartment") String managingDepartment);

    @Insert("insert into detail_codes(group_id,code_value,code_name,parent_code_value,sort_order,additional_attributes,valid_from,valid_to,is_active,created_at,updated_at) values(#{groupId},#{codeValue},#{codeName},#{parentCodeValue},#{sortOrder},cast(#{additionalAttributesJson} as jsonb),#{validFrom},#{validTo},true,now(),now())")
    void insertDetailCode(DetailCodeInsert row);

    @Update("update detail_codes set code_name=#{codeName}, parent_code_value=#{parentCodeValue}, sort_order=#{sortOrder}, additional_attributes=cast(#{additionalAttributesJson} as jsonb), valid_from=#{validFrom}, valid_to=#{validTo}, updated_at=now() where group_id=#{groupId} and code_value=#{codeValue}")
    void updateDetailCode(DetailCodeInsert row);

    @Insert("insert into change_histories(entity_name,entity_key,change_type,before_value,after_value,changed_by,changed_at,reason) values(#{entityName},#{entityKey},#{changeType},cast(#{beforeValue} as jsonb),cast(#{afterValue} as jsonb),#{changedBy},now(),#{reason})")
    void insertHistory(@Param("entityName") String entityName, @Param("entityKey") String entityKey, @Param("changeType") String changeType, @Param("beforeValue") String beforeValue, @Param("afterValue") String afterValue, @Param("changedBy") String changedBy, @Param("reason") String reason);
}

record RoleInsert(Long assignmentId, String userId, String roleCode, String assignmentSource, LocalDate effectiveStartDate, LocalDate effectiveEndDate, String approvedBy) {}
record MenuInsert(Long menuId, Long parentMenuId, String menuLevel, String menuName, String screenId, String url, String icon, String businessCategory, String description, Integer displayOrder) {}
record DetailCodeInsert(String groupId, String codeValue, String codeName, String parentCodeValue, Integer sortOrder, String additionalAttributesJson, LocalDate validFrom, LocalDate validTo) {}
