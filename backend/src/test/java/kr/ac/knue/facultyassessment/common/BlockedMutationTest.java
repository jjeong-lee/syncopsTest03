package kr.ac.knue.facultyassessment.common;

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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Rollback
class BlockedMutationTest {

    private static final String MENU_ID = "MENU-POSITION-ASSIGNMENT-MANAGEMENT";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void inactiveMenuBlocksDirectUrlExcelAndMutationWithoutChangingPersistence() throws Exception {
        Cookie session = loginAsAdmin();
        int before = jdbcTemplate.queryForObject("select count(*) from position_assignment", Integer.class);
        jdbcTemplate.update("update menu set use_yn = 'N' where menu_id = ?", MENU_ID);

        mockMvc.perform(get("/api/position-assignments").cookie(session))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
        mockMvc.perform(get("/api/position-assignments/export").cookie(session))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
        mockMvc.perform(post("/api/position-assignments").cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"positionCode\":\"CHAIR\",\"userId\":\"member\",\"organizationId\":\"ORG-KNUE\",\"effectiveStartDate\":\"2026-01-01\"}"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));

        Assertions.assertEquals(before, jdbcTemplate.queryForObject("select count(*) from position_assignment", Integer.class));
    }

    @Test
    void expiredMenuBlocksDirectUrlWithExistingApiErrorEnvelope() throws Exception {
        Cookie session = loginAsAdmin();
        jdbcTemplate.update(
            "update menu set use_yn = 'Y', exposure_start_at = current_timestamp - interval '2 days', exposure_end_at = current_timestamp - interval '1 second' where menu_id = ?",
            MENU_ID
        );

        mockMvc.perform(get("/api/position-assignments").cookie(session))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
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
