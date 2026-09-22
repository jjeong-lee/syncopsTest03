package kr.ac.knue.facultyassessment.referenceyearmanagement;

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
class ReferenceYearSettingsValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void rejectsMissingTargetYearWithoutChangingReferenceYearSettings() throws Exception {
        Cookie session = login();
        int before = activeSettingCount();

        mockMvc.perform(post("/api/system/settings/reference-years")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"currentEvaluationYear\":2026,\"defaultQueryYear\":2025,\"baselineCopyYn\":\"예\",\"initializationYn\":\"아니오\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.field").value("targetYear"));

        Assertions.assertEquals(before, activeSettingCount());
    }

    @Test
    void rejectsClosedValuesWithoutChangingReferenceYearSettings() throws Exception {
        Cookie session = login();
        int before = activeSettingCount();

        mockMvc.perform(post("/api/system/settings/reference-years")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"currentEvaluationYear\":2026,\"defaultQueryYear\":2025,\"targetYear\":2027,\"baselineCopyYn\":\"Y\",\"initializationYn\":\"아니오\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("INVALID_REFERENCE_YEAR_OPTION"))
            .andExpect(jsonPath("$.error.field").value("baselineCopyYn"));

        Assertions.assertEquals(before, activeSettingCount());
    }

    private Cookie login() throws Exception {
        MvcResult login = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":\"admin\",\"password\":\"admin\"}"))
            .andExpect(status().isOk())
            .andReturn();
        return login.getResponse().getCookie("SESSION");
    }

    private int activeSettingCount() {
        return jdbcTemplate.queryForObject(
            "select count(*) from reference_year_setting where deleted_yn = 'N'",
            Integer.class
        );
    }
}
