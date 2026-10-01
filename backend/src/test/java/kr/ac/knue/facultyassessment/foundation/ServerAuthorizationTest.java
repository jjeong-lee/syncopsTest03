package kr.ac.knue.facultyassessment.foundation;

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
class ServerAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void serverClockExcludesAMenuWhoseExposureHasNotStartedFromTheAuthenticatedMenu() throws Exception {
        jdbcTemplate.update(
            "update menu set exposure_start_at = current_timestamp + interval '1 hour', exposure_end_at = null where menu_id = ?",
            "MENU-MENU-INFORMATION-MANAGEMENT"
        );

        Cookie session = login();

        mockMvc.perform(get("/api/auth/me").cookie(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.menus[?(@.menuId == 'MENU-MENU-INFORMATION-MANAGEMENT')]").isEmpty());
    }

    @Test
    void serverBlocksDirectUrlAndExcelWhenTheTargetMenuIsOutsideItsExposurePeriod() throws Exception {
        jdbcTemplate.update(
            "update menu set exposure_start_at = current_timestamp + interval '1 hour', exposure_end_at = null where menu_id = ?",
            "MENU-POSITION-ASSIGNMENT-MANAGEMENT"
        );

        Cookie session = login();

        mockMvc.perform(get("/system/user-organization/positions").cookie(session))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
        mockMvc.perform(get("/api/position-assignments/export").cookie(session))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
    }

    @Test
    void blockedMenuMutationReturnsTheCommonErrorWithoutChangingStoredState() throws Exception {
        jdbcTemplate.update(
            "update menu set exposure_start_at = current_timestamp + interval '1 hour', exposure_end_at = null where menu_id = ?",
            "MENU-MENU-STRUCTURE-MANAGEMENT"
        );
        int historyBefore = jdbcTemplate.queryForObject(
            "select count(*) from change_history where entity_name = 'menu'", Integer.class
        );
        String originalName = jdbcTemplate.queryForObject(
            "select menu_name from menu where menu_id = 'MENU-MENU-INFORMATION-MANAGEMENT'", String.class
        );
        Cookie session = login();

        mockMvc.perform(post("/api/menus")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"menuName\":\"차단되어야 하는 변경\",\"parentMenuId\":\"MENU-MANAGEMENT\",\"displayOrder\":2,\"screenId\":\"SCR-MENU-INFORMATION-MANAGEMENT\",\"url\":\"/system/menus/information\",\"useYn\":\"Y\"}"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));

        Assertions.assertEquals(originalName, jdbcTemplate.queryForObject(
            "select menu_name from menu where menu_id = 'MENU-MENU-INFORMATION-MANAGEMENT'", String.class
        ));
        Assertions.assertEquals(historyBefore, jdbcTemplate.queryForObject(
            "select count(*) from change_history where entity_name = 'menu'", Integer.class
        ));
    }

    private Cookie login() throws Exception {
        MvcResult login = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":\"admin\",\"password\":\"admin\"}"))
            .andExpect(status().isOk())
            .andReturn();
        return login.getResponse().getCookie("SESSION");
    }
}
