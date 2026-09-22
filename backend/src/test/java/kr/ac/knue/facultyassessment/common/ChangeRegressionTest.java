package kr.ac.knue.facultyassessment.common;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
import java.time.LocalDate;
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
class ChangeRegressionTest {

    private static final String GROUP_ID = "CG-EMPLOYMENT-STATUS";
    private static final String CODE_VALUE = "ACTIVE";
    private static final String MENU_ID = "MENU-DETAIL-CODE-USAGE-MANAGEMENT";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void createDetailCodeFixture() {
        jdbcTemplate.update(
            "insert into code_group (group_id, group_name, use_yn) values (?, ?, 'Y') on conflict (group_id) do nothing",
            GROUP_ID,
            "회귀 검증 코드그룹"
        );
        jdbcTemplate.update(
            "insert into detail_code (detail_code_id, group_id, code_value, code_name, display_order, use_yn, effective_start_date) "
                + "values (?, ?, ?, ?, 1, 'Y', current_date) on conflict (group_id, code_value) do nothing",
            "DETAIL-CODE-REGRESSION-ACTIVE",
            GROUP_ID,
            CODE_VALUE,
            "회귀 검증 코드"
        );
    }

    @Test
    void disablingCodePreservesItsIdentityAndRecordsTheAuditTrail() throws Exception {
        Cookie session = loginAsAdmin();
        String createdAtBefore = jdbcTemplate.queryForObject(
            "select created_at::text from detail_code where group_id = ? and code_value = ?",
            String.class,
            GROUP_ID,
            CODE_VALUE
        );
        int historyBefore = jdbcTemplate.queryForObject(
            "select count(*) from change_history where entity_name = 'detail_code' and entity_id = ?",
            Integer.class,
            GROUP_ID + ":" + CODE_VALUE
        );
        String effectiveStartDate = LocalDate.now().minusDays(1).toString();

        mockMvc.perform(post("/api/system/common-codes/usage")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"groupId\":\"" + GROUP_ID + "\",\"codeValue\":\"" + CODE_VALUE
                    + "\",\"useYn\":\"N\",\"effectiveStartDate\":\"" + effectiveStartDate + "\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.codeValue").value(CODE_VALUE))
            .andExpect(jsonPath("$.data.useYn").value("N"));

        mockMvc.perform(get("/api/system/common-codes/usage")
                .cookie(session)
                .param("groupId", GROUP_ID)
                .param("codeValue", CODE_VALUE)
                .param("useYn", "N"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].codeValue").value(CODE_VALUE))
            .andExpect(jsonPath("$.data[0].useYn").value("N"));

        Assertions.assertEquals(CODE_VALUE, jdbcTemplate.queryForObject(
            "select code_value from detail_code where group_id = ? and code_value = ?",
            String.class,
            GROUP_ID,
            CODE_VALUE
        ));
        Assertions.assertEquals(createdAtBefore, jdbcTemplate.queryForObject(
            "select created_at::text from detail_code where group_id = ? and code_value = ?",
            String.class,
            GROUP_ID,
            CODE_VALUE
        ));
        Assertions.assertEquals("admin", jdbcTemplate.queryForObject(
            "select updated_by from detail_code where group_id = ? and code_value = ?",
            String.class,
            GROUP_ID,
            CODE_VALUE
        ));
        Assertions.assertEquals(historyBefore + 1, jdbcTemplate.queryForObject(
            "select count(*) from change_history where entity_name = 'detail_code' and entity_id = ?",
            Integer.class,
            GROUP_ID + ":" + CODE_VALUE
        ));
    }

    @Test
    void expiredMenuPeriodRejectsDirectUsageApiAccessWithoutChangingTheCode() throws Exception {
        Cookie session = loginAsAdmin();
        String useYnBefore = jdbcTemplate.queryForObject(
            "select use_yn from detail_code where group_id = ? and code_value = ?",
            String.class,
            GROUP_ID,
            CODE_VALUE
        );
        jdbcTemplate.update(
            "update menu set use_yn = 'Y', exposure_start_at = current_timestamp - interval '2 days', "
                + "exposure_end_at = current_timestamp - interval '1 second' where menu_id = ?",
            MENU_ID
        );

        mockMvc.perform(get("/api/system/common-codes/usage").cookie(session))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));

        Assertions.assertEquals(useYnBefore, jdbcTemplate.queryForObject(
            "select use_yn from detail_code where group_id = ? and code_value = ?",
            String.class,
            GROUP_ID,
            CODE_VALUE
        ));
    }

    @Test
    void referenceYearOptionsPersistAsSettingsWithoutCopyingOrInitializingReferenceData() throws Exception {
        Cookie session = loginAsAdmin();
        int menuCountBefore = jdbcTemplate.queryForObject("select count(*) from menu", Integer.class);
        int detailCodeCountBefore = jdbcTemplate.queryForObject("select count(*) from detail_code", Integer.class);

        mockMvc.perform(post("/api/system/settings/reference-years")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"currentEvaluationYear\":2030,\"defaultQueryYear\":2030,\"targetYear\":2030,"
                    + "\"baselineCopyYn\":\"예\",\"initializationYn\":\"아니오\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.targetYear").value(2030))
            .andExpect(jsonPath("$.data.baselineCopyYn").value("예"))
            .andExpect(jsonPath("$.data.initializationYn").value("아니오"));

        mockMvc.perform(get("/api/system/settings/reference-years")
                .cookie(session)
                .param("targetYear", "2030"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].targetYear").value(2030))
            .andExpect(jsonPath("$.data[0].baselineCopyYn").value("예"))
            .andExpect(jsonPath("$.data[0].initializationYn").value("아니오"));

        Assertions.assertEquals(menuCountBefore, jdbcTemplate.queryForObject("select count(*) from menu", Integer.class));
        Assertions.assertEquals(detailCodeCountBefore, jdbcTemplate.queryForObject("select count(*) from detail_code", Integer.class));
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
