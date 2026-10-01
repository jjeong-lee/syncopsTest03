package kr.ac.knue.facultyassessment;

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
class R1BRegressionTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void r09CanReadAllR1BManagementOperations() throws Exception {
        Cookie session = login("admin", "admin");
        String groupId = "CG-R1B-REGRESSION";

        mockMvc.perform(post("/api/code-groups")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"groupId\":\"" + groupId + "\",\"groupName\":\"R1-B 회귀 코드그룹\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/menus").cookie(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
        mockMvc.perform(get("/api/code-groups/{groupId}/detail-codes", groupId)
                .cookie(session)
                .param("includeEnded", "true"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
        mockMvc.perform(get("/api/settings/common").cookie(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
        mockMvc.perform(get("/api/settings/reference-years").cookie(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void unauthenticatedR1BOperationsReturnTheCommonUnauthorizedError() throws Exception {
        mockMvc.perform(get("/api/menus"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("UNAUTHENTICATED"));
        mockMvc.perform(get("/api/code-groups/any/detail-codes"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error.code").value("UNAUTHENTICATED"));
        mockMvc.perform(get("/api/settings/common"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error.code").value("UNAUTHENTICATED"));
        mockMvc.perform(get("/api/settings/reference-years"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error.code").value("UNAUTHENTICATED"));
    }

    @Test
    void nonR09R1BMutationsReturnForbiddenAndLeaveAllTargetRowsUnchanged() throws Exception {
        Cookie memberSession = login("member", "member");
        int menuCount = jdbcTemplate.queryForObject("select count(*) from menu", Integer.class);
        int detailCodeCount = jdbcTemplate.queryForObject("select count(*) from detail_code", Integer.class);
        int settingCount = jdbcTemplate.queryForObject("select count(*) from system_setting", Integer.class);

        mockMvc.perform(post("/api/menus")
                .cookie(memberSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"menuName\":\"권한 없음 메뉴\",\"displayOrder\":99,\"screenId\":\"SCR-R1B-FORBIDDEN\",\"useYn\":\"Y\"}"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
        mockMvc.perform(post("/api/code-groups/{groupId}/detail-codes", "CG-EMPLOYMENT-STATUS")
                .cookie(memberSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"codeValue\":\"R1B-FORBIDDEN\",\"codeName\":\"권한 없음 코드\",\"displayOrder\":99,\"useYn\":\"Y\",\"applicationStartDate\":\"2026-01-01\"}"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
        mockMvc.perform(post("/api/settings/common")
                .cookie(memberSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"sessionIdleMinutes\":45,\"pageSize\":50,\"defaultSearchPeriodDays\":30,\"bulkQueryThreshold\":5000,\"longRunningWorkNoticeSeconds\":90}"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
        mockMvc.perform(post("/api/settings/reference-years")
                .cookie(memberSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"currentEvaluationYear\":2026,\"defaultSearchYear\":2025,\"targetYear\":2027,\"referenceDataCopyYn\":\"Y\",\"initializationYn\":\"N\"}"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));

        Assertions.assertEquals(menuCount, jdbcTemplate.queryForObject("select count(*) from menu", Integer.class));
        Assertions.assertEquals(detailCodeCount, jdbcTemplate.queryForObject("select count(*) from detail_code", Integer.class));
        Assertions.assertEquals(settingCount, jdbcTemplate.queryForObject("select count(*) from system_setting", Integer.class));
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
