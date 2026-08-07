package kr.ac.knue.fpe.common;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class FrontendRouteContractTest {
    @Test
    void openapiContainsEveryUiBackedPrimaryApi() throws Exception {
        String contract = new ClassPathResource("contracts/openapi.yaml").getContentAsString(StandardCharsets.UTF_8);
        for (String path : new String[]{"/api/users", "/api/organizations", "/api/roles", "/api/attachments", "/api/privacy/policies", "/api/batch-results"}) {
            assertThat(contract).contains(path);
        }
    }
}
