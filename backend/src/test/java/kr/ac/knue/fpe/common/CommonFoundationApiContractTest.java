package kr.ac.knue.fpe.common;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import kr.ac.knue.fpe.common.service.CommonFeatureService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CommonFoundationApiContractTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    CommonFeatureService commonFeatureService;

    @Test
    @DisplayName("OpenAPI fixture is loaded from classpath contracts/openapi.yaml")
    void openApiFixtureIsLoadedFromClasspath() throws Exception {
        ClassPathResource contract = new ClassPathResource("contracts/openapi.yaml");
        org.assertj.core.api.Assertions.assertThat(contract.exists()).isTrue();
        org.assertj.core.api.Assertions.assertThat(contract.getContentAsString(java.nio.charset.StandardCharsets.UTF_8))
            .contains("/api/admin/cmn/fr/001")
            .contains("operationId: cmn_fr_080_run");
    }

    @Test
    @DisplayName("POST /api/auth/login creates ACTIVE session and validates required fields")
    void postApiAuthLoginCreatesSessionAndValidatesRequiredFields() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"loginId\":\"admin\",\"password\":\"admin\"}"))
            .andExpect(status().isOk())
            .andExpect(cookie().exists("KNUE-FPE-SESSION"))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.state").value("ACTIVE"))
            .andExpect(jsonPath("$.data.roleCodes[0]").value("R09"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"loginId\":\"admin\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("BAD_REQUEST"));
    }

    @Test
    @DisplayName("POST /api/auth/logout records LOGGED_OUT session transition and protects anonymous calls")
    void postApiAuthLogoutRecordsLoggedOutTransition() throws Exception {
        Cookie cookie = loginCookie();
        mockMvc.perform(post("/api/auth/logout").cookie(cookie))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.state").value("LOGGED_OUT"));

        mockMvc.perform(post("/api/auth/logout"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }

    @Test
    @DisplayName("GET /api/auth/me returns current session envelope and /api/health returns service status")
    void getAuthMeAndHealthReturnResponseContracts() throws Exception {
        Cookie cookie = loginCookie();
        mockMvc.perform(get("/api/auth/me").cookie(cookie))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.loginId").value("admin"))
            .andExpect(jsonPath("$.data.roleCodes[0]").value("R09"));

        mockMvc.perform(get("/api/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("UP"));
    }

    @Test
    @DisplayName("GET admin operation literals return ApiResponse page contracts with portal filtering")
    void getAdminOperationLiteralsReturnPageContracts() throws Exception {
        Cookie cookie = loginCookie();
        mockMvc.perform(get("/api/admin/cmn/fr/001").param("keyword", "관리").cookie(cookie))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.items").isArray())
            .andExpect(jsonPath("$.data.page.size").value(20));

        mockMvc.perform(get("/api/admin/cmn/fr/002").param("keyword", "관리").cookie(cookie))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.items").isArray())
            .andExpect(jsonPath("$.data.page.size").value(20));

        mockMvc.perform(get("/api/admin/cmn/fr/003").param("keyword", "관리").cookie(cookie))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.items").isArray())
            .andExpect(jsonPath("$.data.page.size").value(20));

        mockMvc.perform(get("/api/admin/cmn/fr/005").param("keyword", "관리").cookie(cookie))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.items").isArray())
            .andExpect(jsonPath("$.data.page.size").value(20));

        mockMvc.perform(get("/api/admin/cmn/fr/006").param("keyword", "관리").cookie(cookie))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.items").isArray())
            .andExpect(jsonPath("$.data.page.size").value(20));

        mockMvc.perform(get("/api/admin/cmn/fr/007").param("keyword", "관리").cookie(cookie))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.items").isArray())
            .andExpect(jsonPath("$.data.page.size").value(20));

        mockMvc.perform(get("/api/admin/cmn/fr/008").param("keyword", "관리").cookie(cookie))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.items").isArray())
            .andExpect(jsonPath("$.data.page.size").value(20));

        mockMvc.perform(get("/api/admin/cmn/fr/009").param("keyword", "관리").cookie(cookie))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.items").isArray())
            .andExpect(jsonPath("$.data.page.size").value(20));

        mockMvc.perform(get("/api/admin/cmn/fr/013/014").param("keyword", "관리").cookie(cookie))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.items").isArray())
            .andExpect(jsonPath("$.data.page.size").value(20));

        mockMvc.perform(get("/api/admin/cmn/fr/016").param("keyword", "관리").cookie(cookie))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.items").isArray())
            .andExpect(jsonPath("$.data.page.size").value(20));

        mockMvc.perform(get("/api/admin/cmn/fr/017").param("keyword", "관리").cookie(cookie))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.items").isArray())
            .andExpect(jsonPath("$.data.page.size").value(20));

        mockMvc.perform(get("/api/admin/cmn/fr/019").param("keyword", "관리").cookie(cookie))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.items").isArray())
            .andExpect(jsonPath("$.data.page.size").value(20));

        mockMvc.perform(get("/api/admin/cmn/fr/020").param("keyword", "관리").cookie(cookie))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.items").isArray())
            .andExpect(jsonPath("$.data.page.size").value(20));

        mockMvc.perform(get("/api/admin/cmn/fr/021").param("keyword", "관리").cookie(cookie))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.items").isArray())
            .andExpect(jsonPath("$.data.page.size").value(20));

        mockMvc.perform(get("/api/admin/cmn/fr/023").param("keyword", "관리").cookie(cookie))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.items").isArray())
            .andExpect(jsonPath("$.data.page.size").value(20));

        mockMvc.perform(get("/api/admin/cmn/fr/052/053/054").param("keyword", "관리").cookie(cookie))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.items").isArray())
            .andExpect(jsonPath("$.data.page.size").value(20));

        mockMvc.perform(get("/api/admin/cmn/fr/055").param("keyword", "관리").cookie(cookie))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.items").isArray())
            .andExpect(jsonPath("$.data.page.size").value(20));

        mockMvc.perform(get("/api/admin/cmn/fr/056").param("keyword", "관리").cookie(cookie))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.items").isArray())
            .andExpect(jsonPath("$.data.page.size").value(20));

        mockMvc.perform(get("/api/admin/cmn/fr/056/history").param("keyword", "관리").cookie(cookie))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.items").isArray())
            .andExpect(jsonPath("$.data.page.size").value(20));

        mockMvc.perform(get("/api/admin/cmn/fr/056/errors/download").param("keyword", "관리").cookie(cookie))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.items").isArray())
            .andExpect(jsonPath("$.data.page.size").value(20));

        mockMvc.perform(get("/api/admin/cmn/fr/059").param("keyword", "관리").cookie(cookie))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.items").isArray())
            .andExpect(jsonPath("$.data.page.size").value(20));

        mockMvc.perform(get("/api/admin/cmn/fr/071/072/073").param("keyword", "관리").cookie(cookie))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.items").isArray())
            .andExpect(jsonPath("$.data.page.size").value(20));

        mockMvc.perform(get("/api/admin/cmn/fr/074").param("keyword", "관리").cookie(cookie))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.items").isArray())
            .andExpect(jsonPath("$.data.page.size").value(20));

        mockMvc.perform(get("/api/admin/cmn/fr/074/termination-history").param("keyword", "관리").cookie(cookie))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.items").isArray())
            .andExpect(jsonPath("$.data.page.size").value(20));

        mockMvc.perform(get("/api/admin/cmn/fr/076/077/078").param("keyword", "관리").cookie(cookie))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.items").isArray())
            .andExpect(jsonPath("$.data.page.size").value(20));

        mockMvc.perform(get("/api/admin/cmn/fr/079").param("keyword", "관리").cookie(cookie))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.items").isArray())
            .andExpect(jsonPath("$.data.page.size").value(20));

        mockMvc.perform(get("/api/admin/cmn/fr/080").param("keyword", "관리").cookie(cookie))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.items").isArray())
            .andExpect(jsonPath("$.data.page.size").value(20));

        mockMvc.perform(get("/api/admin/cmn/fr/081").param("keyword", "관리").cookie(cookie))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.items").isArray())
            .andExpect(jsonPath("$.data.page.size").value(20));

        mockMvc.perform(get("/api/admin/cmn/fr/055/TPL-EXCEL-001/download").param("keyword", "관리").cookie(cookie))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.items").isArray())
            .andExpect(jsonPath("$.data.page.size").value(20));

        mockMvc.perform(get("/api/admin/cmn/fr/076/077/078/business").param("keyword", "관리").cookie(cookie))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.items").isArray())
            .andExpect(jsonPath("$.data.page.size").value(20));

        mockMvc.perform(get("/api/admin/cmn/fr/076/077/078/sensitive").param("keyword", "관리").cookie(cookie))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.items").isArray())
            .andExpect(jsonPath("$.data.page.size").value(20));

        mockMvc.perform(get("/api/admin/cmn/fr/076/077/078/authority").param("keyword", "관리").cookie(cookie))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.items").isArray())
            .andExpect(jsonPath("$.data.page.size").value(20));

        mockMvc.perform(get("/api/admin/cmn/fr/071/072/073/access-permissions").param("keyword", "관리").cookie(cookie))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.items").isArray())
            .andExpect(jsonPath("$.data.page.size").value(20));

        mockMvc.perform(get("/api/admin/cmn/fr/071/072/073/access-history").param("keyword", "관리").cookie(cookie))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.items").isArray())
            .andExpect(jsonPath("$.data.page.size").value(20));

        mockMvc.perform(get("/api/admin/cmn/fr/001"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"))
            .andExpect(jsonPath("$.error.message").value(containsString("인증")));
    }

    @Test
    @DisplayName("POST admin operation literals persist side effects and validation contracts")
    void postAdminOperationLiteralsPersistSideEffects() throws Exception {
        Cookie cookie = loginCookie();
        mockMvc.perform(post("/api/admin/cmn/fr/002").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.fieldErrors.reason", notNullValue()));

        mockMvc.perform(post("/api/admin/cmn/fr/002").cookie(cookie).contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"정적 계약 검증\",\"portalId\":\"ADMIN\",\"businessKey\":\"CONTRACT-CHECK\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.recordId", notNullValue()))
            .andExpect(jsonPath("$.data.auditReason").value("정적 계약 검증"))
            .andExpect(jsonPath("$.data.updatedAt", notNullValue()))
            .andExpect(jsonPath("$.data.status", notNullValue()));

        mockMvc.perform(post("/api/admin/cmn/fr/003").cookie(cookie).contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"정적 계약 검증\",\"portalId\":\"ADMIN\",\"businessKey\":\"CONTRACT-CHECK\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.recordId", notNullValue()))
            .andExpect(jsonPath("$.data.auditReason").value("정적 계약 검증"))
            .andExpect(jsonPath("$.data.updatedAt", notNullValue()))
            .andExpect(jsonPath("$.data.status", notNullValue()));

        mockMvc.perform(post("/api/admin/cmn/fr/005").cookie(cookie).contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"정적 계약 검증\",\"portalId\":\"ADMIN\",\"businessKey\":\"CONTRACT-CHECK\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.recordId", notNullValue()))
            .andExpect(jsonPath("$.data.auditReason").value("정적 계약 검증"))
            .andExpect(jsonPath("$.data.updatedAt", notNullValue()))
            .andExpect(jsonPath("$.data.status", notNullValue()));

        mockMvc.perform(post("/api/admin/cmn/fr/006").cookie(cookie).contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"정적 계약 검증\",\"portalId\":\"ADMIN\",\"businessKey\":\"CONTRACT-CHECK\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.recordId", notNullValue()))
            .andExpect(jsonPath("$.data.auditReason").value("정적 계약 검증"))
            .andExpect(jsonPath("$.data.updatedAt", notNullValue()))
            .andExpect(jsonPath("$.data.status", notNullValue()));

        mockMvc.perform(post("/api/admin/cmn/fr/013/014").cookie(cookie).contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"정적 계약 검증\",\"portalId\":\"ADMIN\",\"businessKey\":\"CONTRACT-CHECK\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.recordId", notNullValue()))
            .andExpect(jsonPath("$.data.auditReason").value("정적 계약 검증"))
            .andExpect(jsonPath("$.data.updatedAt", notNullValue()))
            .andExpect(jsonPath("$.data.status", notNullValue()));

        mockMvc.perform(post("/api/admin/cmn/fr/016").cookie(cookie).contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"정적 계약 검증\",\"portalId\":\"ADMIN\",\"businessKey\":\"CONTRACT-CHECK\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.recordId", notNullValue()))
            .andExpect(jsonPath("$.data.auditReason").value("정적 계약 검증"))
            .andExpect(jsonPath("$.data.updatedAt", notNullValue()))
            .andExpect(jsonPath("$.data.status", notNullValue()));

        mockMvc.perform(post("/api/admin/cmn/fr/017").cookie(cookie).contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"정적 계약 검증\",\"portalId\":\"ADMIN\",\"businessKey\":\"CONTRACT-CHECK\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.recordId", notNullValue()))
            .andExpect(jsonPath("$.data.auditReason").value("정적 계약 검증"))
            .andExpect(jsonPath("$.data.updatedAt", notNullValue()))
            .andExpect(jsonPath("$.data.status", notNullValue()));

        mockMvc.perform(post("/api/admin/cmn/fr/020").cookie(cookie).contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"정적 계약 검증\",\"portalId\":\"ADMIN\",\"businessKey\":\"CONTRACT-CHECK\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.recordId", notNullValue()))
            .andExpect(jsonPath("$.data.auditReason").value("정적 계약 검증"))
            .andExpect(jsonPath("$.data.updatedAt", notNullValue()))
            .andExpect(jsonPath("$.data.status", notNullValue()));

        mockMvc.perform(post("/api/admin/cmn/fr/021").cookie(cookie).contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"정적 계약 검증\",\"portalId\":\"ADMIN\",\"businessKey\":\"CONTRACT-CHECK\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.recordId", notNullValue()))
            .andExpect(jsonPath("$.data.auditReason").value("정적 계약 검증"))
            .andExpect(jsonPath("$.data.updatedAt", notNullValue()))
            .andExpect(jsonPath("$.data.status", notNullValue()));

        mockMvc.perform(post("/api/admin/cmn/fr/023").cookie(cookie).contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"정적 계약 검증\",\"portalId\":\"ADMIN\",\"businessKey\":\"CONTRACT-CHECK\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.recordId", notNullValue()))
            .andExpect(jsonPath("$.data.auditReason").value("정적 계약 검증"))
            .andExpect(jsonPath("$.data.updatedAt", notNullValue()))
            .andExpect(jsonPath("$.data.status", notNullValue()));

        mockMvc.perform(post("/api/admin/cmn/fr/052/053/054/integrity-check").cookie(cookie).contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"정적 계약 검증\",\"portalId\":\"ADMIN\",\"businessKey\":\"CONTRACT-CHECK\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.recordId", notNullValue()))
            .andExpect(jsonPath("$.data.auditReason").value("정적 계약 검증"))
            .andExpect(jsonPath("$.data.updatedAt", notNullValue()))
            .andExpect(jsonPath("$.data.status", notNullValue()));

        mockMvc.perform(post("/api/admin/cmn/fr/055").cookie(cookie).contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"정적 계약 검증\",\"portalId\":\"ADMIN\",\"businessKey\":\"CONTRACT-CHECK\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.recordId", notNullValue()))
            .andExpect(jsonPath("$.data.auditReason").value("정적 계약 검증"))
            .andExpect(jsonPath("$.data.updatedAt", notNullValue()))
            .andExpect(jsonPath("$.data.status", notNullValue()));

        mockMvc.perform(post("/api/admin/cmn/fr/056/upload").cookie(cookie).contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"정적 계약 검증\",\"portalId\":\"ADMIN\",\"businessKey\":\"CONTRACT-CHECK\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.recordId", notNullValue()))
            .andExpect(jsonPath("$.data.auditReason").value("정적 계약 검증"))
            .andExpect(jsonPath("$.data.updatedAt", notNullValue()))
            .andExpect(jsonPath("$.data.status", notNullValue()));

        mockMvc.perform(post("/api/admin/cmn/fr/059/download").cookie(cookie).contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"정적 계약 검증\",\"portalId\":\"ADMIN\",\"businessKey\":\"CONTRACT-CHECK\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.recordId", notNullValue()))
            .andExpect(jsonPath("$.data.auditReason").value("정적 계약 검증"))
            .andExpect(jsonPath("$.data.updatedAt", notNullValue()))
            .andExpect(jsonPath("$.data.status", notNullValue()));

        mockMvc.perform(post("/api/admin/cmn/fr/074/SESSION-ACTIVE-001/terminate").cookie(cookie).contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"정적 계약 검증\",\"portalId\":\"ADMIN\",\"businessKey\":\"CONTRACT-CHECK\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.recordId", notNullValue()))
            .andExpect(jsonPath("$.data.auditReason").value("정적 계약 검증"))
            .andExpect(jsonPath("$.data.updatedAt", notNullValue()))
            .andExpect(jsonPath("$.data.status", notNullValue()));

        mockMvc.perform(post("/api/admin/cmn/fr/079").cookie(cookie).contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"정적 계약 검증\",\"portalId\":\"ADMIN\",\"businessKey\":\"CONTRACT-CHECK\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.recordId", notNullValue()))
            .andExpect(jsonPath("$.data.auditReason").value("정적 계약 검증"))
            .andExpect(jsonPath("$.data.updatedAt", notNullValue()))
            .andExpect(jsonPath("$.data.status", notNullValue()));

        mockMvc.perform(post("/api/admin/cmn/fr/080/BATCH-NIGHTLY-001/run").cookie(cookie).contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"정적 계약 검증\",\"portalId\":\"ADMIN\",\"businessKey\":\"CONTRACT-CHECK\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.recordId", notNullValue()))
            .andExpect(jsonPath("$.data.auditReason").value("정적 계약 검증"))
            .andExpect(jsonPath("$.data.updatedAt", notNullValue()))
            .andExpect(jsonPath("$.data.status", notNullValue()));

        mockMvc.perform(post("/api/admin/cmn/fr/080/BATCH-NIGHTLY-001/stop").cookie(cookie).contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"정적 계약 검증\",\"portalId\":\"ADMIN\",\"businessKey\":\"CONTRACT-CHECK\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.recordId", notNullValue()))
            .andExpect(jsonPath("$.data.auditReason").value("정적 계약 검증"))
            .andExpect(jsonPath("$.data.updatedAt", notNullValue()))
            .andExpect(jsonPath("$.data.status", notNullValue()));

        mockMvc.perform(post("/api/admin/cmn/fr/080/BATCH-NIGHTLY-001/rerun").cookie(cookie).contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"정적 계약 검증\",\"portalId\":\"ADMIN\",\"businessKey\":\"CONTRACT-CHECK\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.recordId", notNullValue()))
            .andExpect(jsonPath("$.data.auditReason").value("정적 계약 검증"))
            .andExpect(jsonPath("$.data.updatedAt", notNullValue()))
            .andExpect(jsonPath("$.data.status", notNullValue()));

    }

    @Test
    @DisplayName("PUT admin operation literals persist side effects and validation contracts")
    void putAdminOperationLiteralsPersistSideEffects() throws Exception {
        Cookie cookie = loginCookie();
        mockMvc.perform(put("/api/admin/cmn/fr/007").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.fieldErrors.reason", notNullValue()));

        mockMvc.perform(put("/api/admin/cmn/fr/007").cookie(cookie).contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"정적 계약 검증\",\"portalId\":\"ADMIN\",\"businessKey\":\"CONTRACT-CHECK\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.recordId", notNullValue()))
            .andExpect(jsonPath("$.data.auditReason").value("정적 계약 검증"))
            .andExpect(jsonPath("$.data.updatedAt", notNullValue()))
            .andExpect(jsonPath("$.data.status", notNullValue()));

        mockMvc.perform(put("/api/admin/cmn/fr/008").cookie(cookie).contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"정적 계약 검증\",\"portalId\":\"ADMIN\",\"businessKey\":\"CONTRACT-CHECK\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.recordId", notNullValue()))
            .andExpect(jsonPath("$.data.auditReason").value("정적 계약 검증"))
            .andExpect(jsonPath("$.data.updatedAt", notNullValue()))
            .andExpect(jsonPath("$.data.status", notNullValue()));

        mockMvc.perform(put("/api/admin/cmn/fr/009").cookie(cookie).contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"정적 계약 검증\",\"portalId\":\"ADMIN\",\"businessKey\":\"CONTRACT-CHECK\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.recordId", notNullValue()))
            .andExpect(jsonPath("$.data.auditReason").value("정적 계약 검증"))
            .andExpect(jsonPath("$.data.updatedAt", notNullValue()))
            .andExpect(jsonPath("$.data.status", notNullValue()));

        mockMvc.perform(put("/api/admin/cmn/fr/019").cookie(cookie).contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"정적 계약 검증\",\"portalId\":\"ADMIN\",\"businessKey\":\"CONTRACT-CHECK\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.recordId", notNullValue()))
            .andExpect(jsonPath("$.data.auditReason").value("정적 계약 검증"))
            .andExpect(jsonPath("$.data.updatedAt", notNullValue()))
            .andExpect(jsonPath("$.data.status", notNullValue()));

        mockMvc.perform(put("/api/admin/cmn/fr/071/072/073").cookie(cookie).contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"정적 계약 검증\",\"portalId\":\"ADMIN\",\"businessKey\":\"CONTRACT-CHECK\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.recordId", notNullValue()))
            .andExpect(jsonPath("$.data.auditReason").value("정적 계약 검증"))
            .andExpect(jsonPath("$.data.updatedAt", notNullValue()))
            .andExpect(jsonPath("$.data.status", notNullValue()));

        mockMvc.perform(put("/api/admin/cmn/fr/071/072/073/access-permissions").cookie(cookie).contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"정적 계약 검증\",\"portalId\":\"ADMIN\",\"businessKey\":\"CONTRACT-CHECK\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.recordId", notNullValue()))
            .andExpect(jsonPath("$.data.auditReason").value("정적 계약 검증"))
            .andExpect(jsonPath("$.data.updatedAt", notNullValue()))
            .andExpect(jsonPath("$.data.status", notNullValue()));

    }

    @Test
    @DisplayName("PATCH admin operation literals persist side effects and validation contracts")
    void patchAdminOperationLiteralsPersistSideEffects() throws Exception {
        Cookie cookie = loginCookie();
        mockMvc.perform(patch("/api/admin/cmn/fr/001/USR-ADMIN").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.fieldErrors.reason", notNullValue()));

        MvcResult userUpdate = mockMvc.perform(patch("/api/admin/cmn/fr/001/USR-ADMIN").cookie(cookie).contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"정적 계약 검증\",\"portalId\":\"ADMIN\",\"businessKey\":\"CONTRACT-CHECK\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.recordId", notNullValue()))
            .andExpect(jsonPath("$.data.auditReason").value("정적 계약 검증"))
            .andExpect(jsonPath("$.data.updatedAt", notNullValue()))
            .andExpect(jsonPath("$.data.status", notNullValue()))
            .andReturn();
        String userRecordId = com.jayway.jsonpath.JsonPath.read(userUpdate.getResponse().getContentAsString(), "$.data.recordId");
        org.assertj.core.api.Assertions.assertThat(commonFeatureService.countAuditLogByTargetId(userRecordId)).isGreaterThan(0);

        mockMvc.perform(patch("/api/admin/cmn/fr/002/ORG-COLLEGE").cookie(cookie).contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"정적 계약 검증\",\"portalId\":\"ADMIN\",\"businessKey\":\"CONTRACT-CHECK\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.recordId", notNullValue()))
            .andExpect(jsonPath("$.data.auditReason").value("정적 계약 검증"))
            .andExpect(jsonPath("$.data.updatedAt", notNullValue()))
            .andExpect(jsonPath("$.data.status", notNullValue()));

        mockMvc.perform(patch("/api/admin/cmn/fr/003/APPT-DEAN").cookie(cookie).contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"정적 계약 검증\",\"portalId\":\"ADMIN\",\"businessKey\":\"CONTRACT-CHECK\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.recordId", notNullValue()))
            .andExpect(jsonPath("$.data.auditReason").value("정적 계약 검증"))
            .andExpect(jsonPath("$.data.updatedAt", notNullValue()))
            .andExpect(jsonPath("$.data.status", notNullValue()));

        mockMvc.perform(patch("/api/admin/cmn/fr/005/R09").cookie(cookie).contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"정적 계약 검증\",\"portalId\":\"ADMIN\",\"businessKey\":\"CONTRACT-CHECK\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.recordId", notNullValue()))
            .andExpect(jsonPath("$.data.auditReason").value("정적 계약 검증"))
            .andExpect(jsonPath("$.data.updatedAt", notNullValue()))
            .andExpect(jsonPath("$.data.status", notNullValue()));

        mockMvc.perform(patch("/api/admin/cmn/fr/013/014/MENU-ADMIN").cookie(cookie).contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"정적 계약 검증\",\"portalId\":\"ADMIN\",\"businessKey\":\"CONTRACT-CHECK\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.recordId", notNullValue()))
            .andExpect(jsonPath("$.data.auditReason").value("정적 계약 검증"))
            .andExpect(jsonPath("$.data.updatedAt", notNullValue()))
            .andExpect(jsonPath("$.data.status", notNullValue()));

        mockMvc.perform(patch("/api/admin/cmn/fr/016/GRP-CMN").cookie(cookie).contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"정적 계약 검증\",\"portalId\":\"ADMIN\",\"businessKey\":\"CONTRACT-CHECK\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.recordId", notNullValue()))
            .andExpect(jsonPath("$.data.auditReason").value("정적 계약 검증"))
            .andExpect(jsonPath("$.data.updatedAt", notNullValue()))
            .andExpect(jsonPath("$.data.status", notNullValue()));

        mockMvc.perform(patch("/api/admin/cmn/fr/017/DTL-ACTIVE").cookie(cookie).contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"정적 계약 검증\",\"portalId\":\"ADMIN\",\"businessKey\":\"CONTRACT-CHECK\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.recordId", notNullValue()))
            .andExpect(jsonPath("$.data.auditReason").value("정적 계약 검증"))
            .andExpect(jsonPath("$.data.updatedAt", notNullValue()))
            .andExpect(jsonPath("$.data.status", notNullValue()));

        mockMvc.perform(patch("/api/admin/cmn/fr/020/2026").cookie(cookie).contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"정적 계약 검증\",\"portalId\":\"ADMIN\",\"businessKey\":\"CONTRACT-CHECK\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.recordId", notNullValue()))
            .andExpect(jsonPath("$.data.auditReason").value("정적 계약 검증"))
            .andExpect(jsonPath("$.data.updatedAt", notNullValue()))
            .andExpect(jsonPath("$.data.status", notNullValue()));

        mockMvc.perform(patch("/api/admin/cmn/fr/021/FILE-POLICY-PDF").cookie(cookie).contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"정적 계약 검증\",\"portalId\":\"ADMIN\",\"businessKey\":\"CONTRACT-CHECK\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.recordId", notNullValue()))
            .andExpect(jsonPath("$.data.auditReason").value("정적 계약 검증"))
            .andExpect(jsonPath("$.data.updatedAt", notNullValue()))
            .andExpect(jsonPath("$.data.status", notNullValue()));

        mockMvc.perform(patch("/api/admin/cmn/fr/023/NOTICE-MAINT").cookie(cookie).contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"정적 계약 검증\",\"portalId\":\"ADMIN\",\"businessKey\":\"CONTRACT-CHECK\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.recordId", notNullValue()))
            .andExpect(jsonPath("$.data.auditReason").value("정적 계약 검증"))
            .andExpect(jsonPath("$.data.updatedAt", notNullValue()))
            .andExpect(jsonPath("$.data.status", notNullValue()));

        mockMvc.perform(patch("/api/admin/cmn/fr/055/TPL-EXCEL-001").cookie(cookie).contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"정적 계약 검증\",\"portalId\":\"ADMIN\",\"businessKey\":\"CONTRACT-CHECK\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.recordId", notNullValue()))
            .andExpect(jsonPath("$.data.auditReason").value("정적 계약 검증"))
            .andExpect(jsonPath("$.data.updatedAt", notNullValue()))
            .andExpect(jsonPath("$.data.status", notNullValue()));

        mockMvc.perform(patch("/api/admin/cmn/fr/079/BATCH-NIGHTLY-001").cookie(cookie).contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"정적 계약 검증\",\"portalId\":\"ADMIN\",\"businessKey\":\"CONTRACT-CHECK\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.recordId", notNullValue()))
            .andExpect(jsonPath("$.data.auditReason").value("정적 계약 검증"))
            .andExpect(jsonPath("$.data.updatedAt", notNullValue()))
            .andExpect(jsonPath("$.data.status", notNullValue()));

    }

    @Test
    @DisplayName("DELETE admin operation literals persist side effects and validation contracts")
    void deleteAdminOperationLiteralsPersistSideEffects() throws Exception {
        Cookie cookie = loginCookie();
        mockMvc.perform(delete("/api/admin/cmn/fr/006/ROLE-GRANT-OPS").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.fieldErrors.reason", notNullValue()));

        mockMvc.perform(delete("/api/admin/cmn/fr/006/ROLE-GRANT-OPS").cookie(cookie).contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"정적 계약 검증\",\"portalId\":\"ADMIN\",\"businessKey\":\"CONTRACT-CHECK\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.recordId", notNullValue()))
            .andExpect(jsonPath("$.data.auditReason").value("정적 계약 검증"))
            .andExpect(jsonPath("$.data.updatedAt", notNullValue()))
            .andExpect(jsonPath("$.data.status", notNullValue()));

        mockMvc.perform(delete("/api/admin/cmn/fr/052/053/054/FILE-META-001").cookie(cookie).contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"정적 계약 검증\",\"portalId\":\"ADMIN\",\"businessKey\":\"CONTRACT-CHECK\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.recordId", notNullValue()))
            .andExpect(jsonPath("$.data.auditReason").value("정적 계약 검증"))
            .andExpect(jsonPath("$.data.updatedAt", notNullValue()))
            .andExpect(jsonPath("$.data.status", notNullValue()));

    }

    @Test
    @DisplayName("AttachmentController literal paths upload, list, download metadata, and delete attachments")
    void attachmentControllerLiteralPathsProvideAttachmentContracts() throws Exception {
        Cookie cookie = loginCookie();
        MvcResult uploaded = mockMvc.perform(multipart("/api/admin/cmn/fr/052/053/054/attachments")
                .file(new MockMultipartFile("file", "contract-evidence.txt", MediaType.TEXT_PLAIN_VALUE,
                    "attachment contract evidence".getBytes(java.nio.charset.StandardCharsets.UTF_8)))
                .param("businessKey", "CONTRACT-CHECK")
                .param("reason", "첨부파일 정적 계약 검증")
                .cookie(cookie))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.attachmentId", notNullValue()))
            .andExpect(jsonPath("$.data.businessKey").value("CONTRACT-CHECK"))
            .andExpect(jsonPath("$.data.originalFileName").value("contract-evidence.txt"))
            .andExpect(jsonPath("$.data.malwareScanStatus").value("PASSED"))
            .andReturn();
        String attachmentId = com.jayway.jsonpath.JsonPath.read(uploaded.getResponse().getContentAsString(), "$.data.attachmentId");

        mockMvc.perform(multipart("/api/admin/cmn/fr/052/053/054/attachments")
                .file(new MockMultipartFile("file", "empty.txt", MediaType.TEXT_PLAIN_VALUE, new byte[0]))
                .param("businessKey", "CONTRACT-CHECK")
                .param("reason", "첨부파일 정적 계약 검증")
                .cookie(cookie))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.fieldErrors.file", notNullValue()));

        mockMvc.perform(get("/api/admin/cmn/fr/052/053/054/attachments").cookie(cookie))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.items").isArray())
            .andExpect(jsonPath("$.data.page.totalElements").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)));

        mockMvc.perform(get("/api/admin/cmn/fr/052/053/054/attachments/{attachmentId}/download", attachmentId).cookie(cookie))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.attachmentId").value(attachmentId))
            .andExpect(jsonPath("$.data.originalFileName").value("contract-evidence.txt"));

        mockMvc.perform(delete("/api/admin/cmn/fr/052/053/054/attachments/{attachmentId}", attachmentId).cookie(cookie))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.attachmentId").value(attachmentId))
            .andExpect(jsonPath("$.data.deleteYn").value("Y"));
    }

    private Cookie loginCookie() throws Exception {
        MvcResult login = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"loginId\":\"admin\",\"password\":\"admin\"}"))
            .andExpect(status().isOk())
            .andReturn();
        return login.getResponse().getCookie("KNUE-FPE-SESSION");
    }
}
