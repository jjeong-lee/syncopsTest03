package kr.ac.knue.facultyassessment.roles;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Rollback
class RoleManagementApiContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void r09CanListRolePurposesAndPreservesRoleCodeAfterRoleNameUpdate() throws Exception {
        assertRoleContractIsAvailable();
        Cookie session = loginAsAdmin();

        mockMvc.perform(get("/api/roles").cookie(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[0].roleCode").exists())
            .andExpect(jsonPath("$.data[0].roleName").exists())
            .andExpect(jsonPath("$.data[0].purpose").exists());

        mockMvc.perform(post("/api/roles")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"roleCode\":\"R09\",\"roleName\":\"시스템 운영 관리자\",\"purpose\":\"시스템 관리 기능을 운영하는 관리자 역할\",\"assignmentCriteria\":\"시스템 운영 담당자로 지정된 사용자\",\"defaultDataScope\":\"시스템 관리 전체\",\"reason\":\"운영 역할명 정비\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/roles").cookie(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[8].roleCode").value("R09"))
            .andExpect(jsonPath("$.data[8].roleName").value("시스템 운영 관리자"))
            .andExpect(jsonPath("$.data[8].purpose").value("시스템 관리 기능을 운영하는 관리자 역할"))
            .andExpect(jsonPath("$.data[8].assignmentCriteria").value("시스템 운영 담당자로 지정된 사용자"))
            .andExpect(jsonPath("$.data[8].defaultDataScope").value("시스템 관리 전체"));

        Assertions.assertEquals("R09", jdbcTemplate.queryForObject(
            "select role_code from role where role_code = 'R09'", String.class
        ));
        Assertions.assertEquals(1, jdbcTemplate.queryForObject(
            "select count(*) from change_history where entity_name = 'role' and entity_id = 'R09' "
                + "and actor_user_id = 'admin' and reason = '운영 역할명 정비'",
            Integer.class
        ));
    }

    @Test
    void roleSaveRejectsBlankRoleNameWithoutWriting() throws Exception {
        Cookie session = loginAsAdmin();
        Integer before = jdbcTemplate.queryForObject(
            "select count(*) from change_history where entity_name = 'role' and entity_id = 'R09'",
            Integer.class
        );

        mockMvc.perform(post("/api/roles")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"roleCode\":\"R09\",\"roleName\":\"\",\"purpose\":\"관리자 역할\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.field").value("roleName"));

        Assertions.assertEquals(before, jdbcTemplate.queryForObject(
            "select count(*) from change_history where entity_name = 'role' and entity_id = 'R09'",
            Integer.class
        ));
    }

    @Test
    void roleSaveRejectsUnknownRoleCodeAndKeepsExistingRoleCode() throws Exception {
        Cookie session = loginAsAdmin();

        mockMvc.perform(post("/api/roles")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"roleCode\":\"R10\",\"roleName\":\"다른 역할\",\"purpose\":\"허용되지 않는 역할\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.field").value("roleCode"));

        Assertions.assertEquals(9, jdbcTemplate.queryForObject("select count(*) from role", Integer.class));
        Assertions.assertEquals("R09", jdbcTemplate.queryForObject(
            "select role_code from role where role_name = '시스템관리자'", String.class
        ));
    }

    @Test
    void nonAdministratorCannotManageRoles() throws Exception {
        Cookie session = loginAsMember();

        mockMvc.perform(post("/api/roles")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"roleCode\":\"R09\",\"roleName\":\"시스템관리자\",\"purpose\":\"관리자 역할\"}"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
    }

    private Cookie loginAsAdmin() throws Exception {
        return login("admin", "admin");
    }

    private Cookie loginAsMember() throws Exception {
        return login("member", "member");
    }

    private Cookie login(String userId, String password) throws Exception {
        MvcResult login = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":\"" + userId + "\",\"password\":\"" + password + "\"}"))
            .andExpect(status().isOk())
            .andReturn();
        return login.getResponse().getCookie("SESSION");
    }

    private void assertRoleContractIsAvailable() throws Exception {
        String contract = new org.springframework.core.io.ClassPathResource("contracts/openapi.yaml").getContentAsString(StandardCharsets.UTF_8);
        Assertions.assertTrue(contract.contains("/roles:"));
        Assertions.assertTrue(contract.contains("operationId: listRoles"));
        Assertions.assertTrue(contract.contains("operationId: saveRole"));
        Assertions.assertTrue(contract.contains("RoleRequest:"));
    }
}
