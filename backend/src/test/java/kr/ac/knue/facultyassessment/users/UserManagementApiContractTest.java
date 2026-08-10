package kr.ac.knue.facultyassessment.users;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
import java.nio.charset.StandardCharsets;
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
class UserManagementApiContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void r09CanSearchUsersByEveryApprovedConditionAndSeeRequiredSnapshotFields() throws Exception {
        assertUserContractIsAvailable();
        Cookie session = loginAsAdmin();

        mockMvc.perform(get("/api/users")
                .cookie(session)
                .queryParam("personnelNo", "MEMBER-0001")
                .queryParam("name", "예시")
                .queryParam("organization", "한국교원")
                .queryParam("position", "교원")
                .queryParam("employmentStatus", "재직")
                .queryParam("roleCode", "R01")
                .queryParam("useYn", "Y"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[0].userId").value("member"))
            .andExpect(jsonPath("$.data[0].personnelNo").value("MEMBER-0001"))
            .andExpect(jsonPath("$.data[0].name").value("예시 사용자"))
            .andExpect(jsonPath("$.data[0].organization").value("한국교원대학교"))
            .andExpect(jsonPath("$.data[0].position").value("교원"))
            .andExpect(jsonPath("$.data[0].employmentStatus").value("재직"))
            .andExpect(jsonPath("$.data[0].roleCodes[0]").value("R01"))
            .andExpect(jsonPath("$.data[0].useYn").value("Y"))
            .andExpect(jsonPath("$.data[0].positionTitle").isEmpty())
            .andExpect(jsonPath("$.data[0].retirementDate").doesNotExist())
            .andExpect(jsonPath("$.data[0].lastSyncedAt").exists());
    }

    @Test
    void userSettingsUpdateRequeriesLocalStateWithoutChangingKorusSnapshotAndRecordsHistory() throws Exception {
        Cookie session = loginAsAdmin();
        String originalSnapshotName = jdbcTemplate.queryForObject(
            "select name from korus_personnel_snapshot where personnel_no = 'MEMBER-0001'",
            String.class
        );
        String originalPersonnelNo = jdbcTemplate.queryForObject(
            "select personnel_no from korus_personnel_snapshot where personnel_no = 'MEMBER-0001'",
            String.class
        );

        mockMvc.perform(patch("/api/users/member/settings")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"useYn\":\"N\",\"roleCodes\":[\"R02\"],\"reason\":\"업무 역할 조정\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/users")
                .cookie(session)
                .queryParam("roleCode", "R02")
                .queryParam("useYn", "N"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].userId").value("member"))
            .andExpect(jsonPath("$.data[0].useYn").value("N"))
            .andExpect(jsonPath("$.data[0].roleCodes[0]").value("R02"));

        org.junit.jupiter.api.Assertions.assertEquals(originalSnapshotName, jdbcTemplate.queryForObject(
            "select name from korus_personnel_snapshot where personnel_no = 'MEMBER-0001'",
            String.class
        ));
        org.junit.jupiter.api.Assertions.assertEquals(originalPersonnelNo, jdbcTemplate.queryForObject(
            "select personnel_no from korus_personnel_snapshot where personnel_no = 'MEMBER-0001'",
            String.class
        ));
        Integer historyCount = jdbcTemplate.queryForObject(
            "select count(*) from change_history where entity_name = 'user_account' and entity_id = 'member' "
                + "and actor_user_id = 'admin' and reason = '업무 역할 조정'",
            Integer.class
        );
        org.junit.jupiter.api.Assertions.assertEquals(1, historyCount);
    }

    @Test
    void invalidUseYnDoesNotWriteUserSettings() throws Exception {
        Cookie session = loginAsAdmin();
        String beforeUseYn = jdbcTemplate.queryForObject("select use_yn from user_account where user_id = 'member'", String.class);

        mockMvc.perform(patch("/api/users/member/settings")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"useYn\":\"INVALID\",\"roleCodes\":[\"R01\"]}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.field").value("useYn"));

        org.junit.jupiter.api.Assertions.assertEquals(beforeUseYn, jdbcTemplate.queryForObject(
            "select use_yn from user_account where user_id = 'member'",
            String.class
        ));
    }

    private Cookie loginAsAdmin() throws Exception {
        MvcResult login = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":\"admin\",\"password\":\"admin\"}"))
            .andExpect(status().isOk())
            .andReturn();
        return login.getResponse().getCookie("SESSION");
    }

    private void assertUserContractIsAvailable() throws Exception {
        String contract = new org.springframework.core.io.ClassPathResource("contracts/openapi.yaml").getContentAsString(StandardCharsets.UTF_8);
        org.junit.jupiter.api.Assertions.assertTrue(contract.contains("/users:"));
        org.junit.jupiter.api.Assertions.assertTrue(contract.contains("/users/{userId}/settings:"));
        org.junit.jupiter.api.Assertions.assertTrue(contract.contains("operationId: listUsers"));
        org.junit.jupiter.api.Assertions.assertTrue(contract.contains("operationId: updateUserSettings"));
    }
}
