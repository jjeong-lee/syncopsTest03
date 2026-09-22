package kr.ac.knue.facultyassessment.commonenvironmentsettings;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
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
class CommonEnvironmentSettingsControllerTest {

    private static final String PAGE_SIZE_KEY = "page_size";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void createPageSizeSetting() {
        jdbcTemplate.update(
            "insert into common_environment_setting (common_environment_setting_id, setting_key, setting_value, created_by, updated_by) values (?, ?, ?, ?, ?) on conflict (setting_key) do update set setting_value = excluded.setting_value, updated_by = excluded.updated_by, updated_at = current_timestamp",
            "COMMON-ENVIRONMENT-PAGE-SIZE", PAGE_SIZE_KEY, "20", "admin", "admin"
        );
    }

    @Test
    void r09CanSaveAndRequeryCommonEnvironmentSetting() throws Exception {
        assertCommonEnvironmentContractIsAvailable();
        Cookie session = login("admin", "admin");

        mockMvc.perform(post("/api/system/settings/common-environment")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"settingKey\":\"page_size\",\"settingValue\":\"50\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.settingKey").value(PAGE_SIZE_KEY))
            .andExpect(jsonPath("$.data.settingValue").value("50"));

        mockMvc.perform(get("/api/system/settings/common-environment")
                .cookie(session)
                .param("settingKey", PAGE_SIZE_KEY)
                .param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[0].settingKey").value(PAGE_SIZE_KEY))
            .andExpect(jsonPath("$.data[0].settingValue").value("50"));

        Assertions.assertEquals("admin", jdbcTemplate.queryForObject(
            "select updated_by from common_environment_setting where setting_key = ?",
            String.class,
            PAGE_SIZE_KEY
        ));
    }

    @Test
    void nonAdministratorCannotSaveCommonEnvironmentSetting() throws Exception {
        Cookie session = login("member", "member");

        mockMvc.perform(post("/api/system/settings/common-environment")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"settingKey\":\"page_size\",\"settingValue\":\"50\"}"))
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

    private void assertCommonEnvironmentContractIsAvailable() throws Exception {
        String contract = new org.springframework.core.io.ClassPathResource("contracts/openapi.yaml")
            .getContentAsString(StandardCharsets.UTF_8);
        Assertions.assertTrue(contract.contains("/system/settings/common-environment:"));
        Assertions.assertTrue(contract.contains("operationId: listCommonEnvironmentSettings"));
        Assertions.assertTrue(contract.contains("operationId: saveCommonEnvironmentSettings"));
        Assertions.assertTrue(contract.contains("CommonEnvironmentSettingRequest:"));
    }
}