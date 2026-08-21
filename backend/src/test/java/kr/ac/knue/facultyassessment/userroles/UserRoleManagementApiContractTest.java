package kr.ac.knue.facultyassessment.userroles;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
class UserRoleManagementApiContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void r09CanListCurrentRolesAndGrantAdditionalRoleThenRequeryApprovalAndEffectiveDates() throws Exception {
        assertUserRoleContractIsAvailable();
        Cookie session = loginAsAdmin();

        mockMvc.perform(get("/api/users/member/roles").cookie(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[0].roleCode").value("R01"))
            .andExpect(jsonPath("$.data[0].approvalUserId").value("admin"))
            .andExpect(jsonPath("$.data[0].effectiveStartDate").exists())
            .andExpect(jsonPath("$.data[0].assignmentType").exists());

        mockMvc.perform(post("/api/users/member/roles")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"roleCode\":\"R02\",\"approvalUserId\":\"admin\",\"effectiveStartDate\":\"2026-08-09\",\"effectiveEndDate\":\"2026-12-31\",\"reason\":\"학과장 업무 역할 부여\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/users/member/roles").cookie(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(2))
            .andExpect(jsonPath("$.data[1].roleCode").value("R02"))
            .andExpect(jsonPath("$.data[1].approvalUserId").value("admin"))
            .andExpect(jsonPath("$.data[1].effectiveStartDate").value("2026-08-09"))
            .andExpect(jsonPath("$.data[1].effectiveEndDate").value("2026-12-31"));

        Assertions.assertEquals(1, jdbcTemplate.queryForObject(
            "select count(*) from change_history where entity_name = 'user_role' and entity_id = 'member:R02' "
                + "and actor_user_id = 'admin' and reason = '학과장 업무 역할 부여'",
            Integer.class
        ));
    }

    @Test
    void userRoleSaveRejectsMissingApproverAndUnknownRoleWithoutWriting() throws Exception {
        Cookie session = loginAsAdmin();
        Integer beforeUserRoleCount = jdbcTemplate.queryForObject("select count(*) from user_role where user_id = 'member'", Integer.class);
        Integer beforeHistoryCount = jdbcTemplate.queryForObject("select count(*) from change_history where entity_name = 'user_role'", Integer.class);

        mockMvc.perform(post("/api/users/member/roles")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"roleCode\":\"R02\",\"effectiveStartDate\":\"2026-08-09\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.field").value("approvalUserId"));

        mockMvc.perform(post("/api/users/member/roles")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"roleCode\":\"R10\",\"approvalUserId\":\"admin\",\"effectiveStartDate\":\"2026-08-09\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.field").value("roleCode"));

        Assertions.assertEquals(beforeUserRoleCount, jdbcTemplate.queryForObject("select count(*) from user_role where user_id = 'member'", Integer.class));
        Assertions.assertEquals(beforeHistoryCount, jdbcTemplate.queryForObject("select count(*) from change_history where entity_name = 'user_role'", Integer.class));
    }

    @Test
    void expiredRoleIsNotCurrentAndRevocationIsPersistedWithItsApprovalHistory() throws Exception {
        Cookie session = loginAsAdmin();

        mockMvc.perform(post("/api/users/member/roles")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"roleCode\":\"R02\",\"approvalUserId\":\"admin\",\"effectiveStartDate\":\"2026-08-01\",\"effectiveEndDate\":\"2026-08-20\",\"reason\":\"종료된 역할\"}"))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/users/member/roles").cookie(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(1))
            .andExpect(jsonPath("$.data[0].roleCode").value("R01"));

        String userRoleId = jdbcTemplate.queryForObject(
            "select user_role_id from user_role where user_id = 'member' and role_code = 'R01'",
            String.class
        );
        mockMvc.perform(delete("/api/users/member/roles/{userRoleId}", userRoleId)
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"approvalUserId\":\"admin\",\"reason\":\"역할 회수\"}"))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/users/member/roles").cookie(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isEmpty());
        Assertions.assertEquals("REVOKED", jdbcTemplate.queryForObject(
            "select status from user_role where user_role_id = ?", String.class, userRoleId
        ));
        Assertions.assertEquals(1, jdbcTemplate.queryForObject(
            "select count(*) from change_history where entity_name = 'user_role' and entity_id = ? "
                + "and actor_user_id = 'admin' and reason = '역할 회수'",
            Integer.class,
            "member:" + userRoleId
        ));
    }

    @Test
    void nonAdministratorCannotManageUserRoles() throws Exception {
        Cookie session = loginAsMember();

        mockMvc.perform(post("/api/users/member/roles")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"roleCode\":\"R02\",\"approvalUserId\":\"admin\",\"effectiveStartDate\":\"2026-08-09\"}"))
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

    private void assertUserRoleContractIsAvailable() throws Exception {
        String contract = new org.springframework.core.io.ClassPathResource("contracts/openapi.yaml").getContentAsString(StandardCharsets.UTF_8);
        Assertions.assertTrue(contract.contains("/users/{userId}/roles:"));
        Assertions.assertTrue(contract.contains("operationId: listUserRoles"));
        Assertions.assertTrue(contract.contains("operationId: saveUserRole"));
        Assertions.assertTrue(contract.contains("UserRoleRequest:"));
    }
}
