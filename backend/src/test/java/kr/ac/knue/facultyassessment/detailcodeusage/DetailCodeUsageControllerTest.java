package kr.ac.knue.facultyassessment.detailcodeusage;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
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
class DetailCodeUsageControllerTest {

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
    void r09CanSaveAndRequeryDetailCodeUsageAndEffectivePeriod() throws Exception {
        assertDetailCodeUsageContractIsAvailable();
        Cookie session = login("admin", "admin");
        String effectiveStartDate = LocalDate.now().minusDays(1).toString();
        String effectiveEndDate = LocalDate.now().plusDays(3).toString();

        mockMvc.perform(post("/api/system/common-codes/usage")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"groupId\":\"" + TARGET_GROUP_ID + "\",\"codeValue\":\"" + TARGET_CODE_VALUE + "\",\"useYn\":\"Y\",\"effectiveStartDate\":\"" + effectiveStartDate + "\",\"effectiveEndDate\":\"" + effectiveEndDate + "\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.groupId").value(TARGET_GROUP_ID))
            .andExpect(jsonPath("$.data.codeValue").value(TARGET_CODE_VALUE))
            .andExpect(jsonPath("$.data.useYn").value("Y"));

        mockMvc.perform(get("/api/system/common-codes/usage")
                .cookie(session)
                .param("groupId", TARGET_GROUP_ID)
                .param("codeValue", TARGET_CODE_VALUE)
                .param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[0].groupId").value(TARGET_GROUP_ID))
            .andExpect(jsonPath("$.data[0].codeValue").value(TARGET_CODE_VALUE))
            .andExpect(jsonPath("$.data[0].effectiveStartDate").value(effectiveStartDate));

        Assertions.assertEquals("admin", jdbcTemplate.queryForObject(
            "select updated_by from detail_code where group_id = ? and code_value = ?",
            String.class,
            TARGET_GROUP_ID,
            TARGET_CODE_VALUE
        ));
    }

    @Test
    void nonAdministratorCannotSaveDetailCodeUsageSettings() throws Exception {
        Cookie session = login("member", "member");

        mockMvc.perform(post("/api/system/common-codes/usage")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"groupId\":\"" + TARGET_GROUP_ID + "\",\"codeValue\":\"" + TARGET_CODE_VALUE + "\",\"useYn\":\"Y\",\"effectiveStartDate\":\"2026-01-01\"}"))
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

    private void assertDetailCodeUsageContractIsAvailable() throws Exception {
        String contract = new org.springframework.core.io.ClassPathResource("contracts/openapi.yaml")
            .getContentAsString(StandardCharsets.UTF_8);
        Assertions.assertTrue(contract.contains("/system/common-codes/usage:"));
        Assertions.assertTrue(contract.contains("operationId: listDetailCodeUsageSettings"));
        Assertions.assertTrue(contract.contains("operationId: saveDetailCodeUsageSettings"));
        Assertions.assertTrue(contract.contains("DetailCodeUsageSettingRequest:"));
    }
}
