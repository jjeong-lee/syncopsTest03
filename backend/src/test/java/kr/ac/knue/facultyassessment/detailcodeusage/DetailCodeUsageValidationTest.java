package kr.ac.knue.facultyassessment.detailcodeusage;

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
class DetailCodeUsageValidationTest {

    private static final String TARGET_GROUP_ID = "CG-EMPLOYMENT-STATUS";
    private static final String TARGET_CODE_VALUE = "ACTIVE";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void createDetailCodeFixture() {
        jdbcTemplate.update(
            "insert into code_group (group_id, group_name, use_yn) values (?, ?, 'Y') on conflict (group_id) do nothing",
            TARGET_GROUP_ID,
            "테스트 코드그룹"
        );
        jdbcTemplate.update(
            "insert into detail_code (detail_code_id, group_id, code_value, code_name, display_order, use_yn, effective_start_date) values (?, ?, ?, ?, 1, 'Y', current_date) on conflict (group_id, code_value) do nothing",
            "DETAIL-CODE-EMPLOYMENT-ACTIVE",
            TARGET_GROUP_ID,
            TARGET_CODE_VALUE,
            "테스트 코드"
        );
    }

    @Test
    void missingRequiredFieldsAndUnsupportedUseYnAreRejectedWithoutChangingDetailCode() throws Exception {
        Cookie session = loginAsAdmin();
        String useYnBefore = jdbcTemplate.queryForObject(
            "select use_yn from detail_code where group_id = ? and code_value = ?",
            String.class,
            TARGET_GROUP_ID,
            TARGET_CODE_VALUE
        );

        mockMvc.perform(post("/api/system/common-codes/usage")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"groupId\":\"" + TARGET_GROUP_ID + "\",\"codeValue\":\"" + TARGET_CODE_VALUE + "\",\"useYn\":\"Y\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.field").value("effectiveStartDate"));

        mockMvc.perform(post("/api/system/common-codes/usage")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"groupId\":\"" + TARGET_GROUP_ID + "\",\"codeValue\":\"" + TARGET_CODE_VALUE + "\",\"useYn\":\"X\",\"effectiveStartDate\":\"2026-01-01\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.field").value("useYn"));

        Assertions.assertEquals(useYnBefore, jdbcTemplate.queryForObject(
            "select use_yn from detail_code where group_id = ? and code_value = ?",
            String.class,
            TARGET_GROUP_ID,
            TARGET_CODE_VALUE
        ));
    }

    @Test
    void invertedEffectivePeriodIsRejectedWithoutChangingDetailCode() throws Exception {
        Cookie session = loginAsAdmin();
        String originalStart = jdbcTemplate.queryForObject(
            "select effective_start_date::text from detail_code where group_id = ? and code_value = ?",
            String.class,
            TARGET_GROUP_ID,
            TARGET_CODE_VALUE
        );

        mockMvc.perform(post("/api/system/common-codes/usage")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"groupId\":\"" + TARGET_GROUP_ID + "\",\"codeValue\":\"" + TARGET_CODE_VALUE + "\",\"useYn\":\"N\",\"effectiveStartDate\":\"2026-02-02\",\"effectiveEndDate\":\"2026-02-01\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.field").value("effectiveEndDate"));

        Assertions.assertEquals(originalStart, jdbcTemplate.queryForObject(
            "select effective_start_date::text from detail_code where group_id = ? and code_value = ?",
            String.class,
            TARGET_GROUP_ID,
            TARGET_CODE_VALUE
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
