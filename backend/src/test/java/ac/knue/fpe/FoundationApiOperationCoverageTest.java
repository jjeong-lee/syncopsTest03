package ac.knue.fpe;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class FoundationApiOperationCoverageTest {
  @Autowired MockMvc mvc;

  @Test
  void contract_fixture_is_loaded_from_classpath_and_auth_operations_have_body_contracts() throws Exception {
    assert new ClassPathResource("contracts/openapi.yaml").exists();

    mvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"loginId":"admin","password":"admin"}
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.userId").value("admin-user"))
        .andExpect(jsonPath("$.data.roleCodes", hasItem("R09")))
        .andExpect(cookie().exists("FPESESSION"));

    mvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"loginId":"admin","password":"wrong"}
                """))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.data.code").value("UNAUTHORIZED"));

    MvcResult admin = adminLogin();
    mvc.perform(get("/api/auth/me").cookie(admin.getResponse().getCookies()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.loginId").value("admin"))
        .andExpect(jsonPath("$.data.roleCodes", hasItem("R09")));

    mvc.perform(get("/api/auth/me"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.data.code").value("UNAUTHORIZED"));

    mvc.perform(post("/api/auth/logout").cookie(admin.getResponse().getCookies()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.loggedOut").value(true))
        .andExpect(cookie().maxAge("FPESESSION", 0));

    mvc.perform(post("/api/auth/logout"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.data.code").value("UNAUTHORIZED"));
  }

  @Test
  void portal_and_master_query_operations_return_db_backed_response_contracts() throws Exception {
    MvcResult admin = adminLogin();

    mvc.perform(get("/api/menus/current?level=SCREEN").cookie(admin.getResponse().getCookies()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data", hasSize(9)))
        .andExpect(jsonPath("$.data[*].url", hasItem("/system/users")));

    mvc.perform(get("/api/menus/current?level=SCREEN&parentMenuId=menu-user-org").cookie(admin.getResponse().getCookies()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data[*].menuId", hasItem("menu-users")));

    mvc.perform(get("/api/organizations?organizationType=DEPARTMENT").cookie(admin.getResponse().getCookies()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.content[*].organizationCode", hasItem("ORG-EDU")));

    mvc.perform(get("/api/organizations/tree?rootOrganizationCode=KNUE").cookie(admin.getResponse().getCookies()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data[0].organizationId").value("org-root"));

    mvc.perform(get("/api/roles?useYn=Y").cookie(admin.getResponse().getCookies()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.content[*].roleCode", hasItem("R09")));

    mvc.perform(get("/api/user-roles?userId=admin-user&activeOnly=true").cookie(admin.getResponse().getCookies()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.content[*].roleCode", hasItem("R09")));

    mvc.perform(get("/api/menu-permissions?targetType=ROLE&targetId=R09").cookie(admin.getResponse().getCookies()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.content[*].accessDecision", hasItem("ALLOW")));

    mvc.perform(get("/api/menus/tree?includeInactive=false&level=SCREEN").cookie(admin.getResponse().getCookies()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data[*].screenId", hasItem("CMN-USER-MGMT")));

    mvc.perform(get("/api/code-groups?groupId=USER_STATUS").cookie(admin.getResponse().getCookies()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.content[0].groupId").value("USER_STATUS"));

    mvc.perform(get("/api/code-groups/USER_STATUS/detail-codes?useYn=Y").cookie(admin.getResponse().getCookies()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.content[*].codeValue", hasItem("ACTIVE")));
  }

  @Test
  void user_role_and_organization_write_operations_show_follow_up_db_side_effects() throws Exception {
    MvcResult admin = adminLogin();

    mvc.perform(patch("/api/users/teacher-user/administration?changeReason=contract")
            .cookie(admin.getResponse().getCookies())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"systemUseYn":"N","roleCodes":["R01"]}
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.systemUseYn").value("N"));

    mvc.perform(get("/api/users?staffNo=KNUE-1001").cookie(admin.getResponse().getCookies()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.content[0].systemUseYn").value("N"))
        .andExpect(jsonPath("$.data.content[0].staffName").value("홍길동"));

    mvc.perform(patch("/api/users/UNKNOWN/administration")
            .cookie(admin.getResponse().getCookies())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"systemUseYn":"Y","roleCodes":["R01"]}
                """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.data.code").value("VALIDATION_ERROR"));

    mvc.perform(put("/api/organizations/org-edu/relationship?changeReason=contract")
            .cookie(admin.getResponse().getCookies())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"parentOrganizationId":"org-root","effectiveStartDate":"2026-01-01","effectiveEndDate":"2026-12-31","status":"ACTIVE"}
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.parentOrganizationId").value("org-root"));

    mvc.perform(put("/api/organizations/org-edu/relationship")
            .cookie(admin.getResponse().getCookies())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"parentOrganizationId":"org-edu","effectiveStartDate":"2026-01-01","effectiveEndDate":"2026-12-31","status":"ACTIVE"}
                """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.data.code").value("VALIDATION_ERROR"));

    mvc.perform(post("/api/user-roles/assignments?changeReason=contract")
            .cookie(admin.getResponse().getCookies())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"userId":"teacher-user","roleCodes":["R02"],"assignmentType":"MANUAL","validFrom":"2026-01-01","validTo":"2026-12-31","approverUserId":"admin-user"}
                """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data[*].roleCode", hasItem("R02")));

    mvc.perform(post("/api/user-roles/assignments")
            .cookie(admin.getResponse().getCookies())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"userId":"teacher-user","roleCodes":[],"assignmentType":"MANUAL","validFrom":"2026-01-01"}
                """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.data.fieldErrors.roleCodes").exists());

    mvc.perform(post("/api/user-roles/ur-teacher-r01/revoke?changeReason=contract")
            .cookie(admin.getResponse().getCookies())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"revokeDate":"2026-06-30"}
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.status").value("REVOKED"));
  }

  @Test
  void role_menu_and_code_write_operations_show_follow_up_db_side_effects() throws Exception {
    MvcResult admin = adminLogin();

    mvc.perform(put("/api/roles/R09?changeReason=contract")
            .cookie(admin.getResponse().getCookies())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"roleCode":"R09","roleName":"시스템관리자 계약검증","purpose":"contract","grantCriteria":"system admin","defaultDataScope":"ALL","useYn":"Y"}
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.roleName").value("시스템관리자 계약검증"));

    mvc.perform(put("/api/roles/R09")
            .cookie(admin.getResponse().getCookies())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"roleCode":"R01","roleName":"불일치"}
                """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.data.code").value("VALIDATION_ERROR"));

    mvc.perform(put("/api/menu-permissions/bulk?changeReason=contract")
            .cookie(admin.getResponse().getCookies())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"targetType":"ROLE","targetId":"R09","items":[{"menuId":"menu-users","accessDecision":"ALLOW"}]}
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data[*].menuId", hasItem("menu-users")));

    mvc.perform(put("/api/menus/menu-users/structure?changeReason=contract")
            .cookie(admin.getResponse().getCookies())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"parentMenuId":"menu-user-org","sortOrder":99,"status":"ACTIVE"}
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.sortOrder").value(99));

    mvc.perform(put("/api/menus/menu-users/structure")
            .cookie(admin.getResponse().getCookies())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"parentMenuId":"menu-users","sortOrder":1,"status":"ACTIVE"}
                """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.data.code").value("VALIDATION_ERROR"));

    mvc.perform(put("/api/menus/reorder?changeReason=contract")
            .cookie(admin.getResponse().getCookies())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"menuIds":["menu-users","menu-orgs"]}
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data[*].menuId", hasItem("menu-users")));

    mvc.perform(put("/api/menus/menu-users/info?changeReason=contract")
            .cookie(admin.getResponse().getCookies())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"menuName":"사용자 관리 계약검증","screenId":"CMN-USER-MGMT","url":"/system/users","icon":"user","businessCategory":"CMN","description":"contract"}
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.menuName").value("사용자 관리 계약검증"));

    mvc.perform(put("/api/menus/menu-users/info")
            .cookie(admin.getResponse().getCookies())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"menuName":""}
                """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.data.fieldErrors.menuName").exists());
  }

  @Test
  void code_group_and_detail_code_write_operations_show_follow_up_db_side_effects() throws Exception {
    MvcResult admin = adminLogin();

    mvc.perform(post("/api/code-groups?changeReason=contract")
            .cookie(admin.getResponse().getCookies())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"groupId":"CONTRACT_TEST","groupName":"계약 테스트","description":"contract","managingDepartment":"교수지원과","useYn":"Y"}
                """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.groupId").value("CONTRACT_TEST"));

    mvc.perform(post("/api/code-groups")
            .cookie(admin.getResponse().getCookies())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"groupId":"","groupName":"계약 테스트"}
                """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.data.fieldErrors.groupId").exists());

    mvc.perform(put("/api/code-groups/USER_STATUS?changeReason=contract")
            .cookie(admin.getResponse().getCookies())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"groupId":"USER_STATUS","groupName":"사용자 상태 계약검증","description":"contract","managingDepartment":"교수지원과","useYn":"N"}
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.useYn").value("N"));

    mvc.perform(post("/api/code-groups/USER_STATUS/detail-codes?changeReason=contract")
            .cookie(admin.getResponse().getCookies())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"codeValue":"CONTRACT_DETAIL","codeName":"계약 상세","sortOrder":50,"additionalAttributes":"{}","validFrom":"2026-01-01","useYn":"Y"}
                """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.codeValue").value("CONTRACT_DETAIL"));

    mvc.perform(put("/api/detail-codes/dc-user-active?changeReason=contract")
            .cookie(admin.getResponse().getCookies())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"codeValue":"ACTIVE","codeName":"활성 계약검증","sortOrder":1,"additionalAttributes":"{}","validFrom":"2026-01-01","useYn":"Y"}
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.codeName").value("활성 계약검증"));

    mvc.perform(put("/api/detail-codes/UNKNOWN")
            .cookie(admin.getResponse().getCookies())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"codeValue":"UNKNOWN","codeName":"없음","sortOrder":1,"additionalAttributes":"{}","validFrom":"2026-01-01","useYn":"Y"}
                """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.data.code").value("VALIDATION_ERROR"));
  }

  private MvcResult adminLogin() throws Exception {
    return mvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"loginId":"admin","password":"admin"}
                """))
        .andExpect(status().isOk())
        .andReturn();
  }
}
