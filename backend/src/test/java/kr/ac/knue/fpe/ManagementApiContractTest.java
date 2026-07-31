package kr.ac.knue.fpe;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Map;
import kr.ac.knue.fpe.common.ChangeHistoryService;
import kr.ac.knue.fpe.common.CommonController;
import kr.ac.knue.fpe.common.CommonMapper;
import kr.ac.knue.fpe.common.GlobalExceptionHandler;
import kr.ac.knue.fpe.common.SecurityConfig;
import kr.ac.knue.fpe.common.SessionAuthorizationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(CommonController.class)
@AutoConfigureMockMvc
@Import({GlobalExceptionHandler.class, SecurityConfig.class, SessionAuthorizationFilter.class})
class ManagementApiContractTest {
  @Autowired MockMvc mockMvc;
  @MockBean CommonMapper mapper;
  @MockBean ChangeHistoryService history;

  @Test
  void openApiFixtureIsMaterializedOnClasspath() throws Exception {
    var resource = new ClassPathResource("contracts/openapi.yaml");
    assert resource.exists();
  }

  @Test
  void adminCanLoginAndReadMainApiGroups() throws Exception {
    Map<String, Object> admin = adminUser();
    when(mapper.findUserByLoginId("admin")).thenReturn(admin);
    when(mapper.findActiveRoleCodes("00000000-0000-0000-0000-000000000901")).thenReturn(List.of("R09"));
    when(mapper.listMenus(Map.of("useYn", "Y"))).thenReturn(List.of(Map.of("menuId", "system-users", "urlPath", "/system/users")));
    when(mapper.findUserBySession(any())).thenReturn(admin);
    when(mapper.listUsers(any())).thenReturn(List.of(admin));
    when(mapper.listOrganizations(any())).thenReturn(List.of(Map.of("organizationId", "org-root")));
    when(mapper.listRoles(any())).thenReturn(List.of(Map.of("roleCode", "R09")));
    when(mapper.listCodeGroups(any())).thenReturn(List.of(Map.of("groupId", "STATUS")));

    MvcResult login = mockMvc.perform(post("/api/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"loginId\":\"admin\",\"password\":\"admin\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.roles[0]").value("R09"))
        .andReturn();
    String cookie = login.getResponse().getCookie("FPE_SESSION").getValue();
    mockMvc.perform(get("/api/users").cookie(new jakarta.servlet.http.Cookie("FPE_SESSION", cookie))).andExpect(status().isOk()).andExpect(jsonPath("$.data").isArray());
    mockMvc.perform(get("/api/organizations").cookie(new jakarta.servlet.http.Cookie("FPE_SESSION", cookie))).andExpect(status().isOk()).andExpect(jsonPath("$.data").isArray());
    mockMvc.perform(get("/api/roles").cookie(new jakarta.servlet.http.Cookie("FPE_SESSION", cookie))).andExpect(status().isOk()).andExpect(jsonPath("$.data").isArray());
    mockMvc.perform(get("/api/menus").cookie(new jakarta.servlet.http.Cookie("FPE_SESSION", cookie))).andExpect(status().isOk()).andExpect(jsonPath("$.data").isArray());
    mockMvc.perform(get("/api/code-groups").cookie(new jakarta.servlet.http.Cookie("FPE_SESSION", cookie))).andExpect(status().isOk()).andExpect(jsonPath("$.data").isArray());
  }

  @Test
  void protectedApiRejectsMissingSessionAndInvalidMutationHasFieldError() throws Exception {
    Map<String, Object> admin = adminUser();
    when(mapper.findUserByLoginId("admin")).thenReturn(admin);
    when(mapper.findActiveRoleCodes("00000000-0000-0000-0000-000000000901")).thenReturn(List.of("R09"));
    when(mapper.listMenus(Map.of("useYn", "Y"))).thenReturn(List.of());
    when(mapper.findUserBySession(any())).thenReturn(admin);

    mockMvc.perform(get("/api/users"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    MvcResult login = mockMvc.perform(post("/api/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"loginId\":\"admin\",\"password\":\"admin\"}"))
        .andExpect(status().isOk()).andReturn();
    String cookie = login.getResponse().getCookie("FPE_SESSION").getValue();
    mockMvc.perform(patch("/api/users/00000000-0000-0000-0000-000000000902/usage")
        .cookie(new jakarta.servlet.http.Cookie("FPE_SESSION", cookie))
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"systemUseYn\":\"X\",\"reason\":\"validation test\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.fieldErrors.systemUseYn").exists());
  }

  private Map<String, Object> adminUser() {
    return Map.of(
        "userId", "00000000-0000-0000-0000-000000000901",
        "loginId", "admin",
        "passwordHash", "admin",
        "systemUseYn", "Y",
        "accountStatus", "ACTIVE",
        "staffName", "관리자");
  }
}
