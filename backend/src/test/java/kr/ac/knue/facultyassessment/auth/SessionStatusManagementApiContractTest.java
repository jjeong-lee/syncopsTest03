package kr.ac.knue.facultyassessment.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class SessionStatusManagementApiContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void r09CanForceTerminateAnActiveSessionWithReasonAndImmutableAuditHistory() throws Exception {
        Cookie adminCookie = login("admin", "admin");
        MvcResult memberLogin = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":\"member\",\"password\":\"member\"}"))
            .andExpect(status().isOk())
            .andReturn();
        Cookie memberCookie = memberLogin.getResponse().getCookie("SESSION");

        mockMvc.perform(get("/api/session-status/active").cookie(adminCookie))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[?(@.sessionId == '" + memberCookie.getValue() + "')].userId").value("member"))
            .andExpect(jsonPath("$.data[?(@.sessionId == '" + memberCookie.getValue() + "')].loginAt").exists())
            .andExpect(jsonPath("$.data[?(@.sessionId == '" + memberCookie.getValue() + "')].lastActivityAt").exists())
            .andExpect(jsonPath("$.data[?(@.sessionId == '" + memberCookie.getValue() + "')].ipAddress").exists())
            .andExpect(jsonPath("$.data[?(@.sessionId == '" + memberCookie.getValue() + "')].status").value("ACTIVE"));

        mockMvc.perform(post("/api/session-status/" + memberCookie.getValue() + "/force-terminate")
                .cookie(adminCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"보안 점검을 위해 종료합니다.\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/auth/me").cookie(memberCookie))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error.code").value("UNAUTHENTICATED"));
        org.junit.jupiter.api.Assertions.assertEquals("FORCED_TERMINATED", jdbcTemplate.queryForObject(
            "select status from user_session where session_id = ?", String.class, memberCookie.getValue()
        ));
        org.junit.jupiter.api.Assertions.assertEquals("ADMIN_FORCED", jdbcTemplate.queryForObject(
            "select end_type from session_end_history where session_id = ?", String.class, memberCookie.getValue()
        ));
        org.junit.jupiter.api.Assertions.assertEquals("admin", jdbcTemplate.queryForObject(
            "select actor_user_id from session_end_history where session_id = ?", String.class, memberCookie.getValue()
        ));
        org.junit.jupiter.api.Assertions.assertEquals("보안 점검을 위해 종료합니다.", jdbcTemplate.queryForObject(
            "select reason from session_end_history where session_id = ?", String.class, memberCookie.getValue()
        ));

        mockMvc.perform(get("/api/session-status/history?userId=member").cookie(adminCookie))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].endType").value("ADMIN_FORCED"))
            .andExpect(jsonPath("$.data[0].reason").value("보안 점검을 위해 종료합니다."));
    }

    @Test
    void aNonR09UserCannotReadOrForceTerminateSessions() throws Exception {
        Cookie memberCookie = login("member", "member");

        mockMvc.perform(get("/api/session-status/active").cookie(memberCookie))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
        mockMvc.perform(post("/api/session-status/not-a-session/force-terminate")
                .cookie(memberCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"권한 없는 종료 시도\"}"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
    }

    private Cookie login(String userId, String password) throws Exception {
        return mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":\"" + userId + "\",\"password\":\"" + password + "\"}"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getCookie("SESSION");
    }
}
