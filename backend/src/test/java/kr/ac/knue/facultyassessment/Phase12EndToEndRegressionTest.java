package kr.ac.knue.facultyassessment;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Rollback
class Phase12EndToEndRegressionTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void seedAdministratorCanReachEveryTargetScreenAndItsReadFlow() throws Exception {
        assertOpenApiFixtureDescribesAllTargetReadOperations();
        Cookie session = loginAsAdmin();

        mockMvc.perform(get("/api/auth/me").cookie(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.menus[?(@.route == '/system/user-organization/users')]").isNotEmpty())
            .andExpect(jsonPath("$.data.menus[?(@.route == '/system/user-organization/organizations')]").isNotEmpty())
            .andExpect(jsonPath("$.data.menus[?(@.route == '/system/roles-permissions/roles')]").isNotEmpty())
            .andExpect(jsonPath("$.data.menus[?(@.route == '/system/roles-permissions/user-roles')]").isNotEmpty())
            .andExpect(jsonPath("$.data.menus[?(@.route == '/system/roles-permissions/menu-permissions')]").isNotEmpty())
            .andExpect(jsonPath("$.data.menus[?(@.route == '/system/menus/structure')]").isNotEmpty())
            .andExpect(jsonPath("$.data.menus[?(@.route == '/system/menus/information')]").isNotEmpty())
            .andExpect(jsonPath("$.data.menus[?(@.route == '/system/common-codes/groups')]").isNotEmpty())
            .andExpect(jsonPath("$.data.menus[?(@.route == '/system/common-codes/detail-codes')]").isNotEmpty());

        mockMvc.perform(get("/api/users").cookie(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
        mockMvc.perform(get("/api/organizations").cookie(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
        mockMvc.perform(get("/api/roles").cookie(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
        mockMvc.perform(get("/api/users/member/roles").cookie(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
        mockMvc.perform(get("/api/menu-permissions").cookie(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
        mockMvc.perform(get("/api/menus").cookie(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
        mockMvc.perform(get("/api/code-groups").cookie(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
        mockMvc.perform(get("/api/code-groups/CG-EMPLOYMENT-STATUS/detail-codes").cookie(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void seedAdministratorCanSaveUserSettingsAndObserveTheChangedValueOnRequery() throws Exception {
        Cookie session = loginAsAdmin();

        mockMvc.perform(patch("/api/users/member/settings")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"useYn\":\"N\",\"roleCodes\":[\"R01\"],\"reason\":\"Phase 12 저장 후 재조회\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/users").cookie(session).queryParam("personnelNo", "MEMBER-0001"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].userId").value("member"))
            .andExpect(jsonPath("$.data[0].useYn").value("N"));
    }

    @Test
    void unauthenticatedRequestToTargetReadApiIsRejectedWithoutSensitiveErrorDetails() throws Exception {
        mockMvc.perform(get("/api/users"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("UNAUTHENTICATED"))
            .andExpect(jsonPath("$.error.message").value("인증 세션이 필요합니다."));
    }

    private Cookie loginAsAdmin() throws Exception {
        MvcResult login = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":\"admin\",\"password\":\"admin\"}"))
            .andExpect(status().isOk())
            .andReturn();
        return login.getResponse().getCookie("SESSION");
    }

    private void assertOpenApiFixtureDescribesAllTargetReadOperations() throws Exception {
        String contract = new org.springframework.core.io.ClassPathResource("contracts/openapi.yaml")
            .getContentAsString(StandardCharsets.UTF_8);
        for (String operationId : new String[] {
            "listUsers", "listOrganizations", "listRoles", "listUserRoles", "listMenuPermissions",
            "listMenus", "listCodeGroups", "listDetailCodes"
        }) {
            Assertions.assertTrue(contract.contains("operationId: " + operationId));
        }
    }
}
