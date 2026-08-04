package kr.ac.knue.fpe.contract;

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
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class CommonApiContractTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16.2-alpine")
        .withDatabaseName("fpe_common_test")
        .withUsername("fpe_app")
        .withPassword("fpe_app_password");

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired MockMvc mockMvc;

    @Test
    void openapi_fixture_is_available_from_classpath() throws Exception {
        var resource = new ClassPathResource("contracts/openapi.yaml");
        org.assertj.core.api.Assertions.assertThat(resource.exists()).isTrue();
        org.assertj.core.api.Assertions.assertThat(resource.getContentAsString(java.nio.charset.StandardCharsets.UTF_8)).contains("operationId: login");
    }

    @Test
    void health_returns_api_response_envelope() throws Exception {
        mockMvc.perform(get("/api/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.status").value("UP"));
    }

    @Test
    void login_me_logout_flow_uses_session_cookie_and_hides_password_hash() throws Exception {
        MvcResult login = loginAsAdmin();
        String cookie = login.getResponse().getCookie("SESSION").getValue();
        mockMvc.perform(get("/api/auth/me").cookie(new jakarta.servlet.http.Cookie("SESSION", cookie)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.roles", hasItem("R09")))
            .andExpect(jsonPath("$.data.passwordHash").doesNotExist())
            .andExpect(jsonPath("$.data.menus[*].url", hasItem("/system/users")));
        mockMvc.perform(post("/api/auth/logout").cookie(new jakarta.servlet.http.Cookie("SESSION", cookie)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void protected_api_requires_session_and_returns_api_error_envelope() throws Exception {
        mockMvc.perform(get("/api/users"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }

    @Test
    void user_search_and_usage_update_preserve_korus_readonly_fields() throws Exception {
        var session = sessionCookie();
        mockMvc.perform(get("/api/users").cookie(session).param("staffName", "김"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.items[0].staffNo").exists())
            .andExpect(jsonPath("$.data.items[0].dutyName").exists())
            .andExpect(jsonPath("$.data.items[0].lastSyncedAt").exists());
        mockMvc.perform(patch("/api/users/faculty01/usage").cookie(session).contentType(MediaType.APPLICATION_JSON)
                .content("{\"systemUseYn\":\"N\",\"staffName\":\"변조\",\"changeReason\":\"readonly guard\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.fields[0].field").value("korus"));
        mockMvc.perform(patch("/api/users/faculty01/usage").cookie(session).contentType(MediaType.APPLICATION_JSON)
                .content("{\"systemUseYn\":\"N\",\"changeReason\":\"사용 중지 테스트\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.systemUseYn").value("N"));
    }

    @Test
    void organization_relation_update_validates_date_range_and_followup_read() throws Exception {
        var session = sessionCookie();
        mockMvc.perform(put("/api/organizations/DEP-COMMON/relation").cookie(session).contentType(MediaType.APPLICATION_JSON)
                .content("{\"parentOrganizationCode\":\"COL-EDU\",\"effectiveStartDate\":\"2026-09-01\",\"effectiveEndDate\":\"2026-01-01\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.fields[0].field").value("effectiveEndDate"));
        mockMvc.perform(get("/api/organizations/tree").cookie(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[*].organizationCode", hasItem("DEP-COMMON")));
    }

    @Test
    void role_user_role_menu_permission_and_code_management_contracts_are_exercised() throws Exception {
        var session = sessionCookie();
        mockMvc.perform(get("/api/roles").cookie(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[*].roleCode", hasItems("R01", "R09")));
        mockMvc.perform(put("/api/roles/R01").cookie(session).contentType(MediaType.APPLICATION_JSON)
                .content("{\"roleCode\":\"R99\",\"roleName\":\"교원\",\"purpose\":\"본인 업무\",\"grantCriteria\":\"보직 기준\",\"dataScopeDefault\":\"본인\"}"))
            .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/user-roles/grants").cookie(session).contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":\"faculty01\",\"roleCode\":\"R02\",\"assignmentType\":\"MANUAL\",\"validFrom\":\"2026-01-01\",\"approvedBy\":\"admin\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.roleCode").value("R02"));
        mockMvc.perform(get("/api/menus/tree").cookie(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[*].screenId", hasItem("CMN-FR-001")));
        mockMvc.perform(put("/api/menu-permissions").cookie(session).contentType(MediaType.APPLICATION_JSON)
                .content("{\"targetType\":\"ROLE\",\"targetId\":\"R09\",\"permissions\":[{\"menuId\":\"M-USERS\",\"accessAllowedYn\":\"Y\",\"explicitDenyYn\":\"N\"}]}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.savedCount").value(1));
        mockMvc.perform(post("/api/code-groups").cookie(session).contentType(MediaType.APPLICATION_JSON)
                .content("{\"groupId\":\"TEST_STATUS\",\"groupName\":\"테스트상태\",\"managementDepartment\":\"시스템관리\"}"))
            .andExpect(status().isCreated());
        mockMvc.perform(post("/api/code-groups/TEST_STATUS/codes").cookie(session).contentType(MediaType.APPLICATION_JSON)
                .content("{\"codeValue\":\"READY\",\"codeName\":\"준비\",\"sortOrder\":1,\"useYn\":\"Y\"}"))
            .andExpect(status().isCreated());
    }

    @Test
    void out_of_scope_business_apis_are_not_exposed() throws Exception {
        var session = sessionCookie();
        mockMvc.perform(get("/api/evaluation/rules").cookie(session))
            .andExpect(status().isNotFound());
        mockMvc.perform(post("/api/attachments").cookie(session))
            .andExpect(status().isNotFound());
    }

    private MvcResult loginAsAdmin() throws Exception {
        return mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"admin\",\"password\":\"admin\"}"))
            .andExpect(status().isOk())
            .andExpect(cookie().exists("SESSION"))
            .andReturn();
    }

    private jakarta.servlet.http.Cookie sessionCookie() throws Exception {
        return loginAsAdmin().getResponse().getCookie("SESSION");
    }
}
