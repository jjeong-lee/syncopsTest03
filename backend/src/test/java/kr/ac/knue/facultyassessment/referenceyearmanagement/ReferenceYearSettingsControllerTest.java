package kr.ac.knue.facultyassessment.referenceyearmanagement;

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
class ReferenceYearSettingsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void r09CanSaveAndRequeryReferenceYearSettings() throws Exception {
        assertReferenceYearContractIsAvailable();
        Cookie session = login("admin", "admin");

        mockMvc.perform(post("/api/system/settings/reference-years")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"currentEvaluationYear\":2026,\"defaultQueryYear\":2025,\"targetYear\":2027,\"baselineCopyYn\":\"예\",\"initializationYn\":\"아니오\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.currentEvaluationYear").value(2026))
            .andExpect(jsonPath("$.data.defaultQueryYear").value(2025))
            .andExpect(jsonPath("$.data.targetYear").value(2027))
            .andExpect(jsonPath("$.data.baselineCopyYn").value("예"))
            .andExpect(jsonPath("$.data.initializationYn").value("아니오"));

        mockMvc.perform(get("/api/system/settings/reference-years")
                .cookie(session)
                .param("targetYear", "2027")
                .param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[0].currentEvaluationYear").value(2026))
            .andExpect(jsonPath("$.data[0].defaultQueryYear").value(2025))
            .andExpect(jsonPath("$.data[0].targetYear").value(2027));

        Assertions.assertEquals("admin", jdbcTemplate.queryForObject(
            "select updated_by from reference_year_setting where target_year = ? and deleted_yn = 'N'",
            String.class,
            2027
        ));
        Assertions.assertEquals(1, jdbcTemplate.queryForObject(
            "select count(*) from reference_year_setting where target_year = ? and deleted_yn = 'N'",
            Integer.class,
            2027
        ));
    }

    @Test
    void nonAdministratorCannotSaveReferenceYearSettings() throws Exception {
        Cookie session = login("member", "member");

        mockMvc.perform(post("/api/system/settings/reference-years")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"currentEvaluationYear\":2026,\"defaultQueryYear\":2025,\"targetYear\":2027,\"baselineCopyYn\":\"예\",\"initializationYn\":\"아니오\"}"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").value(false))
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

    private void assertReferenceYearContractIsAvailable() throws Exception {
        String contract = new org.springframework.core.io.ClassPathResource("contracts/openapi.yaml")
            .getContentAsString(StandardCharsets.UTF_8);
        Assertions.assertTrue(contract.contains("/system/settings/reference-years:"));
        Assertions.assertTrue(contract.contains("operationId: listReferenceYearSettings"));
        Assertions.assertTrue(contract.contains("operationId: saveReferenceYearSettings"));
        Assertions.assertTrue(contract.contains("ReferenceYearSettingRequest:"));
    }
}
