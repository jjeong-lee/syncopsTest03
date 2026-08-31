package kr.ac.knue.facultyassessment.externalintegrations;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureMockMvc
class SchoolInformationApiContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SchoolInformationPort schoolInformationPort;

    @Test
    void r09ReceivesInfo000RowsInTheCommonSuccessEnvelope() throws Exception {
        when(schoolInformationPort.lookup(any())).thenReturn(List.of(
            new SchoolInformationPort.SchoolInformation(
                "서울특별시교육청", "가락초등학교", "초등학교", "송파구", "공립", "서울 송파구 가락로 1", "02-0000-0000"
            )
        ));
        Cookie session = login();

        mockMvc.perform(get("/api/external-integrations/schools")
                .cookie(session)
                .param("schoolName", "가락"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[0].schoolName").value("가락초등학교"))
            .andExpect(jsonPath("$.data[0].educationOfficeName").value("서울특별시교육청"));
    }

    @Test
    void r09ReceivesAnEmptySuccessEnvelopeForInfo200() throws Exception {
        when(schoolInformationPort.lookup(any())).thenReturn(List.of());
        Cookie session = login();

        mockMvc.perform(get("/api/external-integrations/schools").cookie(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void externalFailureUsesTheCommonErrorEnvelope() throws Exception {
        when(schoolInformationPort.lookup(any())).thenThrow(new SchoolInformationPort.ExternalLookupException("학교정보 조회에 실패했습니다."));
        Cookie session = login();

        mockMvc.perform(get("/api/external-integrations/schools").cookie(session))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("EXTERNAL_INTEGRATION_ERROR"))
            .andExpect(jsonPath("$.error.message").value("학교정보 조회에 실패했습니다."));
    }

    @Test
    void missingSessionAndNonR09SessionAreRejectedByTheProtectedOperation() throws Exception {
        mockMvc.perform(get("/api/external-integrations/schools"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false));

        MvcResult memberLogin = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":\"member\",\"password\":\"member\"}"))
            .andExpect(status().isOk())
            .andReturn();
        mockMvc.perform(get("/api/external-integrations/schools").cookie(memberLogin.getResponse().getCookie("SESSION")))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void invalidPagingParameterUsesTheCommonValidationEnvelope() throws Exception {
        Cookie session = login();

        mockMvc.perform(get("/api/external-integrations/schools").cookie(session).param("pIndex", "0"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void copiedOpenApiFixtureDeclaresTheProtectedSchoolLookupOperation() throws Exception {
        String contract = new org.springframework.core.io.ClassPathResource("contracts/openapi.yaml").getContentAsString(StandardCharsets.UTF_8);
        Assertions.assertTrue(contract.contains("operationId: listSchoolInformation"));
        Assertions.assertTrue(contract.contains("/external-integrations/schools:"));
        Assertions.assertTrue(contract.contains("- R09"));
    }

    private Cookie login() throws Exception {
        MvcResult login = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":\"admin\",\"password\":\"admin\"}"))
            .andExpect(status().isOk())
            .andReturn();
        return login.getResponse().getCookie("SESSION");
    }
}
