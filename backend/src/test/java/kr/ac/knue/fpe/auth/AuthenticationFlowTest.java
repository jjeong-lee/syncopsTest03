package kr.ac.knue.fpe.auth;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import static org.assertj.core.api.Assertions.assertThat;

class AuthenticationFlowTest {
    @Test void openapi_fixture_is_loaded_from_classpath_and_declares_session_cookie() throws Exception {
        String yaml = new String(new ClassPathResource("contracts/openapi.yaml").getInputStream().readAllBytes());
        assertThat(yaml).contains("operationId: login", "HttpOnly SameSite=Lax", "operationId: getCurrentUser");
    }
}
