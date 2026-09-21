package kr.ac.knue.facultyassessment.assignments;

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
class AssignmentManagementApiContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void savesPositionAssignmentFiltersByReferenceDateAndRejectsOverlappingPeriodWithoutMutation() throws Exception {
        assertAssignmentManagementContractIsAvailable();
        Cookie session = loginAsAdmin();
        String request = "{\"positionCode\":\"CHAIR\",\"userId\":\"member\",\"organizationId\":\"ORG-KNUE\",\"effectiveStartDate\":\"2026-01-01\",\"effectiveEndDate\":\"2026-12-31\"}";

        mockMvc.perform(post("/api/position-assignments").cookie(session).contentType(MediaType.APPLICATION_JSON).content(request))
            .andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true));
        mockMvc.perform(get("/api/position-assignments").cookie(session).queryParam("referenceDate", "2026-06-01"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data[0].positionCode").value("CHAIR"));

        mockMvc.perform(post("/api/position-assignments").cookie(session).contentType(MediaType.APPLICATION_JSON)
                .content("{\"positionCode\":\"CHAIR\",\"userId\":\"member\",\"organizationId\":\"ORG-KNUE\",\"effectiveStartDate\":\"2026-12-31\"}"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error.code").value("OVERLAPPING_EFFECTIVE_PERIOD"));
        Assertions.assertEquals(1, jdbcTemplate.queryForObject("select count(*) from position_assignment where position_code = 'CHAIR'", Integer.class));
    }

    @Test
    void savesWorkAssignmentAndRejectsInvalidDateRange() throws Exception {
        Cookie session = loginAsAdmin();
        mockMvc.perform(post("/api/work-assignments").cookie(session).contentType(MediaType.APPLICATION_JSON)
                .content("{\"organizationId\":\"ORG-KNUE\",\"userId\":\"member\",\"workArea\":\"ACADEMIC\",\"effectiveStartDate\":\"2026-01-01\",\"dataScopeType\":\"전체\",\"processPermission\":\"Y\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true));
        mockMvc.perform(get("/api/work-assignments").cookie(session).queryParam("referenceDate", "2026-01-01"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data[0].workArea").value("ACADEMIC"));
        mockMvc.perform(post("/api/work-assignments").cookie(session).contentType(MediaType.APPLICATION_JSON)
                .content("{\"organizationId\":\"ORG-KNUE\",\"userId\":\"member\",\"workArea\":\"LATE\",\"effectiveStartDate\":\"2026-12-31\",\"effectiveEndDate\":\"2026-01-01\",\"dataScopeType\":\"전체\",\"processPermission\":\"Y\"}"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error.field").value("effectiveEndDate"));
    }

    @Test
    void savesRoleDataScopeWithoutChangingRoleAndRejectsUnknownOrganization() throws Exception {
        Cookie session = loginAsAdmin();
        String roleName = jdbcTemplate.queryForObject("select role_name from role where role_code = 'R09'", String.class);
        mockMvc.perform(post("/api/role-data-scopes").cookie(session).contentType(MediaType.APPLICATION_JSON)
                .content("{\"roleCode\":\"R09\",\"dataScopeType\":\"전체\",\"organizationCode\":\"KNUE\",\"workArea\":\"ACADEMIC\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true));
        mockMvc.perform(get("/api/role-data-scopes").cookie(session).queryParam("roleCode", "R09"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data[0].roleCode").value("R09"));
        Assertions.assertEquals(roleName, jdbcTemplate.queryForObject("select role_name from role where role_code = 'R09'", String.class));
        mockMvc.perform(post("/api/role-data-scopes").cookie(session).contentType(MediaType.APPLICATION_JSON)
                .content("{\"roleCode\":\"R09\",\"dataScopeType\":\"전체\",\"organizationCode\":\"UNKNOWN\"}"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error.field").value("organizationCode"));
    }

    @Test
    void exportsPositionAssignmentsAsXlsxAndRejectsAnonymousRequest() throws Exception {
        Cookie session = loginAsAdmin();

        mockMvc.perform(get("/api/position-assignments/export").cookie(session).queryParam("referenceDate", "2026-01-01"))
            .andExpect(status().isOk())
            .andExpect(result -> Assertions.assertTrue(result.getResponse().getContentType().startsWith("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")))
            .andExpect(result -> Assertions.assertTrue(result.getResponse().getContentAsByteArray().length > 0));
        mockMvc.perform(get("/api/position-assignments/export"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error.code").exists());
    }

    @Test
    void exportsWorkAssignmentsAsXlsxAndRejectsAnonymousRequest() throws Exception {
        Cookie session = loginAsAdmin();

        mockMvc.perform(get("/api/work-assignments/export").cookie(session).queryParam("referenceDate", "2026-01-01"))
            .andExpect(status().isOk())
            .andExpect(result -> Assertions.assertTrue(result.getResponse().getContentType().startsWith("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")))
            .andExpect(result -> Assertions.assertTrue(result.getResponse().getContentAsByteArray().length > 0));
        mockMvc.perform(get("/api/work-assignments/export"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error.code").exists());
    }

    @Test
    void exportsRoleDataScopesAsXlsxAndRejectsAnonymousRequest() throws Exception {
        Cookie session = loginAsAdmin();

        mockMvc.perform(get("/api/role-data-scopes/export").cookie(session).queryParam("roleCode", "R09"))
            .andExpect(status().isOk())
            .andExpect(result -> Assertions.assertTrue(result.getResponse().getContentType().startsWith("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")))
            .andExpect(result -> Assertions.assertTrue(result.getResponse().getContentAsByteArray().length > 0));
        mockMvc.perform(get("/api/role-data-scopes/export"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error.code").exists());
    }

    private void assertAssignmentManagementContractIsAvailable() throws Exception {
        String contract = new org.springframework.core.io.ClassPathResource("contracts/openapi.yaml").getContentAsString(StandardCharsets.UTF_8);
        Assertions.assertTrue(contract.contains("operationId: listPositionAssignments"));
        Assertions.assertTrue(contract.contains("operationId: exportPositionAssignments"));
        Assertions.assertTrue(contract.contains("operationId: listWorkAssignments"));
        Assertions.assertTrue(contract.contains("operationId: exportWorkAssignments"));
        Assertions.assertTrue(contract.contains("operationId: listRoleDataScopes"));
        Assertions.assertTrue(contract.contains("operationId: exportRoleDataScopes"));
    }

    private Cookie loginAsAdmin() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":\"admin\",\"password\":\"admin\"}"))
            .andExpect(status().isOk()).andReturn();
        return result.getResponse().getCookie("SESSION");
    }
}
