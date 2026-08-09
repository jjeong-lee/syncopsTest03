package kr.ac.knue.facultyassessment;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
    "spring.autoconfigure.exclude="
        + "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
        + "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration,"
        + "org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration",
    "app.foundation.enabled=false"
})
@AutoConfigureMockMvc
class HealthApiContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void healthEndpointReturnsTheOpenApiSuccessEnvelopeWithoutExternalIntegrations() throws Exception {
        ClassPathResource openApiContract = new ClassPathResource("contracts/openapi.yaml");
        String contract = openApiContract.getContentAsString(StandardCharsets.UTF_8);

        org.junit.jupiter.api.Assertions.assertTrue(contract.contains("/health:"));
        org.junit.jupiter.api.Assertions.assertTrue(contract.contains("operationId: healthCheck"));

        mockMvc.perform(get("/api/health"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith("application/json"))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.status").value("UP"))
            .andExpect(jsonPath("$.meta").isMap());
    }
}
