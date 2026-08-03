package kr.ac.knue.fpe;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AdminApiContractTest extends PostgresIntegrationTest {
    @Autowired MockMvc mockMvc;
    @Autowired JdbcTemplate jdbc;

    private Cookie adminCookie() throws Exception {
        String setCookie = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin\"}"))
                .andReturn().getResponse().getHeader("Set-Cookie");
        String value = setCookie == null ? "" : setCookie.split(";", 2)[0].split("=", 2)[1];
        return new Cookie("SESSION", value);
    }

    @Test
    void getHealthReturnsServiceAndDataModelContractCounts() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("UP"))
                .andExpect(jsonPath("$.data.dataModelContracts.entityTableContracts", greaterThanOrEqualTo(0)));
    }

    @Test
    void getAdminUsersReturnsPortalFilteredUserPageFromDatabase() throws Exception {
        mockMvc.perform(get("/api/admin/users").cookie(adminCookie()).param("staffNo", "STAFF-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items[0].userId").value("STAFF-001"))
                .andExpect(jsonPath("$.data.items[0].roleCodes").isArray());
    }

    @Test
    void patchAdminUserUsageValidatesReasonAndWritesUserAccountsChangeHistory() throws Exception {
        mockMvc.perform(patch("/api/admin/users/STAFF-001/usage")
                        .cookie(adminCookie())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"systemEnabled\":false}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.fieldErrors[0].field").value("reason"));

        long beforeHistory = countChangeHistories("user_accounts");
        mockMvc.perform(patch("/api/admin/users/STAFF-001/usage")
                        .cookie(adminCookie())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"systemEnabled\":false,\"reason\":\"사용여부 변경 계약 검증\",\"staffName\":\"변경금지\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value("STAFF-001"))
                .andExpect(jsonPath("$.data.systemEnabled").value(false));
        assertThat(countChangeHistories("user_accounts")).isGreaterThan(beforeHistory);
        assertThat(staffNameByStaffNo("STAFF-001")).isEqualTo("김교원");
    }

    @Test
    void putAdminUserRolesRejectsInvalidRoleAndReplacesActiveAssignmentsWithHistory() throws Exception {
        mockMvc.perform(put("/api/admin/users/STAFF-001/roles")
                        .cookie(adminCookie())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roles\":[{\"roleCode\":\"R99\",\"assignmentSource\":\"MANUAL\",\"effectiveStartDate\":\"2026-02-01\",\"approvedBy\":\"admin\"}],\"reason\":\"역할 검증\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.fieldErrors[0].field").value("roleCode"));

        long beforeHistory = countChangeHistories("user_roles");
        mockMvc.perform(put("/api/admin/users/STAFF-001/roles")
                        .cookie(adminCookie())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roles\":[{\"roleCode\":\"R01\",\"assignmentSource\":\"MANUAL\",\"effectiveStartDate\":\"2026-02-01\",\"approvedBy\":\"admin\"},{\"roleCode\":\"R09\",\"assignmentSource\":\"MANUAL\",\"effectiveStartDate\":\"2026-02-01\",\"approvedBy\":\"admin\"}],\"reason\":\"역할 교체 계약 검증\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value("STAFF-001"))
                .andExpect(jsonPath("$.data.roleCodes").isArray());
        assertThat(countActiveUserRoles("STAFF-001")).isGreaterThanOrEqualTo(2L);
        assertThat(countChangeHistories("user_roles")).isGreaterThan(beforeHistory);
    }

    @Test
    void getAdminOrganizationsReturnsOrganizationPageAndFilters() throws Exception {
        mockMvc.perform(get("/api/admin/organizations").cookie(adminCookie()).param("organizationCode", "COMP"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].organizationCode").value("COMP-EDU"))
                .andExpect(jsonPath("$.data.items[0].parentOrganizationCode").value(notNullValue()));
    }

    @Test
    void putAdminOrganizationRelationshipsRejectsInvalidPeriodAndPersistsParentEffectiveHistory() throws Exception {
        mockMvc.perform(put("/api/admin/organizations/COMP-EDU/relationships")
                        .cookie(adminCookie())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"parentOrganizationCode\":\"EDU-COLLEGE\",\"effectiveStartDate\":\"2026-03-01\",\"effectiveEndDate\":\"2026-02-01\",\"reason\":\"기간 검증\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.fieldErrors[0].field").value("effectiveEndDate"));

        long beforeHistory = countChangeHistories("organizations");
        mockMvc.perform(put("/api/admin/organizations/COMP-EDU/relationships")
                        .cookie(adminCookie())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"parentOrganizationCode\":\"EDU-COLLEGE\",\"effectiveStartDate\":\"2026-03-01\",\"effectiveEndDate\":null,\"reason\":\"조직 관계 계약 검증\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.organizationCode").value("COMP-EDU"))
                .andExpect(jsonPath("$.data.parentOrganizationCode").value("EDU-COLLEGE"));
        assertThat(countChangeHistories("organizations")).isGreaterThan(beforeHistory);
    }

    @Test
    void getAdminRolesReturnsRolePageFromDatabase() throws Exception {
        mockMvc.perform(get("/api/admin/roles").cookie(adminCookie()).param("roleCode", "R09"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].roleCode").value("R09"))
                .andExpect(jsonPath("$.data.items[0].defaultDataScope").value("SYSTEM"));
    }

    @Test
    void postAdminRolesValidatesRequiredFieldsAndCreatesRoleWithHistory() throws Exception {
        mockMvc.perform(post("/api/admin/roles")
                        .cookie(adminCookie())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roleCode\":\"R08\",\"purpose\":\"감사\",\"reason\":\"검증\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.fieldErrors[0].field").value("roleName"));

        jdbc.update("delete from user_roles where role_code='R08'");
        jdbc.update("delete from roles where role_code='R08'");
        long beforeHistory = countChangeHistories("roles");
        mockMvc.perform(post("/api/admin/roles")
                        .cookie(adminCookie())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roleCode\":\"R08\",\"roleName\":\"점수산출 감사자\",\"purpose\":\"계약 테스트 생성\",\"grantCriteria\":\"감사자\",\"defaultDataScope\":\"AUDIT_READ\",\"reason\":\"역할 생성 계약 검증\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roleCode").value("R08"))
                .andExpect(jsonPath("$.data.active").value(true));
        assertThat(countActiveRole("R08")).isEqualTo(1L);
        assertThat(countChangeHistories("roles")).isGreaterThan(beforeHistory);
    }

    @Test
    void putAdminRolesValidatesPathBodyMismatchAndUpdatesMutableFieldsWithHistory() throws Exception {
        mockMvc.perform(put("/api/admin/roles/R07")
                        .cookie(adminCookie())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roleCode\":\"R06\",\"roleName\":\"실적부서\",\"purpose\":\"불일치\",\"reason\":\"검증\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.fieldErrors[0].field").value("roleCode"));

        long beforeHistory = countChangeHistories("roles");
        mockMvc.perform(put("/api/admin/roles/R07")
                        .cookie(adminCookie())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roleCode\":\"R07\",\"roleName\":\"실적부서\",\"purpose\":\"수정 계약 검증\",\"grantCriteria\":\"실적 담당 부서\",\"defaultDataScope\":\"ASSIGNED\",\"reason\":\"역할 수정 계약 검증\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roleCode").value("R07"))
                .andExpect(jsonPath("$.data.purpose").value("수정 계약 검증"));
        assertThat(countChangeHistories("roles")).isGreaterThan(beforeHistory);
    }

    @Test
    void getAdminUserRolesReturnsAssignmentPageWithStatusFilter() throws Exception {
        mockMvc.perform(get("/api/admin/user-roles").cookie(adminCookie()).param("userId", "STAFF-001").param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.page").value(0));
    }

    @Test
    void postAdminUserRolesValidatesApprovedByAndCreatesActiveAssignmentHistory() throws Exception {
        mockMvc.perform(post("/api/admin/user-roles")
                        .cookie(adminCookie())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"STAFF-001\",\"roleCode\":\"R02\",\"assignmentSource\":\"MANUAL\",\"effectiveStartDate\":\"2026-04-01\",\"reason\":\"검증\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.fieldErrors[0].field").value("approvedBy"));

        long beforeHistory = countChangeHistories("user_roles");
        mockMvc.perform(post("/api/admin/user-roles")
                        .cookie(adminCookie())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"STAFF-001\",\"roleCode\":\"R02\",\"assignmentSource\":\"MANUAL\",\"effectiveStartDate\":\"2026-04-01\",\"effectiveEndDate\":null,\"approvedBy\":\"admin\",\"reason\":\"역할 부여 계약 검증\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value("STAFF-001"))
                .andExpect(jsonPath("$.data.roleCode").value("R02"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
        assertThat(countChangeHistories("user_roles")).isGreaterThan(beforeHistory);
    }

    @Test
    void deleteAdminUserRolesValidatesPathAndRevokesAssignmentWithHistory() throws Exception {
        mockMvc.perform(delete("/api/admin/user-roles/not-a-number")
                        .cookie(adminCookie())
                        .param("reason", "숫자 검증"))
                .andExpect(status().isBadRequest());

        long beforeHistory = countChangeHistories("user_roles");
        mockMvc.perform(delete("/api/admin/user-roles/2")
                        .cookie(adminCookie())
                        .param("reason", "역할 회수 계약 검증"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.assignmentId").value(2))
                .andExpect(jsonPath("$.data.status").value("REVOKED"));
        assertThat(countRevokedUserRole(2L)).isEqualTo(1L);
        assertThat(countChangeHistories("user_roles")).isGreaterThan(beforeHistory);
    }

    @Test
    void getAdminMenuPermissionsReturnsPrincipalFilteredRows() throws Exception {
        mockMvc.perform(get("/api/admin/menu-permissions").cookie(adminCookie()).param("principalType", "ROLE").param("principalId", "R09"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.size").value(20));
    }

    @Test
    void putAdminMenuPermissionsRejectsInvalidEffectAndReplacesRowsWithHistory() throws Exception {
        mockMvc.perform(put("/api/admin/menu-permissions")
                        .cookie(adminCookie())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"principalType\":\"ROLE\",\"principalId\":\"R09\",\"permissions\":[{\"menuId\":10,\"permissionEffect\":\"BLOCK\"}],\"reason\":\"검증\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.fieldErrors[0].field").value("permissionEffect"));

        long beforeHistory = countChangeHistories("menu_permissions");
        mockMvc.perform(put("/api/admin/menu-permissions")
                        .cookie(adminCookie())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"principalType\":\"ROLE\",\"principalId\":\"R09\",\"permissions\":[{\"menuId\":10,\"permissionEffect\":\"ALLOW\"},{\"menuId\":11,\"permissionEffect\":\"DENY\"}],\"reason\":\"메뉴 권한 계약 검증\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items").isArray());
        assertThat(countMenuPermissions("ROLE", "R09")).isEqualTo(2L);
        assertThat(countChangeHistories("menu_permissions")).isGreaterThan(beforeHistory);
    }

    @Test
    void getAdminMenusTreeReturnsMenuHierarchyRows() throws Exception {
        mockMvc.perform(get("/api/admin/menus/tree").cookie(adminCookie()).param("active", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items[0].menuId").value(notNullValue()));
    }

    @Test
    void putAdminMenusStructureRejectsSelfParentAndPersistsOrderParentHistory() throws Exception {
        mockMvc.perform(put("/api/admin/menus/18/structure")
                        .cookie(adminCookie())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"parentMenuId\":18,\"displayOrder\":5,\"reason\":\"순환 검증\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.fieldErrors[0].field").value("parentMenuId"));

        long beforeHistory = countChangeHistories("menus");
        mockMvc.perform(put("/api/admin/menus/18/structure")
                        .cookie(adminCookie())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"parentMenuId\":5,\"displayOrder\":9,\"reason\":\"메뉴 구조 계약 검증\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.menuId").value(18))
                .andExpect(jsonPath("$.data.parentMenuId").value(5))
                .andExpect(jsonPath("$.data.displayOrder").value(9));
        assertThat(countChangeHistories("menus")).isGreaterThan(beforeHistory);
    }

    @Test
    void getAdminMenusReturnsMenuInfoRows() throws Exception {
        mockMvc.perform(get("/api/admin/menus").cookie(adminCookie()).param("screenId", "USER_MANAGEMENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].screenId").value("USER_MANAGEMENT"))
                .andExpect(jsonPath("$.data.items[0].url").value("/system/users"));
    }

    @Test
    void postAdminMenusValidatesScreenUrlAndCreatesActiveMenuHistory() throws Exception {
        mockMvc.perform(post("/api/admin/menus")
                        .cookie(adminCookie())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"menuLevel\":\"SCREEN\",\"menuName\":\"계약 검증 메뉴\",\"screenId\":\"CONTRACT_MENU\",\"displayOrder\":30,\"reason\":\"검증\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.fieldErrors[0].field").value("url"));

        long beforeHistory = countChangeHistories("menus");
        mockMvc.perform(post("/api/admin/menus")
                        .cookie(adminCookie())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"parentMenuId\":5,\"menuLevel\":\"SCREEN\",\"menuName\":\"계약 검증 메뉴\",\"screenId\":\"CONTRACT_MENU\",\"url\":\"/system/contract-menu\",\"icon\":\"test\",\"businessCategory\":\"COMMON\",\"description\":\"계약 테스트\",\"displayOrder\":30,\"reason\":\"메뉴 생성 계약 검증\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.screenId").value("CONTRACT_MENU"))
                .andExpect(jsonPath("$.data.active").value(true));
        assertThat(countChangeHistories("menus")).isGreaterThan(beforeHistory);
    }

    @Test
    void putAdminMenusValidatesRequiredNameAndUpdatesMenuInfoHistory() throws Exception {
        mockMvc.perform(put("/api/admin/menus/10")
                        .cookie(adminCookie())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"menuLevel\":\"SCREEN\",\"screenId\":\"USER_MANAGEMENT\",\"url\":\"/system/users\",\"displayOrder\":1,\"reason\":\"검증\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.fieldErrors[0].field").value("menuName"));

        long beforeHistory = countChangeHistories("menus");
        mockMvc.perform(put("/api/admin/menus/10")
                        .cookie(adminCookie())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"parentMenuId\":2,\"menuLevel\":\"SCREEN\",\"menuName\":\"사용자 관리\",\"screenId\":\"USER_MANAGEMENT\",\"url\":\"/system/users\",\"icon\":\"user\",\"businessCategory\":\"COMMON\",\"description\":\"사용자 검색과 역할 관리 수정\",\"displayOrder\":1,\"reason\":\"메뉴 수정 계약 검증\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.menuId").value(10))
                .andExpect(jsonPath("$.data.description").value("사용자 검색과 역할 관리 수정"));
        assertThat(countChangeHistories("menus")).isGreaterThan(beforeHistory);
    }

    @Test
    void getAdminCodeGroupsReturnsCodeGroupPage() throws Exception {
        mockMvc.perform(get("/api/admin/code-groups").cookie(adminCookie()).param("groupId", "EVAL_AREA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].groupId").value("EVAL_AREA"))
                .andExpect(jsonPath("$.data.items[0].groupName").value("평가영역"));
    }

    @Test
    void postAdminCodeGroupsValidatesNameAndCreatesActiveGroupHistory() throws Exception {
        mockMvc.perform(post("/api/admin/code-groups")
                        .cookie(adminCookie())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"groupId\":\"CONTRACT_GROUP\",\"reason\":\"검증\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.fieldErrors[0].field").value("groupName"));

        long beforeHistory = countChangeHistories("code_groups");
        jdbc.update("delete from detail_codes where group_id='CONTRACT_GROUP'");
        jdbc.update("delete from code_groups where group_id='CONTRACT_GROUP'");
        mockMvc.perform(post("/api/admin/code-groups")
                        .cookie(adminCookie())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"groupId\":\"CONTRACT_GROUP\",\"groupName\":\"계약 검증 그룹\",\"description\":\"계약 테스트\",\"managingDepartment\":\"교수지원과\",\"reason\":\"코드그룹 생성 계약 검증\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.groupId").value("CONTRACT_GROUP"))
                .andExpect(jsonPath("$.data.active").value(true));
        assertThat(countChangeHistories("code_groups")).isGreaterThan(beforeHistory);
    }

    @Test
    void putAdminCodeGroupsValidatesReasonAndUpdatesGroupHistory() throws Exception {
        mockMvc.perform(put("/api/admin/code-groups/EVAL_AREA")
                        .cookie(adminCookie())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"groupName\":\"평가영역 수정\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.fieldErrors[0].field").value("reason"));

        long beforeHistory = countChangeHistories("code_groups");
        mockMvc.perform(put("/api/admin/code-groups/EVAL_AREA")
                        .cookie(adminCookie())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"groupName\":\"평가영역\",\"description\":\"평가영역 선택값 수정\",\"managingDepartment\":\"교수지원과\",\"reason\":\"코드그룹 수정 계약 검증\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.groupId").value("EVAL_AREA"))
                .andExpect(jsonPath("$.data.description").value("평가영역 선택값 수정"));
        assertThat(countChangeHistories("code_groups")).isGreaterThan(beforeHistory);
    }

    @Test
    void getAdminDetailCodesReturnsGroupScopedCodePage() throws Exception {
        mockMvc.perform(get("/api/admin/code-groups/EVAL_AREA/codes").cookie(adminCookie()).param("codeValue", "TEACH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].groupId").value("EVAL_AREA"))
                .andExpect(jsonPath("$.data.items[0].codeValue").value("TEACH"));
    }

    @Test
    void postAdminDetailCodesValidatesSortOrderAndCreatesActiveDetailHistory() throws Exception {
        mockMvc.perform(post("/api/admin/code-groups/EVAL_AREA/codes")
                        .cookie(adminCookie())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codeValue\":\"CONTRACT_DETAIL\",\"codeName\":\"계약 상세\",\"reason\":\"검증\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.fieldErrors[0].field").value("sortOrder"));

        long beforeHistory = countChangeHistories("detail_codes");
        jdbc.update("delete from detail_codes where group_id='EVAL_AREA' and code_value='CONTRACT_DETAIL'");
        mockMvc.perform(post("/api/admin/code-groups/EVAL_AREA/codes")
                        .cookie(adminCookie())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codeValue\":\"CONTRACT_DETAIL\",\"codeName\":\"계약 상세\",\"parentCodeValue\":\"TEACH\",\"sortOrder\":90,\"additionalAttributes\":{\"contract\":true},\"validFrom\":\"2026-01-01\",\"validTo\":null,\"reason\":\"상세코드 생성 계약 검증\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.codeValue").value("CONTRACT_DETAIL"))
                .andExpect(jsonPath("$.data.parentCodeValue").value("TEACH"));
        assertThat(countChangeHistories("detail_codes")).isGreaterThan(beforeHistory);
    }

    @Test
    void putAdminDetailCodesValidatesDateOrderAndUpdatesCodeNameParentAttributesHistory() throws Exception {
        mockMvc.perform(put("/api/admin/code-groups/EVAL_AREA/codes/TEACH")
                        .cookie(adminCookie())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codeValue\":\"TEACH\",\"codeName\":\"교육\",\"sortOrder\":1,\"validFrom\":\"2026-02-01\",\"validTo\":\"2026-01-01\",\"reason\":\"검증\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.fieldErrors[0].field").value("validTo"));

        long beforeHistory = countChangeHistories("detail_codes");
        mockMvc.perform(put("/api/admin/code-groups/EVAL_AREA/codes/TEACH")
                        .cookie(adminCookie())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codeValue\":\"TEACH\",\"codeName\":\"교육 수정\",\"parentCodeValue\":null,\"sortOrder\":1,\"additionalAttributes\":{\"changed\":true},\"validFrom\":\"2026-01-01\",\"validTo\":null,\"reason\":\"상세코드 수정 계약 검증\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.codeValue").value("TEACH"))
                .andExpect(jsonPath("$.data.codeName").value("교육 수정"));
        assertThat(countChangeHistories("detail_codes")).isGreaterThan(beforeHistory);
    }

    private long countChangeHistories(String entityName) {
        Long value = jdbc.queryForObject(
                "select count(*) from change_histories where entity_name=?",
                Long.class,
                entityName);
        return value == null ? 0L : value;
    }

    private String staffNameByStaffNo(String staffNo) {
        return jdbc.queryForObject(
                "select staff_name from korus_staff_snapshots where staff_no=?",
                String.class,
                staffNo);
    }

    private long countActiveUserRoles(String userId) {
        Long value = jdbc.queryForObject(
                "select count(*) from user_roles where user_id=? and status=?",
                Long.class,
                userId,
                "ACTIVE");
        return value == null ? 0L : value;
    }

    private long countActiveRole(String roleCode) {
        Long value = jdbc.queryForObject(
                "select count(*) from roles where role_code=? and is_active=true",
                Long.class,
                roleCode);
        return value == null ? 0L : value;
    }

    private long countRevokedUserRole(long assignmentId) {
        Long value = jdbc.queryForObject(
                "select count(*) from user_roles where assignment_id=? and status=?",
                Long.class,
                assignmentId,
                "REVOKED");
        return value == null ? 0L : value;
    }

    private long countMenuPermissions(String principalType, String principalId) {
        Long value = jdbc.queryForObject(
                "select count(*) from menu_permissions where principal_type=? and principal_id=?",
                Long.class,
                principalType,
                principalId);
        return value == null ? 0L : value;
    }
}
