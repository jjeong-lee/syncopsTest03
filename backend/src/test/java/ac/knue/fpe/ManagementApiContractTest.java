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

@SpringBootTest
@AutoConfigureMockMvc
class ManagementApiContractTest {
  @Autowired MockMvc mvc;

  @Test void openapi_fixture_is_available_on_classpath() {
    assert new ClassPathResource("contracts/openapi.yaml").exists();
  }

  @Test void health_returns_api_response_envelope() throws Exception {
    mvc.perform(get("/api/health")).andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true)).andExpect(jsonPath("$.data.status").value("UP"));
  }

  @Test void protected_api_distinguishes_unauthorized_from_forbidden() throws Exception {
    mvc.perform(get("/api/users")).andExpect(status().isUnauthorized()).andExpect(jsonPath("$.data.code").value("UNAUTHORIZED"));
    MvcResult teacher = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{\"loginId\":\"teacher\",\"password\":\"teacher\"}"))
        .andExpect(status().isOk()).andReturn();
    mvc.perform(get("/api/users").cookie(teacher.getResponse().getCookies())).andExpect(status().isForbidden()).andExpect(jsonPath("$.data.code").value("FORBIDDEN"));
  }

  @Test void admin_can_login_and_access_nine_screen_menus() throws Exception {
    MvcResult login = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{\"loginId\":\"admin\",\"password\":\"admin\"}"))
        .andExpect(status().isOk()).andExpect(jsonPath("$.data.roleCodes", hasItem("R09"))).andReturn();
    mvc.perform(get("/api/menus/current?level=SCREEN").cookie(login.getResponse().getCookies()))
        .andExpect(status().isOk()).andExpect(jsonPath("$.data", hasSize(9))).andExpect(jsonPath("$.data[*].screenId", hasItem("CMN-USER-MGMT")));
  }

  @Test void user_search_shows_korus_fields_and_local_use_flag() throws Exception {
    MvcResult login = adminLogin();
    mvc.perform(get("/api/users?staffName=관리자").cookie(login.getResponse().getCookies()))
        .andExpect(status().isOk()).andExpect(jsonPath("$.data.content[0].positionName").exists()).andExpect(jsonPath("$.data.content[0].lastSyncedAt").exists()).andExpect(jsonPath("$.data.content[0].systemUseYn").value("Y"));
  }

  @Test void validation_errors_are_field_level_for_representative_mutations() throws Exception {
    MvcResult login = adminLogin();
    mvc.perform(put("/api/roles/R09").cookie(login.getResponse().getCookies()).contentType(MediaType.APPLICATION_JSON).content("{\"roleCode\":\"R09\",\"roleName\":\"\"}"))
        .andExpect(status().isBadRequest()).andExpect(jsonPath("$.data.fieldErrors.roleName").exists());
  }

  @Test void smoke_all_nine_management_query_apis() throws Exception {
    MvcResult login = adminLogin();
    String[] urls = {"/api/users", "/api/organizations", "/api/roles", "/api/user-roles", "/api/menu-permissions", "/api/menus/tree", "/api/menus/tree", "/api/code-groups", "/api/code-groups/USER_STATUS/detail-codes"};
    for (String url : urls) mvc.perform(get(url).cookie(login.getResponse().getCookies())).andExpect(status().is2xxSuccessful()).andExpect(jsonPath("$.success").value(true));
  }

  private MvcResult adminLogin() throws Exception {
    return mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{\"loginId\":\"admin\",\"password\":\"admin\"}"))
        .andExpect(status().isOk()).andReturn();
  }
}
