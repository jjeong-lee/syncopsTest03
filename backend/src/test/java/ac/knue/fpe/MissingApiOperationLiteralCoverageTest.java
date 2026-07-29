package ac.knue.fpe;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MissingApiOperationLiteralCoverageTest {
  @Autowired MockMvc mvc;

  @Test
  void get_code_groups_returns_page_contract_and_supports_portal_filtering() throws Exception {
    MvcResult admin = adminLogin();

    mvc.perform(get("/api/code-groups")
            .cookie(admin.getResponse().getCookies())
            .param("groupId", "USER_STATUS"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.totalElements").value(1))
        .andExpect(jsonPath("$.data.content[0].groupId").value("USER_STATUS"))
        .andExpect(jsonPath("$.data.content[0].groupName").exists());

    mvc.perform(get("/api/code-groups"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.data.code").value("UNAUTHORIZED"));
  }

  @Test
  void get_code_group_detail_codes_returns_page_contract_and_path_group_filter() throws Exception {
    MvcResult admin = adminLogin();

    mvc.perform(get("/api/code-groups/USER_STATUS/detail-codes")
            .cookie(admin.getResponse().getCookies())
            .param("useYn", "Y"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.content[*].groupId", hasItem("USER_STATUS")))
        .andExpect(jsonPath("$.data.content[*].codeValue", hasItem("ACTIVE")));
  }

  @Test
  void get_menu_permissions_returns_role_menu_permission_page_contract() throws Exception {
    MvcResult admin = adminLogin();

    mvc.perform(get("/api/menu-permissions")
            .cookie(admin.getResponse().getCookies())
            .param("targetType", "ROLE")
            .param("targetId", "R09")
            .param("menuId", "menu-users"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.content[0].targetType").value("ROLE"))
        .andExpect(jsonPath("$.data.content[0].targetId").value("R09"))
        .andExpect(jsonPath("$.data.content[0].accessDecision").value("ALLOW"));
  }

  @Test
  void get_current_menus_applies_session_role_and_parent_portal_filter() throws Exception {
    MvcResult admin = adminLogin();

    mvc.perform(get("/api/menus/current")
            .cookie(admin.getResponse().getCookies())
            .param("level", "SCREEN")
            .param("parentMenuId", "menu-user-org"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data[*].menuId", hasItem("menu-users")))
        .andExpect(jsonPath("$.data[*].url", hasItem("/system/users")));
  }

  @Test
  void get_menu_tree_returns_db_backed_menu_nodes() throws Exception {
    MvcResult admin = adminLogin();

    mvc.perform(get("/api/menus/tree")
            .cookie(admin.getResponse().getCookies())
            .param("includeInactive", "false")
            .param("level", "SCREEN"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data", hasSize(9)))
        .andExpect(jsonPath("$.data[*].screenId", hasItem("CMN-USER-MGMT")));
  }

  @Test
  void get_organizations_returns_filtered_page_contract() throws Exception {
    MvcResult admin = adminLogin();

    mvc.perform(get("/api/organizations")
            .cookie(admin.getResponse().getCookies())
            .param("organizationType", "DEPARTMENT"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.content[*].organizationCode", hasItem("ORG-EDU")))
        .andExpect(jsonPath("$.data.content[*].organizationType", hasItem("DEPARTMENT")));
  }

  @Test
  void get_organization_tree_returns_root_node_contract() throws Exception {
    MvcResult admin = adminLogin();

    mvc.perform(get("/api/organizations/tree")
            .cookie(admin.getResponse().getCookies())
            .param("rootOrganizationCode", "KNUE"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data[0].organizationId").value("org-root"))
        .andExpect(jsonPath("$.data[0].organizationCode").value("KNUE"));
  }

  @Test
  void get_roles_returns_filtered_role_page_contract() throws Exception {
    MvcResult admin = adminLogin();

    mvc.perform(get("/api/roles")
            .cookie(admin.getResponse().getCookies())
            .param("useYn", "Y"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.content[*].roleCode", hasItem("R09")))
        .andExpect(jsonPath("$.data.content[*].roleName", hasItem("시스템관리자")));
  }

  @Test
  void get_user_roles_returns_active_assignments_for_user() throws Exception {
    MvcResult admin = adminLogin();

    mvc.perform(get("/api/user-roles")
            .cookie(admin.getResponse().getCookies())
            .param("userId", "admin-user")
            .param("activeOnly", "true"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.content[*].userId", hasItem("admin-user")))
        .andExpect(jsonPath("$.data.content[*].roleCode", hasItem("R09")));
  }

  @Test
  void post_detail_codes_creates_db_row_and_follow_up_read_finds_it() throws Exception {
    MvcResult admin = adminLogin();

    mvc.perform(post("/api/code-groups/USER_STATUS/detail-codes")
            .cookie(admin.getResponse().getCookies())
            .param("changeReason", "literal-contract")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"codeValue":"LITERAL_DETAIL","codeName":"리터럴 상세","sortOrder":77,"additionalAttributes":"{}","validFrom":"2026-01-01","useYn":"Y"}
                """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.groupId").value("USER_STATUS"))
        .andExpect(jsonPath("$.data.codeValue").value("LITERAL_DETAIL"));

    mvc.perform(get("/api/code-groups/USER_STATUS/detail-codes")
            .cookie(admin.getResponse().getCookies())
            .param("useYn", "Y"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.content[*].codeValue", hasItem("LITERAL_DETAIL")));

    mvc.perform(post("/api/code-groups/USER_STATUS/detail-codes")
            .cookie(admin.getResponse().getCookies())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"codeValue":"","codeName":"리터럴 상세"}
                """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.data.fieldErrors.codeValue").exists());
  }

  @Test
  void post_user_role_revoke_changes_status_and_rejects_duplicate_revoke() throws Exception {
    MvcResult admin = adminLogin();

    mvc.perform(post("/api/user-roles/ur-teacher-r01/revoke")
            .cookie(admin.getResponse().getCookies())
            .param("changeReason", "literal-contract")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"revokeDate":"2026-06-30"}
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.userRoleId").value("ur-teacher-r01"))
        .andExpect(jsonPath("$.data.status").value("REVOKED"));

    mvc.perform(get("/api/user-roles")
            .cookie(admin.getResponse().getCookies())
            .param("userId", "teacher-user"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.content[*].status", hasItem("REVOKED")));

    mvc.perform(post("/api/user-roles/ur-teacher-r01/revoke")
            .cookie(admin.getResponse().getCookies())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"revokeDate":"2026-06-30"}
                """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.data.code").value("VALIDATION_ERROR"));
  }

  @Test
  void put_menu_permissions_bulk_upserts_decision_and_follow_up_read_observes_it() throws Exception {
    MvcResult admin = adminLogin();

    mvc.perform(put("/api/menu-permissions/bulk")
            .cookie(admin.getResponse().getCookies())
            .param("changeReason", "literal-contract")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"targetType":"ROLE","targetId":"R09","items":[{"menuId":"menu-users","accessDecision":"DENY"}]}
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data[0].menuId").value("menu-users"))
        .andExpect(jsonPath("$.data[0].accessDecision").value("DENY"));

    mvc.perform(get("/api/menu-permissions")
            .cookie(admin.getResponse().getCookies())
            .param("targetType", "ROLE")
            .param("targetId", "R09")
            .param("menuId", "menu-users"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.content[0].accessDecision").value("DENY"));
  }

  @Test
  void put_menus_reorder_updates_sort_order_and_validates_empty_request() throws Exception {
    MvcResult admin = adminLogin();

    mvc.perform(put("/api/menus/reorder")
            .cookie(admin.getResponse().getCookies())
            .param("changeReason", "literal-contract")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"menuIds":["menu-orgs","menu-users"]}
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data[*].menuId", hasItem("menu-orgs")))
        .andExpect(jsonPath("$.data[*].menuId", hasItem("menu-users")));

    mvc.perform(put("/api/menus/reorder")
            .cookie(admin.getResponse().getCookies())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"menuIds":[]}
                """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.data.fieldErrors.menuIds").exists());
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
