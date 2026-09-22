package kr.ac.knue.facultyassessment.commonenvironmentsettings;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
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
class CommonEnvironmentSettingsValidationTest {

    private static final String PAGE_SIZE_KEY = "page_size";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void createPageSizeSetting() {
        jdbcTemplate.update(
            "insert into common_environment_setting (common_environment_setting_id, setting_key, setting_value, created_by, updated_by) values (?, ?, ?, ?, ?) on conflict (setting_key) do update set setting_value = excluded.setting_value, updated_by = excluded.updated_by, updated_at = current_timestamp",
            "COMMON-ENVIRONMENT-PAGE-SIZE-VALIDATION", PAGE_SIZE_KEY, "20", "admin", "admin"
        );
    }

    @Test
    void missingSettingValueAndInvalidPageSizeAreRejectedWithoutChangingStoredSetting() throws Exception {
        Cookie session = loginAsAdmin();
        String before = jdbcTemplate.queryForObject(
            "select setting_value from common_environment_setting where setting_key = ?",
            String.class,
            PAGE_SIZE_KEY
        );

        mockMvc.perform(post("/api/system/settings/common-environment")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"settingKey\":\"page_size\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.field").value("settingValue"));

        mockMvc.perform(post("/api/system/settings/common-environment")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"settingKey\":\"page_size\",\"settingValue\":\"25\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.field").value("settingValue"));

        Assertions.assertEquals(before, jdbcTemplate.queryForObject(
            "select setting_value from common_environment_setting where setting_key = ?",
            String.class,
            PAGE_SIZE_KEY
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
}