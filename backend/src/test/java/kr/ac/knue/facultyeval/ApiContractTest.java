package kr.ac.knue.facultyeval;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
  "spring.datasource.url=jdbc:h2:mem:faculty_eval;MODE=PostgreSQL;DATABASE_TO_UPPER=false;DB_CLOSE_DELAY=-1",
  "spring.datasource.driver-class-name=org.h2.Driver",
  "spring.datasource.username=sa",
  "spring.datasource.password=",
  "spring.flyway.locations=classpath:db/testmigration"
})
@Transactional
class ApiContractTest {
  @Autowired MockMvc mockMvc;
  @Autowired JdbcTemplate jdbcTemplate;

  @Test
  void openapi_fixture_is_available_on_classpath() throws Exception {
    new ClassPathResource("contracts/openapi.yaml").getInputStream().close();
  }

  @Test
  void get_api_health_returns_readiness_contract() throws Exception {
    mockMvc.perform(get("/api/health"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.success").value(true))
      .andExpect(jsonPath("$.data.status").value("UP"))
      .andExpect(jsonPath("$.data.service").value("faculty-evaluation-common"))
      .andExpect(jsonPath("$.data.checkedAt").isString());
  }

  @Test
  void post_api_auth_login_creates_active_session_cookie_and_rejects_invalid_credentials() throws Exception {
    int sessionsBefore = countRows("user_session", "status='ACTIVE'");
    MvcResult login = mockMvc.perform(post("/api/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
          {"loginId":"admin","password":"admin"}
          """))
      .andExpect(status().isCreated())
      .andExpect(header().string("Set-Cookie", containsString("session_id=")))
      .andExpect(header().string("Set-Cookie", containsString("HttpOnly")))
      .andExpect(header().string("Set-Cookie", containsString("SameSite=Lax")))
      .andExpect(jsonPath("$.success").value(true))
      .andExpect(jsonPath("$.data.sessionId").isString())
      .andExpect(jsonPath("$.data.userId").value("admin"))
      .andExpect(jsonPath("$.data.roleCodes", hasItem("R09")))
      .andReturn();
    String cookie = login.getResponse().getHeader("Set-Cookie");
    String sessionId = cookie.substring(cookie.indexOf("session_id=") + "session_id=".length(), cookie.indexOf(';'));
    org.assertj.core.api.Assertions.assertThat(countRows("user_session", "status='ACTIVE'")).isEqualTo(sessionsBefore + 1);
    org.assertj.core.api.Assertions.assertThat(stringValue("select status from user_session where session_id=?", sessionId)).isEqualTo("ACTIVE");

    mockMvc.perform(post("/api/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
          {"loginId":"admin","password":"wrong"}
          """))
      .andExpect(status().isUnauthorized())
      .andExpect(jsonPath("$.success").value(false))
      .andExpect(jsonPath("$.error.code").value("INVALID_CREDENTIALS"));
    org.assertj.core.api.Assertions.assertThat(countRows("user_session", "status='ACTIVE'")).isEqualTo(sessionsBefore + 1);
  }

  @Test
  void post_api_auth_login_validation_reports_field_errors() throws Exception {
    mockMvc.perform(post("/api/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
          {"loginId":"","password":""}
          """))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.success").value(false))
      .andExpect(jsonPath("$.error.fields.loginId").exists())
      .andExpect(jsonPath("$.error.fields.password").exists());
  }

  @Test
  void get_api_auth_me_and_post_api_auth_logout_read_and_revoke_session_state() throws Exception {
    String cookie = adminCookie();
    String sessionId = sessionId(cookie);
    mockMvc.perform(get("/api/auth/me").header("Cookie", cookie))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.success").value(true))
      .andExpect(jsonPath("$.data.userId").value("admin"))
      .andExpect(jsonPath("$.data.menus", hasSize(greaterThanOrEqualTo(9))));

    mockMvc.perform(post("/api/auth/logout").header("Cookie", cookie))
      .andExpect(status().isCreated())
      .andExpect(header().string("Set-Cookie", containsString("Max-Age=0")))
      .andExpect(jsonPath("$.success").value(true))
      .andExpect(jsonPath("$.data.loggedOut").value(true));
    org.assertj.core.api.Assertions.assertThat(stringValue("select status from user_session where session_id=?", sessionId)).isEqualTo("REVOKED");

    mockMvc.perform(get("/api/auth/me").header("Cookie", cookie))
      .andExpect(status().isUnauthorized())
      .andExpect(jsonPath("$.error.code").value("UNAUTHENTICATED"));
  }

  @Test
  void protected_apis_require_session_and_readonly_portal_role_is_filtered_from_write_side_effects() throws Exception {
    mockMvc.perform(get("/api/users"))
      .andExpect(status().isUnauthorized())
      .andExpect(jsonPath("$.error.code").value("UNAUTHENTICATED"));

    String supportCookie = supportCookie();
    mockMvc.perform(get("/api/users").header("Cookie", supportCookie))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.items", hasSize(greaterThanOrEqualTo(1))));

    String before = stringValue("select system_enabled from app_user where user_id=?", "professor-001");
    mockMvc.perform(patch("/api/users/professor-001/usage")
        .header("Cookie", supportCookie)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
          {"systemEnabled":"N","reason":"readonly portal must not write"}
          """))
      .andExpect(status().isForbidden())
      .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
    org.assertj.core.api.Assertions.assertThat(stringValue("select system_enabled from app_user where user_id=?", "professor-001")).isEqualTo(before);
  }

  @Test
  void get_api_users_and_get_api_users_user_id_return_page_detail_and_not_found_contracts() throws Exception {
    String cookie = adminCookie();
    mockMvc.perform(get("/api/users").header("Cookie", cookie))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.success").value(true))
      .andExpect(jsonPath("$.data.items", hasSize(greaterThanOrEqualTo(1))))
      .andExpect(jsonPath("$.data.items[0].userId").exists())
      .andExpect(jsonPath("$.data.items[0].systemEnabled").exists());

    mockMvc.perform(get("/api/users/professor-001").header("Cookie", cookie))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.success").value(true))
      .andExpect(jsonPath("$.data.userId").value("professor-001"))
      .andExpect(jsonPath("$.data.roleCodes", hasItem("R01")));

    mockMvc.perform(get("/api/users/missing-user").header("Cookie", cookie))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.error.code").value("NOT_FOUND"));
  }

  @Test
  void patch_api_users_user_id_usage_updates_app_user_and_change_history_with_validation() throws Exception {
    String cookie = adminCookie();
    int historyBefore = countRows("change_history", "entity_name='app_user' and entity_id='professor-001'");
    mockMvc.perform(patch("/api/users/professor-001/usage")
        .header("Cookie", cookie)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
          {"systemEnabled":"N","reason":"disable for contract test"}
          """))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.success").value(true))
      .andExpect(jsonPath("$.data.userId").value("professor-001"))
      .andExpect(jsonPath("$.data.systemEnabled").value("N"));
    org.assertj.core.api.Assertions.assertThat(stringValue("select system_enabled from app_user where user_id=?", "professor-001")).isEqualTo("N");
    org.assertj.core.api.Assertions.assertThat(countRows("change_history", "entity_name='app_user' and entity_id='professor-001'")).isEqualTo(historyBefore + 1);

    mockMvc.perform(patch("/api/users/professor-001/usage")
        .header("Cookie", cookie)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
          {"systemEnabled":"X"}
          """))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.error.fields.systemEnabled").exists());
  }

  @Test
  void put_api_users_user_id_roles_replaces_active_assignments_and_rejects_empty_roles() throws Exception {
    String cookie = adminCookie();
    mockMvc.perform(put("/api/users/professor-001/roles")
        .header("Cookie", cookie)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
          {"roleCodes":["R04","R08"],"reason":"replace role set"}
          """))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.success").value(true))
      .andExpect(jsonPath("$.data.userId").value("professor-001"))
      .andExpect(jsonPath("$.data.roleCodes", hasItem("R04")))
      .andExpect(jsonPath("$.data.roleCodes", hasItem("R08")));
    org.assertj.core.api.Assertions.assertThat(countRows("user_role_assignment", "user_id='professor-001' and status='ACTIVE'")).isEqualTo(2);
    org.assertj.core.api.Assertions.assertThat(countRows("user_role_assignment", "user_id='professor-001' and status='REVOKED'")).isGreaterThanOrEqualTo(1);
    org.assertj.core.api.Assertions.assertThat(countRows("change_history", "entity_name='user_role_assignment' and entity_id='professor-001'")).isGreaterThanOrEqualTo(1);

    mockMvc.perform(put("/api/users/professor-001/roles")
        .header("Cookie", cookie)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
          {"roleCodes":[]}
          """))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.error.fields.roleCodes").exists());
  }

