package kr.ac.knue.facultyassessment.settings;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
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
class ReferenceYearManagementIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void r09CanSaveDifferentReferenceYearsAndRequeryTargetYearFlagsWithoutChangingExistingData() throws Exception {
        Cookie session = login("admin", "admin");
        int existingMenuCount = jdbcTemplate.queryForObject("select count(*) from menu", Integer.class);

        saveReferenceYears(session, 2026, 2025, 2027, "Y", "N")
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/settings/reference-years").cookie(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.currentEvaluationYear").value(2026))
            .andExpect(jsonPath("$.data.defaultSearchYear").value(2025))
            .andExpect(jsonPath("$.data.targetYear").value(2027))
            .andExpect(jsonPath("$.data.referenceDataCopyYn").value("Y"))
            .andExpect(jsonPath("$.data.initializationYn").value("N"));

        Assertions.assertEquals(existingMenuCount, jdbcTemplate.queryForObject("select count(*) from menu", Integer.class));
        Assertions.assertEquals(2027, jdbcTemplate.queryForObject(
            "select target_year from system_setting where setting_key = 'REFERENCE_DATA_COPY_YN'", Integer.class
        ));
    }

    @Test
    void invalidTargetYearFlagIsRejectedWithoutChangingStoredSettingsOrExistingData() throws Exception {
        Cookie session = login("admin", "admin");
        saveReferenceYears(session, 2026, 2025, 2027, "Y", "N").andExpect(status().isOk());
        int existingMenuCount = jdbcTemplate.queryForObject("select count(*) from menu", Integer.class);

        saveReferenceYears(session, 2028, 2024, 2027, "INVALID", "Y")
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.field").value("referenceDataCopyYn"));

        mockMvc.perform(get("/api/settings/reference-years").cookie(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.currentEvaluationYear").value(2026))
            .andExpect(jsonPath("$.data.defaultSearchYear").value(2025))
            .andExpect(jsonPath("$.data.targetYear").value(2027))
            .andExpect(jsonPath("$.data.referenceDataCopyYn").value("Y"))
            .andExpect(jsonPath("$.data.initializationYn").value("N"));

        Assertions.assertEquals(existingMenuCount, jdbcTemplate.queryForObject("select count(*) from menu", Integer.class));
    }

    @Test
    void missingOrUnauthorizedRequestsDoNotExposeOrChangeReferenceYearSettings() throws Exception {
        mockMvc.perform(get("/api/settings/reference-years"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error.code").value("UNAUTHENTICATED"));

        Cookie memberSession = login("member", "member");
        saveReferenceYears(memberSession, 2026, 2025, 2027, "Y", "N")
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));

        Cookie adminSession = login("admin", "admin");
        mockMvc.perform(post("/api/settings/reference-years")
                .cookie(adminSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"currentEvaluationYear\":2026,\"defaultSearchYear\":2025,\"referenceDataCopyYn\":\"Y\",\"initializationYn\":\"N\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.field").value("targetYear"));
    }

    private org.springframework.test.web.servlet.ResultActions saveReferenceYears(
        Cookie session,
        int currentEvaluationYear,
        int defaultSearchYear,
        int targetYear,
        String referenceDataCopyYn,
        String initializationYn
    ) throws Exception {
        return mockMvc.perform(post("/api/settings/reference-years")
            .cookie(session)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"currentEvaluationYear\":" + currentEvaluationYear
                + ",\"defaultSearchYear\":" + defaultSearchYear
                + ",\"targetYear\":" + targetYear
                + ",\"referenceDataCopyYn\":\"" + referenceDataCopyYn
                + "\",\"initializationYn\":\"" + initializationYn + "\"}"));
    }

    private Cookie login(String userId, String password) throws Exception {
        MvcResult login = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":\"" + userId + "\",\"password\":\"" + password + "\"}"))
            .andExpect(status().isOk())
            .andReturn();
        return login.getResponse().getCookie("SESSION");
    }
}
