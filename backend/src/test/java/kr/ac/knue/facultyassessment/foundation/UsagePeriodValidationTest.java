package kr.ac.knue.facultyassessment.foundation;

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
class UsagePeriodValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void menuUsesTimestampPeriodAndRejectsAnInvertedRangeWithoutChangingTheRow() throws Exception {
        Cookie session = login();
        String menuId = "MENU-MENU-INFORMATION-MANAGEMENT";
        String originalName = jdbcTemplate.queryForObject(
            "select menu_name from menu where menu_id = ?", String.class, menuId
        );

        mockMvc.perform(post("/api/menus")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"menuName\":\"변경되면 안 되는 메뉴\",\"parentMenuId\":\"MENU-MANAGEMENT\",\"displayOrder\":2,\"screenId\":\"SCR-MENU-INFORMATION-MANAGEMENT\",\"url\":\"/system/menus/information\",\"useYn\":\"Y\",\"exposureStartAt\":\"2026-10-02T10:00:00Z\",\"exposureEndAt\":\"2026-10-01T10:00:00Z\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.field").value("exposureEndAt"));

        Assertions.assertEquals(originalName, jdbcTemplate.queryForObject(
            "select menu_name from menu where menu_id = ?", String.class, menuId
        ));
    }

    @Test
    void detailCodeUsesDatePeriodAndRejectsAnInvertedRangeWithoutChangingTheRow() throws Exception {
        Cookie session = login();
        mockMvc.perform(post("/api/code-groups")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"groupId\":\"CG-USAGE-PERIOD\",\"groupName\":\"기간 검증 코드그룹\"}"))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/code-groups/{groupId}/detail-codes", "CG-USAGE-PERIOD")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"codeValue\":\"DATE-ONLY\",\"codeName\":\"일자 단위 코드\",\"displayOrder\":1,\"useYn\":\"Y\",\"applicationStartDate\":\"2026-10-02\",\"applicationEndDate\":\"2026-10-01\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.field").value("applicationEndDate"));

        Assertions.assertEquals(0, jdbcTemplate.queryForObject(
            "select count(*) from detail_code where group_id = 'CG-USAGE-PERIOD' and code_value = 'DATE-ONLY'",
            Integer.class
        ));
    }

    @Test
    void nullableEndPersistsAnOpenEndedTimestampAndDatePeriod() throws Exception {
        Cookie session = login();
        mockMvc.perform(post("/api/menus")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"menuName\":\"메뉴 정보 관리\",\"parentMenuId\":\"MENU-MANAGEMENT\",\"displayOrder\":2,\"screenId\":\"SCR-MENU-INFORMATION-MANAGEMENT\",\"url\":\"/system/menus/information\",\"useYn\":\"Y\",\"exposureStartAt\":\"2026-10-01T09:30:00Z\",\"exposureEndAt\":null}"))
            .andExpect(status().isOk());

        Assertions.assertEquals(java.time.OffsetDateTime.parse("2026-10-01T09:30:00Z"), jdbcTemplate.queryForObject(
            "select exposure_start_at from menu where menu_id = 'MENU-MENU-INFORMATION-MANAGEMENT'",
            java.time.OffsetDateTime.class
        ));
        Assertions.assertNull(jdbcTemplate.queryForObject(
            "select exposure_end_at from menu where menu_id = 'MENU-MENU-INFORMATION-MANAGEMENT'",
            java.time.OffsetDateTime.class
        ));

        mockMvc.perform(post("/api/code-groups")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"groupId\":\"CG-OPEN-ENDED\",\"groupName\":\"종료일 없는 기간 코드그룹\"}"))
            .andExpect(status().isOk());
        mockMvc.perform(post("/api/code-groups/{groupId}/detail-codes", "CG-OPEN-ENDED")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"codeValue\":\"OPEN-ENDED\",\"codeName\":\"종료일 없는 코드\",\"displayOrder\":1,\"useYn\":\"Y\",\"applicationStartDate\":\"2026-10-01\",\"applicationEndDate\":null}"))
            .andExpect(status().isOk());

        Assertions.assertEquals(java.time.LocalDate.parse("2026-10-01"), jdbcTemplate.queryForObject(
            "select application_start_date from detail_code where group_id = 'CG-OPEN-ENDED' and code_value = 'OPEN-ENDED'",
            java.time.LocalDate.class
        ));
        Assertions.assertNull(jdbcTemplate.queryForObject(
            "select application_end_date from detail_code where group_id = 'CG-OPEN-ENDED' and code_value = 'OPEN-ENDED'",
            java.time.LocalDate.class
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
