package kr.ac.knue.facultyassessment.menuusage;

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
class MenuUsageValidationTest {

    private static final String TARGET_MENU_ID = "MENU-MENU-INFORMATION-MANAGEMENT";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void missingRequiredFieldsAndUnsupportedUseYnAreRejectedWithoutChangingMenu() throws Exception {
        Cookie session = loginAsAdmin();
        String useYnBefore = jdbcTemplate.queryForObject(
            "select use_yn from menu where menu_id = ?", String.class, TARGET_MENU_ID
        );

        mockMvc.perform(post("/api/system/menus/usage")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"menuId\":\"" + TARGET_MENU_ID + "\",\"useYn\":\"Y\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.field").value("exposureStartAt"));

        mockMvc.perform(post("/api/system/menus/usage")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"menuId\":\"" + TARGET_MENU_ID + "\",\"useYn\":\"X\",\"exposureStartAt\":\"2026-01-01T00:00:00Z\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.field").value("useYn"));

        Assertions.assertEquals(useYnBefore, jdbcTemplate.queryForObject(
            "select use_yn from menu where menu_id = ?", String.class, TARGET_MENU_ID
        ));
    }

    @Test
    void invertedExposurePeriodIsRejectedWithoutChangingMenu() throws Exception {
        Cookie session = loginAsAdmin();
        String originalStart = jdbcTemplate.queryForObject(
            "select exposure_start_at::text from menu where menu_id = ?", String.class, TARGET_MENU_ID
        );

        mockMvc.perform(post("/api/system/menus/usage")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"menuId\":\"" + TARGET_MENU_ID + "\",\"useYn\":\"N\",\"exposureStartAt\":\"2026-02-02T00:00:00Z\",\"exposureEndAt\":\"2026-02-01T00:00:00Z\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.field").value("exposureEndAt"));

        Assertions.assertEquals(originalStart, jdbcTemplate.queryForObject(
            "select exposure_start_at::text from menu where menu_id = ?", String.class, TARGET_MENU_ID
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
