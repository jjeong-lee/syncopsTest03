package kr.ac.knue.facultyassessment.settings;

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
class CommonEnvironmentSettingsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void r09CanSaveAllCommonSettingsThenRequeryThemAndOnlyNewSessionsUseTheNewIdleValue() throws Exception {
        Cookie existingSession = login("admin", "admin");
        Integer existingIdleMinutes = jdbcTemplate.queryForObject(
            "select idle_minutes from user_session where session_id = ?", Integer.class, existingSession.getValue()
        );

        saveCommonSettings(existingSession, 45, 50, 30, 5000, 90)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/settings/common").cookie(existingSession))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.sessionIdleMinutes").value(45))
            .andExpect(jsonPath("$.data.pageSize").value(50))
            .andExpect(jsonPath("$.data.defaultSearchPeriodDays").value(30))
            .andExpect(jsonPath("$.data.bulkQueryThreshold").value(5000))
            .andExpect(jsonPath("$.data.longRunningWorkNoticeSeconds").value(90));

        Assertions.assertEquals(existingIdleMinutes, jdbcTemplate.queryForObject(
            "select idle_minutes from user_session where session_id = ?", Integer.class, existingSession.getValue()
        ));

        Cookie newSession = login("admin", "admin");
        Assertions.assertEquals(45, jdbcTemplate.queryForObject(
            "select idle_minutes from user_session where session_id = ?", Integer.class, newSession.getValue()
        ));
    }

    @Test
    void outOfRangeSettingsAreRejectedWithoutChangingTheExistingValues() throws Exception {
        Cookie session = login("admin", "admin");
        saveCommonSettings(session, 60, 20, 14, 1000, 60).andExpect(status().isOk());

        mockMvc.perform(post("/api/settings/common")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"sessionIdleMinutes\":1441,\"pageSize\":30,\"defaultSearchPeriodDays\":14,\"bulkQueryThreshold\":1000,\"longRunningWorkNoticeSeconds\":60}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.field").value("sessionIdleMinutes"));

        mockMvc.perform(get("/api/settings/common").cookie(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.sessionIdleMinutes").value(60))
            .andExpect(jsonPath("$.data.pageSize").value(20));
    }

    @Test
    void unauthenticatedAndNonR09UsersCannotReadOrSaveCommonSettings() throws Exception {
        mockMvc.perform(get("/api/settings/common"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error.code").value("UNAUTHENTICATED"));

        Cookie memberSession = login("member", "member");
        saveCommonSettings(memberSession, 60, 20, 14, 1000, 60)
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
    }

    private org.springframework.test.web.servlet.ResultActions saveCommonSettings(
        Cookie session,
        int sessionIdleMinutes,
        int pageSize,
        int defaultSearchPeriodDays,
        int bulkQueryThreshold,
        int longRunningWorkNoticeSeconds
    ) throws Exception {
        return mockMvc.perform(post("/api/settings/common")
            .cookie(session)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"sessionIdleMinutes\":" + sessionIdleMinutes
                + ",\"pageSize\":" + pageSize
                + ",\"defaultSearchPeriodDays\":" + defaultSearchPeriodDays
                + ",\"bulkQueryThreshold\":" + bulkQueryThreshold
                + ",\"longRunningWorkNoticeSeconds\":" + longRunningWorkNoticeSeconds + "}"));
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
