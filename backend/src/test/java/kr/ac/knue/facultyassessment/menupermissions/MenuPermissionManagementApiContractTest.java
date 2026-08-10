package kr.ac.knue.facultyassessment.menupermissions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Rollback
class MenuPermissionManagementApiContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void r09CanFilterRoleOrganizationAndUserMenuPermissionsAndRequerySavedAccess() throws Exception {
        assertMenuPermissionContractIsAvailable();
        Cookie session = login("admin", "admin");

        mockMvc.perform(get("/api/menu-permissions")
                .cookie(session)
                .param("subjectType", "ROLE")
                .param("subjectId", "R09"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[0].subjectType").value("ROLE"))
            .andExpect(jsonPath("$.data[0].majorMenuName").value("시스템 관리"))
            .andExpect(jsonPath("$.data[0].middleMenuName").exists())
            .andExpect(jsonPath("$.data[0].screenName").exists());

        mockMvc.perform(put("/api/menu-permissions")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"subjectType\":\"ORGANIZATION\",\"subjectId\":\"ORG-KNUE\",\"menuId\":\"MENU-ROLE-MANAGEMENT\",\"accessAllowed\":\"Y\",\"reason\":\"조직 운영 권한 부여\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/menu-permissions")
                .cookie(session)
                .param("subjectType", "ORGANIZATION")
                .param("subjectId", "ORG-KNUE")
                .param("menuId", "MENU-ROLE-MANAGEMENT"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].accessAllowed").value("Y"))
            .andExpect(jsonPath("$.data[0].screenName").value("역할 관리"));

        Assertions.assertEquals(1, jdbcTemplate.queryForObject(
            "select count(*) from change_history where entity_name = 'menu_permission' and entity_id = 'ORGANIZATION:ORG-KNUE:MENU-ROLE-MANAGEMENT' and actor_user_id = 'admin'",
            Integer.class
        ));
    }

    @Test
    void savedUserMenuPermissionIsUsedForBothCurrentMenuVisibilityAndProtectedApiAccess() throws Exception {
        Cookie adminSession = login("admin", "admin");

        mockMvc.perform(put("/api/menu-permissions")
                .cookie(adminSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"subjectType\":\"USER\",\"subjectId\":\"member\",\"menuId\":\"MENU-USER-MANAGEMENT\",\"accessAllowed\":\"Y\",\"reason\":\"사용자 조회 권한 부여\"}"))
            .andExpect(status().isOk());

        Cookie memberSession = login("member", "member");
        mockMvc.perform(get("/api/auth/me").cookie(memberSession))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.menus[0].menuId").value("MENU-USER-MANAGEMENT"));

        mockMvc.perform(get("/api/users").cookie(memberSession))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void menuPermissionSaveRejectsMissingSubjectTypeAndUnknownMenuWithoutWriting() throws Exception {
        Cookie session = login("admin", "admin");
        Integer beforePermissions = jdbcTemplate.queryForObject("select count(*) from menu_permission", Integer.class);
        Integer beforeHistory = jdbcTemplate.queryForObject("select count(*) from change_history where entity_name = 'menu_permission'", Integer.class);

        mockMvc.perform(put("/api/menu-permissions")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"subjectId\":\"member\",\"menuId\":\"MENU-USER-MANAGEMENT\",\"accessAllowed\":\"Y\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.field").value("subjectType"));

        mockMvc.perform(put("/api/menu-permissions")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"subjectType\":\"USER\",\"subjectId\":\"member\",\"menuId\":\"MENU-NOT-FOUND\",\"accessAllowed\":\"Y\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.field").value("menuId"));

        Assertions.assertEquals(beforePermissions, jdbcTemplate.queryForObject("select count(*) from menu_permission", Integer.class));
        Assertions.assertEquals(beforeHistory, jdbcTemplate.queryForObject("select count(*) from change_history where entity_name = 'menu_permission'", Integer.class));
    }

    @Test
    void nonAdministratorCannotManageMenuPermissions() throws Exception {
        Cookie session = login("member", "member");

        mockMvc.perform(put("/api/menu-permissions")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"subjectType\":\"USER\",\"subjectId\":\"member\",\"menuId\":\"MENU-USER-MANAGEMENT\",\"accessAllowed\":\"Y\"}"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
    }

    private Cookie login(String userId, String password) throws Exception {
        MvcResult login = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":\"" + userId + "\",\"password\":\"" + password + "\"}"))
            .andExpect(status().isOk())
            .andReturn();
        return login.getResponse().getCookie("SESSION");
    }

    private void assertMenuPermissionContractIsAvailable() throws Exception {
        String contract = new org.springframework.core.io.ClassPathResource("contracts/openapi.yaml").getContentAsString(StandardCharsets.UTF_8);
        Assertions.assertTrue(contract.contains("/menu-permissions:"));
        Assertions.assertTrue(contract.contains("operationId: listMenuPermissions"));
        Assertions.assertTrue(contract.contains("operationId: saveMenuPermissions"));
        Assertions.assertTrue(contract.contains("MenuPermissionRequest:"));
    }
}
