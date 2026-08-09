package kr.ac.knue.facultyassessment.organizations;

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
class OrganizationManagementApiContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void r09CanSearchOrganizationsByCodeAndSeeParentChildHierarchy() throws Exception {
        assertOrganizationContractIsAvailable();
        Cookie session = loginAsAdmin();

        mockMvc.perform(get("/api/organizations")
                .cookie(session)
                .queryParam("organizationCode", "KNUE"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[0].organizationId").value("ORG-KNUE"))
            .andExpect(jsonPath("$.data[0].organizationCode").value("KNUE"))
            .andExpect(jsonPath("$.data[0].organizationName").value("한국교원대학교"))
            .andExpect(jsonPath("$.data[0].children[0].organizationCode").value("KNUE-EDU"));
    }

    @Test
    void r09CanSaveOrganizationRelationshipAndRequeryItWithoutChangingKorusSnapshot() throws Exception {
        Cookie session = loginAsAdmin();
        String originalSnapshotOrganization = jdbcTemplate.queryForObject(
            "select organization_code from korus_personnel_snapshot where personnel_no = 'MEMBER-0001'",
            String.class
        );

        mockMvc.perform(put("/api/organizations/ORG-KNUE-EDU/relationship")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"parentOrganizationId\":\"ORG-KNUE\",\"effectiveStartDate\":\"2026-01-01\",\"effectiveEndDate\":\"2026-12-31\",\"reason\":\"조직 관계 적용기간 조정\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/organizations")
                .cookie(session)
                .queryParam("organizationCode", "KNUE-EDU"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].parentOrganizationId").value("ORG-KNUE"))
            .andExpect(jsonPath("$.data[0].effectiveStartDate").value("2026-01-01"))
            .andExpect(jsonPath("$.data[0].effectiveEndDate").value("2026-12-31"));

        Assertions.assertEquals(originalSnapshotOrganization, jdbcTemplate.queryForObject(
            "select organization_code from korus_personnel_snapshot where personnel_no = 'MEMBER-0001'",
            String.class
        ));
        Assertions.assertEquals(1, jdbcTemplate.queryForObject(
            "select count(*) from change_history where entity_name = 'organization_relationship' "
                + "and entity_id = 'ORG-KNUE-EDU' and actor_user_id = 'admin' and reason = '조직 관계 적용기간 조정'",
            Integer.class
        ));
    }

    @Test
    void relationshipSaveRejectsMissingEffectiveStartDateWithoutWriting() throws Exception {
        Cookie session = loginAsAdmin();
        Integer before = jdbcTemplate.queryForObject(
            "select count(*) from change_history where entity_name = 'organization_relationship' and entity_id = 'ORG-KNUE-EDU'",
            Integer.class
        );

        mockMvc.perform(put("/api/organizations/ORG-KNUE-EDU/relationship")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"parentOrganizationId\":\"ORG-KNUE\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.field").value("effectiveStartDate"));

        Assertions.assertEquals(before, jdbcTemplate.queryForObject(
            "select count(*) from change_history where entity_name = 'organization_relationship' and entity_id = 'ORG-KNUE-EDU'",
            Integer.class
        ));
    }

    @Test
    void nonAdministratorCannotManageOrganizations() throws Exception {
        Cookie session = loginAsMember();

        mockMvc.perform(put("/api/organizations/ORG-KNUE-EDU/relationship")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"parentOrganizationId\":\"ORG-KNUE\",\"effectiveStartDate\":\"2026-01-01\"}"))
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

    private void assertOrganizationContractIsAvailable() throws Exception {
        String contract = new org.springframework.core.io.ClassPathResource("contracts/openapi.yaml").getContentAsString(StandardCharsets.UTF_8);
        Assertions.assertTrue(contract.contains("/organizations:"));
        Assertions.assertTrue(contract.contains("/organizations/{organizationId}/relationship:"));
        Assertions.assertTrue(contract.contains("operationId: listOrganizations"));
        Assertions.assertTrue(contract.contains("operationId: saveOrganizationRelationship"));
    }
}
