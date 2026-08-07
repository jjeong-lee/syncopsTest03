package kr.ac.knue.fpe.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ApiStaticOperationCoverageTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;

    private Cookie loginCookie() throws Exception {
        return mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("loginId", "admin", "password", "admin"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.loginId").value("admin"))
            .andExpect(jsonPath("$.data.roleCodes[0]").value("R09"))
            .andExpect(cookie().httpOnly("AIOPS_SESSION", true))
            .andReturn().getResponse().getCookie("AIOPS_SESSION");
    }

    private String managedRecord(String id, String title, Map<String, Object> payload) throws Exception {
        return objectMapper.writeValueAsString(Map.of("id", id, "title", title, "status", "ACTIVE", "useYn", "Y", "payload", payload));
    }

    @Test
    void missingGetOperationsReturnHappyBodyContractAndPortalKeywordFiltering() throws Exception {
        Cookie cookie = loginCookie();
        mvc.perform(get("/api/attachments").cookie(cookie).param("keyword", "계약검증").param("page", "0").param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray());
        mvc.perform(get("/api/auth/me").cookie(cookie))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.loginId").value("admin"));
        mvc.perform(get("/api/base-years").cookie(cookie).param("keyword", "계약검증").param("page", "0").param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray());
        mvc.perform(get("/api/batch-executions").cookie(cookie).param("keyword", "계약검증").param("page", "0").param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray());
        mvc.perform(get("/api/batch-results").cookie(cookie).param("keyword", "계약검증").param("page", "0").param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray());
        mvc.perform(get("/api/code-groups").cookie(cookie).param("keyword", "계약검증").param("page", "0").param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray());
        mvc.perform(get("/api/configurations").cookie(cookie).param("keyword", "계약검증").param("page", "0").param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray());
        mvc.perform(get("/api/data-scope-permissions").cookie(cookie).param("keyword", "계약검증").param("page", "0").param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray());
        mvc.perform(get("/api/detail-codes").cookie(cookie).param("keyword", "계약검증").param("page", "0").param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray());
        mvc.perform(get("/api/excel/downloads").cookie(cookie).param("keyword", "계약검증").param("page", "0").param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray());
        mvc.perform(get("/api/excel/templates").cookie(cookie).param("keyword", "계약검증").param("page", "0").param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray());
        mvc.perform(get("/api/excel/uploads").cookie(cookie).param("keyword", "계약검증").param("page", "0").param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray());
        mvc.perform(get("/api/file-policies").cookie(cookie).param("keyword", "계약검증").param("page", "0").param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray());
        mvc.perform(get("/api/function-permissions").cookie(cookie).param("keyword", "계약검증").param("page", "0").param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray());
        mvc.perform(get("/api/menu-permissions").cookie(cookie).param("keyword", "계약검증").param("page", "0").param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray());
        mvc.perform(get("/api/menus").cookie(cookie).param("keyword", "계약검증").param("page", "0").param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray());
        mvc.perform(get("/api/notices").cookie(cookie).param("keyword", "계약검증").param("page", "0").param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray());
        mvc.perform(get("/api/organizations").cookie(cookie).param("keyword", "계약검증").param("page", "0").param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray());
        mvc.perform(get("/api/positions").cookie(cookie).param("keyword", "계약검증").param("page", "0").param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray());
        mvc.perform(get("/api/privacy/policies").cookie(cookie).param("keyword", "계약검증").param("page", "0").param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray());
        mvc.perform(get("/api/roles").cookie(cookie).param("keyword", "계약검증").param("page", "0").param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray());
        mvc.perform(get("/api/sessions").cookie(cookie).param("keyword", "계약검증").param("page", "0").param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray());
        mvc.perform(get("/api/user-roles").cookie(cookie).param("keyword", "계약검증").param("page", "0").param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void literalOperationsEnforceAuthAndValidationNegativeContracts() throws Exception {
        mvc.perform(get("/api/attachments"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
        mvc.perform(post("/api/attachments").contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
        mvc.perform(post("/api/audit-logs").contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
        mvc.perform(post("/api/auth/logout").contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
        mvc.perform(post("/api/base-years").contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
        mvc.perform(post("/api/batch-definitions").contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
        mvc.perform(post("/api/batch-executions").contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
        mvc.perform(post("/api/batch-results").contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
        mvc.perform(post("/api/code-groups").contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
        mvc.perform(post("/api/configurations").contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
        mvc.perform(post("/api/data-scope-permissions").contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
        mvc.perform(post("/api/detail-codes").contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
        mvc.perform(post("/api/excel/downloads").contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
        mvc.perform(post("/api/excel/templates").contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
        mvc.perform(post("/api/excel/uploads").contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
        mvc.perform(post("/api/file-policies").contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
        mvc.perform(post("/api/function-permissions").contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
        mvc.perform(post("/api/menu-permissions").contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
        mvc.perform(post("/api/menus").contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
        mvc.perform(post("/api/notices").contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
        mvc.perform(post("/api/organizations").contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
        mvc.perform(post("/api/positions").contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
        mvc.perform(post("/api/privacy/policies").contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
        mvc.perform(post("/api/roles").contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
        mvc.perform(post("/api/sessions").contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
        mvc.perform(post("/api/user-roles").contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
        mvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
        Cookie cookie = loginCookie();
        mvc.perform(post("/api/attachments").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(Map.of("title", "식별자 누락"))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.details.id").exists());
        mvc.perform(post("/api/audit-logs").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(Map.of("title", "식별자 누락"))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.details.id").exists());
        mvc.perform(post("/api/base-years").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(Map.of("title", "식별자 누락"))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.details.id").exists());
        mvc.perform(post("/api/batch-definitions").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(Map.of("title", "식별자 누락"))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.details.id").exists());
        mvc.perform(post("/api/batch-executions").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(Map.of("title", "식별자 누락"))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.details.id").exists());
        mvc.perform(post("/api/batch-results").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(Map.of("title", "식별자 누락"))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.details.id").exists());
        mvc.perform(post("/api/code-groups").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(Map.of("title", "식별자 누락"))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.details.id").exists());
        mvc.perform(post("/api/configurations").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(Map.of("title", "식별자 누락"))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.details.id").exists());
        mvc.perform(post("/api/data-scope-permissions").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(Map.of("title", "식별자 누락"))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.details.id").exists());
        mvc.perform(post("/api/detail-codes").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(Map.of("title", "식별자 누락"))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.details.id").exists());
        mvc.perform(post("/api/excel/downloads").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(Map.of("title", "식별자 누락"))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.details.id").exists());
        mvc.perform(post("/api/excel/templates").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(Map.of("title", "식별자 누락"))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.details.id").exists());
        mvc.perform(post("/api/excel/uploads").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(Map.of("title", "식별자 누락"))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.details.id").exists());
        mvc.perform(post("/api/file-policies").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(Map.of("title", "식별자 누락"))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.details.id").exists());
        mvc.perform(post("/api/function-permissions").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(Map.of("title", "식별자 누락"))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.details.id").exists());
        mvc.perform(post("/api/menu-permissions").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(Map.of("title", "식별자 누락"))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.details.id").exists());
        mvc.perform(post("/api/menus").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(Map.of("title", "식별자 누락"))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.details.id").exists());
        mvc.perform(post("/api/notices").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(Map.of("title", "식별자 누락"))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.details.id").exists());
        mvc.perform(post("/api/organizations").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(Map.of("title", "식별자 누락"))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.details.id").exists());
        mvc.perform(post("/api/positions").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(Map.of("title", "식별자 누락"))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.details.id").exists());
        mvc.perform(post("/api/privacy/policies").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(Map.of("title", "식별자 누락"))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.details.id").exists());
        mvc.perform(post("/api/roles").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(Map.of("title", "식별자 누락"))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.details.id").exists());
        mvc.perform(post("/api/user-roles").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(Map.of("title", "식별자 누락"))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.details.id").exists());
        mvc.perform(post("/api/users").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(Map.of("title", "식별자 누락"))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.details.id").exists());
    }

    @Test
    void vendorPostOperationsPersistViaMyBatisTransactionAndExposeHappyBusinessSideEffects() throws Exception {
        Cookie cookie = loginCookie();
        mvc.perform(post("/api/attachments").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content(managedRecord("CONTRACT-ATTACHMENTS-1", "계약 검증 attachments", Map.of("reason", "contract side effect", "portalScope", "KNUE", "transactionProbe", "mybatis", "deleteYn", "N", "malwareScanResult", "CLEAN"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").exists())
            .andExpect(jsonPath("$.data.payload.transactionProbe").value("mybatis"));
        mvc.perform(get("/api/attachments").cookie(cookie).param("keyword", "CONTRACT-ATTACHMENTS-1").param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.length()", greaterThanOrEqualTo(1)));
        mvc.perform(post("/api/audit-logs").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content(managedRecord("CONTRACT-AUDIT_LOGS-2", "계약 검증 audit-logs", Map.of("reason", "contract side effect", "portalScope", "KNUE", "transactionProbe", "mybatis"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").exists())
            .andExpect(jsonPath("$.data.payload.transactionProbe").value("mybatis"));
        mvc.perform(get("/api/audit-logs").cookie(cookie).param("keyword", "CONTRACT-AUDIT_LOGS-2").param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.length()", greaterThanOrEqualTo(1)));
        mvc.perform(post("/api/base-years").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content(managedRecord("CONTRACT-BASE_YEARS-3", "계약 검증 base-years", Map.of("reason", "contract side effect", "portalScope", "KNUE", "transactionProbe", "mybatis"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").exists())
            .andExpect(jsonPath("$.data.payload.transactionProbe").value("mybatis"));
        mvc.perform(get("/api/base-years").cookie(cookie).param("keyword", "CONTRACT-BASE_YEARS-3").param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.length()", greaterThanOrEqualTo(1)));
        mvc.perform(post("/api/batch-definitions").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content(managedRecord("CONTRACT-BATCH_DEFINITIONS-4", "계약 검증 batch-definitions", Map.of("reason", "contract side effect", "portalScope", "KNUE", "transactionProbe", "mybatis"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").exists())
            .andExpect(jsonPath("$.data.payload.transactionProbe").value("mybatis"));
        mvc.perform(get("/api/batch-definitions").cookie(cookie).param("keyword", "CONTRACT-BATCH_DEFINITIONS-4").param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.length()", greaterThanOrEqualTo(1)));
        mvc.perform(post("/api/batch-executions").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content(managedRecord("CONTRACT-BATCH_EXECUTIONS-5", "계약 검증 batch-executions", Map.of("reason", "contract side effect", "portalScope", "KNUE", "transactionProbe", "mybatis", "executionStatus", "REQUESTED"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").exists())
            .andExpect(jsonPath("$.data.payload.transactionProbe").value("mybatis"));
        mvc.perform(get("/api/batch-executions").cookie(cookie).param("keyword", "CONTRACT-BATCH_EXECUTIONS-5").param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.length()", greaterThanOrEqualTo(1)));
        mvc.perform(post("/api/batch-results").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content(managedRecord("CONTRACT-BATCH_RESULTS-6", "계약 검증 batch-results", Map.of("reason", "contract side effect", "portalScope", "KNUE", "transactionProbe", "mybatis"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").exists())
            .andExpect(jsonPath("$.data.payload.transactionProbe").value("mybatis"));
        mvc.perform(get("/api/batch-results").cookie(cookie).param("keyword", "CONTRACT-BATCH_RESULTS-6").param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.length()", greaterThanOrEqualTo(1)));
        mvc.perform(post("/api/code-groups").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content(managedRecord("CONTRACT-CODE_GROUPS-7", "계약 검증 code-groups", Map.of("reason", "contract side effect", "portalScope", "KNUE", "transactionProbe", "mybatis"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").exists())
            .andExpect(jsonPath("$.data.payload.transactionProbe").value("mybatis"));
        mvc.perform(get("/api/code-groups").cookie(cookie).param("keyword", "CONTRACT-CODE_GROUPS-7").param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.length()", greaterThanOrEqualTo(1)));
        mvc.perform(post("/api/configurations").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content(managedRecord("CONTRACT-CONFIGURATIONS-8", "계약 검증 configurations", Map.of("reason", "contract side effect", "portalScope", "KNUE", "transactionProbe", "mybatis"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").exists())
            .andExpect(jsonPath("$.data.payload.transactionProbe").value("mybatis"));
        mvc.perform(get("/api/configurations").cookie(cookie).param("keyword", "CONTRACT-CONFIGURATIONS-8").param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.length()", greaterThanOrEqualTo(1)));
        mvc.perform(post("/api/data-scope-permissions").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content(managedRecord("CONTRACT-DATA_SCOPE_PERMISSIONS-9", "계약 검증 data-scope-permissions", Map.of("reason", "contract side effect", "portalScope", "KNUE", "transactionProbe", "mybatis"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").exists())
            .andExpect(jsonPath("$.data.payload.transactionProbe").value("mybatis"));
        mvc.perform(get("/api/data-scope-permissions").cookie(cookie).param("keyword", "CONTRACT-DATA_SCOPE_PERMISSIONS-9").param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.length()", greaterThanOrEqualTo(1)));
        mvc.perform(post("/api/detail-codes").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content(managedRecord("CONTRACT-DETAIL_CODES-10", "계약 검증 detail-codes", Map.of("reason", "contract side effect", "portalScope", "KNUE", "transactionProbe", "mybatis"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").exists())
            .andExpect(jsonPath("$.data.payload.transactionProbe").value("mybatis"));
        mvc.perform(get("/api/detail-codes").cookie(cookie).param("keyword", "CONTRACT-DETAIL_CODES-10").param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.length()", greaterThanOrEqualTo(1)));
        mvc.perform(post("/api/excel/downloads").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content(managedRecord("CONTRACT-EXCEL_DOWNLOADS-11", "계약 검증 excel-downloads", Map.of("reason", "contract side effect", "portalScope", "KNUE", "transactionProbe", "mybatis"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").exists())
            .andExpect(jsonPath("$.data.payload.transactionProbe").value("mybatis"));
        mvc.perform(get("/api/excel/downloads").cookie(cookie).param("keyword", "CONTRACT-EXCEL_DOWNLOADS-11").param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.length()", greaterThanOrEqualTo(1)));
        mvc.perform(post("/api/excel/templates").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content(managedRecord("CONTRACT-EXCEL_TEMPLATES-12", "계약 검증 excel-templates", Map.of("reason", "contract side effect", "portalScope", "KNUE", "transactionProbe", "mybatis"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").exists())
            .andExpect(jsonPath("$.data.payload.transactionProbe").value("mybatis"));
        mvc.perform(get("/api/excel/templates").cookie(cookie).param("keyword", "CONTRACT-EXCEL_TEMPLATES-12").param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.length()", greaterThanOrEqualTo(1)));
        mvc.perform(post("/api/excel/uploads").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content(managedRecord("CONTRACT-EXCEL_UPLOADS-13", "계약 검증 excel-uploads", Map.of("reason", "contract side effect", "portalScope", "KNUE", "transactionProbe", "mybatis", "uploadStatus", "SAVED"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").exists())
            .andExpect(jsonPath("$.data.payload.transactionProbe").value("mybatis"));
        mvc.perform(get("/api/excel/uploads").cookie(cookie).param("keyword", "CONTRACT-EXCEL_UPLOADS-13").param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.length()", greaterThanOrEqualTo(1)));
        mvc.perform(post("/api/file-policies").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content(managedRecord("CONTRACT-FILE_POLICIES-14", "계약 검증 file-policies", Map.of("reason", "contract side effect", "portalScope", "KNUE", "transactionProbe", "mybatis"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").exists())
            .andExpect(jsonPath("$.data.payload.transactionProbe").value("mybatis"));
        mvc.perform(get("/api/file-policies").cookie(cookie).param("keyword", "CONTRACT-FILE_POLICIES-14").param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.length()", greaterThanOrEqualTo(1)));
        mvc.perform(post("/api/function-permissions").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content(managedRecord("CONTRACT-FUNCTION_PERMISSIONS-15", "계약 검증 function-permissions", Map.of("reason", "contract side effect", "portalScope", "KNUE", "transactionProbe", "mybatis"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").exists())
            .andExpect(jsonPath("$.data.payload.transactionProbe").value("mybatis"));
        mvc.perform(get("/api/function-permissions").cookie(cookie).param("keyword", "CONTRACT-FUNCTION_PERMISSIONS-15").param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.length()", greaterThanOrEqualTo(1)));
        mvc.perform(post("/api/menu-permissions").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content(managedRecord("CONTRACT-MENU_PERMISSIONS-16", "계약 검증 menu-permissions", Map.of("reason", "contract side effect", "portalScope", "KNUE", "transactionProbe", "mybatis"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").exists())
            .andExpect(jsonPath("$.data.payload.transactionProbe").value("mybatis"));
        mvc.perform(get("/api/menu-permissions").cookie(cookie).param("keyword", "CONTRACT-MENU_PERMISSIONS-16").param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.length()", greaterThanOrEqualTo(1)));
        mvc.perform(post("/api/menus").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content(managedRecord("CONTRACT-MENUS-17", "계약 검증 menus", Map.of("reason", "contract side effect", "portalScope", "KNUE", "transactionProbe", "mybatis"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").exists())
            .andExpect(jsonPath("$.data.payload.transactionProbe").value("mybatis"));
        mvc.perform(get("/api/menus").cookie(cookie).param("keyword", "CONTRACT-MENUS-17").param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.length()", greaterThanOrEqualTo(1)));
        mvc.perform(post("/api/notices").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content(managedRecord("CONTRACT-NOTICES-18", "계약 검증 notices", Map.of("reason", "contract side effect", "portalScope", "KNUE", "transactionProbe", "mybatis"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").exists())
            .andExpect(jsonPath("$.data.payload.transactionProbe").value("mybatis"));
        mvc.perform(get("/api/notices").cookie(cookie).param("keyword", "CONTRACT-NOTICES-18").param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.length()", greaterThanOrEqualTo(1)));
        mvc.perform(post("/api/organizations").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content(managedRecord("CONTRACT-ORGANIZATIONS-19", "계약 검증 organizations", Map.of("reason", "contract side effect", "portalScope", "KNUE", "transactionProbe", "mybatis"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").exists())
            .andExpect(jsonPath("$.data.payload.transactionProbe").value("mybatis"));
        mvc.perform(get("/api/organizations").cookie(cookie).param("keyword", "CONTRACT-ORGANIZATIONS-19").param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.length()", greaterThanOrEqualTo(1)));
        mvc.perform(post("/api/positions").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content(managedRecord("CONTRACT-POSITIONS-20", "계약 검증 positions", Map.of("reason", "contract side effect", "portalScope", "KNUE", "transactionProbe", "mybatis"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").exists())
            .andExpect(jsonPath("$.data.payload.transactionProbe").value("mybatis"));
        mvc.perform(get("/api/positions").cookie(cookie).param("keyword", "CONTRACT-POSITIONS-20").param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.length()", greaterThanOrEqualTo(1)));
        mvc.perform(post("/api/privacy/policies").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content(managedRecord("CONTRACT-PRIVACY_POLICIES-21", "계약 검증 privacy-policies", Map.of("reason", "contract side effect", "portalScope", "KNUE", "transactionProbe", "mybatis"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").exists())
            .andExpect(jsonPath("$.data.payload.transactionProbe").value("mybatis"));
        mvc.perform(get("/api/privacy/policies").cookie(cookie).param("keyword", "CONTRACT-PRIVACY_POLICIES-21").param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.length()", greaterThanOrEqualTo(1)));
        mvc.perform(post("/api/roles").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content(managedRecord("CONTRACT-ROLES-22", "계약 검증 roles", Map.of("reason", "contract side effect", "portalScope", "KNUE", "transactionProbe", "mybatis"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").exists())
            .andExpect(jsonPath("$.data.payload.transactionProbe").value("mybatis"));
        mvc.perform(get("/api/roles").cookie(cookie).param("keyword", "CONTRACT-ROLES-22").param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.length()", greaterThanOrEqualTo(1)));
        mvc.perform(post("/api/user-roles").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content(managedRecord("CONTRACT-USER_ROLES-23", "계약 검증 user-roles", Map.of("reason", "contract side effect", "portalScope", "KNUE", "transactionProbe", "mybatis", "assignmentStatus", "ACTIVE"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").exists())
            .andExpect(jsonPath("$.data.payload.transactionProbe").value("mybatis"));
        mvc.perform(get("/api/user-roles").cookie(cookie).param("keyword", "CONTRACT-USER_ROLES-23").param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.length()", greaterThanOrEqualTo(1)));
        mvc.perform(post("/api/users").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content(managedRecord("USR-ADMIN", "계약 검증 users", Map.of("reason", "contract side effect", "portalScope", "KNUE", "transactionProbe", "mybatis", "systemUseYn", "Y", "primaryRoleCode", "R09"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").exists())
            .andExpect(jsonPath("$.data.payload.transactionProbe").value("mybatis"));
        mvc.perform(get("/api/users").cookie(cookie).param("keyword", "USR-ADMIN").param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.length()", greaterThanOrEqualTo(1)));
    }

    @Test
    void sessionAuthAndAdminOperationsExposeLiteralHappyBusinessSideEffectContracts() throws Exception {
        Cookie cookie = loginCookie();
        mvc.perform(post("/api/auth/session").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(Map.of("reason", "session check"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.loginId").value("admin"));
        mvc.perform(post("/api/auth/session/request-body").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(Map.of("requestId", "REQ-CONTRACT"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.user.loginId").value("admin"))
            .andExpect(jsonPath("$.data.accepted.requestId").value("REQ-CONTRACT"));
        mvc.perform(post("/api/admin/active-sessions").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content(managedRecord("ADMIN-CONTRACT-ACTIVE_SESSIONS-1", "관리자 계약 검증", Map.of("reason", "admin side effect", "transactionProbe", "mybatis"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.payload.transactionProbe").value("mybatis"));
        mvc.perform(post("/api/admin/attachments").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content(managedRecord("ADMIN-CONTRACT-ATTACHMENTS-2", "관리자 계약 검증", Map.of("reason", "admin side effect", "transactionProbe", "mybatis"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.payload.transactionProbe").value("mybatis"));
        mvc.perform(post("/api/admin/audit-logs").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content(managedRecord("ADMIN-CONTRACT-AUDIT_LOGS-3", "관리자 계약 검증", Map.of("reason", "admin side effect", "transactionProbe", "mybatis"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.payload.transactionProbe").value("mybatis"));
        mvc.perform(post("/api/admin/base-years").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content(managedRecord("ADMIN-CONTRACT-BASE_YEARS-4", "관리자 계약 검증", Map.of("reason", "admin side effect", "transactionProbe", "mybatis"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.payload.transactionProbe").value("mybatis"));
        mvc.perform(post("/api/admin/batch-definitions").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content(managedRecord("ADMIN-CONTRACT-BATCH_DEFINITIONS-5", "관리자 계약 검증", Map.of("reason", "admin side effect", "transactionProbe", "mybatis"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.payload.transactionProbe").value("mybatis"));
        mvc.perform(post("/api/admin/batch-executions").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content(managedRecord("ADMIN-CONTRACT-BATCH_EXECUTIONS-6", "관리자 계약 검증", Map.of("reason", "admin side effect", "transactionProbe", "mybatis"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.payload.transactionProbe").value("mybatis"));
        mvc.perform(post("/api/admin/batch-results").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content(managedRecord("ADMIN-CONTRACT-BATCH_RESULTS-7", "관리자 계약 검증", Map.of("reason", "admin side effect", "transactionProbe", "mybatis"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.payload.transactionProbe").value("mybatis"));
        mvc.perform(post("/api/admin/code-groups").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content(managedRecord("ADMIN-CONTRACT-CODE_GROUPS-8", "관리자 계약 검증", Map.of("reason", "admin side effect", "transactionProbe", "mybatis"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.payload.transactionProbe").value("mybatis"));
        mvc.perform(post("/api/admin/codes").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content(managedRecord("ADMIN-CONTRACT-CODES-9", "관리자 계약 검증", Map.of("reason", "admin side effect", "transactionProbe", "mybatis"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.payload.transactionProbe").value("mybatis"));
        mvc.perform(post("/api/admin/data-scope-permissions").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content(managedRecord("ADMIN-CONTRACT-DATA_SCOPE_PERMISSIONS-10", "관리자 계약 검증", Map.of("reason", "admin side effect", "transactionProbe", "mybatis"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.payload.transactionProbe").value("mybatis"));
        mvc.perform(post("/api/admin/excel-downloads").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content(managedRecord("ADMIN-CONTRACT-EXCEL_DOWNLOADS-11", "관리자 계약 검증", Map.of("reason", "admin side effect", "transactionProbe", "mybatis"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.payload.transactionProbe").value("mybatis"));
        mvc.perform(post("/api/admin/excel-uploads").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content(managedRecord("ADMIN-CONTRACT-EXCEL_UPLOADS-12", "관리자 계약 검증", Map.of("reason", "admin side effect", "transactionProbe", "mybatis"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.payload.transactionProbe").value("mybatis"));
        mvc.perform(post("/api/admin/file-policies").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content(managedRecord("ADMIN-CONTRACT-FILE_POLICIES-13", "관리자 계약 검증", Map.of("reason", "admin side effect", "transactionProbe", "mybatis"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.payload.transactionProbe").value("mybatis"));
        mvc.perform(post("/api/admin/function-permissions").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content(managedRecord("ADMIN-CONTRACT-FUNCTION_PERMISSIONS-14", "관리자 계약 검증", Map.of("reason", "admin side effect", "transactionProbe", "mybatis"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.payload.transactionProbe").value("mybatis"));
        mvc.perform(post("/api/admin/menu-permissions").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content(managedRecord("ADMIN-CONTRACT-MENU_PERMISSIONS-15", "관리자 계약 검증", Map.of("reason", "admin side effect", "transactionProbe", "mybatis"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.payload.transactionProbe").value("mybatis"));
        mvc.perform(post("/api/admin/menus").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content(managedRecord("ADMIN-CONTRACT-MENUS-16", "관리자 계약 검증", Map.of("reason", "admin side effect", "transactionProbe", "mybatis"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.payload.transactionProbe").value("mybatis"));
        mvc.perform(post("/api/admin/notices").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content(managedRecord("ADMIN-CONTRACT-NOTICES-17", "관리자 계약 검증", Map.of("reason", "admin side effect", "transactionProbe", "mybatis"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.payload.transactionProbe").value("mybatis"));
        mvc.perform(post("/api/admin/organizations").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content(managedRecord("ADMIN-CONTRACT-ORGANIZATIONS-18", "관리자 계약 검증", Map.of("reason", "admin side effect", "transactionProbe", "mybatis"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.payload.transactionProbe").value("mybatis"));
        mvc.perform(post("/api/admin/personal-information").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content(managedRecord("ADMIN-CONTRACT-PERSONAL_INFORMATION-19", "관리자 계약 검증", Map.of("reason", "admin side effect", "transactionProbe", "mybatis"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.payload.transactionProbe").value("mybatis"));
        mvc.perform(post("/api/admin/positions").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content(managedRecord("ADMIN-CONTRACT-POSITIONS-20", "관리자 계약 검증", Map.of("reason", "admin side effect", "transactionProbe", "mybatis"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.payload.transactionProbe").value("mybatis"));
        mvc.perform(post("/api/admin/roles").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content(managedRecord("ADMIN-CONTRACT-ROLES-21", "관리자 계약 검증", Map.of("reason", "admin side effect", "transactionProbe", "mybatis"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.payload.transactionProbe").value("mybatis"));
        mvc.perform(post("/api/admin/system-settings").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content(managedRecord("ADMIN-CONTRACT-SYSTEM_SETTINGS-22", "관리자 계약 검증", Map.of("reason", "admin side effect", "transactionProbe", "mybatis"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.payload.transactionProbe").value("mybatis"));
        mvc.perform(post("/api/admin/upload-templates").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content(managedRecord("ADMIN-CONTRACT-UPLOAD_TEMPLATES-23", "관리자 계약 검증", Map.of("reason", "admin side effect", "transactionProbe", "mybatis"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.payload.transactionProbe").value("mybatis"));
        mvc.perform(post("/api/admin/user-roles").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content(managedRecord("ADMIN-CONTRACT-USER_ROLES-24", "관리자 계약 검증", Map.of("reason", "admin side effect", "transactionProbe", "mybatis"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.payload.transactionProbe").value("mybatis"));
        mvc.perform(post("/api/admin/users").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content(managedRecord("USR-ADMIN", "관리자 계약 검증", Map.of("reason", "admin side effect", "transactionProbe", "mybatis", "systemUseYn", "Y", "primaryRoleCode", "R09"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.payload.transactionProbe").value("mybatis"));
        String sessionId = cookie.getValue();
        mvc.perform(post("/api/sessions").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content(managedRecord(sessionId, "세션 강제 종료", Map.of("reason", "contract forced termination", "sessionStatus", "FORCED_END"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("FORCED_END"))
            .andExpect(jsonPath("$.data.payload.endReason").value("contract forced termination"));
        Cookie logoutCookie = loginCookie();
        mvc.perform(post("/api/auth/logout").cookie(logoutCookie).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(Map.of("reason", "contract logout"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("LOGGED_OUT"))
            .andExpect(cookie().maxAge("AIOPS_SESSION", 0));
    }

    @Test
    void authLoginRejectsInvalidCredentialsWithoutCreatingActiveSession() throws Exception {
        mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("loginId", "admin", "password", "wrong-password"))))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }
}
