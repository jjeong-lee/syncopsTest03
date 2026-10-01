package kr.ac.knue.facultyassessment.detailcodes;

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
class CodeUsageManagementIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void r09CanSaveUsagePeriodThenDistinguishDefaultAndHistoricalCodeQueriesAndReactivateIt() throws Exception {
        Cookie session = login("admin", "admin");
        createCodeGroup(session, "CG-CODE-USAGE");

        saveDetailCode(session, "CG-CODE-USAGE", "HISTORICAL", "과거 코드", "N", "2020-01-01", "2020-12-31");

        mockMvc.perform(get("/api/code-groups/{groupId}/detail-codes", "CG-CODE-USAGE").cookie(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isEmpty());

        mockMvc.perform(get("/api/code-groups/{groupId}/detail-codes", "CG-CODE-USAGE")
                .cookie(session)
                .param("includeEnded", "true"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].codeValue").value("HISTORICAL"))
            .andExpect(jsonPath("$.data[0].codeName").value("과거 코드"))
            .andExpect(jsonPath("$.data[0].useYn").value("N"))
            .andExpect(jsonPath("$.data[0].applicationStartDate").value("2020-01-01"))
            .andExpect(jsonPath("$.data[0].applicationEndDate").value("2020-12-31"));

        saveDetailCode(session, "CG-CODE-USAGE", "HISTORICAL", "과거 코드", "Y", "2020-01-01", "2099-12-31");

        mockMvc.perform(get("/api/code-groups/{groupId}/detail-codes", "CG-CODE-USAGE").cookie(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].codeValue").value("HISTORICAL"))
            .andExpect(jsonPath("$.data[0].useYn").value("Y"))
            .andExpect(jsonPath("$.data[0].applicationEndDate").value("2099-12-31"));
    }

    @Test
    void missingOrReversedApplicationDatesAreRejectedWithoutChangingTheExistingCode() throws Exception {
        Cookie session = login("admin", "admin");
        createCodeGroup(session, "CG-CODE-USAGE-VALIDATION");
        saveDetailCode(session, "CG-CODE-USAGE-VALIDATION", "STABLE", "유지 코드", "Y", "2026-01-01", null);

        mockMvc.perform(post("/api/code-groups/{groupId}/detail-codes", "CG-CODE-USAGE-VALIDATION")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"codeValue\":\"MISSING\",\"codeName\":\"시작일 없음\",\"displayOrder\":2,\"useYn\":\"Y\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.field").value("applicationStartDate"));

        mockMvc.perform(post("/api/code-groups/{groupId}/detail-codes", "CG-CODE-USAGE-VALIDATION")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"codeValue\":\"STABLE\",\"codeName\":\"변경되면 안 되는 코드\",\"displayOrder\":1,\"useYn\":\"N\",\"applicationStartDate\":\"2026-12-31\",\"applicationEndDate\":\"2026-01-01\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.field").value("applicationEndDate"));

        Assertions.assertEquals("유지 코드", jdbcTemplate.queryForObject(
            "select code_name from detail_code where group_id = 'CG-CODE-USAGE-VALIDATION' and code_value = 'STABLE'",
            String.class
        ));
        Assertions.assertEquals("Y", jdbcTemplate.queryForObject(
            "select use_yn from detail_code where group_id = 'CG-CODE-USAGE-VALIDATION' and code_value = 'STABLE'",
            String.class
        ));
    }

    @Test
    void unauthenticatedAndNonR09RequestsCannotReadOrChangeCodeUsage() throws Exception {
        mockMvc.perform(get("/api/code-groups/{groupId}/detail-codes", "CG-CODE-USAGE"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error.code").value("UNAUTHENTICATED"));

        Cookie memberSession = login("member", "member");
        mockMvc.perform(post("/api/code-groups/{groupId}/detail-codes", "CG-CODE-USAGE")
                .cookie(memberSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"codeValue\":\"FORBIDDEN\",\"codeName\":\"권한 없음\",\"displayOrder\":1,\"useYn\":\"Y\",\"applicationStartDate\":\"2026-01-01\"}"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
    }

    private void createCodeGroup(Cookie session, String groupId) throws Exception {
        mockMvc.perform(post("/api/code-groups")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"groupId\":\"" + groupId + "\",\"groupName\":\"코드 사용 관리 테스트 그룹\"}"))
            .andExpect(status().isOk());
    }

    private void saveDetailCode(
        Cookie session,
        String groupId,
        String codeValue,
        String codeName,
        String useYn,
        String applicationStartDate,
        String applicationEndDate
    ) throws Exception {
        String endDateField = applicationEndDate == null ? "" : ",\"applicationEndDate\":\"" + applicationEndDate + "\"";
        mockMvc.perform(post("/api/code-groups/{groupId}/detail-codes", groupId)
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"codeValue\":\"" + codeValue + "\",\"codeName\":\"" + codeName + "\",\"displayOrder\":1,\"useYn\":\"" + useYn + "\",\"applicationStartDate\":\"" + applicationStartDate + "\"" + endDateField + "}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    private Cookie login(String userId, String password) throws Exception {
        MvcResult login = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":\"" + userId + "\",\"password\":\"" + password + "\"}"))
            .andExpect(status().isOk())
            .andReturn();
        return login.getResponse().getCookie("SESSION");
    }
}
