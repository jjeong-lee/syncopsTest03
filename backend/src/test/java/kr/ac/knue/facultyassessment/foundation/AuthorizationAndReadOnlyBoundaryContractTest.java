package kr.ac.knue.facultyassessment.foundation;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
import java.util.List;
import kr.ac.knue.facultyassessment.auth.AuthenticationPort;
import kr.ac.knue.facultyassessment.personnel.PersonnelInformationPort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class AuthorizationAndReadOnlyBoundaryContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthenticationPort authenticationPort;

    @Autowired
    private PersonnelInformationPort personnelInformationPort;

    @Test
    void menuVisibilityAndDirectProtectedApiUseTheSameAuthorizationDecision() throws Exception {
        MvcResult login = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":\"member\",\"password\":\"member\"}"))
            .andExpect(status().isOk())
            .andReturn();

        Cookie cookie = login.getResponse().getCookie("SESSION");
        mockMvc.perform(get("/api/auth/me").cookie(cookie))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.menus").isEmpty());

        mockMvc.perform(get("/api/users").cookie(cookie))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
    }

    @Test
    void korusSnapshotPortExposesOnlyReadOperationsAndReturnsSeedPersonnel() {
        List<PersonnelInformationPort.PersonnelSnapshot> personnel = personnelInformationPort.findPersonnel();

        org.junit.jupiter.api.Assertions.assertFalse(personnel.isEmpty());
        org.junit.jupiter.api.Assertions.assertTrue(PersonnelInformationPort.class.getDeclaredMethods().length > 0);
        org.junit.jupiter.api.Assertions.assertTrue(java.util.Arrays.stream(PersonnelInformationPort.class.getDeclaredMethods())
            .allMatch(method -> method.getName().startsWith("find")));
    }

    @Test
    void aMalformedCommandPayloadReturnsFieldErrorBeforeAnyFutureDomainWrite() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":\"admin\",\"password\":\"admin\"}"))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":\"admin\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.field").value("password"));
    }
}
