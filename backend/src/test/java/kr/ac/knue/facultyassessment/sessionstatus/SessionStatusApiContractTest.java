package kr.ac.knue.facultyassessment.sessionstatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
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
class SessionStatusApiContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void durableOpenApiFixtureDeclaresSessionStatusOperations() throws Exception {
        ClassPathResource contract = new ClassPathResource("contracts/openapi.yaml");
        String openApi = new String(contract.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);

        assertTrue(contract.exists());
        assertTrue(openApi.contains("/session-status:"));
        assertTrue(openApi.contains("/session-status/{sessionId}/termination:"));
        assertTrue(openApi.contains("/session-termination-history:"));
    }

    @Test
    void r09CanListStudioUiActiveSessionsWithRequiredStatusFields() throws Exception {
        Cookie admin = login("admin", "admin");
        String sessionId = insertSession("member", "ACTIVE", null);

        mockMvc.perform(get("/api/session-status").cookie(admin).queryParam("page", "0").queryParam("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[*].sessionId").value(hasItem(sessionId)))
            .andExpect(jsonPath("$.data[?(@.sessionId == '" + sessionId + "')].userId").value(hasItem("member")))
            .andExpect(jsonPath("$.data[?(@.sessionId == '" + sessionId + "')].loginAt").exists())
            .andExpect(jsonPath("$.data[?(@.sessionId == '" + sessionId + "')].lastActivityAt").exists())
            .andExpect(jsonPath("$.data[?(@.sessionId == '" + sessionId + "')].ipAddress").value(hasItem("203.0.113.8")))
            .andExpect(jsonPath("$.data[?(@.sessionId == '" + sessionId + "')].status").value(hasItem("ACTIVE")));
    }

    @Test
    void r09TerminationInvalidatesSessionAndRecordsAuditInOneFlow() throws Exception {
        Cookie admin = login("admin", "admin");
        String sessionId = insertSession("member", "ACTIVE", null);

        mockMvc.perform(post("/api/session-status/{sessionId}/termination", sessionId)
                .cookie(admin).contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"보안 점검에 따른 종료\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        assertEquals("TERMINATED", jdbcTemplate.queryForObject(
            "select status from user_session where session_id = ?", String.class, sessionId));
        assertEquals("ADMIN_TERMINATED", jdbcTemplate.queryForObject(
            "select termination_type from user_session where session_id = ?", String.class, sessionId));
        assertEquals(1, jdbcTemplate.queryForObject(
            "select count(*) from change_history where entity_name = 'user_session' and entity_id = ? and actor_user_id = 'admin' and reason = '보안 점검에 따른 종료'",
            Integer.class, sessionId));
    }

    @Test
    void blankReasonAndAlreadyTerminatedSessionDoNotCreateAdditionalAudit() throws Exception {
        Cookie admin = login("admin", "admin");
        String activeSession = insertSession("member", "ACTIVE", null);
        String terminatedSession = insertSession("member", "TERMINATED", "LOGOUT");

        mockMvc.perform(post("/api/session-status/{sessionId}/termination", activeSession)
                .cookie(admin).contentType(MediaType.APPLICATION_JSON).content("{\"reason\":\"   \"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.field").value("reason"));
        mockMvc.perform(post("/api/session-status/{sessionId}/termination", terminatedSession)
                .cookie(admin).contentType(MediaType.APPLICATION_JSON).content("{\"reason\":\"다시 종료\"}"))
            .andExpect(status().isConflict());
        assertEquals(0, jdbcTemplate.queryForObject(
            "select count(*) from change_history where entity_name = 'user_session' and entity_id in (?, ?)",
            Integer.class, activeSession, terminatedSession));
    }

    @Test
    void nonR09CannotReadOrTerminateSessionStatus() throws Exception {
        jdbcTemplate.update("update user_role set status = 'REVOKED' where user_id = 'admin' and role_code = 'R09'");
        Cookie member = login("admin", "admin");
        String sessionId = insertSession("member", "ACTIVE", null);
        mockMvc.perform(get("/api/session-status").cookie(member)).andExpect(status().isForbidden());
        mockMvc.perform(post("/api/session-status/{sessionId}/termination", sessionId)
                .cookie(member).contentType(MediaType.APPLICATION_JSON).content("{\"reason\":\"권한 없음\"}"))
            .andExpect(status().isForbidden());
    }

    @Test
    void r09CanFilterImmutableTerminationHistoryAndReadItsDetail() throws Exception {
        Cookie admin = login("admin", "admin");
        String logout = insertSession("member", "TERMINATED", "LOGOUT");
        String adminTerminated = insertSession("member", "TERMINATED", "ADMIN_TERMINATED");

        mockMvc.perform(get("/api/session-termination-history").cookie(admin)
                .queryParam("userId", "member")
                .queryParam("startedAt", OffsetDateTime.now().minusDays(1).toString())
                .queryParam("endedAt", OffsetDateTime.now().plusDays(1).toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].terminationType").exists());
        mockMvc.perform(get("/api/session-termination-history/{sessionId}", adminTerminated).cookie(admin))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.sessionId").value(adminTerminated));
        mockMvc.perform(get("/api/session-termination-history").cookie(admin)
                .queryParam("startedAt", OffsetDateTime.now().toString())
                .queryParam("endedAt", OffsetDateTime.now().minusDays(1).toString()))
            .andExpect(status().isBadRequest());
        assertEquals("LOGOUT", jdbcTemplate.queryForObject("select termination_type from user_session where session_id = ?", String.class, logout));
    }

    private Cookie login(String userId, String password) throws Exception {
        MvcResult login = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":\"" + userId + "\",\"password\":\"" + password + "\"}"))
            .andExpect(status().isOk()).andReturn();
        return login.getResponse().getCookie("SESSION");
    }

    private String insertSession(String userId, String status, String terminationType) {
        String sessionId = "session-" + java.util.UUID.randomUUID();
        if (terminationType == null) {
            jdbcTemplate.update("insert into user_session (session_id, user_id, status, login_at, last_activity_at, ip_address, termination_type, terminated_at) values (?, ?, ?, current_timestamp, current_timestamp, '203.0.113.8', null, null)",
                sessionId, userId, status);
        } else {
            jdbcTemplate.update("insert into user_session (session_id, user_id, status, login_at, last_activity_at, ip_address, termination_type, terminated_at) values (?, ?, ?, current_timestamp, current_timestamp, '203.0.113.8', ?, current_timestamp)",
                sessionId, userId, status, terminationType);
        }
        return sessionId;
    }
}