  @Test
  void get_api_organizations_and_get_api_organizations_tree_support_portal_filtering() throws Exception {
    String cookie = adminCookie();
    mockMvc.perform(get("/api/organizations").header("Cookie", cookie))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.success").value(true))
      .andExpect(jsonPath("$.data.items", hasSize(greaterThanOrEqualTo(1))))
      .andExpect(jsonPath("$.data.items[0].orgCode").exists())
      .andExpect(jsonPath("$.data.items[0].orgType").exists());

    mockMvc.perform(get("/api/organizations/tree").header("Cookie", cookie))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.success").value(true))
      .andExpect(jsonPath("$.data.items", hasSize(greaterThanOrEqualTo(1))))
      .andExpect(jsonPath("$.data.items[0].children").isArray());
  }

  @Test
  void put_api_organizations_org_code_relationship_updates_parent_period_and_history_with_validation() throws Exception {
    String cookie = adminCookie();
    mockMvc.perform(put("/api/organizations/COMPEDU/relationship")
        .header("Cookie", cookie)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
          {"parentOrgCode":"SUPPORT","effectiveStartDate":"2026-02-01","effectiveEndDate":"2026-12-31","reason":"relationship update"}
          """))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.success").value(true))
      .andExpect(jsonPath("$.data.orgCode").value("COMPEDU"))
      .andExpect(jsonPath("$.data.parentOrgCode").value("SUPPORT"))
      .andExpect(jsonPath("$.data.effectiveStartDate").value("2026-02-01"));
    org.assertj.core.api.Assertions.assertThat(countRows("change_history", "entity_name='organization' and entity_id='COMPEDU'")).isGreaterThanOrEqualTo(1);

    mockMvc.perform(put("/api/organizations/COMPEDU/relationship")
        .header("Cookie", cookie)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
          {"parentOrgCode":"COMPEDU","effectiveStartDate":"2026-03-01","effectiveEndDate":"2026-02-01"}
          """))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.error.fields.effectiveEndDate").exists());
  }

  @Test
  void get_post_put_api_roles_cover_contract_and_side_effects() throws Exception {
    String cookie = adminCookie();
    mockMvc.perform(get("/api/roles").header("Cookie", cookie))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.success").value(true))
      .andExpect(jsonPath("$.data.items", hasSize(greaterThanOrEqualTo(1))))
      .andExpect(jsonPath("$.data.items[0].roleCode").exists());

    mockMvc.perform(post("/api/roles")
        .header("Cookie", cookie)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
          {"roleCode":"R02","roleName":"계약테스트 역할","purpose":"계약 테스트","grantCriteria":"수동 승인","defaultDataScope":"ORG","isActive":"Y","reason":"role create"}
          """))
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.success").value(true))
      .andExpect(jsonPath("$.data.roleCode").value("R02"))
      .andExpect(jsonPath("$.data.defaultDataScope").value("ORG"));
    org.assertj.core.api.Assertions.assertThat(countRows("role", "role_code='R02'")).isEqualTo(1);
    org.assertj.core.api.Assertions.assertThat(countRows("change_history", "entity_name='role' and entity_id='R02'")).isEqualTo(1);

    mockMvc.perform(put("/api/roles/R04")
        .header("Cookie", cookie)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
          {"roleCode":"R04","roleName":"교수지원과 수정","purpose":"기준정보 조회와 관리","grantCriteria":"담당자 기준","defaultDataScope":"ALL","isActive":"Y","reason":"role update"}
          """))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.success").value(true))
      .andExpect(jsonPath("$.data.roleCode").value("R04"))
      .andExpect(jsonPath("$.data.grantCriteria").value("담당자 기준"));
    org.assertj.core.api.Assertions.assertThat(countRows("change_history", "entity_name='role' and entity_id='R04'")).isGreaterThanOrEqualTo(1);

    mockMvc.perform(post("/api/roles")
        .header("Cookie", cookie)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
          {"roleCode":"BAD","roleName":"오류","purpose":"오류","grantCriteria":"오류","defaultDataScope":"ALL","isActive":"Y"}
          """))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.error.fields.roleCode").exists());
  }

  @Test
  void get_post_put_delete_api_user_roles_cover_assignment_lifecycle() throws Exception {
    String cookie = adminCookie();
    mockMvc.perform(get("/api/user-roles").header("Cookie", cookie))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.success").value(true))
      .andExpect(jsonPath("$.data.items", hasSize(greaterThanOrEqualTo(1))))
      .andExpect(jsonPath("$.data.items[0].assignmentId").exists());

    mockMvc.perform(post("/api/user-roles")
        .header("Cookie", cookie)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
          {"userId":"support-001","roleCode":"R08","assignmentType":"MANUAL","approvedByUserId":"admin","effectiveStartDate":"2026-01-01","effectiveEndDate":"2026-12-31","status":"ACTIVE","reason":"assignment create"}
          """))
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.success").value(true))
      .andExpect(jsonPath("$.data.assignmentId").isString())
      .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    org.assertj.core.api.Assertions.assertThat(countRows("user_role_assignment", "user_id='support-001' and role_code='R08' and status='ACTIVE'")).isEqualTo(1);

    mockMvc.perform(put("/api/user-roles/URA-support-R04")
        .header("Cookie", cookie)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
          {"userId":"support-001","roleCode":"R04","assignmentType":"MANUAL","approvedByUserId":"admin","effectiveStartDate":"2026-01-01","effectiveEndDate":"2026-12-31","status":"ACTIVE","reason":"assignment update"}
          """))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.success").value(true))
      .andExpect(jsonPath("$.data.assignmentId").value("URA-support-R04"))
      .andExpect(jsonPath("$.data.approvedByUserId").value("admin"));

    mockMvc.perform(delete("/api/user-roles/URA-support-R04").header("Cookie", cookie))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.success").value(true))
      .andExpect(jsonPath("$.data.assignmentId").value("URA-support-R04"))
      .andExpect(jsonPath("$.data.status").value("REVOKED"));
    org.assertj.core.api.Assertions.assertThat(stringValue("select status from user_role_assignment where assignment_id=?", "URA-support-R04")).isEqualTo("REVOKED");
    org.assertj.core.api.Assertions.assertThat(countRows("change_history", "entity_name='user_role_assignment' and entity_id='URA-support-R04'")).isGreaterThanOrEqualTo(1);

    mockMvc.perform(post("/api/user-roles")
        .header("Cookie", cookie)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
          {"userId":"support-001","roleCode":"R04","effectiveStartDate":"2026-12-31","effectiveEndDate":"2026-01-01"}
          """))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.error.fields.effectiveEndDate").exists());
  }

  @Test
  void get_put_api_menu_permissions_cover_upsert_side_effect_and_validation() throws Exception {
    String cookie = adminCookie();
    mockMvc.perform(get("/api/menu-permissions").header("Cookie", cookie))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.success").value(true))
      .andExpect(jsonPath("$.data.items", hasSize(greaterThanOrEqualTo(1))))
      .andExpect(jsonPath("$.data.items[0].targetType").value("ROLE"));

    mockMvc.perform(put("/api/menu-permissions")
        .header("Cookie", cookie)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
          {"targetType":"ROLE","targetId":"R08","permissions":[{"menuId":"SCR-USER-MGMT","permissionLevel":"READ"},{"menuId":"SCR-ROLE-MGMT","permissionLevel":"NONE"}],"reason":"permission upsert"}
          """))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.success").value(true))
      .andExpect(jsonPath("$.data.items", hasSize(2)))
      .andExpect(jsonPath("$.data.items[0].targetId").value("R08"));
    org.assertj.core.api.Assertions.assertThat(countRows("menu_permission", "target_type='ROLE' and target_id='R08'")).isEqualTo(2);
    org.assertj.core.api.Assertions.assertThat(countRows("change_history", "entity_name='menu_permission' and entity_id='ROLE:R08'")).isEqualTo(1);

    mockMvc.perform(put("/api/menu-permissions")
        .header("Cookie", cookie)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
          {"targetType":"BAD","targetId":"R08","permissions":[]}
          """))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.error.fields.targetType").exists());
  }

  @Test
  void get_post_put_api_menus_and_tree_parent_reorder_cover_menu_side_effects() throws Exception {
    String cookie = adminCookie();
    mockMvc.perform(get("/api/menus").header("Cookie", cookie))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.success").value(true))
      .andExpect(jsonPath("$.data.items", hasSize(greaterThanOrEqualTo(1))))
      .andExpect(jsonPath("$.data.items[0].menuId").exists());

    mockMvc.perform(get("/api/menus/tree").header("Cookie", cookie))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.success").value(true))
      .andExpect(jsonPath("$.data.items", hasSize(greaterThanOrEqualTo(1))));

    mockMvc.perform(get("/api/menus/SCR-USER-MGMT").header("Cookie", cookie))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.success").value(true))
      .andExpect(jsonPath("$.data.menuId").value("SCR-USER-MGMT"));

    mockMvc.perform(post("/api/menus")
        .header("Cookie", cookie)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
          {"menuId":"SCR-CONTRACT","parentMenuId":null,"menuName":"계약 테스트 메뉴","screenId":"SCR-CONTRACT","routePath":"/system/contract","iconName":"test","businessCategory":"MENU","description":"contract","displayOrder":30,"isActive":"Y","reason":"menu create"}
          """))
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.success").value(true))
      .andExpect(jsonPath("$.data.menuId").value("SCR-CONTRACT"));
    org.assertj.core.api.Assertions.assertThat(countRows("change_history", "entity_name='menu' and entity_id='SCR-CONTRACT'")).isEqualTo(1);

    mockMvc.perform(put("/api/menus/SCR-USER-MGMT")
        .header("Cookie", cookie)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
          {"parentMenuId":null,"menuName":"사용자 관리 수정","screenId":"SCR-USER-MGMT","routePath":"/system/users","iconName":"user","businessCategory":"USER_ORG","description":"사용자 관리 수정","displayOrder":1,"isActive":"N","reason":"menu update"}
          """))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.success").value(true))
      .andExpect(jsonPath("$.data.menuId").value("SCR-USER-MGMT"))
      .andExpect(jsonPath("$.data.isActive").value("N"));

    mockMvc.perform(put("/api/menus/SCR-ORG-MGMT/parent")
        .header("Cookie", cookie)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
          {"parentMenuId":"SCR-USER-MGMT","reason":"parent update"}
          """))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.success").value(true))
      .andExpect(jsonPath("$.data.menuId").value("SCR-ORG-MGMT"))
      .andExpect(jsonPath("$.data.parentMenuId").value("SCR-USER-MGMT"));

    mockMvc.perform(put("/api/menus/reorder")
        .header("Cookie", cookie)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
          {"items":[{"menuId":"SCR-USER-MGMT","displayOrder":11,"parentMenuId":null},{"menuId":"SCR-ORG-MGMT","displayOrder":12,"parentMenuId":"SCR-USER-MGMT"}],"reason":"menu reorder"}
          """))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.success").value(true))
      .andExpect(jsonPath("$.data.items").isArray());
    org.assertj.core.api.Assertions.assertThat(countRows("change_history", "entity_name='menu' and entity_id='reorder'")).isEqualTo(1);

    mockMvc.perform(put("/api/menus/SCR-USER-MGMT/parent")
        .header("Cookie", cookie)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
          {"parentMenuId":"SCR-USER-MGMT"}
          """))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.error.fields.parentMenuId").exists());
  }

  @Test
  void get_post_put_api_code_groups_and_group_details_cover_contracts() throws Exception {
    String cookie = adminCookie();
    mockMvc.perform(get("/api/code-groups").header("Cookie", cookie))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.success").value(true))
      .andExpect(jsonPath("$.data.items", hasSize(greaterThanOrEqualTo(1))))
      .andExpect(jsonPath("$.data.items[0].groupId").exists());

    mockMvc.perform(get("/api/code-groups/PROCESS_STATUS").header("Cookie", cookie))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.success").value(true))
      .andExpect(jsonPath("$.data.groupId").value("PROCESS_STATUS"));

    mockMvc.perform(get("/api/code-groups/PROCESS_STATUS/code-details").header("Cookie", cookie))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.success").value(true))
      .andExpect(jsonPath("$.data.items", hasSize(greaterThanOrEqualTo(1))))
      .andExpect(jsonPath("$.data.items[0].groupId").value("PROCESS_STATUS"));

    mockMvc.perform(post("/api/code-groups")
        .header("Cookie", cookie)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
          {"groupId":"CONTRACT_STATUS","groupName":"계약 상태","description":"계약 테스트 코드그룹","managingDepartment":"교수지원과","isActive":"Y","reason":"code group create"}
          """))
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.success").value(true))
      .andExpect(jsonPath("$.data.groupId").value("CONTRACT_STATUS"));
    org.assertj.core.api.Assertions.assertThat(countRows("change_history", "entity_name='code_group' and entity_id='CONTRACT_STATUS'")).isEqualTo(1);

    mockMvc.perform(put("/api/code-groups/PROCESS_STATUS")
        .header("Cookie", cookie)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
          {"groupName":"처리상태 수정","description":"공통 처리 상태 수정","managingDepartment":"교수지원과","isActive":"Y","reason":"code group update"}
          """))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.success").value(true))
      .andExpect(jsonPath("$.data.groupId").value("PROCESS_STATUS"))
      .andExpect(jsonPath("$.data.groupName").value("처리상태 수정"));

    mockMvc.perform(get("/api/code-groups/UNKNOWN_GROUP").header("Cookie", cookie))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.error.code").value("NOT_FOUND"));
  }

  @Test
  void get_post_put_api_code_details_cover_detail_side_effects_and_validation() throws Exception {
    String cookie = adminCookie();
    mockMvc.perform(get("/api/code-details").header("Cookie", cookie))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.success").value(true))
      .andExpect(jsonPath("$.data.items", hasSize(greaterThanOrEqualTo(1))))
      .andExpect(jsonPath("$.data.items[0].codeId").exists());

    mockMvc.perform(post("/api/code-details")
        .header("Cookie", cookie)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
          {"codeId":"CD-CONTRACT","groupId":"PROCESS_STATUS","codeValue":"CONTRACT","codeName":"계약 테스트","parentCodeId":null,"displayOrder":2,"extraAttributes":"{}","effectiveStartDate":"2026-01-01","effectiveEndDate":null,"isActive":"Y","reason":"code detail create"}
          """))
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.success").value(true))
      .andExpect(jsonPath("$.data.codeId").value("CD-CONTRACT"))
      .andExpect(jsonPath("$.data.groupId").value("PROCESS_STATUS"));
    org.assertj.core.api.Assertions.assertThat(countRows("change_history", "entity_name='code_detail' and entity_id='CD-CONTRACT'")).isEqualTo(1);

    mockMvc.perform(put("/api/code-details/CD-STATUS-ACTIVE")
        .header("Cookie", cookie)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
          {"groupId":"PROCESS_STATUS","codeValue":"ACTIVE","codeName":"활성 수정","parentCodeId":null,"displayOrder":1,"extraAttributes":"{}","effectiveStartDate":"2026-01-01","effectiveEndDate":null,"isActive":"Y","reason":"code detail update"}
          """))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.success").value(true))
      .andExpect(jsonPath("$.data.codeId").value("CD-STATUS-ACTIVE"))
      .andExpect(jsonPath("$.data.codeName").value("활성 수정"));

    mockMvc.perform(post("/api/code-details")
        .header("Cookie", cookie)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
          {"groupId":"PROCESS_STATUS","codeValue":"BROKEN","codeName":"오류","extraAttributes":"{}","effectiveStartDate":"2026-12-31","effectiveEndDate":"2026-01-01","isActive":"Y"}
          """))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.error.fields.effectiveEndDate").exists());
  }

  private String adminCookie() throws Exception {
    return loginCookie("admin", "admin");
  }

  private String supportCookie() throws Exception {
    return loginCookie("support01", "admin");
  }

  private String loginCookie(String loginId, String password) throws Exception {
    MvcResult login = mockMvc.perform(post("/api/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"loginId\":\"" + loginId + "\",\"password\":\"" + password + "\"}"))
      .andExpect(status().isCreated())
      .andReturn();
    return login.getResponse().getHeader("Set-Cookie");
  }

  private String sessionId(String cookie) {
    return cookie.substring(cookie.indexOf("session_id=") + "session_id=".length(), cookie.indexOf(';'));
  }

  private int countRows(String table, String predicate) {
    return jdbcTemplate.queryForObject("select count(*) from " + table + " where " + predicate, Integer.class);
  }

  private String stringValue(String sql, Object argument) {
    return jdbcTemplate.queryForObject(sql, String.class, argument);
  }
}
