package kr.ac.knue.facultyassessment.menuusage;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
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
class MenuUsageControllerTest {

    private static final String TARGET_MENU_ID = "MENU-MENU-INFORMATION-MANAGEMENT";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void r09CanSaveAndRequeryMenuUsageAndExposurePeriod() throws Exception {
        assertMenuUsageContractIsAvailable();
        Cookie session = login("admin", "admin");
        String exposureStartAt = OffsetDateTime.now().minusMinutes(1).withNano(0).toString();
        String exposureEndAt = OffsetDateTime.now().plusDays(3).withNano(0).toString();

        mockMvc.perform(post("/api/system/menus/usage")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"menuId\":\"" + TARGET_MENU_ID + "\",\"useYn\":\"Y\",\"exposureStartAt\":\"" + exposureStartAt + "\",\"exposureEndAt\":\"" + exposureEndAt + "\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.menuId").value(TARGET_MENU_ID))
            .andExpect(jsonPath("$.data.useYn").value("Y"));

        mockMvc.perform(get("/api/system/menus/usage")
                .cookie(session)
                .param("menuId", TARGET_MENU_ID)
                .param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[0].menuId").value(TARGET_MENU_ID))
            .andExpect(jsonPath("$.data[0].exposureStartAt").value(exposureStartAt));

        Assertions.assertEquals("admin", jdbcTemplate.queryForObject(
            "select updated_by from menu where menu_id = ?", String.class, TARGET_MENU_ID
        ));
    }

    @Test
    void nonAdministratorCannotSaveMenuUsageSettings() throws Exception {
        Cookie session = login("member", "member");

        mockMvc.perform(post("/api/system/menus/usage")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"menuId\":\"" + TARGET_MENU_ID + "\",\"useYn\":\"Y\",\"exposureStartAt\":\"2026-01-01T00:00:00Z\"}"))
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

    private void assertMenuUsageContractIsAvailable() throws Exception {
        String contract = new org.springframework.core.io.ClassPathResource("contracts/openapi.yaml")
            .getContentAsString(StandardCharsets.UTF_8);
        Assertions.assertTrue(contract.contains("/system/menus/usage:"));
        Assertions.assertTrue(contract.contains("operationId: listMenuUsageSettings"));
        Assertions.assertTrue(contract.contains("operationId: saveMenuUsageSettings"));
        Assertions.assertTrue(contract.contains("MenuUsageSettingRequest:"));
    }
}
