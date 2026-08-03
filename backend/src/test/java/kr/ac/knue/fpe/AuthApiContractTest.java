package kr.ac.knue.fpe;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthApiContractTest extends PostgresIntegrationTest {
    @Autowired MockMvc mockMvc;
    @Autowired JdbcTemplate jdbc;

    @Test
    void postAuthLoginIssuesHttpOnlySessionAndPersistsActiveSessionSideEffect() throws Exception {
        long beforeSessions = countActiveSessions();
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin\"}"))
                .andExpect(status().isOk())
                .andExpect(header().string("Set-Cookie", containsString("HttpOnly")))
                .andExpect(header().string("Set-Cookie", containsString("SameSite=Lax")))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value("admin"))
                .andExpect(jsonPath("$.data.roles[0]").value("R09"))
                .andReturn();

        String cookieValue = result.getResponse().getHeader("Set-Cookie").split(";", 2)[0].split("=", 2)[1];
        assertThat(countActiveSessions()).isGreaterThan(beforeSessions);
        assertThat(countActiveSessionById(cookieValue)).isEqualTo(1L);
    }

    @Test
    void postAuthLoginRejectsMissingPasswordWithoutCreatingSession() throws Exception {
        long beforeSessions = countSessions();
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.fieldErrors[0].field").value("password"));
        assertThat(countSessions()).isEqualTo(beforeSessions);
    }

    @Test
    void postAuthLoginRejectsDisabledLocalAccountWithoutExternalSsoDependency() throws Exception {
        jdbc.update("update user_accounts set system_enabled=false where user_id='STAFF-001'");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"professor01\",\"password\":\"admin\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
        assertThat(countActiveSessionsForUser("STAFF-001")).isEqualTo(0L);
        jdbc.update("update user_accounts set system_enabled=true where user_id='STAFF-001'");
    }

    @Test
    void getAuthMeReturnsCurrentUserForActiveSessionAndRejectsMissingSession() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin\"}"))
                .andReturn();
        String cookieValue = result.getResponse().getHeader("Set-Cookie").split(";", 2)[0].split("=", 2)[1];

        mockMvc.perform(get("/api/auth/me").cookie(new Cookie("SESSION", cookieValue)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("admin"))
                .andExpect(jsonPath("$.data.roles[0]").value("R09"));

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void postAuthLogoutRevokesActiveSessionExpiresCookieAndBlocksReuse() throws Exception {
        MvcResult login = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin\"}"))
                .andReturn();
        String cookieValue = login.getResponse().getHeader("Set-Cookie").split(";", 2)[0].split("=", 2)[1];

        mockMvc.perform(post("/api/auth/logout").cookie(new Cookie("SESSION", cookieValue)))
                .andExpect(status().isOk())
                .andExpect(header().string("Set-Cookie", containsString("Max-Age=0")))
                .andExpect(jsonPath("$.success").value(true));
        assertThat(countRevokedSessionById(cookieValue)).isEqualTo(1L);

        mockMvc.perform(get("/api/auth/me").cookie(new Cookie("SESSION", cookieValue)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void postAuthLogoutRejectsMissingAndMalformedSessionCookie() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));

        mockMvc.perform(post("/api/auth/logout").cookie(new Cookie("SESSION", "malformed-session")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    private long countSessions() {
        Long value = jdbc.queryForObject("select count(*) from sessions", Long.class);
        return value == null ? 0L : value;
    }

    private long countActiveSessions() {
        Long value = jdbc.queryForObject("select count(*) from sessions where status=?", Long.class, "ACTIVE");
        return value == null ? 0L : value;
    }

    private long countActiveSessionById(String sessionId) {
        Long value = jdbc.queryForObject(
                "select count(*) from sessions where session_id=? and status=? and created_at is not null",
                Long.class,
                sessionId,
                "ACTIVE");
        return value == null ? 0L : value;
    }

    private long countRevokedSessionById(String sessionId) {
        Long value = jdbc.queryForObject(
                "select count(*) from sessions where session_id=? and status=? and updated_at is not null",
                Long.class,
                sessionId,
                "REVOKED");
        return value == null ? 0L : value;
    }

    private long countActiveSessionsForUser(String userId) {
        Long value = jdbc.queryForObject(
                "select count(*) from sessions s where s.user_id=? and s.status=?",
                Long.class,
                userId,
                "ACTIVE");
        return value == null ? 0L : value;
    }
}
