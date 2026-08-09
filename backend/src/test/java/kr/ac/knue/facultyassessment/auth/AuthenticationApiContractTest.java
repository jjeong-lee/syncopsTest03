package kr.ac.knue.facultyassessment.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class AuthenticationApiContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void loginCreatesPersistedActiveSessionAndLogoutTransitionsTheSameRowToTerminated() throws Exception {
        assertAuthenticationContractIsAvailable();

        MvcResult login = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":\"admin\",\"password\":\"admin\"}"))
            .andExpect(status().isOk())
            .andExpect(cookie().httpOnly("SESSION", true))
            .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("SameSite=Lax")))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.userId").value("admin"))
            .andReturn();

        String sessionCookie = login.getResponse().getCookie("SESSION").getValue();
        org.junit.jupiter.api.Assertions.assertEquals("admin", jdbcTemplate.queryForObject(
            "select user_id from user_session where session_id = ? and status = 'ACTIVE'",
            String.class,
            sessionCookie
        ));
        mockMvc.perform(get("/api/auth/me").cookie(new jakarta.servlet.http.Cookie("SESSION", sessionCookie)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.userId").value("admin"))
            .andExpect(jsonPath("$.data.roleCodes[0]").value("R09"));

        mockMvc.perform(post("/api/auth/logout")
                .cookie(new jakarta.servlet.http.Cookie("SESSION", sessionCookie))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":\"admin\",\"password\":\"admin\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        org.junit.jupiter.api.Assertions.assertEquals("TERMINATED", jdbcTemplate.queryForObject(
            "select status from user_session where session_id = ?",
            String.class,
            sessionCookie
        ));

        mockMvc.perform(get("/api/auth/me").cookie(new jakarta.servlet.http.Cookie("SESSION", sessionCookie)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("UNAUTHENTICATED"));
    }

    @Test
    void invalidLoginAndValidationErrorsDoNotCreateAuthenticatedSession() throws Exception {
        Integer sessionsBefore = jdbcTemplate.queryForObject("select count(*) from user_session", Integer.class);
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":\"\",\"password\":\"admin\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.field").value("userId"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":\"admin\",\"password\":\"wrong\"}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("INVALID_CREDENTIALS"));

        org.junit.jupiter.api.Assertions.assertEquals(sessionsBefore, jdbcTemplate.queryForObject(
            "select count(*) from user_session",
            Integer.class
        ));
    }

    @Test
    void unauthenticatedProtectedOperationsAreRejectedBeforeTheirControllersRun() throws Exception {
        mockMvc.perform(get("/api/users"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("UNAUTHENTICATED"));
    }

    private void assertAuthenticationContractIsAvailable() throws Exception {
        String contract = new ClassPathResource("contracts/openapi.yaml").getContentAsString(StandardCharsets.UTF_8);
        org.junit.jupiter.api.Assertions.assertTrue(contract.contains("/auth/login:"));
        org.junit.jupiter.api.Assertions.assertTrue(contract.contains("/auth/logout:"));
        org.junit.jupiter.api.Assertions.assertTrue(contract.contains("/auth/me:"));
    }
}
