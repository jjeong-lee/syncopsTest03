package kr.ac.knue.fpe.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CommonApiContractTest {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    private Cookie loginCookie() throws Exception {
        var result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("loginId", "admin", "password", "admin"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.roleCodes[0]").value("R09"))
            .andReturn();
        return result.getResponse().getCookie("AIOPS_SESSION");
    }

    @Test
    void openapiFixtureIsPackagedOnClasspath() throws Exception {
        var fixture = new ClassPathResource("contracts/openapi.yaml");
        assertThat(fixture.exists()).isTrue();
        var body = fixture.getContentAsString(StandardCharsets.UTF_8);
        assertThat(body).contains("/api/users", "operationId: listUsers", "x-required-tests");
    }

    @Test
    void schemaContainsDataModelKorusMockSnapshotTableContract() throws Exception {
        var schema = new ClassPathResource("db/migration/V1__common_functions_schema.sql")
            .getContentAsString(StandardCharsets.UTF_8);

        assertThat(schema)
            .contains("CREATE TABLE IF NOT EXISTS k_o_r_u_s_mock_snapshot")
            .contains("employee_no varchar(120) PRIMARY KEY")
            .contains("employment_status varchar(40) NOT NULL")
            .contains("CONSTRAINT chk_k_o_r_u_s_mock_snapshot_employment_status")
            .contains("CREATE INDEX IF NOT EXISTS idx_k_o_r_u_s_mock_snapshot_organization")
            .contains("COMMENT ON TABLE k_o_r_u_s_mock_snapshot");
    }

    @Test
    void protectedApiRequiresSessionCookie() throws Exception {
        mockMvc.perform(get("/api/users"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }

    @Test
    void adminCanSearchAndSaveUserWithAuditSideEffect() throws Exception {
        Cookie cookie = loginCookie();
        mockMvc.perform(get("/api/users").cookie(cookie).param("keyword", "관리자"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].id").exists())
            .andExpect(jsonPath("$.data[0].payload.employeeNo").value("USR-ADMIN"))
            .andExpect(jsonPath("$.data[0].payload.employmentStatus").value("ACTIVE"));

        mockMvc.perform(post("/api/users").cookie(cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("id", "USR-ADMIN", "title", "시스템관리자", "systemUseYn", "N", "primaryRoleCode", "R09"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.id").value("USR-ADMIN"))
            .andExpect(jsonPath("$.data.useYn").value("N"))
            .andExpect(jsonPath("$.data.payload.systemUseYn").value("N"))
            .andExpect(jsonPath("$.data.payload.primaryRoleCode").value("R09"));

        mockMvc.perform(get("/api/audit-logs").cookie(cookie).param("keyword", "users"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].payload.action").value("SAVE"));
    }

    @Test
    void saveRejectsMissingIdentifierWithoutLeakingSensitiveErrors() throws Exception {
        Cookie cookie = loginCookie();
        mockMvc.perform(post("/api/roles").cookie(cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("title", "식별자 없음"))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.details.id").exists())
            .andExpect(jsonPath("$.error.message").value("입력값을 확인해 주세요."));
    }

    @Test
    void attachmentDeleteRequiresReasonAndFinalizedRecordsAreBlocked() throws Exception {
        Cookie cookie = loginCookie();
        mockMvc.perform(post("/api/attachments").cookie(cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("id", "ATT-LOCKED", "title", "확정자료", "deleteYn", "Y", "finalizedRecordYn", "Y"))))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error.code").value("BUSINESS_RULE_VIOLATION"));

        mockMvc.perform(post("/api/attachments").cookie(cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("id", "ATT-001", "title", "삭제요청", "deleteYn", "Y"))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.details.deleteReason").exists());
    }

    @Test
    void operationsHealthAuditAndBatchApisReturnStableEnvelope() throws Exception {
        Cookie cookie = loginCookie();
        mockMvc.perform(get("/api/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("UP"));
        mockMvc.perform(get("/api/batch-definitions").cookie(cookie))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray());
        mockMvc.perform(post("/api/batch-executions").cookie(cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("id", "BEX-MANUAL-001", "title", "수동 실행", "reason", "검증 실행"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.id").value("BEX-MANUAL-001"));
    }
}
